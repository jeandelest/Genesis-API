package fr.insee.genesis.controller.rest;

import cucumber.TestConstants;
import fr.insee.genesis.controller.responses.SurveyUnitUpdateSimplified;
import fr.insee.genesis.controller.service.SurveyUnitQualityService;
import fr.insee.genesis.controller.utils.ControllerUtils;
import fr.insee.genesis.domain.dtos.CollectedVariableDto;
import fr.insee.genesis.domain.dtos.DataState;
import fr.insee.genesis.domain.dtos.Mode;
import fr.insee.genesis.domain.dtos.SurveyUnitId;
import fr.insee.genesis.domain.dtos.SurveyUnitUpdateDto;
import fr.insee.genesis.domain.dtos.VariableDto;
import fr.insee.genesis.domain.ports.api.SurveyUnitUpdateApiPort;
import fr.insee.genesis.domain.service.SurveyUnitUpdateImpl;
import fr.insee.genesis.infrastructure.utils.FileUtils;
import fr.insee.genesis.stubs.ConfigStub;
import fr.insee.genesis.stubs.SurveyUnitUpdatePersistencePortStub;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


class ResponseControllerTest {
    //Given
    static ResponseController responseControllerStatic;
    static SurveyUnitUpdatePersistencePortStub surveyUnitUpdatePersistencePortStub;

    static List<SurveyUnitId> surveyUnitIdList;

    @BeforeAll
    static void init(){
        surveyUnitUpdatePersistencePortStub = new SurveyUnitUpdatePersistencePortStub();
        SurveyUnitUpdateApiPort surveyUnitUpdateApiPort = new SurveyUnitUpdateImpl(surveyUnitUpdatePersistencePortStub);

        FileUtils fileUtils = new FileUtils(new ConfigStub());
        responseControllerStatic = new ResponseController(
                surveyUnitUpdateApiPort
                ,new SurveyUnitQualityService()
                ,fileUtils
                ,new ControllerUtils(fileUtils)
        );

        surveyUnitIdList = new ArrayList<>();
        surveyUnitIdList.add(new SurveyUnitId("TESTIDUE"));
    }

    @BeforeEach
    void reset() throws IOException {
        //MongoDB stub management
        surveyUnitUpdatePersistencePortStub.getMongoStub().clear();

        List<VariableDto> externalVariableDtoList = new ArrayList<>();
        VariableDto variableDto = VariableDto.builder().idVar("TESTIDVAR").values(List.of(new String[]{"V1", "V2"})).build();
        externalVariableDtoList.add(variableDto);

        List<CollectedVariableDto> collectedVariableDtoList = new ArrayList<>();
        CollectedVariableDto collectedVariableDto = new CollectedVariableDto("TESTIDVAR", List.of(new String[]{"V1", "V2"}),"TESTIDLOOP","TESTIDPARENT");
        collectedVariableDtoList.add(collectedVariableDto);
        surveyUnitUpdatePersistencePortStub.getMongoStub().add(SurveyUnitUpdateDto.builder()
                .idCampaign("TESTIDCAMPAIGN")
                .mode(Mode.WEB)
                .idUE("TESTIDUE")
                .idQuest("TESTIDQUESTIONNAIRE")
                .state(DataState.COLLECTED)
                .fileDate(LocalDateTime.of(2023,1,1,0,0,0))
                .recordDate(LocalDateTime.of(2024,1,1,0,0,0))
                .externalVariables(externalVariableDtoList)
                .collectedVariables(collectedVariableDtoList)
                .build());


        //Test file management
        if(Path.of(TestConstants.TEST_RESOURCES_DIRECTORY).resolve("DONE").toFile().exists())
            Files.walk(Path.of(TestConstants.TEST_RESOURCES_DIRECTORY).resolve("DONE"))
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);

        if(!Path.of(TestConstants.TEST_RESOURCES_DIRECTORY)
                .resolve("IN")
                .resolve("WEB")
                .resolve("SAMPLETEST-PARADATA-v1")
                .resolve("data.complete.validated.STPDv1.20231122164209.xml")
                .toFile().exists()
        )
            Files.copy(
                Path.of(TestConstants.TEST_RESOURCES_DIRECTORY)
                    .resolve("IN")
                    .resolve("WEB")
                    .resolve("SAMPLETEST-PARADATA-v1")
                    .resolve("reponse-platine")
                    .resolve("data.complete.validated.STPDv1.20231122164209.xml")
                ,Path.of(TestConstants.TEST_RESOURCES_DIRECTORY)
                    .resolve("IN")
                    .resolve("WEB")
                    .resolve("SAMPLETEST-PARADATA-v1")
                    .resolve("data.complete.validated.STPDv1.20231122164209.xml")
            );
    }

    //When + Then
    @Test
    void saveResponseFromXMLFileTest() throws Exception {
        responseControllerStatic.saveResponsesFromXmlFile(
                Path.of(TestConstants.TEST_RESOURCES_DIRECTORY,"IN/WEB/SAMPLETEST-PARADATA-v1/reponse-platine/data.complete.validated.STPDv1.20231122164209.xml").toString()
                ,Path.of(TestConstants.TEST_RESOURCES_DIRECTORY,"IN/specs/SAMPLETEST-PARADATA-v1/ddi-SAMPLETEST-PARADATA-v1.xml").toString()
                ,Mode.WEB
        );

        Assertions.assertThat(surveyUnitUpdatePersistencePortStub.getMongoStub()).isNotEmpty();
    }

    @Test
    void saveResponsesFromXmlCampaignFolderTest() throws Exception {
        responseControllerStatic.saveResponsesFromXmlCampaignFolder(
                "SAMPLETEST-PARADATA-v1"
                ,Mode.WEB
        );

        Assertions.assertThat(surveyUnitUpdatePersistencePortStub.getMongoStub()).isNotEmpty();
    }

    @Test
    void saveResponsesFromXmlCampaignFolderTest_noData() throws Exception {
        surveyUnitUpdatePersistencePortStub.getMongoStub().clear();

        responseControllerStatic.saveResponsesFromXmlCampaignFolder(
                "TESTNODATA"
                ,Mode.WEB
        );

        Assertions.assertThat(surveyUnitUpdatePersistencePortStub.getMongoStub()).isEmpty();
    }

    @Test
    void findResponsesByUEAndQuestionnaireTest(){
        ResponseEntity<List<SurveyUnitUpdateDto>> response = responseControllerStatic.findResponsesByUEAndQuestionnaire("TESTIDUE","TESTIDQUESTIONNAIRE");

        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(response.getBody()).isNotNull().isNotEmpty();
        Assertions.assertThat(response.getBody().get(0).getIdUE()).isEqualTo("TESTIDUE");
        Assertions.assertThat(response.getBody().get(0).getIdQuest()).isEqualTo("TESTIDQUESTIONNAIRE");
    }

    @Test
    void findAllResponsesByQuestionnaireTest(){
        ResponseEntity<List<SurveyUnitUpdateDto>> response = responseControllerStatic.findAllResponsesByQuestionnaire("TESTIDQUESTIONNAIRE");

        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(response.getBody()).isNotNull().isNotEmpty();
        Assertions.assertThat(response.getBody().get(0).getIdQuest()).isEqualTo("TESTIDQUESTIONNAIRE");
    }

    //TODO refaire pour répondre à la logique du call
    @Test
    void getLatestByUETest(){
        addAdditionnalDtoToMongoStub();

        ResponseEntity<List<SurveyUnitUpdateDto>> response = responseControllerStatic.getLatestByUE("TESTIDUE","TESTIDQUESTIONNAIRE");

        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(response.getBody()).isNotNull().isNotEmpty();
        Assertions.assertThat(response.getBody().get(0).getIdUE()).isEqualTo("TESTIDUE");
        Assertions.assertThat(response.getBody().get(0).getIdQuest()).isEqualTo("TESTIDQUESTIONNAIRE");
        Assertions.assertThat(response.getBody().get(0).getFileDate()).hasMonth(Month.FEBRUARY);
    }

    @Test
    void getLatestByUEOneObjectTest(){
        ResponseEntity<SurveyUnitUpdateSimplified> response = responseControllerStatic.getLatestByUEOneObject("TESTIDUE","TESTIDQUESTIONNAIRE", Mode.WEB);

        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(response.getBody()).isNotNull();
        Assertions.assertThat(response.getBody().getIdUE()).isEqualTo("TESTIDUE");
        Assertions.assertThat(response.getBody().getIdQuest()).isEqualTo("TESTIDQUESTIONNAIRE");
    }

    @Test
    void getLatestForUEListTest(){
        ResponseEntity<List<SurveyUnitUpdateSimplified>> response = responseControllerStatic.getLatestForUEList("TESTIDQUESTIONNAIRE",surveyUnitIdList);

        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(response.getBody()).isNotNull().isNotEmpty();
        Assertions.assertThat(response.getBody().get(0).getIdUE()).isEqualTo("TESTIDUE");
    }

    @Test
    void getAllIdUEsByQuestionnaireTest(){
        ResponseEntity<List<SurveyUnitId>> response = responseControllerStatic.getAllIdUEsByQuestionnaire("TESTIDQUESTIONNAIRE");

        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(response.getBody()).isNotNull().isNotEmpty();
        Assertions.assertThat(response.getBody().get(0).getIdUE()).isEqualTo("TESTIDUE");
    }

    @Test
    void getModesByQuestionnaireTest(){
        ResponseEntity<List<Mode>> response = responseControllerStatic.getModesByQuestionnaire("TESTIDQUESTIONNAIRE");

        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(response.getBody()).isNotNull().isNotEmpty().hasSize(1);
        Assertions.assertThat(response.getBody().get(0)).isEqualTo(Mode.WEB);
    }

    private void addAdditionnalDtoToMongoStub(){
        List<VariableDto> externalVariableDtoList = new ArrayList<>();
        VariableDto variableDto = VariableDto.builder().idVar("TESTIDVAR").values(List.of(new String[]{"V1", "V2"})).build();
        externalVariableDtoList.add(variableDto);

        List<CollectedVariableDto> collectedVariableDtoList = new ArrayList<>();
        CollectedVariableDto collectedVariableDto = new CollectedVariableDto("TESTIDVAR", List.of(new String[]{"V1", "V2"}),"TESTIDLOOP","TESTIDPARENT");
        collectedVariableDtoList.add(collectedVariableDto);

        SurveyUnitUpdateDto recentDTO = SurveyUnitUpdateDto.builder()
                .idCampaign("TESTIDCAMPAIGN")
                .mode(Mode.WEB)
                .idUE("TESTIDUE")
                .idQuest("TESTIDQUESTIONNAIRE")
                .state(DataState.COLLECTED)
                .fileDate(LocalDateTime.of(2023,2,2,0,0,0))
                .recordDate(LocalDateTime.of(2024,2,2,0,0,0))
                .externalVariables(externalVariableDtoList)
                .collectedVariables(collectedVariableDtoList)
                .build();
        surveyUnitUpdatePersistencePortStub.getMongoStub().add(recentDTO);
    }

}