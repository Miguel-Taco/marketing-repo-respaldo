package pe.unmsm.crm.marketing.campanas.telefonicas.infra.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pe.unmsm.crm.marketing.campanas.telefonicas.domain.strategy.assignment.CallAssignmentStrategy;
import pe.unmsm.crm.marketing.campanas.telefonicas.domain.strategy.assignment.RoundRobinAssignmentStrategy;
import pe.unmsm.crm.marketing.campanas.telefonicas.domain.strategy.retry.FixedIntervalRetryStrategy;
import pe.unmsm.crm.marketing.campanas.telefonicas.domain.strategy.retry.RetryStrategy;

@Configuration
public class StrategyConfig {

    @Bean
    public CallAssignmentStrategy callAssignmentStrategy(RoundRobinAssignmentStrategy strategy) {
        return strategy;
    }

    @Bean
    public RetryStrategy retryStrategy(FixedIntervalRetryStrategy strategy) {
        return strategy;
    }
}
