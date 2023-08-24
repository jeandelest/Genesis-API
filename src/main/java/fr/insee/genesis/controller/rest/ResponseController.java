package fr.insee.genesis.controller.rest;

import fr.insee.genesis.controller.adapter.LunaticXmlAdapter;
import fr.insee.genesis.controller.model.Mode;
import fr.insee.genesis.controller.sources.ddi.DDIReader;
import fr.insee.genesis.controller.sources.ddi.VariablesMap;
import fr.insee.genesis.controller.sources.xml.LunaticXmlCampaign;
import fr.insee.genesis.controller.sources.xml.LunaticXmlDataParser;
import fr.insee.genesis.controller.sources.xml.LunaticXmlSurveyUnit;
import fr.insee.genesis.domain.dtos.SurveyUnitUpdateDto;
import fr.insee.genesis.domain.ports.api.SurveyUnitUpdateApiPort;
import fr.insee.genesis.exceptions.GenesisError;
import fr.insee.genesis.exceptions.GenesisException;
import fr.insee.genesis.exceptions.NoDataError;
import fr.insee.genesis.infrastructure.utils.FileUtils;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@RequestMapping(path = "/response")
@Controller
@Slf4j
public class ResponseController {

    @Autowired
    SurveyUnitUpdateApiPort surveyUnitService;

    @Autowired
    FileUtils fileUtils;

    @Operation(summary = "Save responses from XML Lunatic in Genesis Database")
    @PutMapping(path = "/save/lunatic-xml/one-file")
    public ResponseEntity<Object> saveResponsesFromXmlFile(     @RequestParam("pathLunaticXml") String xmlFile,
                                                                @RequestParam("pathDDI") String ddiFile)
            throws Exception {
        log.info(String.format("Try to read Xml file : %s", xmlFile));
        LunaticXmlDataParser parser = new LunaticXmlDataParser();
        LunaticXmlCampaign campaign = parser.parseDataFile(Paths.get(xmlFile));
        log.info(String.format("Try to read DDI file : %s", ddiFile));
        Path ddiFilePath = Paths.get(ddiFile);
        VariablesMap variablesMap;
        try {
            variablesMap = DDIReader.getVariablesFromDDI(ddiFilePath.toFile().toURI().toURL());
        } catch (GenesisException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getMessage());
        }
        List<SurveyUnitUpdateDto> suDtos = new ArrayList<>();
        for (LunaticXmlSurveyUnit su : campaign.getSurveyUnits()) {
            SurveyUnitUpdateDto suDto = LunaticXmlAdapter.convert(su, variablesMap);
            suDtos.add(suDto);
        }
        surveyUnitService.saveSurveyUnits(suDtos);
        log.info("File {} treated", xmlFile);
        return new ResponseEntity<>("Test", HttpStatus.OK);
    }

    @Operation(summary = "Save multiples files in Genesis Database")
    @PutMapping(path = "/save/lunatic-xml")
    public ResponseEntity<Object> saveResponsesFromXmlCampaignFolder(@RequestParam("campaignName") String campaignName,
                                                                     @RequestParam(value = "mode", required = false) Mode mode)
            throws Exception {
        log.info("Try to import data for campaign : {}", campaignName);
        LunaticXmlDataParser parser = new LunaticXmlDataParser();
        List<Mode> modes = new ArrayList<>();
        // We list the mode to treat, if no mode is specified, we treat all modes in the campaign.
        // If a mode is specified, we treat only this mode. If no node is specified and no specs are found, we return an error
        if (mode != null){
            modes.add(mode);
        } else {
            String specFolder = fileUtils.getSpecFolder(campaignName);
            List<String> specFolders = fileUtils.listFolders(specFolder);
            if (specFolders.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No specification folder found " + specFolder);
            }
            for(String modeLabel : specFolders){
                modes.add(Mode.getEnumFromModeName(modeLabel));
            }
            if (modes.contains(Mode.FAF) && modes.contains(Mode.TEL)) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Cannot treat simultaneously TEL and FAF modes");
            }
        }

        List<GenesisError> errors = new ArrayList<>();
        for(Mode currentMode : modes) {
            log.info("Try to import data for mode : {}", currentMode.getModeName());
            String dataFolder = fileUtils.getDataFolder(campaignName, currentMode.getFolder());
            List<String> dataFiles = fileUtils.listFiles(dataFolder);
            log.info("Numbers of files to load in folder {} : {}", dataFolder, dataFiles.size());
            if (dataFiles.isEmpty()) {
                errors.add(new NoDataError("No data file found",Mode.getEnumFromModeName(currentMode.getModeName())));
                log.info("No data file found in folder " + dataFolder);
            }
            for (String fileName : dataFiles) {
                String pathFile = String.format("%s/%s", dataFolder, fileName);
                log.info("Try to read Xml file : {}", fileName);
                LunaticXmlCampaign campaign = parser.parseDataFile(Paths.get(pathFile));
                VariablesMap variablesMap;
                try {
                    Path ddiFilePath = fileUtils.findDDIFile(campaignName, currentMode.getModeName());
                    variablesMap = DDIReader.getVariablesFromDDI(ddiFilePath.toFile().toURI().toURL());
                } catch (GenesisException e) {
                    return ResponseEntity.status(e.getStatus()).body(e.getMessage());
                } catch(Exception e) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
                }
                List<SurveyUnitUpdateDto> suDtos = new ArrayList<>();
                for (LunaticXmlSurveyUnit su : campaign.getSurveyUnits()) {
                    SurveyUnitUpdateDto suDto = LunaticXmlAdapter.convert(su, variablesMap);
                    suDtos.add(suDto);
                }
                surveyUnitService.saveSurveyUnits(suDtos);
                log.info("File {} saved", fileName);
                fileUtils.moveDataFile(campaignName, currentMode.getFolder(), fileName);
            }
        }
        return new ResponseEntity<>("Data saved", HttpStatus.OK);
    }

    @Operation(summary = "Retrieve responses with IdUE and IdQuestionnaire from Genesis Database")
    @GetMapping(path = "/findResponsesByUEAndQuestionnaire")
    public ResponseEntity<List<SurveyUnitUpdateDto>> findResponsesByUEAndQuestionnaire(     @RequestParam("idUE") String idUE,
                                                                                            @RequestParam("idQuestionnaire") String idQuestionnaire) {
        List<SurveyUnitUpdateDto> responses = surveyUnitService.findByIdsUEAndQuestionnaire(idUE, idQuestionnaire);
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }

    @Operation(summary = "Retrieve all responses of one questionnaire")
    @GetMapping(path = "/findAllResponsesByQuestionnaire")
    public ResponseEntity<List<SurveyUnitUpdateDto>> findAllResponsesByQuestionnaire(@RequestParam("idQuestionnaire") String idQuestionnaire) {
        log.info("Try to find all responses of questionnaire : " + idQuestionnaire);
        List<SurveyUnitUpdateDto> responses = surveyUnitService.findByIdQuestionnaire(idQuestionnaire);
        log.info("Responses found : " + responses.size());
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }

}
