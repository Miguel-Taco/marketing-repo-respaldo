package pe.unmsm.crm.marketing.campanas.telefonicas.infra;

import org.springframework.stereotype.Component;
import pe.unmsm.crm.marketing.campanas.telefonicas.domain.memento.ScriptSessionMemento;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Almacen in-memory para sesiones de guion (caretaker).
 */
@Component
public class ScriptSessionStore {

    private final Map<Long, ScriptSessionMemento> store = new ConcurrentHashMap<>();

    public void save(ScriptSessionMemento memento) {
        store.put(memento.getLlamadaId(), memento);
    }

    public ScriptSessionMemento get(Long llamadaId) {
        return store.get(llamadaId);
    }

    public void clear(Long llamadaId) {
        store.remove(llamadaId);
    }
}

