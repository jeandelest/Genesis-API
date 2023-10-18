package fr.insee.genesis.domain.ports.api;

import fr.insee.genesis.controller.model.Source;
import fr.insee.genesis.controller.model.SurveyUnitId;
import fr.insee.genesis.domain.dtos.SurveyUnitDto;
import fr.insee.genesis.domain.dtos.SurveyUnitUpdateDto;

import java.util.List;


public interface SurveyUnitUpdateApiPort {

    void saveSurveyUnits(List<SurveyUnitUpdateDto> suList);

    List<SurveyUnitUpdateDto> findByIdsUEAndQuestionnaire(String idUE, String idQuest);

    List<SurveyUnitUpdateDto> findByIdUE(String idUE);

    List<SurveyUnitUpdateDto> findByIdUEsAndIdQuestionnaire(List<SurveyUnitDto> idUEs, String idQuestionnaire);

    List<SurveyUnitUpdateDto> findByIdQuestionnaire(String idQuestionnaire);

    List<SurveyUnitUpdateDto> findLatestByIds(String idUE, String idQuest);

    List<SurveyUnitId> findDistinctIdUEsByIdQuestionnaire(String idQuestionnaire);

    List<Source> findSourcesByIdQuestionnaire(String idQuestionnaire);
}
