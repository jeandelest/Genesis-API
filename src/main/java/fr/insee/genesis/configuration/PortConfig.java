package fr.insee.genesis.configuration;

import fr.insee.genesis.controller.rest.ResponseController;
import fr.insee.genesis.domain.ports.api.SurveyUnitUpdateApiPort;
import fr.insee.genesis.domain.ports.spi.SurveyUnitUpdatePersistencePort;
import fr.insee.genesis.domain.service.SurveyUnitUpdateImpl;
import fr.insee.genesis.infrastructure.adapter.SurveyUnitUpdateJpaAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PortConfig {

    @Bean
    SurveyUnitUpdatePersistencePort surveyUnitUpdatePersistence(){
        return new SurveyUnitUpdateJpaAdapter();
    }

    @Bean
    SurveyUnitUpdateApiPort surveyUnitService(){
        return new SurveyUnitUpdateImpl(surveyUnitUpdatePersistence());
    }

}
