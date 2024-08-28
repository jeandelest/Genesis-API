package fr.insee.genesis.domain.ports.api;

import fr.insee.genesis.domain.model.surveyunit.CampaignWithQuestionnaire;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.QuestionnaireWithCampaign;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnit;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitId;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;


public interface SurveyUnitApiPort {

    void saveSurveyUnits(List<SurveyUnit> suList);

    List<SurveyUnit> findByIdsUEAndQuestionnaire(String idUE, String idQuest);

    List<SurveyUnit> findByIdUE(String idUE);

    Stream<SurveyUnit> findByIdQuestionnaire(String idQuestionnaire);

    List<SurveyUnit> findLatestByIdAndByIdQuestionnaire(String idUE, String idQuest);

    List<SurveyUnit> findIdUEsAndModesByIdQuestionnaire(String idQuestionnaire);

    List<SurveyUnitId> findDistinctIdUEsByIdQuestionnaire(String idQuestionnaire);

    List<Mode> findModesByIdQuestionnaire(String idQuestionnaire);

    List<Mode> findModesByIdCampaign(String idCampaign);

    Long deleteByIdQuestionnaire(String idQuestionnaire);

    long countResponses();

    Set<String> findIdQuestionnairesByIdCampaign(String idCampaign);

    Set<String> findDistinctIdCampaigns();

    long countResponsesByIdCampaign(String idCampaign);

    Set<String> findDistinctIdQuestionnaires();

    List<CampaignWithQuestionnaire> findCampaignsWithQuestionnaires();

    List<QuestionnaireWithCampaign> findQuestionnairesWithCampaigns();
}
