package fr.insee.genesis.infrastructure.adapter;

import fr.insee.genesis.domain.ports.spi.SchedulePersistencePort;
import fr.insee.genesis.infrastructure.model.document.schedule.SurveyScheduleDocument;
import fr.insee.genesis.infrastructure.repository.ScheduleMongoDBRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class ScheduleMongoAdapter implements SchedulePersistencePort {
    @Autowired
    private ScheduleMongoDBRepository scheduleMongoDBRepository;


    @Override
    public List<SurveyScheduleDocument> getAll() {
        return scheduleMongoDBRepository.findAll();
    }

    @Override
    public void saveAll(List<SurveyScheduleDocument> surveyScheduleDocuments) {
        scheduleMongoDBRepository.saveAll(surveyScheduleDocuments);
    }

    @Override
    public List<SurveyScheduleDocument> findBySurveyName(String surveyName) {
        return scheduleMongoDBRepository.findBySurveyName(surveyName);
    }

    @Override
    public void deleteBySurveyName(String surveyName) {
        scheduleMongoDBRepository.deleteBySurveyName(surveyName);
    }

    @Override
    public long countSchedules() {
        return scheduleMongoDBRepository.count();
    }
}
