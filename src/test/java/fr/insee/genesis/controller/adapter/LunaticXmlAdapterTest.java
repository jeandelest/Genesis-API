package fr.insee.genesis.controller.adapter;

import fr.insee.genesis.Constants;
import fr.insee.genesis.controller.sources.ddi.Group;
import fr.insee.genesis.controller.sources.ddi.Variable;
import fr.insee.genesis.controller.sources.ddi.VariableType;
import fr.insee.genesis.controller.sources.ddi.VariablesMap;
import fr.insee.genesis.controller.sources.xml.*;
import fr.insee.genesis.domain.dtos.CollectedVariableDto;
import fr.insee.genesis.domain.dtos.DataState;
import fr.insee.genesis.domain.dtos.Mode;
import fr.insee.genesis.domain.dtos.SurveyUnitUpdateDto;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

class LunaticXmlAdapterTest {

    private static final String LOOP_NAME = "BOUCLE1";
    private static final String ID_CAMPAIGN = "ID_CAMPAIGN";
    LunaticXmlSurveyUnit lunaticXmlSurveyUnit1 = new LunaticXmlSurveyUnit();
    LunaticXmlSurveyUnit lunaticXmlSurveyUnit2 = new LunaticXmlSurveyUnit();
    LunaticXmlSurveyUnit lunaticXmlSurveyUnit3 = new LunaticXmlSurveyUnit();
    LunaticXmlSurveyUnit lunaticXmlSurveyUnit4 = new LunaticXmlSurveyUnit();
    LunaticXmlSurveyUnit lunaticXmlSurveyUnit5 = new LunaticXmlSurveyUnit();
    LunaticXmlSurveyUnit lunaticXmlSurveyUnit6 = new LunaticXmlSurveyUnit();
    LunaticXmlSurveyUnit lunaticXmlSurveyUnit7 = new LunaticXmlSurveyUnit();
    LunaticXmlSurveyUnit lunaticXmlSurveyUnit8 = new LunaticXmlSurveyUnit();
    VariablesMap variablesMap = new VariablesMap();

    @BeforeEach
    void setUp() {
        //Given
        //SurveyUnit 1 : Only collected data
        LunaticXmlData lunaticXmlData = new LunaticXmlData();
        LunaticXmlCollectedData lunaticXmlCollectedData = new LunaticXmlCollectedData();
        lunaticXmlCollectedData.setVariableName("var1");
        lunaticXmlCollectedData.setCollected(List.of(new ValueType("1", "string"), new ValueType("2", "string")));
        LunaticXmlCollectedData lunaticXmlCollectedData2 = new LunaticXmlCollectedData();
        lunaticXmlCollectedData2.setVariableName("var2");
        lunaticXmlCollectedData2.setCollected(List.of(new ValueType("3", "string"), new ValueType("4", "string")));
        List<LunaticXmlCollectedData> collected = List.of(lunaticXmlCollectedData, lunaticXmlCollectedData2);
        lunaticXmlData.setCollected(collected);
        List<LunaticXmlOtherData> external = List.of();
        lunaticXmlData.setExternal(external);
        lunaticXmlSurveyUnit1.setId("idUE1");
        lunaticXmlSurveyUnit1.setQuestionnaireModelId("idQuest1");
        lunaticXmlSurveyUnit1.setData(lunaticXmlData);

        //SurveyUnit 2 : COLLECTED + EDITED
        lunaticXmlData = new LunaticXmlData();

        lunaticXmlCollectedData = new LunaticXmlCollectedData();
        lunaticXmlCollectedData.setVariableName("var1");
        lunaticXmlCollectedData.setCollected(List.of(new ValueType("1", "string"), new ValueType("2", "string")));
        lunaticXmlCollectedData.setEdited(List.of(new ValueType("1e", "string"), new ValueType("2e", "string")));

        lunaticXmlCollectedData2 = new LunaticXmlCollectedData();
        lunaticXmlCollectedData2.setVariableName("var2");
        lunaticXmlCollectedData2.setCollected(List.of(new ValueType("3", "string"), new ValueType("4", "string")));
        collected = List.of(lunaticXmlCollectedData, lunaticXmlCollectedData2);
        lunaticXmlData.setCollected(collected);

        lunaticXmlData.setExternal(external);
        lunaticXmlSurveyUnit2.setId("idUE1");
        lunaticXmlSurveyUnit2.setQuestionnaireModelId("idQuest1");
        lunaticXmlSurveyUnit2.setData(lunaticXmlData);

        //SurveyUnit 3 : COLLECTED + EDITED + FORCED
        lunaticXmlData = new LunaticXmlData();

        lunaticXmlCollectedData = new LunaticXmlCollectedData();
        lunaticXmlCollectedData.setVariableName("var1");
        lunaticXmlCollectedData.setCollected(List.of(new ValueType("1", "string"), new ValueType("2", "string")));
        lunaticXmlCollectedData.setEdited(List.of(new ValueType("1e", "string"), new ValueType("2e", "string")));

        lunaticXmlCollectedData2 = new LunaticXmlCollectedData();
        lunaticXmlCollectedData2.setVariableName("var2");
        lunaticXmlCollectedData2.setCollected(List.of(new ValueType("3", "string"), new ValueType("4", "string")));
        lunaticXmlCollectedData2.setForced(List.of(new ValueType("3f", "string"), new ValueType("4f", "string")));
        collected = List.of(lunaticXmlCollectedData, lunaticXmlCollectedData2);
        lunaticXmlData.setCollected(collected);

        lunaticXmlData.setExternal(external);
        lunaticXmlSurveyUnit3.setId("idUE1");
        lunaticXmlSurveyUnit3.setQuestionnaireModelId("idQuest1");
        lunaticXmlSurveyUnit3.setData(lunaticXmlData);

        //SurveyUnit 4 : COLLECTED + EDITED + PREVIOUS
        lunaticXmlData = new LunaticXmlData();

        lunaticXmlCollectedData = new LunaticXmlCollectedData();
        lunaticXmlCollectedData.setVariableName("var1");
        lunaticXmlCollectedData.setCollected(List.of(new ValueType("1", "string"), new ValueType("2", "string")));
        lunaticXmlCollectedData.setEdited(List.of(new ValueType("1e", "string"), new ValueType("2e", "string")));

        lunaticXmlCollectedData2 = new LunaticXmlCollectedData();
        lunaticXmlCollectedData2.setVariableName("var2");
        lunaticXmlCollectedData2.setCollected(List.of(new ValueType("3", "string"), new ValueType("4", "string")));
        lunaticXmlCollectedData2.setPrevious(List.of(new ValueType("3p", "string"), new ValueType("4p", "string")));
        collected = List.of(lunaticXmlCollectedData, lunaticXmlCollectedData2);
        lunaticXmlData.setCollected(collected);

        lunaticXmlData.setExternal(external);
        lunaticXmlSurveyUnit4.setId("idUE1");
        lunaticXmlSurveyUnit4.setQuestionnaireModelId("idQuest1");
        lunaticXmlSurveyUnit4.setData(lunaticXmlData);

        //SurveyUnit 5 : COLLECTED + EDITED + PREVIOUS + INPUTED
        lunaticXmlData = new LunaticXmlData();

        lunaticXmlCollectedData = new LunaticXmlCollectedData();
        lunaticXmlCollectedData.setVariableName("var1");
        lunaticXmlCollectedData.setCollected(List.of(new ValueType("1", "string"), new ValueType("2", "string")));
        lunaticXmlCollectedData.setEdited(List.of(new ValueType("1e", "string"), new ValueType("2e", "string")));
        lunaticXmlCollectedData.setInputed(List.of(new ValueType("1i", "string"), new ValueType("2i", "string")));

        lunaticXmlCollectedData2 = new LunaticXmlCollectedData();
        lunaticXmlCollectedData2.setVariableName("var2");
        lunaticXmlCollectedData2.setCollected(List.of(new ValueType("3", "string"), new ValueType("4", "string")));
        lunaticXmlCollectedData2.setPrevious(List.of(new ValueType("3p", "string"), new ValueType("4p", "string")));
        collected = List.of(lunaticXmlCollectedData, lunaticXmlCollectedData2);
        lunaticXmlData.setCollected(collected);

        lunaticXmlData.setExternal(external);
        lunaticXmlSurveyUnit5.setId("idUE1");
        lunaticXmlSurveyUnit5.setQuestionnaireModelId("idQuest1");
        lunaticXmlSurveyUnit5.setData(lunaticXmlData);

        //SurveyUnit 6 : COLLECTED only, has one unknown variable
        lunaticXmlData = new LunaticXmlData();

        lunaticXmlCollectedData = new LunaticXmlCollectedData();
        lunaticXmlCollectedData.setVariableName("var1");
        lunaticXmlCollectedData.setCollected(List.of(new ValueType("1", "string"), new ValueType("2", "string")));

        lunaticXmlCollectedData2 = new LunaticXmlCollectedData();
        lunaticXmlCollectedData2.setVariableName("var3");
        lunaticXmlCollectedData2.setCollected(List.of(new ValueType("1", "string"), new ValueType("2", "string")));


        collected = List.of(lunaticXmlCollectedData, lunaticXmlCollectedData2);
        lunaticXmlData.setCollected(collected);

        lunaticXmlData.setExternal(external);
        lunaticXmlSurveyUnit6.setId("idUE1");
        lunaticXmlSurveyUnit6.setQuestionnaireModelId("idQuest1");
        lunaticXmlSurveyUnit6.setData(lunaticXmlData);

        //SurveyUnit 7 : COLLECTED only, has one unknown variable with known variable prefix
        lunaticXmlData = new LunaticXmlData();

        lunaticXmlCollectedData = new LunaticXmlCollectedData();
        lunaticXmlCollectedData.setVariableName("var1");
        lunaticXmlCollectedData.setCollected(List.of(new ValueType("1", "string"), new ValueType("2", "string")));

        lunaticXmlCollectedData2 = new LunaticXmlCollectedData();
        lunaticXmlCollectedData2.setVariableName("var1_MISSING");
        lunaticXmlCollectedData2.setCollected(List.of(new ValueType("1", "string"), new ValueType("2", "string")));

        collected = List.of(lunaticXmlCollectedData, lunaticXmlCollectedData2);
        lunaticXmlData.setCollected(collected);

        lunaticXmlData.setExternal(external);
        lunaticXmlSurveyUnit7.setId("idUE1");
        lunaticXmlSurveyUnit7.setQuestionnaireModelId("idQuest1");
        lunaticXmlSurveyUnit7.setData(lunaticXmlData);

        //SurveyUnit 8 : Only collected data
        lunaticXmlData = new LunaticXmlData();
        lunaticXmlCollectedData = new LunaticXmlCollectedData();
        lunaticXmlCollectedData.setVariableName("var1");
        lunaticXmlCollectedData.setCollected(List.of(new ValueType(null,"null"),new ValueType("1", "string"), new ValueType("2", "string")));
        lunaticXmlCollectedData2 = new LunaticXmlCollectedData();
        lunaticXmlCollectedData2.setVariableName("var2");
        lunaticXmlCollectedData2.setCollected(List.of(new ValueType("4", "string")));
        collected = List.of(lunaticXmlCollectedData, lunaticXmlCollectedData2);
        lunaticXmlData.setCollected(collected);
        external = List.of();
        lunaticXmlData.setExternal(external);
        lunaticXmlSurveyUnit8.setId("idUE1");
        lunaticXmlSurveyUnit8.setQuestionnaireModelId("idQuest1");
        lunaticXmlSurveyUnit8.setData(lunaticXmlData);

        //VariablesMap
        Group group = new Group(LOOP_NAME, Constants.ROOT_GROUP_NAME);
        Variable var1 = new Variable("var1", group, VariableType.STRING, "1");
        Variable var2 = new Variable("var2", variablesMap.getRootGroup(), VariableType.STRING, "1");
        variablesMap.putVariable(var1);
        variablesMap.putVariable(var2);
    }

    @Test
    @DisplayName("SurveyUnitUpdateDto should not be null")
    void test01() {
        // When
        List<SurveyUnitUpdateDto> suDtos = LunaticXmlAdapter.convert(lunaticXmlSurveyUnit1, variablesMap, ID_CAMPAIGN, Mode.WEB);
        // Then
        Assertions.assertThat(suDtos).isNotNull().isNotEmpty();
    }

    @Test
    @DisplayName("SurveyUnitUpdateDto should have the right idQuest")
    void test02() {
        // When
        List<SurveyUnitUpdateDto> suDtos = LunaticXmlAdapter.convert(lunaticXmlSurveyUnit1, variablesMap, ID_CAMPAIGN, Mode.WEB);
        // Then
        Assertions.assertThat(suDtos.get(0).getIdQuest()).isEqualTo("idQuest1");
    }

    @Test
    @DisplayName("SurveyUnitUpdateDto should have the right id")
    void test03() {
        // When
        List<SurveyUnitUpdateDto> suDtos = LunaticXmlAdapter.convert(lunaticXmlSurveyUnit1, variablesMap, ID_CAMPAIGN, Mode.WEB);
        // Then
        Assertions.assertThat(suDtos.get(0).getIdUE()).isEqualTo("idUE1");
    }

    @Test
    @DisplayName("SurveyUnitUpdateDto should contains 4 variable state updates")
    void test04() {
        // When
        List<SurveyUnitUpdateDto> suDtos = LunaticXmlAdapter.convert(lunaticXmlSurveyUnit1, variablesMap, ID_CAMPAIGN, Mode.WEB);
        // Then
        Assertions.assertThat(suDtos.get(0).getCollectedVariables()).hasSize(4);
    }

    @Test
    @DisplayName("There should be a EDITED DTO with EDITED data")
    void test05() {
        // When
        List<SurveyUnitUpdateDto> suDtos = LunaticXmlAdapter.convert(lunaticXmlSurveyUnit2, variablesMap, ID_CAMPAIGN, Mode.WEB);
        // Then
        Assertions.assertThat(suDtos).hasSize(2);
        Assertions.assertThat(suDtos).filteredOn(surveyUnitUpdateDto ->
                surveyUnitUpdateDto.getState().equals(DataState.EDITED)
        ).isNotEmpty();

        Optional<SurveyUnitUpdateDto> editedDTO = suDtos.stream().filter(surveyUnitUpdateDto ->
                surveyUnitUpdateDto.getState().equals(DataState.EDITED)
        ).findFirst();
        Assertions.assertThat(editedDTO).isPresent();

        //Content check
        for (CollectedVariableDto collectedVariableDto : editedDTO.get().getCollectedVariables()) {
            Assertions.assertThat(collectedVariableDto.getValues()).containsAnyOf("1e", "2e").doesNotContain("1", "2");
        }
    }

    @Test
    @DisplayName("There should be both EDITED DTO and FORCED DTO if there is EDITED and FORCED data")
    void test06() {
        // When
        List<SurveyUnitUpdateDto> suDtos = LunaticXmlAdapter.convert(lunaticXmlSurveyUnit3, variablesMap, ID_CAMPAIGN, Mode.WEB);
        // Then
        Assertions.assertThat(suDtos).hasSize(3);
        Assertions.assertThat(suDtos).filteredOn(surveyUnitUpdateDto ->
                surveyUnitUpdateDto.getState().equals(DataState.EDITED)
        ).isNotEmpty();
        Assertions.assertThat(suDtos).filteredOn(surveyUnitUpdateDto ->
                surveyUnitUpdateDto.getState().equals(DataState.FORCED)
        ).isNotEmpty();
    }

    @Test
    @DisplayName("There should be a EDITED DTO and PREVIOUS DTO if there is EDITED and PREVIOUS data")
    void test07() {
        // When
        List<SurveyUnitUpdateDto> suDtos = LunaticXmlAdapter.convert(lunaticXmlSurveyUnit4, variablesMap, ID_CAMPAIGN, Mode.WEB);
        // Then
        Assertions.assertThat(suDtos).hasSize(3);
        Assertions.assertThat(suDtos).filteredOn(surveyUnitUpdateDto ->
                surveyUnitUpdateDto.getState().equals(DataState.EDITED)
        ).isNotEmpty();
        Assertions.assertThat(suDtos).filteredOn(surveyUnitUpdateDto ->
                surveyUnitUpdateDto.getState().equals(DataState.PREVIOUS)
        ).isNotEmpty();
    }

    @Test
    @DisplayName("There should be multiple DTOs if there is different data states (all 4)")
    void test08() {
        // When
        List<SurveyUnitUpdateDto> suDtos = LunaticXmlAdapter.convert(lunaticXmlSurveyUnit5, variablesMap, ID_CAMPAIGN, Mode.WEB);
        // Then
        Assertions.assertThat(suDtos).hasSize(4);
        Assertions.assertThat(suDtos).filteredOn(surveyUnitUpdateDto ->
                surveyUnitUpdateDto.getState().equals(DataState.EDITED)
        ).isNotEmpty();
        Assertions.assertThat(suDtos).filteredOn(surveyUnitUpdateDto ->
                surveyUnitUpdateDto.getState().equals(DataState.PREVIOUS)
        ).isNotEmpty();
        Assertions.assertThat(suDtos).filteredOn(surveyUnitUpdateDto ->
                surveyUnitUpdateDto.getState().equals(DataState.INPUTED)
        ).isNotEmpty();
    }

    @Test
    @DisplayName("If a variable not present in DDI then he is in the root group")
    void test09() {
        // When
        List<SurveyUnitUpdateDto> suDtos = LunaticXmlAdapter.convert(lunaticXmlSurveyUnit6, variablesMap, ID_CAMPAIGN, Mode.WEB);

        // Then
        Assertions.assertThat(suDtos).hasSize(1);

        Assertions.assertThat(suDtos.get(0).getCollectedVariables()).filteredOn(collectedVariableDto ->
                collectedVariableDto.getIdVar().equals("var3")).isNotEmpty();
        Assertions.assertThat(suDtos.get(0).getCollectedVariables().stream().filter(collectedVariableDto ->
                collectedVariableDto.getIdVar().equals("var3")).toList().get(0).getIdParent()).isNull();
        Assertions.assertThat(suDtos.get(0).getCollectedVariables().stream().filter(collectedVariableDto ->
                collectedVariableDto.getIdVar().equals("var3")).toList().get(0).getIdLoop()).isEqualTo(Constants.ROOT_GROUP_NAME);
    }

    @Test
    @DisplayName("If a variable A not present in DDI and is the extension of a known variable B, then the variable A has B as related and is in the same group")
    void test10() {
        // When
        List<SurveyUnitUpdateDto> suDtos = LunaticXmlAdapter.convert(lunaticXmlSurveyUnit7, variablesMap, ID_CAMPAIGN, Mode.WEB);

        // Then
        Assertions.assertThat(suDtos).hasSize(1);

        Assertions.assertThat(suDtos.get(0).getCollectedVariables()).filteredOn(collectedVariableDto ->
                collectedVariableDto.getIdVar().equals("var1_MISSING")).isNotEmpty();
        Assertions.assertThat(suDtos.get(0).getCollectedVariables().stream().filter(collectedVariableDto ->
                collectedVariableDto.getIdVar().equals("var1_MISSING")).toList().get(0).getIdParent()).isNotNull().isEqualTo("var1");
        Assertions.assertThat(suDtos.get(0).getCollectedVariables().stream().filter(collectedVariableDto ->
                collectedVariableDto.getIdVar().equals("var1_MISSING")).toList().get(0).getIdLoop()).isNotEqualTo(Constants.ROOT_GROUP_NAME).isEqualTo(LOOP_NAME);
    }

    @Test
    @DisplayName("Value should be affected in the good loop iteration")
    void test11(){
        // When
        List<SurveyUnitUpdateDto> suDtos = LunaticXmlAdapter.convert(lunaticXmlSurveyUnit8, variablesMap, ID_CAMPAIGN, Mode.WEB);
        // Then
        Assertions.assertThat(suDtos).hasSize(1);
        Assertions.assertThat(suDtos.get(0).getCollectedVariables()).hasSize(3);
        Assertions.assertThat(suDtos.get(0).getCollectedVariables()).filteredOn(collectedVariableDto ->
                collectedVariableDto.getIdVar().equals("var1")).isNotEmpty();
        Assertions.assertThat(suDtos.get(0).getCollectedVariables()).filteredOn(collectedVariableDto ->
                collectedVariableDto.getIdLoop().equals("BOUCLE1_1")).isEmpty();
        Assertions.assertThat(suDtos.get(0).getCollectedVariables().stream().filter(collectedVariableDto ->
                collectedVariableDto.getIdLoop().equals("BOUCLE1_2")).toList().getFirst().getValues().getFirst()).isEqualTo("1");
        Assertions.assertThat(suDtos.get(0).getCollectedVariables().stream().filter(collectedVariableDto ->
                collectedVariableDto.getIdLoop().equals("BOUCLE1_3")).toList().getFirst().getValues().getFirst()).isEqualTo("2");

    }
}
