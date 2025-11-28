package pe.unmsm.crm.marketing.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import pe.unmsm.crm.marketing.shared.infra.exception.BusinessException;
import pe.unmsm.crm.marketing.shared.infra.exception.NotFoundException;
@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/business")
    public String testBusiness() {
        throw new BusinessException("DEMO_BUSINESS", "Error de negocio de prueba");
    }

    @GetMapping("/not-found")
    public String testNotFound() {
        throw new NotFoundException("Lead", 123L);
    }
}
