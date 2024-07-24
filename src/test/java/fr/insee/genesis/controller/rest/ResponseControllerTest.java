package fr.insee.genesis.controller.rest;

import cucumber.TestConstants;
import fr.insee.genesis.Constants;
import fr.insee.genesis.controller.responses.SurveyUnitUpdateSimplified;
import fr.insee.genesis.controller.service.SurveyUnitQualityService;
import fr.insee.genesis.controller.service.VolumetryLogService;
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
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

class ResponseControllerTest {
    //Given
    static ResponseController responseControllerStatic;
    static SurveyUnitUpdatePersistencePortStub surveyUnitUpdatePersistencePortStub;

    static List<SurveyUnitId> surveyUnitIdList;

    @BeforeAll
    static void init() {
        surveyUnitUpdatePersistencePortStub = new SurveyUnitUpdatePersistencePortStub();
        SurveyUnitUpdateApiPort surveyUnitUpdateApiPort = new SurveyUnitUpdateImpl(surveyUnitUpdatePersistencePortStub);

        FileUtils fileUtils = new FileUtils(new ConfigStub());
        responseControllerStatic = new ResponseController(
                surveyUnitUpdateApiPort
                , new SurveyUnitQualityService()
                , new VolumetryLogService(new ConfigStub())
                , fileUtils
                , new ControllerUtils(fileUtils)
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
        CollectedVariableDto collectedVariableDto = new CollectedVariableDto("TESTIDVAR", List.of(new String[]{"V1", "V2"}), "TESTIDLOOP", "TESTIDPARENT");
        collectedVariableDtoList.add(collectedVariableDto);
        surveyUnitUpdatePersistencePortStub.getMongoStub().add(SurveyUnitUpdateDto.builder()
                .idCampaign("TESTIDCAMPAIGN")
                .mode(Mode.WEB)
                .idUE("TESTIDUE")
                .idQuest("TESTIDQUESTIONNAIRE")
                .state(DataState.COLLECTED)
                .fileDate(LocalDateTime.of(2023, 1, 1, 0, 0, 0))
                .recordDate(LocalDateTime.of(2024, 1, 1, 0, 0, 0))
                .externalVariables(externalVariableDtoList)
                .collectedVariables(collectedVariableDtoList)
                .build());

        //Test file management
        //Clean DONE folder
        Path testResourcesPath = Path.of(TestConstants.TEST_RESOURCES_DIRECTORY);
        if (testResourcesPath.resolve("DONE").toFile().exists())
            Files.walk(testResourcesPath.resolve("DONE"))
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);

        //Recreate data files
        //SAMPLETEST-PARADATA-v1
        if (!testResourcesPath
                .resolve("IN")
                .resolve("WEB")
                .resolve("SAMPLETEST-PARADATA-v1")
                .resolve("data.complete.validated.STPDv1.20231122164209.xml")
                .toFile().exists()
        ){
            Files.copy(
                    testResourcesPath
                            .resolve("IN")
                            .resolve("WEB")
                            .resolve("SAMPLETEST-PARADATA-v1")
                            .resolve("reponse-platine")
                            .resolve("data.complete.partial.STPDv1.20231122164209.xml")
                    , testResourcesPath
                            .resolve("IN")
                            .resolve("WEB")
                            .resolve("SAMPLETEST-PARADATA-v1")
                            .resolve("data.complete.partial.STPDv1.20231122164209.xml")
            );
            Files.copy(
                    testResourcesPath
                            .resolve("IN")
                            .resolve("WEB")
                            .resolve("SAMPLETEST-PARADATA-v1")
                            .resolve("reponse-platine")
                            .resolve("data.complete.validated.STPDv1.20231122164209.xml")
                    , testResourcesPath
                            .resolve("IN")
                            .resolve("WEB")
                            .resolve("SAMPLETEST-PARADATA-v1")
                            .resolve("data.complete.validated.STPDv1.20231122164209.xml")
            );
        }
        //SAMPLETEST-PARADATA-v2
        if (!testResourcesPath
                .resolve("IN")
                .resolve("WEB")
                .resolve("SAMPLETEST-PARADATA-v2")
                .resolve("data.complete.validated.STPDv2.20231122164209.xml")
                .toFile().exists()
        ){
            Files.copy(
                    testResourcesPath
                            .resolve("IN")
                            .resolve("WEB")
                            .resolve("SAMPLETEST-PARADATA-v2")
                            .resolve("reponse-platine")
                            .resolve("data.complete.partial.STPDv2.20231122164209.xml")
                    , testResourcesPath
                            .resolve("IN")
                            .resolve("WEB")
                            .resolve("SAMPLETEST-PARADATA-v2")
                            .resolve("data.complete.partial.STPDv2.20231122164209.xml")
            );
            Files.copy(
                    testResourcesPath
                            .resolve("IN")
                            .resolve("WEB")
                            .resolve("SAMPLETEST-PARADATA-v2")
                            .resolve("reponse-platine")
                            .resolve("data.complete.validated.STPDv2.20231122164209.xml")
                    , testResourcesPath
                            .resolve("IN")
                            .resolve("WEB")
                            .resolve("SAMPLETEST-PARADATA-v2")
                            .resolve("data.complete.validated.STPDv2.20231122164209.xml")
            );
        }
    }


    //When + Then
    @Test
    void saveResponseFromXMLFileTest() throws Exception {
        responseControllerStatic.saveResponsesFromXmlFile(
                Path.of(TestConstants.TEST_RESOURCES_DIRECTORY, "IN/WEB/SAMPLETEST-PARADATA-v1/reponse-platine/data.complete.validated.STPDv1.20231122164209.xml").toString()
                , Path.of(TestConstants.TEST_RESOURCES_DIRECTORY, "specs/SAMPLETEST-PARADATA-v1/ddi-SAMPLETEST-PARADATA-v1.xml").toString()
                , Mode.WEB
        );

        Assertions.assertThat(surveyUnitUpdatePersistencePortStub.getMongoStub()).isNotEmpty();
    }

    @Test
    void saveResponsesFromXmlCampaignFolderTest() throws Exception {
        responseControllerStatic.saveResponsesFromXmlCampaignFolder(
                "SAMPLETEST-PARADATA-v1"
                , Mode.WEB
        );

        Assertions.assertThat(surveyUnitUpdatePersistencePortStub.getMongoStub()).isNotEmpty();
    }

    @Test
    void saveResponsesFromXmlCampaignFolderTest_noData() throws Exception {
        surveyUnitUpdatePersistencePortStub.getMongoStub().clear();

        responseControllerStatic.saveResponsesFromXmlCampaignFolder(
                "TESTNODATA"
                , Mode.WEB
        );

        Assertions.assertThat(surveyUnitUpdatePersistencePortStub.getMongoStub()).isEmpty();
    }

    @Test
    void saveResponsesFromAllCampaignFoldersTests(){
        surveyUnitUpdatePersistencePortStub.getMongoStub().clear();
        responseControllerStatic.saveResponsesFromAllCampaignFolders();

        Assertions.assertThat(surveyUnitUpdatePersistencePortStub.getMongoStub()).isNotEmpty();
    }

    @Test
    void findResponsesByUEAndQuestionnaireTest() {
        ResponseEntity<List<SurveyUnitUpdateDto>> response = responseControllerStatic.findResponsesByUEAndQuestionnaire("TESTIDUE", "TESTIDQUESTIONNAIRE");

        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(response.getBody()).isNotNull().isNotEmpty();
        Assertions.assertThat(response.getBody().getFirst().getIdUE()).isEqualTo("TESTIDUE");
        Assertions.assertThat(response.getBody().getFirst().getIdQuest()).isEqualTo("TESTIDQUESTIONNAIRE");
    }

    @Test
    void findAllResponsesByQuestionnaireTest() {
        Path path = Path.of(TestConstants.TEST_RESOURCES_DIRECTORY, "OUT", "TESTIDQUESTIONNAIRE");
        File dir = new File(String.valueOf(path));
        FileSystemUtils.deleteRecursively(dir);

        ResponseEntity<Path> response = responseControllerStatic.findAllResponsesByQuestionnaire("TESTIDQUESTIONNAIRE");

        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(response.getBody()).isNotNull();
        Assertions.assertThat(Files.exists(path)).isTrue();
        File[] dir_contents = dir.listFiles();
        Assertions.assertThat(dir_contents).hasSize(1);
        Assertions.assertThat(dir_contents[0].length()).isPositive().isNotNull();
        FileSystemUtils.deleteRecursively(dir);
        dir.deleteOnExit();
    }

    @Test
    void getAllResponsesByQuestionnaireTestSequential() throws IOException {
        //Given
        surveyUnitUpdatePersistencePortStub.getMongoStub().clear();

        for (int i = 0; i < Constants.BATCH_SIZE + 2; i++) {
            List<VariableDto> externalVariableDtoList = new ArrayList<>();
            VariableDto variableDto = VariableDto.builder().idVar("TESTIDVAR").values(List.of(new String[]{"V1", "V2"})).build();
            externalVariableDtoList.add(variableDto);

            List<CollectedVariableDto> collectedVariableDtoList = new ArrayList<>();
            CollectedVariableDto collectedVariableDto = new CollectedVariableDto("TESTIDVAR", List.of(new String[]{"V1", "V2"}), "TESTIDLOOP", "TESTIDPARENT");
            collectedVariableDtoList.add(collectedVariableDto);

            surveyUnitUpdatePersistencePortStub.getMongoStub().add(SurveyUnitUpdateDto.builder()
                    .idCampaign("TESTIDCAMPAIGN")
                    .mode(Mode.WEB)
                    .idUE("TESTIDUE" + i)
                    .idQuest("TESTIDQUESTIONNAIRE")
                    .state(DataState.COLLECTED)
                    .fileDate(LocalDateTime.of(2023, 1, 1, 0, 0, 0))
                    .recordDate(LocalDateTime.of(2024, 1, 1, 0, 0, 0))
                    .externalVariables(externalVariableDtoList)
                    .collectedVariables(collectedVariableDtoList)
                    .build());
        }

        //When
        ResponseEntity<Path> response = responseControllerStatic.findAllResponsesByQuestionnaire("TESTIDQUESTIONNAIRE");

        //Then
        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(response.getBody()).isNotNull();

        Assertions.assertThat(response.getBody()).isNotNull();
        Assertions.assertThat(response.getBody().toFile()).isNotNull().exists();

        Files.deleteIfExists(response.getBody());
    }

    @Test
    void getLatestByUETest() {
        addAdditionnalDtoToMongoStub();

        ResponseEntity<List<SurveyUnitUpdateDto>> response = responseControllerStatic.getLatestByUE("TESTIDUE", "TESTIDQUESTIONNAIRE");

        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(response.getBody()).isNotNull().isNotEmpty();
        Assertions.assertThat(response.getBody().getFirst().getIdUE()).isEqualTo("TESTIDUE");
        Assertions.assertThat(response.getBody().getFirst().getIdQuest()).isEqualTo("TESTIDQUESTIONNAIRE");
        Assertions.assertThat(response.getBody().getFirst().getFileDate()).hasMonth(Month.FEBRUARY);
    }

    @Test
    void getLatestByUEOneObjectTest() {
        ResponseEntity<SurveyUnitUpdateSimplified> response = responseControllerStatic.getLatestByUEOneObject("TESTIDUE", "TESTIDQUESTIONNAIRE", Mode.WEB);

        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(response.getBody()).isNotNull();
        Assertions.assertThat(response.getBody().getIdUE()).isEqualTo("TESTIDUE");
        Assertions.assertThat(response.getBody().getIdQuest()).isEqualTo("TESTIDQUESTIONNAIRE");
    }

    @Test
    void getLatestForUEListTest() {
        ResponseEntity<List<SurveyUnitUpdateSimplified>> response = responseControllerStatic.getLatestForUEList("TESTIDQUESTIONNAIRE", surveyUnitIdList);

        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(response.getBody()).isNotNull().isNotEmpty();
        Assertions.assertThat(response.getBody().getFirst().getIdUE()).isEqualTo("TESTIDUE");
    }

    @Test
    void getAllIdUEsByQuestionnaireTest() {
        ResponseEntity<List<SurveyUnitId>> response = responseControllerStatic.getAllIdUEsByQuestionnaire("TESTIDQUESTIONNAIRE");

        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(response.getBody()).isNotNull().isNotEmpty();
        Assertions.assertThat(response.getBody().getFirst().getIdUE()).isEqualTo("TESTIDUE");
    }

    @Test
    void getModesByQuestionnaireTest() {
        ResponseEntity<List<Mode>> response = responseControllerStatic.getModesByQuestionnaire("TESTIDQUESTIONNAIRE");

        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(response.getBody()).isNotNull().isNotEmpty().hasSize(1);
        Assertions.assertThat(response.getBody().getFirst()).isEqualTo(Mode.WEB);
    }

    @Test
    void getModesByCampaignTest() {
        ResponseEntity<List<Mode>> response = responseControllerStatic.getModesByCampaign("TESTIDCAMPAIGN");

        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(response.getBody()).isNotNull().isNotEmpty().hasSize(1);
        Assertions.assertThat(response.getBody().getFirst()).isEqualTo(Mode.WEB);
    }

    @Test
    void getCampaignsTest() {
        addAdditionnalDtoToMongoStub("TESTCAMPAIGN2","TESTQUESTIONNAIRE2");

        ResponseEntity<Set<String>> response = responseControllerStatic.getCampaigns();

        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(response.getBody()).isNotNull().isNotEmpty().containsExactly(
                "TESTIDCAMPAIGN","TESTCAMPAIGN2");
    }

    @Test
    void getQuestionnairesTest() {
        addAdditionnalDtoToMongoStub("TESTQUESTIONNAIRE2");

        ResponseEntity<Set<String>> response = responseControllerStatic.getQuestionnaires();

        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(response.getBody()).isNotNull().isNotEmpty().containsExactly(
                "TESTIDQUESTIONNAIRE","TESTQUESTIONNAIRE2");
    }

    @Test
    void getQuestionnairesByCampaignTest() {
        addAdditionnalDtoToMongoStub("TESTQUESTIONNAIRE2");

        ResponseEntity<List<String>> response = responseControllerStatic.getQuestionnairesByCampaign("TESTIDCAMPAIGN");

        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(response.getBody()).isNotNull().isNotEmpty().containsExactly(
                "TESTIDQUESTIONNAIRE","TESTQUESTIONNAIRE2");
    }

    @Test
    void getCampaignsWithQuestionnairesTest() {
        addAdditionnalDtoToMongoStub("TESTQUESTIONNAIRE2");
        addAdditionnalDtoToMongoStub("TESTCAMPAIGN2","TESTQUESTIONNAIRE2");

        ResponseEntity<Map<String,List<String>>> response = responseControllerStatic.getCampaignsWithQuestionnaires();

        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(response.getBody()).isNotNull().isNotEmpty().containsKeys("TESTIDCAMPAIGN",
                "TESTCAMPAIGN2");
        Assertions.assertThat(response.getBody().get("TESTIDCAMPAIGN")).isNotNull().isNotEmpty().containsExactly(
                "TESTIDQUESTIONNAIRE",
                "TESTQUESTIONNAIRE2");
        Assertions.assertThat(response.getBody().get("TESTCAMPAIGN2")).isNotNull().isNotEmpty().containsExactly(
                "TESTQUESTIONNAIRE2");
    }

    @Test
    void getQuestionnairesWithCampaignsTest() {
        addAdditionnalDtoToMongoStub("TESTQUESTIONNAIRE2");
        addAdditionnalDtoToMongoStub("TESTCAMPAIGN2","TESTQUESTIONNAIRE2");

        ResponseEntity<Map<String,List<String>>> response = responseControllerStatic.getQuestionnairesWithCampaigns();

        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(response.getBody()).isNotNull().isNotEmpty().containsKeys("TESTIDQUESTIONNAIRE",
                "TESTQUESTIONNAIRE2");
        Assertions.assertThat(response.getBody().get("TESTIDQUESTIONNAIRE")).isNotNull().isNotEmpty().containsExactly(
                "TESTIDCAMPAIGN");
        Assertions.assertThat(response.getBody().get("TESTQUESTIONNAIRE2")).isNotNull().isNotEmpty().containsExactly(
                "TESTIDCAMPAIGN",
                        "TESTCAMPAIGN2");
    }

    @Test
    void saveVolumetryTest() throws IOException {
        //WHEN
        ResponseEntity<Object> response = responseControllerStatic.saveVolumetry();

        //THEN
        Path logFilePath = Path.of(
                        new ConfigStub().getLogFolder())
                        .resolve(Constants.VOLUMETRY_FOLDER_NAME)
                        .resolve(LocalDate.now().format(DateTimeFormatter.ofPattern(Constants.VOLUMETRY_FILE_DATE_FORMAT))
                                + Constants.VOLUMETRY_FILE_SUFFIX + ".csv");
        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(logFilePath).exists().content().isNotEmpty().contains("TESTIDCAMPAIGN;1");

        //CLEAN
        Files.deleteIfExists(logFilePath);
    }

    @Test
    void saveVolumetryTest_overwrite() throws IOException {
        //WHEN
        responseControllerStatic.saveVolumetry();
        ResponseEntity<Object> response = responseControllerStatic.saveVolumetry();

        //THEN
        Path logFilePath = Path.of(
                        new ConfigStub().getLogFolder())
                .resolve(Constants.VOLUMETRY_FOLDER_NAME)
                .resolve(LocalDate.now().format(DateTimeFormatter.ofPattern(Constants.VOLUMETRY_FILE_DATE_FORMAT))
                        + Constants.VOLUMETRY_FILE_SUFFIX + ".csv");
        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(logFilePath).exists().content().isNotEmpty().contains("TESTIDCAMPAIGN;1").doesNotContain("TESTIDCAMPAIGN;1\nTESTIDCAMPAIGN;1");

        //CLEAN
        Files.deleteIfExists(logFilePath);
    }

    @Test
    void saveVolumetryTest_additionnal_campaign() throws IOException {
        //Given
        addAdditionnalDtoToMongoStub("TESTIDCAMPAIGN2","TESTQUEST2");

        //WHEN
        ResponseEntity<Object> response = responseControllerStatic.saveVolumetry();

        //THEN
        Path logFilePath = Path.of(
                        new ConfigStub().getLogFolder())
                .resolve(Constants.VOLUMETRY_FOLDER_NAME)
                .resolve(LocalDate.now().format(DateTimeFormatter.ofPattern(Constants.VOLUMETRY_FILE_DATE_FORMAT))
                        + Constants.VOLUMETRY_FILE_SUFFIX + ".csv");
        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(logFilePath).exists().content().isNotEmpty().contains("TESTIDCAMPAIGN;1").contains("TESTIDCAMPAIGN2;1");

        //CLEAN
        Files.deleteIfExists(logFilePath);
    }
    @Test
    void saveVolumetryTest_additionnal_campaign_and_document() throws IOException {
        //Given
        addAdditionnalDtoToMongoStub("TESTQUEST");
        addAdditionnalDtoToMongoStub("TESTIDCAMPAIGN2","TESTQUEST2");

        //WHEN
        ResponseEntity<Object> response = responseControllerStatic.saveVolumetry();

        //THEN
        Path logFilePath = Path.of(
                        new ConfigStub().getLogFolder())
                .resolve(Constants.VOLUMETRY_FOLDER_NAME)
                .resolve(LocalDate.now().format(DateTimeFormatter.ofPattern(Constants.VOLUMETRY_FILE_DATE_FORMAT))
                        + Constants.VOLUMETRY_FILE_SUFFIX + ".csv");
        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(logFilePath).exists().content().isNotEmpty().contains("TESTIDCAMPAIGN;2").contains("TESTIDCAMPAIGN2;1");

        //CLEAN
        Files.deleteIfExists(logFilePath);
    }

    @Test
    void cleanOldVolumetryLogFiles() throws IOException {
        //GIVEN
        Path oldLogFilePath = Path.of(
                        new ConfigStub().getLogFolder())
                .resolve(Constants.VOLUMETRY_FOLDER_NAME)
                .resolve(LocalDate.of(2000,1,1).format(DateTimeFormatter.ofPattern(Constants.VOLUMETRY_FILE_DATE_FORMAT))
                        + Constants.VOLUMETRY_FILE_SUFFIX + ".csv");

        Files.createDirectories(oldLogFilePath.getParent());
        Files.write(oldLogFilePath, "test".getBytes());

        //WHEN
        ResponseEntity<Object> response = responseControllerStatic.saveVolumetry();

        //THEN
        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(oldLogFilePath).doesNotExist();

        //CLEAN
        try(Stream<Path> stream = Files.walk(oldLogFilePath.getParent())){
            for(Path filePath : stream.filter(path -> path.getFileName().toString().endsWith(".csv")).toList()){
                Files.deleteIfExists(filePath);
            }
        }
    }



    // Utilities
    private void addAdditionnalDtoToMongoStub() {
        List<VariableDto> externalVariableDtoList = new ArrayList<>();
        VariableDto variableDto = VariableDto.builder().idVar("TESTIDVAR").values(List.of(new String[]{"V1", "V2"})).build();
        externalVariableDtoList.add(variableDto);

        List<CollectedVariableDto> collectedVariableDtoList = new ArrayList<>();
        CollectedVariableDto collectedVariableDto = new CollectedVariableDto("TESTIDVAR", List.of(new String[]{"V1", "V2"}), "TESTIDLOOP", "TESTIDPARENT");
        collectedVariableDtoList.add(collectedVariableDto);

        SurveyUnitUpdateDto recentDTO = SurveyUnitUpdateDto.builder()
                .idCampaign("TESTIDCAMPAIGN")
                .mode(Mode.WEB)
                .idUE("TESTIDUE")
                .idQuest("TESTIDQUESTIONNAIRE")
                .state(DataState.COLLECTED)
                .fileDate(LocalDateTime.of(2023, 2, 2, 0, 0, 0))
                .recordDate(LocalDateTime.of(2024, 2, 2, 0, 0, 0))
                .externalVariables(externalVariableDtoList)
                .collectedVariables(collectedVariableDtoList)
                .build();
        surveyUnitUpdatePersistencePortStub.getMongoStub().add(recentDTO);
    }

    private void addAdditionnalDtoToMongoStub(String idQuestionnaire) {
        List<VariableDto> externalVariableDtoList = new ArrayList<>();
        VariableDto variableDto = VariableDto.builder().idVar("TESTIDVAR").values(List.of(new String[]{"V1", "V2"})).build();
        externalVariableDtoList.add(variableDto);

        List<CollectedVariableDto> collectedVariableDtoList = new ArrayList<>();
        CollectedVariableDto collectedVariableDto = new CollectedVariableDto("TESTIDVAR", List.of(new String[]{"V1", "V2"}), "TESTIDLOOP", "TESTIDPARENT");
        collectedVariableDtoList.add(collectedVariableDto);

        SurveyUnitUpdateDto recentDTO = SurveyUnitUpdateDto.builder()
                .idCampaign("TESTIDCAMPAIGN")
                .mode(Mode.WEB)
                .idUE("TESTIDUE")
                .idQuest(idQuestionnaire)
                .state(DataState.COLLECTED)
                .fileDate(LocalDateTime.of(2023, 2, 2, 0, 0, 0))
                .recordDate(LocalDateTime.of(2024, 2, 2, 0, 0, 0))
                .externalVariables(externalVariableDtoList)
                .collectedVariables(collectedVariableDtoList)
                .build();
        surveyUnitUpdatePersistencePortStub.getMongoStub().add(recentDTO);
    }

    private void addAdditionnalDtoToMongoStub(String idCampaign, String idQuestionnaire) {
        List<VariableDto> externalVariableDtoList = new ArrayList<>();
        VariableDto variableDto = VariableDto.builder().idVar("TESTIDVAR").values(List.of(new String[]{"V1", "V2"})).build();
        externalVariableDtoList.add(variableDto);

        List<CollectedVariableDto> collectedVariableDtoList = new ArrayList<>();
        CollectedVariableDto collectedVariableDto = new CollectedVariableDto("TESTIDVAR", List.of(new String[]{"V1", "V2"}), "TESTIDLOOP", "TESTIDPARENT");
        collectedVariableDtoList.add(collectedVariableDto);

        SurveyUnitUpdateDto recentDTO = SurveyUnitUpdateDto.builder()
                .idCampaign(idCampaign)
                .mode(Mode.WEB)
                .idUE("TESTIDUE")
                .idQuest(idQuestionnaire)
                .state(DataState.COLLECTED)
                .fileDate(LocalDateTime.of(2023, 2, 2, 0, 0, 0))
                .recordDate(LocalDateTime.of(2024, 2, 2, 0, 0, 0))
                .externalVariables(externalVariableDtoList)
                .collectedVariables(collectedVariableDtoList)
                .build();
        surveyUnitUpdatePersistencePortStub.getMongoStub().add(recentDTO);
    }
}
