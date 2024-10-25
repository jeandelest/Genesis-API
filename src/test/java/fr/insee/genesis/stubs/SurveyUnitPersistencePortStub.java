package fr.insee.genesis.stubs;

import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.domain.ports.spi.SurveyUnitPersistencePort;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

@Getter
public class SurveyUnitPersistencePortStub implements SurveyUnitPersistencePort {
    List<SurveyUnitModel> mongoStub = new ArrayList<>();

    @Override
    public void saveAll(List<SurveyUnitModel> suList) {
        mongoStub.addAll(suList);
    }

    @Override
    public List<SurveyUnitModel> findByIds(String idUE, String idQuest) {
        List<SurveyUnitModel> surveyUnitModelList = new ArrayList<>();
        for(SurveyUnitModel SurveyUnitModel : mongoStub){
            if(SurveyUnitModel.getIdUE().equals(idUE) && SurveyUnitModel.getIdQuest().equals(idQuest))
                surveyUnitModelList.add(SurveyUnitModel);
        }

        return surveyUnitModelList;
    }

    @Override
    public List<SurveyUnitModel> findByIdUE(String idUE) {
        List<SurveyUnitModel> surveyUnitModelList = new ArrayList<>();
        for(SurveyUnitModel SurveyUnitModel : mongoStub){
            if(SurveyUnitModel.getIdUE().equals(idUE))
                surveyUnitModelList.add(SurveyUnitModel);
        }

        return surveyUnitModelList;
    }

    @Override
    public List<SurveyUnitModel> findByIdUEsAndIdQuestionnaire(List<SurveyUnitModel> idUEs, String idQuestionnaire) {
        List<SurveyUnitModel> surveyUnitModelList = new ArrayList<>();
        for(SurveyUnitModel surveyUnitModel : idUEs) {
            for (SurveyUnitModel document : mongoStub) {
                if (surveyUnitModel.getIdUE().equals(document.getIdUE()) && document.getIdQuest().equals(idQuestionnaire))
                    surveyUnitModelList.add(document);
            }
        }

        return surveyUnitModelList;
    }

    @Override
    public Stream<SurveyUnitModel> findByIdQuestionnaire(String idQuestionnaire) {
        List<SurveyUnitModel> surveyUnitModelList = new ArrayList<>();
        for(SurveyUnitModel SurveyUnitModel : mongoStub){
            if(SurveyUnitModel.getIdQuest().equals(idQuestionnaire))
                surveyUnitModelList.add(SurveyUnitModel);
        }

        return surveyUnitModelList.stream();
    }

    @Override
    public List<SurveyUnitModel> findIdUEsByIdQuestionnaire(String idQuestionnaire) {
        List<SurveyUnitModel> surveyUnitModelList = new ArrayList<>();
        for(SurveyUnitModel SurveyUnitModel : mongoStub){
            if(SurveyUnitModel.getIdQuest().equals(idQuestionnaire))
                surveyUnitModelList.add(
                        new SurveyUnitModel(SurveyUnitModel.getIdUE(), SurveyUnitModel.getMode())
                );
        }

        return surveyUnitModelList;
    }

    @Override
    public List<SurveyUnitModel> findIdUEsByIdCampaign(String idCampaign) {
        List<SurveyUnitModel> surveyUnitModelList = new ArrayList<>();
        for(SurveyUnitModel SurveyUnitModel : mongoStub){
            if(SurveyUnitModel.getIdCampaign().equals(idCampaign))
                surveyUnitModelList.add(
                        new SurveyUnitModel(SurveyUnitModel.getIdUE(), SurveyUnitModel.getMode())
                );
        }

        return surveyUnitModelList;
    }

    @Override
    public Long deleteByIdQuestionnaire(String idQuestionnaire) {
        return null;
    }

    @Override
    public long count() {
        return mongoStub.size();
    }

    @Override
    public Set<String> findIdQuestionnairesByIdCampaign(String idCampaign) {
        Set<String> idQuestionnaireSet = new HashSet<>();
        for(SurveyUnitModel SurveyUnitModel : mongoStub){
            if(SurveyUnitModel.getIdCampaign().equals(idCampaign))
                idQuestionnaireSet.add(SurveyUnitModel.getIdQuest());
        }

        return idQuestionnaireSet;
    }

    @Override
    public Set<String> findDistinctIdCampaigns() {
        Set<String> campaignIds = new HashSet<>();
        for(SurveyUnitModel SurveyUnitModel : mongoStub){
            campaignIds.add(SurveyUnitModel.getIdCampaign());
        }

        return campaignIds;
    }

    @Override
    public long countByIdCampaign(String idCampaign) {
        return mongoStub.stream().filter(
                SurveyUnitDto -> SurveyUnitDto.getIdCampaign().equals(idCampaign)).toList().size();
    }

    @Override
    public Set<String> findDistinctIdQuestionnaires() {
        Set<String> questionnaireIds = new HashSet<>();
        for(SurveyUnitModel SurveyUnitModel : mongoStub){
            questionnaireIds.add(SurveyUnitModel.getIdQuest());
        }
        return questionnaireIds;
    }

    @Override
    public Set<String> findIdCampaignsByIdQuestionnaire(String idQuestionnaire) {
        Set<String> idCampaignSet = new HashSet<>();
        for(SurveyUnitModel SurveyUnitModel : mongoStub){
            if(SurveyUnitModel.getIdQuest().equals(idQuestionnaire))
                idCampaignSet.add(SurveyUnitModel.getIdCampaign());
        }

        return idCampaignSet;
    }
}
