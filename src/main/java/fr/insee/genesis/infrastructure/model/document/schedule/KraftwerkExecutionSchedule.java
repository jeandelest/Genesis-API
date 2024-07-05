package fr.insee.genesis.infrastructure.model.document.schedule;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.lang.Nullable;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class KraftwerkExecutionSchedule {
    private String frequency;

    private ServiceToCall serviceToCall;

    private LocalDateTime scheduleBeginDate;
    private LocalDateTime scheduleEndDate;

    private Boolean useTrustEncryption;
}
