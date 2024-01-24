package fr.insee.genesis.infrastructure.adapter;

import fr.insee.genesis.domain.dtos.Mode;
import fr.insee.genesis.domain.dtos.SurveyUnitDto;
import fr.insee.genesis.domain.dtos.SurveyUnitUpdateDto;
import fr.insee.genesis.infrastructure.model.ExternalVariable;
import fr.insee.genesis.infrastructure.model.VariableState;
import fr.insee.genesis.infrastructure.model.document.SurveyUnitUpdateDocument;
import fr.insee.genesis.infrastructure.repository.SurveyUnitUpdateMongoDBRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SurveyUnitUpdateMongoAdapterTest {

	@Mock
	SurveyUnitUpdateMongoDBRepository mongoRepository;

	@InjectMocks
	static SurveyUnitUpdateMongoAdapter surveyUnitUpdateMongoAdapter;

	static SurveyUnitUpdateDocument suDoc;
	static SurveyUnitUpdateDocument suDoc2;
	static SurveyUnitUpdateDocument suDoc3;

	@BeforeAll
	static void setUp() {
		surveyUnitUpdateMongoAdapter = new SurveyUnitUpdateMongoAdapter();
		suDoc= new SurveyUnitUpdateDocument();
		suDoc.setIdCampaign("idCampaign");
		suDoc.setIdUE("UE1100000001");
		suDoc.setIdQuestionnaire("TEST2023X01");
		suDoc.setState("COLLECTED");
		suDoc.setMode("WEB");
		suDoc.setRecordDate(LocalDateTime.now());
		suDoc.setCollectedVariables(List.of(new VariableState()));
		suDoc.setExternalVariables(List.of(new ExternalVariable()));

		suDoc2= new SurveyUnitUpdateDocument();
		suDoc2.setIdCampaign("idCampaign");
		suDoc2.setIdUE("UE1100000001");
		suDoc2.setIdQuestionnaire("TEST2023X01");
		suDoc2.setState("COLLECTED");
		suDoc2.setMode("TEL");
		suDoc2.setRecordDate(LocalDateTime.now());
		suDoc2.setCollectedVariables(List.of(new VariableState()));
		suDoc2.setExternalVariables(List.of(new ExternalVariable()));

		suDoc3= new SurveyUnitUpdateDocument();
		suDoc3.setIdCampaign("idCampaign");
		suDoc3.setIdUE("UE1100000002");
		suDoc3.setIdQuestionnaire("TEST2023X01");
		suDoc3.setState("COLLECTED");
		suDoc3.setMode("WEB");
		suDoc3.setRecordDate(LocalDateTime.now());
		suDoc3.setCollectedVariables(List.of(new VariableState()));
		suDoc3.setExternalVariables(List.of(new ExternalVariable()));
	}

	@Test
	void shouldReturnListOfSurveyUnitUpdateDto_IfIdsFoundInDataBase() {
		//Given
		List<SurveyUnitUpdateDocument> responses = new ArrayList<>();
		responses.add(suDoc);
		responses.add(suDoc2);
		when(mongoRepository.findByIdUEAndIdQuestionnaire(any(String.class), any(String.class))).thenReturn(responses);
		// When
		List< SurveyUnitUpdateDto> updates = surveyUnitUpdateMongoAdapter.findByIds("UE1100000001", "TEST2023X01");
		// Then
		Assertions.assertThat(updates).isNotNull().hasSize(2);
		Assertions.assertThat(updates.get(0).getMode()).isEqualTo(Mode.WEB);
	}

	@Test
	void shouldReturnNull_IfIdsNotFoundInDataBase() {
		//Given
		when(mongoRepository.findByIdUEAndIdQuestionnaire(any(String.class), any(String.class))).thenReturn(List.of());
		// When
		List< SurveyUnitUpdateDto> updates = surveyUnitUpdateMongoAdapter.findByIds("UE1100000001", "TEST2023X01");
		// Then
		Assertions.assertThat(updates).isNull();
	}

	@Test
	void shouldReturnListOfSurveyUnitUpdateDto_IfIdUEFoundInDataBase() {
		//Given
		List<SurveyUnitUpdateDocument> responses = new ArrayList<>();
		responses.add(suDoc);
		responses.add(suDoc2);
		when(mongoRepository.findByIdUE(any(String.class))).thenReturn(responses);
		// When
		List< SurveyUnitUpdateDto> updates = surveyUnitUpdateMongoAdapter.findByIdUE("UE1100000001");
		// Then
		Assertions.assertThat(updates).isNotNull().hasSize(2);
		Assertions.assertThat(updates.get(0).getMode()).isEqualTo(Mode.WEB);
	}

	@Test
	void shouldReturnNull_IfIdUENotFoundInDataBase() {
		//Given
		when(mongoRepository.findByIdUE(any(String.class))).thenReturn(List.of());
		// When
		List< SurveyUnitUpdateDto> updates = surveyUnitUpdateMongoAdapter.findByIdUE("UE1100000001");
		// Then
		Assertions.assertThat(updates).isNull();
	}

	@Test
	void shouldReturnListOfSurveyUnitUpdateDto_IfIdQuestionnaireFoundInDataBase() {
		//Given
		List<SurveyUnitUpdateDocument> responses = new ArrayList<>();
		responses.add(suDoc);
		responses.add(suDoc2);
		responses.add(suDoc3);
		when(mongoRepository.findByIdQuestionnaire(any(String.class))).thenReturn(responses);
		// When
		List< SurveyUnitUpdateDto> updates = surveyUnitUpdateMongoAdapter.findByIdQuestionnaire("TEST2023X01");
		// Then
		Assertions.assertThat(updates).isNotNull().hasSize(3);
		Assertions.assertThat(updates.get(2).getMode()).isEqualTo(Mode.WEB);
	}

	@Test
	void shouldReturnNull_IfIdQuestionnaireNotFoundInDataBase() {
		//Given
		when(mongoRepository.findByIdQuestionnaire(any(String.class))).thenReturn(List.of());
		// When
		List< SurveyUnitUpdateDto> updates = surveyUnitUpdateMongoAdapter.findByIdQuestionnaire("TEST2023X01");
		// Then
		Assertions.assertThat(updates).isNull();
	}

	@Test
	void shouldReturnListOfSurveyUnitUpdateDto_WhenGivenAListOfIdUEs() {
		//Given
		List<SurveyUnitUpdateDocument> responses1 = new ArrayList<>();
		responses1.add(suDoc);
		responses1.add(suDoc2);
		List<SurveyUnitUpdateDocument> responses2 = new ArrayList<>();
		responses2.add(suDoc3);
		when(mongoRepository.findByIdUEAndIdQuestionnaire("UE1100000001", "TEST2023X01")).thenReturn(responses1);
		when(mongoRepository.findByIdUEAndIdQuestionnaire("UE1100000002", "TEST2023X01")).thenReturn(responses2);
		SurveyUnitDto id1 = SurveyUnitDto.builder().idUE("UE1100000001").build();
		SurveyUnitDto id2 = SurveyUnitDto.builder().idUE("UE1100000002").build();
		List<SurveyUnitDto> ids = List.of(id1, id2);
		// When
		List< SurveyUnitUpdateDto> updates = surveyUnitUpdateMongoAdapter.findByIdUEsAndIdQuestionnaire(ids, "TEST2023X01");
		// Then
		Assertions.assertThat(updates).isNotNull().hasSize(3);
		Assertions.assertThat(updates.get(0).getMode()).isEqualTo(Mode.WEB);
	}

	@Test
	void shouldReturnNull_IfIdUEsNotFoundInDataBase() {
		//Given
		when(mongoRepository.findByIdUEAndIdQuestionnaire(any(String.class),any(String.class))).thenReturn(List.of());
		SurveyUnitDto id1 = SurveyUnitDto.builder().idUE("UE1100000001").build();
		SurveyUnitDto id2 = SurveyUnitDto.builder().idUE("UE1100000002").build();
		List<SurveyUnitDto> ids = List.of(id1, id2);
		// When
		List< SurveyUnitUpdateDto> updates = surveyUnitUpdateMongoAdapter.findByIdUEsAndIdQuestionnaire(ids, "TEST2023X01");
		// Then
		Assertions.assertThat(updates).isNull();
	}

}