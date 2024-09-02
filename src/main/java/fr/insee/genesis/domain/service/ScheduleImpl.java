package fr.insee.genesis.domain.service;

import fr.insee.genesis.Constants;
import fr.insee.genesis.domain.ports.api.ScheduleApiPort;
import fr.insee.genesis.exceptions.InvalidCronExpressionException;
import fr.insee.genesis.exceptions.NotFoundException;
import fr.insee.genesis.infrastructure.model.document.schedule.KraftwerkExecutionSchedule;
import fr.insee.genesis.infrastructure.model.document.schedule.ServiceToCall;
import fr.insee.genesis.infrastructure.model.document.schedule.StoredSurveySchedule;
import fr.insee.genesis.infrastructure.model.document.schedule.TrustParameters;
import fr.insee.genesis.infrastructure.repository.ScheduleMongoDBRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ScheduleImpl implements ScheduleApiPort {
    private final ScheduleMongoDBRepository scheduleMongoDBRepository;
    private final MongoTemplate mongoTemplate;

    @Autowired
    public ScheduleImpl(ScheduleMongoDBRepository scheduleMongoDBRepository, MongoTemplate mongoTemplate) {
        this.scheduleMongoDBRepository = scheduleMongoDBRepository;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public List<StoredSurveySchedule> getAllSchedules() {
        return scheduleMongoDBRepository.findAll();
    }

    @Override
    public void addSchedule(String surveyName, ServiceToCall serviceToCall, String frequency,
                            LocalDateTime scheduleBeginDate, LocalDateTime scheduleEndDate, TrustParameters trustParameters) throws InvalidCronExpressionException{
        //Frequency format check
        if(!CronExpression.isValidExpression(frequency)) {
            throw new InvalidCronExpressionException();
        }
        
        List<StoredSurveySchedule> storedSurveySchedules = scheduleMongoDBRepository.findBySurveyName(surveyName);

        StoredSurveySchedule storedSurveySchedule;
        if (storedSurveySchedules.isEmpty()) {
            //Create if not exists
            log.info("Creation of new survey document for survey {}", surveyName);
            storedSurveySchedules.add(new StoredSurveySchedule(surveyName, new ArrayList<>()));
        }
        ScheduleUnicityService scheduleUnicityService = new ScheduleUnicityService();
        storedSurveySchedule = scheduleUnicityService.deduplicateSurveySchedules(surveyName, storedSurveySchedules);
        storedSurveySchedules.clear();
        storedSurveySchedules.add(storedSurveySchedule);
        storedSurveySchedule.getKraftwerkExecutionScheduleList().add(
                new KraftwerkExecutionSchedule(
                        frequency,
                        serviceToCall,
                        scheduleBeginDate,
                        scheduleEndDate,
                        trustParameters
                )
        );
        scheduleMongoDBRepository.deleteBySurveyName(surveyName);
        scheduleMongoDBRepository.saveAll(storedSurveySchedules);
    }

    @Override
    public void deleteSchedule(String surveyName) {
        scheduleMongoDBRepository.deleteBySurveyName(surveyName);
    }

    public List<KraftwerkExecutionSchedule> deleteExpiredSchedules(String surveyName) throws NotFoundException {
        List<StoredSurveySchedule> storedSurveySchedules = scheduleMongoDBRepository.findBySurveyName(surveyName);
        if (storedSurveySchedules.isEmpty()) {
            throw new NotFoundException();
        }
        List<KraftwerkExecutionSchedule> deletedKraftwerkExecutionSchedules = new ArrayList<>();
        for (StoredSurveySchedule surveySchedule : storedSurveySchedules) {
            deletedKraftwerkExecutionSchedules.addAll(removeExpiredSchedules(surveySchedule));
            //Delete schedule if empty kraftwerkExecutionScheduleList
            scheduleMongoDBRepository.findBySurveyName(surveyName)
                    .stream()
                    .filter(storedSurveySchedule -> storedSurveySchedule.getKraftwerkExecutionScheduleList().isEmpty())
                    .forEach(storedSurveySchedule -> {
                        try {
                            deleteSchedule(surveyName);
                        } catch (NotFoundException e) {
                            log.error("Tried to delete schedule for {} but wasn't found !", surveyName);
                        }
                    });
        }
        return deletedKraftwerkExecutionSchedules;
    }

    @Override
    public void updateLastExecutionName(String surveyName, LocalDateTime newDate) throws NotFoundException {
        List<StoredSurveySchedule> storedSurveySchedules = scheduleMongoDBRepository.findBySurveyName(surveyName);

        if (!storedSurveySchedules.isEmpty()) {
            for(StoredSurveySchedule surveySchedule : storedSurveySchedules){
                surveySchedule.setLastExecution(newDate);
            }
            scheduleMongoDBRepository.saveAll(storedSurveySchedules);
        }else{
            throw new NotFoundException();
        }
    }

    @Override
    public long countSchedules() {
        return scheduleMongoDBRepository.count();
    }

    @Override
    public List<KraftwerkExecutionSchedule> removeExpiredSchedules(StoredSurveySchedule surveySchedule) {
        List<KraftwerkExecutionSchedule> deletedKraftwerkExecutionSchedules = new ArrayList<>();
        for (KraftwerkExecutionSchedule kraftwerkExecutionScheduleToRemove :
                surveySchedule.getKraftwerkExecutionScheduleList().stream().filter(
                        kraftwerkExecutionSchedule -> kraftwerkExecutionSchedule.getScheduleEndDate().isBefore(LocalDateTime.now())
                ).toList()) {
            deletedKraftwerkExecutionSchedules.add(kraftwerkExecutionScheduleToRemove);
            Query query =
                    Query.query(Criteria.where("scheduleEndDate").is(kraftwerkExecutionScheduleToRemove.getScheduleEndDate()));
            mongoTemplate.updateMulti(Query.query(Criteria.where("surveyName").is(surveySchedule.getSurveyName())), new Update().pull(
                            "kraftwerkExecutionScheduleList", query),
                    Constants.MONGODB_SCHEDULE_COLLECTION_NAME);
            log.info("Removed kraftwerk execution schedule on {} because it is expired since {}", surveySchedule.getSurveyName(),
                    kraftwerkExecutionScheduleToRemove.getScheduleEndDate());
        }
        return deletedKraftwerkExecutionSchedules;
    }
}
