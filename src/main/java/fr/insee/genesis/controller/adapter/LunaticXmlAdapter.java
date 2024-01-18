package fr.insee.genesis.controller.adapter;

import fr.insee.genesis.controller.sources.ddi.VariablesMap;
import fr.insee.genesis.controller.sources.xml.LunaticXmlCollectedData;
import fr.insee.genesis.controller.sources.xml.LunaticXmlSurveyUnit;
import fr.insee.genesis.controller.sources.xml.ValueType;
import fr.insee.genesis.controller.utils.LoopIdentifier;
import fr.insee.genesis.domain.dtos.*;
import lombok.experimental.UtilityClass;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class LunaticXmlAdapter {

    /**
     * Convert a Lunatic XML survey unit into a genesis survey unit DTO
     * @param su Lunatic XML survey unit to convert
     * @param variablesMap variable definitions (used for loops)
     * @param idCampaign survey id
     * @return a genesis survey unit DTO
     */
    public static List<SurveyUnitUpdateDto> convert(LunaticXmlSurveyUnit su, VariablesMap variablesMap, String idCampaign, Mode mode){
        //Get COLLECTED Data and external variables
        List<SurveyUnitUpdateDto> surveyUnitUpdateDtoList = new ArrayList<>();
        SurveyUnitUpdateDto surveyUnitUpdateDto = getStateDataFromSurveyUnit(su, variablesMap, idCampaign, DataState.COLLECTED, mode);
        getExternalDataFromSurveyUnit(su,surveyUnitUpdateDto);

        surveyUnitUpdateDtoList.add(surveyUnitUpdateDto);

        //Get data from other states
        SurveyUnitUpdateDto editedSurveyUnitUpdateDto = getStateDataFromSurveyUnit(su, variablesMap, idCampaign, DataState.EDITED,mode);
        if(editedSurveyUnitUpdateDto != null) surveyUnitUpdateDtoList.add(editedSurveyUnitUpdateDto);

        SurveyUnitUpdateDto inputedSurveyUnitUpdateDto = getStateDataFromSurveyUnit(su, variablesMap, idCampaign, DataState.INPUTED,mode);
        if(inputedSurveyUnitUpdateDto != null) surveyUnitUpdateDtoList.add(inputedSurveyUnitUpdateDto);

        SurveyUnitUpdateDto forcedSurveyUnitUpdateDto = getStateDataFromSurveyUnit(su, variablesMap, idCampaign, DataState.FORCED,mode);
        if(forcedSurveyUnitUpdateDto != null) surveyUnitUpdateDtoList.add(forcedSurveyUnitUpdateDto);

        SurveyUnitUpdateDto previousSurveyUnitUpdateDto = getStateDataFromSurveyUnit(su, variablesMap, idCampaign, DataState.PREVIOUS,mode);
        if(previousSurveyUnitUpdateDto != null) surveyUnitUpdateDtoList.add(previousSurveyUnitUpdateDto);


        return surveyUnitUpdateDtoList;
    }

    /**
     * Collects data from XML survey unit depending on the data state
     * @param su source XML Survey Unit
     * @param variablesMap variable definitions (used for loops)
     * @param idCampaign survey id
     * @param dataState state of the DTO to generate
     * @return Survey Unit DTO with a specific state
     */
    private static SurveyUnitUpdateDto getStateDataFromSurveyUnit(LunaticXmlSurveyUnit su, VariablesMap variablesMap, String idCampaign, DataState dataState,Mode mode) {
        SurveyUnitUpdateDto surveyUnitUpdateDto = SurveyUnitUpdateDto.builder()
                .idQuest(su.getQuestionnaireModelId())
                .idCampaign(idCampaign)
                .idUE(su.getId())
                .state(dataState)
                .mode(mode)
                .recordDate(LocalDateTime.now())
                .fileDate(su.getFileDate())
                .build();

        return getCollectedDataFromSurveyUnit(su, surveyUnitUpdateDto, variablesMap, dataState);
    }


    /**
     * Gets data from a specific state and put it into DTO's data
     * @param su XML survey unit to extract from
     * @param surveyUnitUpdateDto DTO to aliment
     * @param variablesMap variables definitions (used for loops)
     * @param dataState data state from XML
     * @return the DTO containing data, null if no data and not COLLECTED
     */
    private static SurveyUnitUpdateDto getCollectedDataFromSurveyUnit(LunaticXmlSurveyUnit su, SurveyUnitUpdateDto surveyUnitUpdateDto, VariablesMap variablesMap, DataState dataState) {
        List<CollectedVariableDto> variablesUpdate = new ArrayList<>();

        int dataCount = 0;
        for (LunaticXmlCollectedData lunaticXmlCollectedData : su.getData().getCollected()){
            List<ValueType> valueTypeList;
            switch (dataState){
                case COLLECTED: valueTypeList = lunaticXmlCollectedData.getCollected();
                break;
                case EDITED : valueTypeList = lunaticXmlCollectedData.getEdited();
                break;
                case FORCED : valueTypeList = lunaticXmlCollectedData.getForced();
                break;
                case INPUTED: valueTypeList = lunaticXmlCollectedData.getInputed();
                break;
                case PREVIOUS: valueTypeList = lunaticXmlCollectedData.getPrevious();
                break;
                default:
                    return null;
            }
            if(valueTypeList != null) {
                for (int i = 1; i <= valueTypeList.size(); i++) {
                    List<String> variableValues = getValuesFromValueTypeList(valueTypeList);
                    if (!variableValues.isEmpty()) {
                        variablesUpdate.add(CollectedVariableDto.collectedVariableBuilder()
                                .idVar(lunaticXmlCollectedData.getVariableName())
                                .values(variableValues)
                                .idLoop(LoopIdentifier.getLoopIdentifier(lunaticXmlCollectedData.getVariableName(), variablesMap, i))
                                .idParent(LoopIdentifier.getParentGroupName(lunaticXmlCollectedData.getVariableName(), variablesMap))
                                .build());
                        dataCount++;
                    }
                }
            }
        }
        surveyUnitUpdateDto.setCollectedVariables(variablesUpdate);

        //Return null if no data and not COLLECTED
        if(dataCount > 0 || dataState.equals(DataState.COLLECTED)) return surveyUnitUpdateDto;
        return null;
    }



    /**
     * Extract external data from XML survey unit and put it into DTO
     * @param su XML survey unit
     * @param surveyUnitUpdateDto DTO to aliment
     */
    private static void getExternalDataFromSurveyUnit(LunaticXmlSurveyUnit su, SurveyUnitUpdateDto surveyUnitUpdateDto) {
        List<VariableDto> externalVariables = new ArrayList<>();
        su.getData().getExternal().forEach(lunaticXmlExternalData ->
                externalVariables.add(VariableDto.builder()
                        .idVar(lunaticXmlExternalData.getVariableName())
                        .values(getValuesFromValueTypeList(lunaticXmlExternalData.getValues()))
                        .build())
        );
        surveyUnitUpdateDto.setExternalVariables(externalVariables);
    }

    private static List<String> getValuesFromValueTypeList(List<ValueType> valueTypeList) {
        if (!valueTypeList.isEmpty()){
            List<String> values = new ArrayList<>();
            for(ValueType valueType : valueTypeList)
                if (valueType.getValue() != null) {
                    values.add(valueType.getValue());
                }
            return values;
        }
        return List.of();
    }

}
