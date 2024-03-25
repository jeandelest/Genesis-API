package fr.insee.genesis.infrastructure.repository;

import fr.insee.genesis.infrastructure.model.document.schedule.ScheduleDocument;
import fr.insee.genesis.infrastructure.model.document.schedule.ServiceToCall;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScheduleMongoDBRepository extends MongoRepository<ScheduleDocument, String>{
    List<ScheduleDocument> findAll();

    @Query(value = "{ 'surveyName' : ?0 }")
    List<ScheduleDocument> findBySurveyName(String surveyName);
}
