package fr.insee.genesis.controller.rest;

import fr.insee.bpm.exceptions.MetadataParserException;
import fr.insee.bpm.metadata.model.VariablesMap;
import fr.insee.bpm.metadata.reader.ddi.DDIReader;
import fr.insee.bpm.metadata.reader.lunatic.LunaticReader;
import fr.insee.genesis.Constants;
import fr.insee.genesis.controller.adapter.LunaticXmlAdapter;
import fr.insee.genesis.controller.dto.CampaignWithQuestionnaire;
import fr.insee.genesis.controller.dto.QuestionnaireWithCampaign;
import fr.insee.genesis.controller.dto.SurveyUnitDto;
import fr.insee.genesis.controller.dto.SurveyUnitId;
import fr.insee.genesis.controller.dto.SurveyUnitSimplified;
import fr.insee.genesis.controller.sources.xml.LunaticXmlCampaign;
import fr.insee.genesis.controller.sources.xml.LunaticXmlDataParser;
import fr.insee.genesis.controller.sources.xml.LunaticXmlDataSequentialParser;
import fr.insee.genesis.controller.sources.xml.LunaticXmlSurveyUnit;
import fr.insee.genesis.controller.utils.ControllerUtils;
import fr.insee.genesis.domain.model.surveyunit.CollectedVariable;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.domain.model.surveyunit.Variable;
import fr.insee.genesis.domain.ports.api.SurveyUnitApiPort;
import fr.insee.genesis.domain.service.surveyunit.SurveyUnitQualityService;
import fr.insee.genesis.domain.service.volumetry.VolumetryLogService;
import fr.insee.genesis.exceptions.GenesisError;
import fr.insee.genesis.exceptions.GenesisException;
import fr.insee.genesis.exceptions.NoDataError;
import fr.insee.genesis.infrastructure.utils.FileUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

@RequestMapping(path = "/response" )
@Controller
@Tag(name = "Response services for interrogations", description = "A **response** is considered the entire set of data associated with an interrogation (idUE x idQuestionnaire). \n\n These data may have different state (collected, edited, external, ...) ")
@Slf4j
public class ResponseController {

    private static final String DDI_REGEX = "ddi[\\w,\\s-]+\\.xml";
    public static final String S_S = "%s/%s";
    private final SurveyUnitApiPort surveyUnitService;
    private final SurveyUnitQualityService surveyUnitQualityService;
    private final VolumetryLogService volumetryLogService;
    private final FileUtils fileUtils;
    private final ControllerUtils controllerUtils;

    @Autowired
    public ResponseController(SurveyUnitApiPort surveyUnitService, SurveyUnitQualityService surveyUnitQualityService, VolumetryLogService volumetryLogService, FileUtils fileUtils, ControllerUtils controllerUtils) {
        this.surveyUnitService = surveyUnitService;
        this.surveyUnitQualityService = surveyUnitQualityService;
        this.volumetryLogService = volumetryLogService;
        this.fileUtils = fileUtils;
        this.controllerUtils = controllerUtils;
    }

    //SAVE
    @Operation(summary = "Save one file of responses to Genesis Database, passing its path as a parameter")
    @PutMapping(path = "/save/lunatic-xml/one-file")
    public ResponseEntity<Object> saveResponsesFromXmlFile(@RequestParam("pathLunaticXml") String xmlFile,
                                                           @RequestParam(value = "pathSpecFile") String metadataFilePath,
                                                           @RequestParam(value = "mode") Mode modeSpecified,
                                                           @RequestParam(value = "withDDI", defaultValue = "true") boolean withDDI
    )throws Exception {
        VariablesMap variablesMap;
        if(withDDI) {
            //Parse DDI
            log.info(String.format("Try to read DDI file : %s", metadataFilePath));
            try {
                variablesMap =
                        DDIReader.getMetadataFromDDI(Path.of(metadataFilePath).toFile().toURI().toURL().toString(),
                                new FileInputStream(metadataFilePath)).getVariables();
            } catch (MetadataParserException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
            }
        }else{
            //Parse Lunatic
            log.info(String.format("Try to read lunatic file : %s", metadataFilePath));

            variablesMap = LunaticReader.getMetadataFromLunatic(new FileInputStream(metadataFilePath)).getVariables();
        }

        log.info(String.format("Try to read Xml file : %s", xmlFile));
        Path filepath = Paths.get(xmlFile);

        if (filepath.toFile().length() / 1024 / 1024 <= Constants.MAX_FILE_SIZE_UNTIL_SEQUENTIAL) {
            return treatXmlFileWithMemory(filepath, modeSpecified, variablesMap);
        } else {
            return treatXmlFileSequentially(filepath, modeSpecified, variablesMap);
        }
    }

    @Operation(summary = "Save multiple files to Genesis Database from the campaign root folder")
    @PutMapping(path = "/save/lunatic-xml")
    public ResponseEntity<Object> saveResponsesFromXmlCampaignFolder(@RequestParam("campaignName") String campaignName,
                                                                     @RequestParam(value = "mode", required = false) Mode modeSpecified,
                                                                     @RequestParam(value = "withDDI", defaultValue = "true") boolean withDDI
    )throws Exception {
        List<GenesisError> errors = new ArrayList<>();

        log.info("Try to import XML data for campaign : {}", campaignName);

        try {
            List<Mode> modesList = controllerUtils.getModesList(campaignName, modeSpecified);
            for (Mode currentMode : modesList) {
                treatCampaignWithMode(campaignName, currentMode, errors, null, withDDI);
            }
        } catch (GenesisException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getMessage());
        }
        return ResponseEntity.ok("Data saved");
    }

    //SAVE ALL
    @Operation(summary = "Save all files to Genesis Database (differential data folder only), regardless of the campaign")
    @PutMapping(path = "/save/lunatic-xml/all-campaigns")
    public ResponseEntity<Object> saveResponsesFromAllCampaignFolders(){
        List<GenesisError> errors = new ArrayList<>();
        List<File> campaignFolders = fileUtils.listAllSpecsFolders();

        if (campaignFolders.isEmpty()) {
            return ResponseEntity.ok("No campaign to save");
        }

        for (File  campaignFolder: campaignFolders) {
            String campaignName = campaignFolder.getName();
            log.info("Try to import data for campaign : {}", campaignName);

            try {
                List<Mode> modesList = controllerUtils.getModesList(campaignName, null); //modeSpecified null = all modes
                for (Mode currentMode : modesList) {
                    treatCampaignWithMode(campaignName, currentMode, errors, Constants.DIFFRENTIAL_DATA_FOLDER_NAME);
                }
            } catch (Exception e) {
                log.warn("Error for campaign {} : {}", campaignName, e.toString());
                errors.add(new GenesisError(e.getMessage()));
            }
        }
        if (errors.isEmpty()) {
            return ResponseEntity.ok("Data saved");
        } else {
            return ResponseEntity.status(209).body("Data saved with " + errors.size() + " errors");
        }
    }

    @Operation(summary = "Record volumetrics of each campaign in a folder")
    @PutMapping(path = "/save-volumetry/all-campaigns")
    public ResponseEntity<Object> saveVolumetry() throws IOException {
        volumetryLogService.writeVolumetries(surveyUnitService);
        volumetryLogService.cleanOldFiles();
        return ResponseEntity.ok("Volumetric saved");
    }
    
    //DELETE
    @Operation(summary = "Delete all responses associated with a questionnaire")
    @DeleteMapping(path = "/delete-responses/by-questionnaire")
    public ResponseEntity<Object> deleteAllResponsesByQuestionnaire(@RequestParam("idQuestionnaire") String idQuestionnaire) {
        log.info("Try to delete all responses of questionnaire : {}", idQuestionnaire);
        Long ndDocuments = surveyUnitService.deleteByIdQuestionnaire(idQuestionnaire);
        log.info("{} responses deleted", ndDocuments);
        return ResponseEntity.ok(String.format("%d responses deleted", ndDocuments));
    }

    //GET
    @Operation(summary = "Retrieve responses for an interrogation, using IdUE and IdQuestionnaire from Genesis Database")
    @GetMapping(path = "/get-responses/by-ue-and-questionnaire")
    public ResponseEntity<List<SurveyUnitModel>> findResponsesByUEAndQuestionnaire(@RequestParam("idUE") String idUE,
                                                                                   @RequestParam("idQuestionnaire") String idQuestionnaire) {
        List<SurveyUnitModel> responses = surveyUnitService.findByIdsUEAndQuestionnaire(idUE, idQuestionnaire);
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "Retrieve responses for an interrogation, using IdUE and IdQuestionnaire from Genesis Database with the latest value for each available state of every variable")
    @GetMapping(path = "/get-responses/by-ue-and-questionnaire/latest-states")
    public ResponseEntity<SurveyUnitDto> findResponsesByUEAndQuestionnaireLastestStates(
            @RequestParam("idUE") String idUE,
            @RequestParam("idQuestionnaire") String idQuestionnaire) {
        SurveyUnitDto response = surveyUnitService.findLatestValuesByStateByIdAndByIdQuestionnaire(idUE, idQuestionnaire);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Retrieve all responses (for all interrogations) of one questionnaire")
    @GetMapping(path = "/get-responses/by-questionnaire")
    public ResponseEntity<Path> findAllResponsesByQuestionnaire(@RequestParam("idQuestionnaire") String idQuestionnaire) {
        log.info("Try to find all responses of questionnaire : {}", idQuestionnaire);

        //Get all IdUEs/modes of the survey
        List<SurveyUnitModel> idUEsResponses = surveyUnitService.findIdUEsAndModesByIdQuestionnaire(idQuestionnaire);
        log.info("Responses found : {}", idUEsResponses.size());

        String filepathString = String.format("OUT/%s/OUT_ALL_%s.json", idQuestionnaire, LocalDateTime.now().toString().replace(":", ""));
        Path filepath = Path.of(fileUtils.getDataFolderSource(), filepathString);

        try (Stream<SurveyUnitModel> responsesStream = surveyUnitService.findByIdQuestionnaire(idQuestionnaire)) {
            fileUtils.writeSuUpdatesInFile(filepath, responsesStream);
        } catch (IOException e) {
            log.error("Error while writing file", e);
            return ResponseEntity.internalServerError().body(filepath);
        }
        log.info("End of extraction, responses extracted : {}", idUEsResponses.size());
        return ResponseEntity.ok(filepath);
    }

    @Operation(summary = "Retrieve responses for an interrogation, using IdUE and IdQuestionnaire from Genesis Database. It returns only the latest value of each variable regardless of the state.")
    @GetMapping(path = "/get-responses/by-ue-and-questionnaire/latest")
    public ResponseEntity<List<SurveyUnitModel>> getLatestByUE(@RequestParam("idUE") String idUE,
                                                               @RequestParam("idQuestionnaire") String idQuestionnaire) {
        List<SurveyUnitModel> responses = surveyUnitService.findLatestByIdAndByIdQuestionnaire(idUE, idQuestionnaire);
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "Retrieve response latest state with IdUE and IdQuestionnaire in one object in the output")
    @GetMapping(path = "/get-simplified-response/by-ue-and-questionnaire/latest")
    public ResponseEntity<SurveyUnitSimplified> getLatestByUEOneObject(@RequestParam("idUE") String idUE,
                                                                             @RequestParam("idQuestionnaire") String idQuestionnaire,
                                                                             @RequestParam("mode") Mode mode) {
        List<SurveyUnitModel> responses = surveyUnitService.findLatestByIdAndByIdQuestionnaire(idUE, idQuestionnaire);
        List<CollectedVariable> outputVariables = new ArrayList<>();
        List<Variable> outputExternalVariables = new ArrayList<>();
        responses.stream().filter(rep -> rep.getMode().equals(mode)).forEach(response -> {
            outputVariables.addAll(response.getCollectedVariables());
            outputExternalVariables.addAll(response.getExternalVariables());
        });
        return ResponseEntity.ok(SurveyUnitSimplified.builder()
                .idQuest(responses.getFirst().getIdQuest())
                .idCampaign(responses.getFirst().getIdCampaign())
                .idUE(responses.getFirst().getIdUE())
                .variablesUpdate(outputVariables)
                .externalVariables(outputExternalVariables)
                .build());
    }


    @Operation(summary = "Retrieve all responses for a questionnaire and a list of UE",
            description = "Return the latest state for each variable for the given ids and a given questionnaire.<br>" +
                    "For a given id, the endpoint returns a single document that merges all collection modes (if there is more than one).")
    @PostMapping(path = "/get-simplified-responses/by-ue-and-questionnaire/latest")
    public ResponseEntity<List<SurveyUnitSimplified>> getLatestForUEList(@RequestParam("idQuestionnaire") String idQuestionnaire,
                                                                               @RequestBody List<SurveyUnitId> idUEs) {
        List<SurveyUnitSimplified> results = new ArrayList<>();
        List<Mode> modes = surveyUnitService.findModesByIdQuestionnaire(idQuestionnaire);
        idUEs.forEach(idUE -> {
            List<SurveyUnitModel> responses = surveyUnitService.findLatestByIdAndByIdQuestionnaire(idUE.getIdUE(), idQuestionnaire);
            modes.forEach(mode -> {
                List<CollectedVariable> outputVariables = new ArrayList<>();
                List<Variable> outputExternalVariables = new ArrayList<>();
                responses.stream().filter(rep -> rep.getMode().equals(mode)).forEach(response -> {
                    outputVariables.addAll(response.getCollectedVariables());
                    outputExternalVariables.addAll(response.getExternalVariables());
                });
                if (!outputVariables.isEmpty() || !outputExternalVariables.isEmpty()) {
                    results.add(SurveyUnitSimplified.builder()
                            .idQuest(responses.getFirst().getIdQuest())
                            .idCampaign(responses.getFirst().getIdCampaign())
                            .idUE(responses.getFirst().getIdUE())
                            .mode(mode)
                            .variablesUpdate(outputVariables)
                            .externalVariables(outputExternalVariables)
                            .build());
                }
            });
        });
        return ResponseEntity.ok(results);
    }

    @Operation(summary = "Retrieve all IdUEs for a given questionnaire")
    @GetMapping(path = "/get-idUEs/by-questionnaire")
    public ResponseEntity<List<SurveyUnitId>> getAllIdUEsByQuestionnaire(@RequestParam("idQuestionnaire") String idQuestionnaire) {
        List<SurveyUnitId> responses = surveyUnitService.findDistinctIdUEsByIdQuestionnaire(idQuestionnaire);
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "List sources/modes used for a given questionnaire")
    @GetMapping(path = "/get-modes/by-questionnaire")
    public ResponseEntity<List<Mode>> getModesByQuestionnaire(@RequestParam("idQuestionnaire") String idQuestionnaire) {
        List<Mode> modes = surveyUnitService.findModesByIdQuestionnaire(idQuestionnaire);
        return ResponseEntity.ok(modes);
    }

    @Operation(summary = "List sources/modes used for a given campaign")
    @GetMapping(path = "/get-modes/by-campaign")
    public ResponseEntity<List<Mode>> getModesByCampaign(@RequestParam("idCampaign") String idCampaign) {
        List<Mode> modes = surveyUnitService.findModesByIdCampaign(idCampaign);
        return ResponseEntity.ok(modes);
    }

    @Operation(summary = "List questionnaires in database")
    @GetMapping(path = "/get-questionnaires")
    public ResponseEntity<Set<String>> getQuestionnaires() {
        Set<String> questionnaires = surveyUnitService.findDistinctIdQuestionnaires();
        return ResponseEntity.ok(questionnaires);
    }


    @Operation(summary = "List questionnaires in database with their campaigns")
    @GetMapping(path = "/get-questionnaires/with-campaigns")
    public ResponseEntity<List<QuestionnaireWithCampaign>> getQuestionnairesWithCampaigns() {
        List<QuestionnaireWithCampaign> questionnaireWithCampaignList =
                surveyUnitService.findQuestionnairesWithCampaigns();
        return ResponseEntity.ok(questionnaireWithCampaignList);
    }

    @Operation(summary = "List questionnaires used for a given campaign")
    @GetMapping(path = "/get-questionnaires/by-campaign")
    public ResponseEntity<Set<String>> getQuestionnairesByCampaign(@RequestParam("idCampaign") String idCampaign) {
        Set<String> questionnaires = surveyUnitService.findIdQuestionnairesByIdCampaign(idCampaign);
        return ResponseEntity.ok(questionnaires);
    }

    @Operation(summary = "List campaigns in database")
    @GetMapping(path = "/get-campaigns")
    public ResponseEntity<Set<String>> getCampaigns() {
        Set<String> campaigns = surveyUnitService.findDistinctIdCampaigns();
        return ResponseEntity.ok(campaigns);
    }

    @Operation(summary = "List campaigns in database with their questionnaires")
    @GetMapping(path = "/get-campaigns/with-questionnaires")
    public ResponseEntity<List<CampaignWithQuestionnaire>> getCampaignsWithQuestionnaires() {
        List<CampaignWithQuestionnaire> questionnairesByCampaigns = surveyUnitService.findCampaignsWithQuestionnaires();
        return ResponseEntity.ok(questionnairesByCampaigns);
    }

    //Utilities

    /**
     * Checks if DDI is present for a campaign and mode or not and treats it accordingly
     * @param campaignName name of campaign
     * @param currentMode mode of collected data
     * @param errors error list to fill
     */
    private void treatCampaignWithMode(String campaignName, Mode currentMode, List<GenesisError> errors, String rootDataFolder) throws GenesisException,
            SAXException, XMLStreamException {
        try {
            fileUtils.findFile(String.format(S_S, fileUtils.getSpecFolder(campaignName),currentMode), DDI_REGEX);
            //DDI if DDI file found
            treatCampaignWithMode(campaignName, currentMode, errors, rootDataFolder, true);
        }catch (RuntimeException re){
            //Lunatic if no DDI
            log.info("No DDI File found for {}, {} mode. Will try to use Lunatic...", campaignName,
                    currentMode.getModeName());
            try{
                treatCampaignWithMode(campaignName, currentMode, errors, rootDataFolder, false);
            } catch (IOException | ParserConfigurationException e) {
                throw new GenesisException(500, e.toString());
            }
        }catch (IOException | ParserConfigurationException e){
            log.error(e.toString());
            throw new GenesisException(500, e.toString());
        }
    }


    /**
     * Treat a campaign with a specific mode
     * @param campaignName name of campaign
     * @param mode mode of collected data
     * @param errors error list to fill
     * @param withDDI true if it uses DDI, false if Lunatic
     */
    private void treatCampaignWithMode(String campaignName, Mode mode, List<GenesisError> errors, String rootDataFolder, boolean withDDI)
            throws IOException, ParserConfigurationException,SAXException, XMLStreamException {
        log.info("Try to import data for mode : {}", mode.getModeName());
        String dataFolder = rootDataFolder == null ?
                fileUtils.getDataFolder(campaignName, mode.getFolder())
                : fileUtils.getDataFolder(campaignName, mode.getFolder(), rootDataFolder);
        List<String> dataFiles = fileUtils.listFiles(dataFolder);
        log.info("Numbers of files to load in folder {} : {}", dataFolder, dataFiles.size());
        if (dataFiles.isEmpty()) {
            errors.add(new NoDataError("No data file found", Mode.getEnumFromModeName(mode.getModeName())));
            log.warn("No data file found in folder {}", dataFolder);
            return;
        }

        VariablesMap variablesMap = readMetadatas(campaignName, mode, errors, withDDI);
        if (variablesMap == null){
            return;
        }

        //For each XML data file
        for (String fileName : dataFiles.stream().filter(s -> s.endsWith(".xml")).toList()) {
            String filepathString = String.format(S_S, dataFolder, fileName);
            Path filepath = Paths.get(filepathString);
            //Check if file not in done folder, delete if true
            if(isDataFileInDoneFolder(filepath, campaignName, mode.getFolder())){
                log.warn("File {} already exists in DONE folder ! Deleting...", fileName);
                Files.deleteIfExists(filepath);
            }else{
                //Read file
                log.info("Try to read Xml file : {}", fileName);
                ResponseEntity<Object> response;
                if (filepath.toFile().length() / 1024 / 1024 <= Constants.MAX_FILE_SIZE_UNTIL_SEQUENTIAL) {
                    response = treatXmlFileWithMemory(filepath, mode, variablesMap);
                } else {
                    response = treatXmlFileSequentially(filepath, mode, variablesMap);
                }
                log.debug("File {} saved", fileName);
                if(response.getStatusCode() == HttpStatus.OK){
                    fileUtils.moveDataFile(campaignName, mode.getFolder(), filepath);
                }else{
                    log.error("Error on file {}", fileName);
                }
            }
        }
    }

    private VariablesMap readMetadatas(String campaignName, Mode mode, List<GenesisError> errors, boolean withDDI) {
        VariablesMap variablesMap;
        if(withDDI){
            //Read DDI
            try {
                Path ddiFilePath = fileUtils.findFile(String.format(S_S, fileUtils.getSpecFolder(campaignName),
                            mode.getModeName()), DDI_REGEX);
                variablesMap = DDIReader.getMetadataFromDDI(ddiFilePath.toUri().toURL().toString(),
                        new FileInputStream(ddiFilePath.toString())).getVariables();
            } catch (Exception e) {
                log.error(e.toString());
                errors.add(new GenesisError(e.toString()));
                return null;
            }
        }else{
            //Read Lunatic
            try {
                Path lunaticFilePath = fileUtils.findFile(String.format(S_S, fileUtils.getSpecFolder(campaignName),
                        mode.getModeName()), "lunatic[\\w," + "\\s-]+\\.json");
                variablesMap = LunaticReader.getMetadataFromLunatic(new FileInputStream(lunaticFilePath.toString())).getVariables();
            } catch (Exception e) {
                log.error(e.toString());
                errors.add(new GenesisError(e.toString()));
                return null;
            }
        }
        return variablesMap;
    }

    private boolean isDataFileInDoneFolder(Path filepath, String campaignName, String modeFolder) {
        return Path.of(fileUtils.getDoneFolder(campaignName, modeFolder)).resolve(filepath.getFileName()).toFile().exists();
    }

    private ResponseEntity<Object> treatXmlFileWithMemory(Path filepath, Mode modeSpecified, VariablesMap variablesMap) throws IOException, ParserConfigurationException, SAXException {
        LunaticXmlCampaign campaign;
        // DOM method
        LunaticXmlDataParser parser = new LunaticXmlDataParser();
        try {
            campaign = parser.parseDataFile(filepath);
        } catch (GenesisException e) {
            log.error(e.toString());
            return ResponseEntity.status(e.getStatus()).body(e.getMessage());
        }

        List<SurveyUnitModel> suDtos = new ArrayList<>();
        for (LunaticXmlSurveyUnit su : campaign.getSurveyUnits()) {
            suDtos.addAll(LunaticXmlAdapter.convert(su, variablesMap, campaign.getIdCampaign(), modeSpecified));
        }
        surveyUnitQualityService.verifySurveyUnits(suDtos, variablesMap);

        log.debug("Saving {} survey units updates", suDtos.size());
        surveyUnitService.saveSurveyUnits(suDtos);
        log.debug("Survey units updates saved");

        log.info("File {} treated with {} survey units", filepath.getFileName(), suDtos.size());
        return ResponseEntity.ok().build();
    }

    private ResponseEntity<Object> treatXmlFileSequentially(Path filepath, Mode modeSpecified, VariablesMap variablesMap) throws IOException, XMLStreamException {
        LunaticXmlCampaign campaign;
        //Sequential method
        log.warn("File size > " + Constants.MAX_FILE_SIZE_UNTIL_SEQUENTIAL + "MB! Parsing XML file using sequential method...");
        try (final InputStream stream = new FileInputStream(filepath.toFile())) {
            LunaticXmlDataSequentialParser parser = new LunaticXmlDataSequentialParser(filepath, stream);
            int suCount = 0;

            campaign = parser.getCampaign();
            LunaticXmlSurveyUnit su = parser.readNextSurveyUnit();

            while (su != null) {
                List<SurveyUnitModel> suDtos = new ArrayList<>(LunaticXmlAdapter.convert(su, variablesMap, campaign.getIdCampaign(), modeSpecified));

                surveyUnitQualityService.verifySurveyUnits(suDtos, variablesMap);
                surveyUnitService.saveSurveyUnits(suDtos);
                suCount++;

                su = parser.readNextSurveyUnit();
            }

            log.info("Saved {} survey units updates", suCount);
        }

        log.info("File {} treated", filepath.getFileName());
        return ResponseEntity.ok().build();
    }
}
