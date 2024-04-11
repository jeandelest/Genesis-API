package fr.insee.genesis.controller.rest;

import fr.insee.genesis.Constants;
import fr.insee.genesis.domain.ports.api.ScheduleApiPort;
import fr.insee.genesis.exceptions.InvalidCronExpressionException;
import fr.insee.genesis.exceptions.NotFoundException;
import fr.insee.genesis.infrastructure.model.document.schedule.StoredSurveySchedule;
import fr.insee.genesis.infrastructure.model.document.schedule.ServiceToCall;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;

@RequestMapping(path = "/schedule")
@Controller
@Slf4j
public class ScheduleController {

    private final ScheduleApiPort scheduleApiPort;

    @Autowired
    public ScheduleController(ScheduleApiPort scheduleApiPort) {
        this.scheduleApiPort = scheduleApiPort;
    }

    //TODO French traduction for users

    @Operation(summary = "Fetch all schedules (for Bangles only)", hidden = true)
    @GetMapping(path = "/all")
    public ResponseEntity<Object> getAllSchedules() {
        log.debug("Got GET all schedules request");

        List<StoredSurveySchedule> storedSurveySchedules = scheduleApiPort.getAllSchedules();

        log.info("Returning " + storedSurveySchedules.size() + " schedule documents...");

        return new ResponseEntity<>(storedSurveySchedules, HttpStatus.OK);
    }

    @Operation(summary = "Schedule a Kraftwerk execution")
    @PutMapping(path = "/create")
    public ResponseEntity<Object> addSchedule(
            @Parameter(description = "Survey name to call Kraftwerk on") @RequestParam("surveyName") String surveyName,
            @Parameter(description = "Kraftwerk endpoint") @RequestParam(value = "serviceTocall", defaultValue = Constants.KRAFTWERK_MAIN_ENDPOINT) ServiceToCall serviceToCall,
            @Parameter(description = "Frequency in Spring cron format. \n Example : 0 0 6 * * *") @RequestParam("frequency") String frequency,
            @Parameter(description = "Schedule effective date and time", example = "2023-06-16T12:00:00") @RequestParam("scheduleBeginDate") LocalDateTime scheduleBeginDate,
            @Parameter(description = "Schedule end date and time", example = "2023-06-17T12:00:00") @RequestParam("scheduleEndDate") LocalDateTime scheduleEndDate
    ){
        try {
            log.info("New schedule request for survey " + surveyName);
            scheduleApiPort.addSchedule(surveyName, serviceToCall, frequency, scheduleBeginDate, scheduleEndDate);
        }catch (InvalidCronExpressionException e){
            log.warn("Returned error for wrong frequency : " + frequency);
            return new ResponseEntity<>("Wrong frequency syntax",HttpStatus.BAD_REQUEST);
        }
        log.info("New schedule created for survey " + surveyName);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "Update last execution date (for Bangles only)", hidden = true)
    @PostMapping(path = "/updateLastExecutionDate")
    public ResponseEntity<Object> updateSurveyLastExecution(
            @Parameter(description = "Survey name to call Kraftwerk on") @RequestBody String surveyName
    ) {
        try {
            log.debug("Got update last execution on " + surveyName);
            scheduleApiPort.updateLastExecutionName(surveyName);
            log.info(surveyName + " last execution updated !");
        }catch (NotFoundException e){
            log.warn("Survey " + surveyName + " not found !");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }
}