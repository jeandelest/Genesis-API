package fr.insee.genesis.infrastructure.adapter;

import fr.insee.genesis.domain.dtos.SurveyUnitDto;
import fr.insee.genesis.domain.dtos.SurveyUnitUpdateDto;
import fr.insee.genesis.domain.ports.spi.SurveyUnitUpdatePersistencePort;
import fr.insee.genesis.infrastructure.mappers.SurveyUnitDocumentMapper;
import fr.insee.genesis.infrastructure.mappers.SurveyUnitUpdateDocumentMapper;
import fr.insee.genesis.infrastructure.model.document.SurveyUnitDocument;
import fr.insee.genesis.infrastructure.model.document.SurveyUnitUpdateDocument;
import fr.insee.genesis.infrastructure.repository.SurveyUnitUpdateMongoDBRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class SurveyUnitUpdateMongoAdapter implements SurveyUnitUpdatePersistencePort {

	@Autowired
	private SurveyUnitUpdateMongoDBRepository mongoRepository;

	@Override
	public void saveAll(List<SurveyUnitUpdateDto> suListDto) {
		List<SurveyUnitUpdateDocument> suList = SurveyUnitUpdateDocumentMapper.INSTANCE.listDtoToListDocument(suListDto);
		mongoRepository.insert(suList);
	}

	@Override
	public List<SurveyUnitUpdateDto> findByIds(String idUE, String idQuest) {
		List<SurveyUnitUpdateDocument> surveyUnitsUpdate = mongoRepository.findByIdUEAndIdQuestionnaire(idUE, idQuest);
		return surveyUnitsUpdate.isEmpty() ? null : SurveyUnitUpdateDocumentMapper.INSTANCE.listDocumentToListDto(surveyUnitsUpdate);
	}

	@Override
	public List<SurveyUnitUpdateDto> findByIdUE(String idUE) {
		List<SurveyUnitUpdateDocument> surveyUnitsUpdate = mongoRepository.findByIdUE(idUE);
		return surveyUnitsUpdate.isEmpty() ? null : SurveyUnitUpdateDocumentMapper.INSTANCE.listDocumentToListDto(surveyUnitsUpdate);
	}

	@Override
	public List<SurveyUnitUpdateDto> findByIdUEsAndIdQuestionnaire(List<SurveyUnitDto> idUEs, String idQuestionnaire) {
		List<SurveyUnitUpdateDocument> surveyUnitsUpdate= new ArrayList<>();
		// TODO: 18-10-2023 : find a way to do this in one query
		idUEs.forEach(su -> {
			List<SurveyUnitUpdateDocument> docs = mongoRepository.findByIdUEAndIdQuestionnaire(su.getIdUE(), idQuestionnaire);
			surveyUnitsUpdate.addAll(docs);
		});
		return surveyUnitsUpdate.isEmpty() ? null : SurveyUnitUpdateDocumentMapper.INSTANCE.listDocumentToListDto(surveyUnitsUpdate);
	}


	@Override
	public List<SurveyUnitUpdateDto> findByIdQuestionnaire(String idQuestionnaire) {
		List<SurveyUnitUpdateDocument> surveyUnitsUpdate = mongoRepository.findByIdQuestionnaire(idQuestionnaire);
		return surveyUnitsUpdate.isEmpty() ? null : SurveyUnitUpdateDocumentMapper.INSTANCE.listDocumentToListDto(surveyUnitsUpdate);
	}

	@Override
	public Long deleteByIdQuestionnaire(String idQuestionnaire) {
		return mongoRepository.deleteByIdQuestionnaire(idQuestionnaire);
	}

	public List<SurveyUnitDto> findIdUEsByIdQuestionnaire(String idQuestionnaire) {
		List<SurveyUnitDocument> surveyUnits = mongoRepository.findIdUEsByIdQuestionnaire(idQuestionnaire);
		return surveyUnits.isEmpty() ? null : SurveyUnitDocumentMapper.INSTANCE.listDocumentToListDto(surveyUnits);
	}


}
