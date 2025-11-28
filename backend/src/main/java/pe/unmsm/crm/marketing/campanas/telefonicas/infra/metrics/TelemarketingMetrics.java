package pe.unmsm.crm.marketing.campanas.telefonicas.infra.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Registrador de métricas de telemarketing (Micrometer).
 */
@Component
public class TelemarketingMetrics {

    private final Counter callResults;
    private final Timer callResultTimer;
    private final Counter queuedCommands;

    public TelemarketingMetrics(MeterRegistry meterRegistry) {
        this.callResults = Counter.builder("telemarketing.call.results.total")
                .description("Total de resultados de llamada registrados")
                .register(meterRegistry);
        this.callResultTimer = Timer.builder("telemarketing.call.result.duration")
                .description("Duración del registro de resultados de llamada")
                .publishPercentileHistogram()
                .register(meterRegistry);
        this.queuedCommands = Counter.builder("telemarketing.commands.enqueued.total")
                .description("Comandos encolados en el bus in-memory")
                .register(meterRegistry);
    }

    public void recordCallResult(String outcome, long durationNanos) {
        callResults.increment();
        callResultTimer.record(durationNanos, TimeUnit.NANOSECONDS);
    }

    public void incrementEnqueuedCommands() {
        queuedCommands.increment();
    }
}
