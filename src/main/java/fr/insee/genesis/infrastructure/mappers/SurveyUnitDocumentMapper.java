package fr.insee.genesis.infrastructure.mappers;

import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.infrastructure.document.surveyunit.SurveyUnitDocument;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface SurveyUnitDocumentMapper {
	SurveyUnitDocumentMapper INSTANCE = Mappers.getMapper(SurveyUnitDocumentMapper.class);

	@Mapping(source = "idQuestionnaire", target = "idQuest")
	SurveyUnitModel documentToModel(SurveyUnitDocument surveyUnit);

	@Mapping(source = "idQuest", target = "idQuestionnaire")
	SurveyUnitDocument modelToDocument(SurveyUnitModel surveyUnitModel);

	List<SurveyUnitModel> listDocumentToListModel(List<SurveyUnitDocument> surveyUnits);

	List<SurveyUnitDocument> listModelToListDocument(List<SurveyUnitModel> surveyUnitsDtoModel);

}
