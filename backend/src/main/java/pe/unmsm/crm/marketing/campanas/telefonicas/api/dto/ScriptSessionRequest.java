package pe.unmsm.crm.marketing.campanas.telefonicas.api.dto;

import lombok.Data;

import java.util.Map;

@Data
public class ScriptSessionRequest {
    private int pasoActual;
    private Map<String, String> respuestas;
}

