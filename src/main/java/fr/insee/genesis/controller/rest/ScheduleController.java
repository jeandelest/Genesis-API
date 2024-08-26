package fr.insee.genesis.controller.rest;

import fr.insee.genesis.Constants;
import fr.insee.genesis.domain.ports.api.ScheduleApiPort;
import fr.insee.genesis.exceptions.InvalidCronExpressionException;
import fr.insee.genesis.exceptions.NotFoundException;
import fr.insee.genesis.infrastructure.model.document.schedule.ServiceToCall;
import fr.insee.genesis.infrastructure.model.document.schedule.StoredSurveySchedule;
import fr.insee.genesis.infrastructure.model.document.schedule.TrustParameters;
import fr.insee.genesis.infrastructure.utils.FileUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RequestMapping(path = "/schedule")
@Controller
@Slf4j
public class ScheduleController {

    private final ScheduleApiPort scheduleApiPort;
    private final FileUtils fileUtils;

    @Autowired
    public ScheduleController(ScheduleApiPort scheduleApiPort, FileUtils fileUtils) {
        this.scheduleApiPort = scheduleApiPort;
        this.fileUtils = fileUtils;
    }

    @Operation(summary = "Fetch all schedules")
    @GetMapping(path = "/all")
    public ResponseEntity<Object> getAllSchedules() {
        log.debug("Got GET all schedules request");

        List<StoredSurveySchedule> storedSurveySchedules = scheduleApiPort.getAllSchedules();

        log.info("Returning {} schedule documents...", storedSurveySchedules.size());
        return ResponseEntity.ok(storedSurveySchedules);
    }

    @Operation(summary = "Schedule a Kraftwerk execution")
    @PutMapping(path = "/create")
    public ResponseEntity<Object> addSchedule(
            @Parameter(description = "Survey name to call Kraftwerk on") @RequestParam("surveyName") String surveyName,
            @Parameter(description = "Kraftwerk endpoint") @RequestParam(value = "serviceTocall", defaultValue = Constants.KRAFTWERK_MAIN_ENDPOINT) ServiceToCall serviceToCall,
            @Parameter(description = "Frequency in Spring cron format (6 inputs, go to https://crontab.cronhub.io/ for generator)  \n Example : 0 0 6 * * *") @RequestParam("frequency") String frequency,
            @Parameter(description = "Schedule effective date and time", example = "2024-01-01T12:00:00") @RequestParam("scheduleBeginDate") LocalDateTime scheduleBeginDate,
            @Parameter(description = "Schedule end date and time", example = "2024-01-01T12:00:00") @RequestParam("scheduleEndDate") LocalDateTime scheduleEndDate,
            @Parameter(description = "Encrypt after process ? Ignore next parameters if false") @RequestParam(value =
                    "useEncryption",
                    defaultValue = "false") boolean useEncryption,
            @Parameter(description = "(Encryption) vault path") @RequestParam(value = "encryptionVaultPath", defaultValue = "") String encryptionVaultPath,
            @Parameter(description = "(Encryption) output folder") @RequestParam(value = "encryptionOutputFolder",
                    defaultValue = "") String encryptionOutputFolder,
            @Parameter(description = "(Encryption) Use signature system") @RequestParam(value = "useSignature", defaultValue = "false") boolean useSignature
    ){
        try {
            if(useEncryption){
                TrustParameters trustParameters = new TrustParameters(
                        fileUtils.getKraftwerkOutFolder(surveyName),
                        encryptionOutputFolder,
                        encryptionVaultPath,
                        useSignature
                );
                log.info("New schedule request for survey {} with encryption", surveyName);
                scheduleApiPort.addSchedule(surveyName, serviceToCall, frequency, scheduleBeginDate, scheduleEndDate,
                        trustParameters);
            }else{
                log.info("New schedule request for survey {}", surveyName);
                scheduleApiPort.addSchedule(surveyName, serviceToCall, frequency, scheduleBeginDate, scheduleEndDate,
                        null);
            }

        }catch (InvalidCronExpressionException e){
            log.warn("Returned error for wrong frequency : {}", frequency);
            return ResponseEntity.badRequest().body("Wrong frequency syntax");
        }
        log.info("New schedule created for survey {}", surveyName);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Delete a Kraftwerk execution schedule(s) by its survey name")
    @DeleteMapping(path = "/delete")
    public ResponseEntity<Object> deleteSchedule(
            @Parameter(description = "Survey name of the schedule(s) to delete") @RequestParam("surveyName") String surveyName
    ){
        try {
            log.info("Delete schedule request for survey {}", surveyName);
            scheduleApiPort.deleteSchedule(surveyName);
        }catch (NotFoundException e){
            log.warn("Survey {} not found !", surveyName);
            return ResponseEntity.notFound().build();
        }
        log.info("Schedule deleted for survey {}", surveyName);
        return ResponseEntity.ok().build();
    }


    @Operation(summary = "Set last execution date with new date or empty")
    @PostMapping(path = "/setLastExecutionDate")
    public ResponseEntity<Object> setSurveyLastExecution(
            @Parameter(description = "Survey name to call Kraftwerk on") @RequestBody String surveyName,
            @Parameter(description = "Date to save as last execution date", example = "2024-01-01T12:00:00") @RequestParam("newDate") LocalDateTime newDate
            ) {
        try {
            log.debug("Got update last execution on {} with data param {}", surveyName, newDate);
            scheduleApiPort.updateLastExecutionName(surveyName, newDate);
            log.info("{} last execution updated at {} !", surveyName, newDate);
        }catch (NotFoundException e){
            log.warn("Survey {} not found !", surveyName);
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().build();
    }
}
