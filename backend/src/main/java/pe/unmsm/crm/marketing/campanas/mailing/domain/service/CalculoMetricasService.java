package pe.unmsm.crm.marketing.campanas.mailing.domain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.unmsm.crm.marketing.campanas.mailing.domain.model.MetricaCampana;

@Service
@RequiredArgsConstructor
@Slf4j
public class CalculoMetricasService {

    /**
     * Calcula la tasa de apertura (%)
     * Formula: (aperturas / enviados) * 100
     */
    public Double calcularTasaApertura(MetricaCampana metricas) {
        if (metricas == null || metricas.getEnviados() == null || metricas.getEnviados() == 0) {
            return 0.0;
        }
        
        return (metricas.getAperturas().doubleValue() / metricas.getEnviados().doubleValue()) * 100;
    }

    /**
     * Calcula la tasa de clics (%)
     * Formula: (clics / enviados) * 100
     */
    public Double calcularTasaClics(MetricaCampana metricas) {
        if (metricas == null || metricas.getEnviados() == null || metricas.getEnviados() == 0) {
            return 0.0;
        }
        
        return (metricas.getClics().doubleValue() / metricas.getEnviados().doubleValue()) * 100;
    }

    /**
     * Calcula la tasa de bajas (%)
     * Formula: (bajas / enviados) * 100
     */
    public Double calcularTasaBajas(MetricaCampana metricas) {
        if (metricas == null || metricas.getEnviados() == null || metricas.getEnviados() == 0) {
            return 0.0;
        }
        
        return (metricas.getBajas().doubleValue() / metricas.getEnviados().doubleValue()) * 100;
    }

    /**
     * Calcula la tasa de rebotes (%)
     * Formula: (rebotes / enviados) * 100
     */
    public Double calcularTasaRebotes(MetricaCampana metricas) {
        if (metricas == null || metricas.getEnviados() == null || metricas.getEnviados() == 0) {
            return 0.0;
        }
        
        return (metricas.getRebotes().doubleValue() / metricas.getEnviados().doubleValue()) * 100;
    }

    /**
     * Calcula CTR (Click-Through Rate)
     * Formula: (clics / aperturas) * 100
     * Si no hay aperturas, retorna 0
     */
    public Double calcularCTR(MetricaCampana metricas) {
        if (metricas == null || metricas.getAperturas() == null || metricas.getAperturas() == 0) {
            return 0.0;
        }
        
        return (metricas.getClics().doubleValue() / metricas.getAperturas().doubleValue()) * 100;
    }

    /**
     * Calcula tasa de entrega (%)
     * Formula: (entregados / enviados) * 100
     */
    public Double calcularTasaEntrega(MetricaCampana metricas) {
        if (metricas == null || metricas.getEnviados() == null || metricas.getEnviados() == 0) {
            return 0.0;
        }
        
        return (metricas.getEntregados().doubleValue() / metricas.getEnviados().doubleValue()) * 100;
    }

    /**
     * Obtiene resumen de métricas en porcentajes
     */
    public MetricasResumenDTO obtenerResumen(MetricaCampana metricas) {
        return MetricasResumenDTO.builder()
                .enviados(metricas.getEnviados())
                .entregados(metricas.getEntregados())
                .aperturas(metricas.getAperturas())
                .clics(metricas.getClics())
                .rebotes(metricas.getRebotes())
                .bajas(metricas.getBajas())
                .tasaEntrega(calcularTasaEntrega(metricas))
                .tasaApertura(calcularTasaApertura(metricas))
                .tasaClics(calcularTasaClics(metricas))
                .tasaRebotes(calcularTasaRebotes(metricas))
                .ctr(calcularCTR(metricas))
                .build();
    }

    // DTO auxiliar
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class MetricasResumenDTO {
        private Integer enviados;
        private Integer entregados;
        private Integer aperturas;
        private Integer clics;
        private Integer rebotes;
        private Integer bajas;
        private Double tasaEntrega;
        private Double tasaApertura;
        private Double tasaClics;
        private Double tasaRebotes;
        private Double ctr;
    }
}