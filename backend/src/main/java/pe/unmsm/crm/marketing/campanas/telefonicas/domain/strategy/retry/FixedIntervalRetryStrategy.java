package pe.unmsm.crm.marketing.campanas.telefonicas.domain.strategy.retry;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pe.unmsm.crm.marketing.campanas.telefonicas.api.dto.ResultadoLlamadaRequest;
import pe.unmsm.crm.marketing.campanas.telefonicas.domain.model.RetryPlanStep;
import pe.unmsm.crm.marketing.campanas.telefonicas.domain.model.RetryPolicyType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Calcula reintentos con intervalo fijo.
 */
@Component
public class FixedIntervalRetryStrategy implements RetryStrategy {

    private final int intervalMinutes;
    private final int maxAttempts;

    public FixedIntervalRetryStrategy(
            @Value("${telemarketing.retry.fixed.interval-minutes:15}") int intervalMinutes,
            @Value("${telemarketing.retry.fixed.max-attempts:3}") int maxAttempts
    ) {
        this.intervalMinutes = intervalMinutes;
        this.maxAttempts = maxAttempts;
    }

    @Override
    public List<RetryPlanStep> planRetries(ResultadoLlamadaRequest request, int intentoActual) {
        List<RetryPlanStep> steps = new ArrayList<>();
        LocalDateTime base = request.getFechaReagendamiento() != null
                ? request.getFechaReagendamiento()
                : LocalDateTime.now().plusMinutes(intervalMinutes);

        for (int i = 1; i <= maxAttempts; i++) {
            int intento = intentoActual + i;
            steps.add(RetryPlanStep.builder()
                    .intento(intento)
                    .programadoPara(base.plusMinutes((long) intervalMinutes * (i - 1)))
                    .politica(RetryPolicyType.FIXED_INTERVAL)
                    .build());
        }
        return steps;
    }
}
