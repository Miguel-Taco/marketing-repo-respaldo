package pe.unmsm.crm.marketing.campanas.telefonicas.domain.command;

import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Cola/bus in-memory simple con un solo worker para procesar comandos.
 */
@Component
public class InMemoryCallCommandBus implements CallCommandBus {

    private final BlockingQueue<CallCommand> queue = new LinkedBlockingQueue<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final pe.unmsm.crm.marketing.campanas.telefonicas.infra.metrics.TelemarketingMetrics metrics;

    public InMemoryCallCommandBus(pe.unmsm.crm.marketing.campanas.telefonicas.infra.metrics.TelemarketingMetrics metrics) {
        this.metrics = metrics;
    }

    @Override
    public String enqueue(CallCommand command) {
        queue.add(command);
        executor.submit(this::processPending);
        metrics.incrementEnqueuedCommands();
        return command.id();
    }

    @Override
    public void processPending() {
        CallCommand command;
        while ((command = queue.poll()) != null) {
            command.execute();
        }
    }

    @Override
    public int pendingSize() {
        return queue.size();
    }

    @PreDestroy
    public void shutdown() {
        executor.shutdownNow();
    }
}
