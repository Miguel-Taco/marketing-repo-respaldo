package pe.unmsm.crm.marketing.shared.infra.api;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pe.unmsm.crm.marketing.shared.infra.exception.BusinessException;
import pe.unmsm.crm.marketing.shared.infra.exception.NotFoundException;
import pe.unmsm.crm.marketing.shared.infra.exception.ValidationException;
import pe.unmsm.crm.marketing.shared.infra.exception.DuplicateLeadException;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

        private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

        @ExceptionHandler(NotFoundException.class)
        public ResponseEntity<ApiErrorResponse> handleNotFound(
                        NotFoundException ex,
                        HttpServletRequest request) {
                ApiErrorResponse body = ApiErrorResponse.of(
                                ex.getCode(),
                                ex.getMessage(),
                                request.getRequestURI());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
        }

        @ExceptionHandler(ValidationException.class)
        public ResponseEntity<ApiErrorResponse> handleValidation(
                        ValidationException ex,
                        HttpServletRequest request) {
                ApiErrorResponse body = ApiErrorResponse.of(
                                ex.getCode(),
                                ex.getMessage(),
                                request.getRequestURI());
                return ResponseEntity.badRequest().body(body);
        }

        @ExceptionHandler(BusinessException.class)
        public ResponseEntity<ApiErrorResponse> handleBusiness(
                        BusinessException ex,
                        HttpServletRequest request) {
                ApiErrorResponse body = ApiErrorResponse.of(
                                ex.getCode(),
                                ex.getMessage(),
                                request.getRequestURI());
                // 422 para errores de negocio que no son validación simple
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(body);
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(
                        MethodArgumentNotValidException ex,
                        HttpServletRequest request) {
                String message = ex.getBindingResult()
                                .getFieldErrors()
                                .stream()
                                .map(fieldError -> formatFieldError(fieldError))
                                .collect(Collectors.joining("; "));

                ApiErrorResponse body = ApiErrorResponse.of(
                                "VALIDATION_ERROR",
                                message,
                                request.getRequestURI());

                return ResponseEntity.badRequest().body(body);
        }

        private String formatFieldError(FieldError fieldError) {
                return fieldError.getField() + ": " + fieldError.getDefaultMessage();
        }

        @ExceptionHandler(DuplicateLeadException.class)
        public ResponseEntity<ApiErrorResponse> handleDuplicateLead(
                        DuplicateLeadException ex,
                        HttpServletRequest request) {
                ApiErrorResponse body = ApiErrorResponse.of(
                                "DUPLICATE_LEAD",
                                ex.getMessage(),
                                request.getRequestURI());
                return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
        }

        @ExceptionHandler(DataIntegrityViolationException.class)
        public ResponseEntity<ApiErrorResponse> handleDataIntegrityViolation(
                        DataIntegrityViolationException ex,
                        HttpServletRequest request) {
                String message = "Error de integridad de datos";

                // Detectar si es violación de email o teléfono duplicado
                if (ex.getMessage() != null) {
                        String exMsg = ex.getMessage().toLowerCase();
                        if (exMsg.contains("email") || exMsg.contains("uk_leads_email")) {
                                message = "El email ya está registrado en el sistema";
                        } else if (exMsg.contains("telefono") || exMsg.contains("uk_leads_telefono")) {
                                message = "El teléfono ya está registrado en el sistema";
                        }
                }

                ApiErrorResponse body = ApiErrorResponse.of(
                                "DUPLICATE_ENTRY",
                                message,
                                request.getRequestURI());
                return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiErrorResponse> handleGeneric(
                        Exception ex,
                        HttpServletRequest request) {
                ApiErrorResponse body = ApiErrorResponse.of(
                                "INTERNAL_ERROR",
                                "Ha ocurrido un error interno inesperado",
                                request.getRequestURI());
                log.error("Error no controlado: {}", ex.getMessage(), ex);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
        }
}
