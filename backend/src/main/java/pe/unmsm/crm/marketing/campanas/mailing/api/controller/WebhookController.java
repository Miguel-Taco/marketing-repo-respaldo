package pe.unmsm.crm.marketing.campanas.mailing.api.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;
import pe.unmsm.crm.marketing.campanas.mailing.api.dto.request.ResendWebhookRequest;
import pe.unmsm.crm.marketing.campanas.mailing.application.service.WebhookResendService;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * Controller para manejar:
 * 1. Webhooks de Resend (notificaciones de eventos de email)
 * 2. Tracking propio de clics y bajas
 * 
 * ENDPOINTS:
 * - POST /api/v1/mailing/webhooks/resend    → Recibe webhooks de Resend
 * - GET  /api/v1/mailing/track/click        → Tracking de clics (redirige a URL real)
 * - GET  /api/v1/mailing/track/unsubscribe  → Maneja bajas
 * - GET  /api/v1/mailing/webhooks/test      → Test del endpoint
 * - POST /api/v1/mailing/webhooks/simulate-click → Simular clic (testing)
 * 
 * IMPORTANTE: Este controller REEMPLAZA al anterior WebhookController de SendGrid.
 */
@RestController
@RequestMapping("/api/v1/mailing")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {

    private final WebhookResendService webhookService;

    // ========================================================================
    // WEBHOOK DE RESEND
    // ========================================================================

    /**
     * Endpoint para recibir webhooks de Resend.
     * 
     * Configuración en Resend Dashboard (https://resend.com/webhooks):
     * 1. URL: https://TU-BACKEND.com/api/v1/mailing/webhooks/resend
     * 2. Eventos: email.delivered, email.opened, email.clicked, email.bounced
     * 
     * Headers que envía Resend (firmados con Svix):
     * - svix-id: ID único del webhook
     * - svix-timestamp: Timestamp
     * - svix-signature: Firma para validación
     * 
     * @param evento Payload del webhook de Resend
     * @param svixId ID del webhook (para logging)
     * @param svixTimestamp Timestamp del envío
     * @param svixSignature Firma (para validación futura)
     * @return 200 OK siempre (Resend reintenta si no recibe 2xx)
     */
    @PostMapping("/webhooks/resend")
    public ResponseEntity<String> procesarWebhookResend(
            @RequestBody ResendWebhookRequest evento,
            @RequestHeader(value = "svix-id", required = false) String svixId,
            @RequestHeader(value = "svix-timestamp", required = false) String svixTimestamp,
            @RequestHeader(value = "svix-signature", required = false) String svixSignature) {

        log.info("╔══════════════════════════════════════════════════╗");
        log.info("║  WEBHOOK RESEND RECIBIDO                         ║");
        log.info("╠══════════════════════════════════════════════════╣");
        log.info("║  Tipo: {}", evento != null ? evento.getType() : "null");
        log.info("║  Svix-ID: {}", svixId);
        log.info("╚══════════════════════════════════════════════════╝");

        if (evento == null) {
            log.warn("Webhook vacío recibido");
            return ResponseEntity.ok("OK");
        }

        try {
            // TODO: En producción, validar la firma del webhook
            // usando svixSignature y tu RESEND_WEBHOOK_SECRET
            // Esto previene webhooks falsos
            
            webhookService.procesarEventoResend(evento);
            
            return ResponseEntity.ok("OK");

        } catch (Exception e) {
            log.error("Error procesando webhook: {}", e.getMessage(), e);
            // Retornar 200 de todos modos para que Resend no reintente
            // (los reintentos podrían causar duplicados)
            return ResponseEntity.ok("OK");
        }
    }

    // ========================================================================
    // TRACKING DE CLICS (Nuestro propio sistema)
    // ========================================================================

    /**
     * Endpoint de tracking para clics en el CTA del email.
     * 
     * FLUJO:
     * 1. Usuario hace clic en el botón del email
     * 2. La URL del botón apunta aquí (generada en ResendMailAdapter)
     * 3. Registramos el clic y derivamos a Ventas
     * 4. Redirigimos al usuario a la URL real (encuesta)
     * 
     * URL ejemplo:
     * /api/v1/mailing/track/click?cid=123&email=test@test.com&redirect=https://encuesta.com
     * 
     * @param cid ID de la campaña de mailing
     * @param email Email del destinatario (URL encoded)
     * @param redirect URL a la que redirigir (la encuesta, URL encoded)
     * @return Redirect 302 a la URL de destino
     */
    @GetMapping("/track/click")
    public RedirectView trackClick(
            @RequestParam("cid") Integer cid,
            @RequestParam("email") String email,
            @RequestParam("redirect") String redirect) {

        log.info("╔══════════════════════════════════════════════════╗");
        log.info("║  TRACKING CLIC                                   ║");
        log.info("╠══════════════════════════════════════════════════╣");
        log.info("║  Campaña: {}", cid);
        log.info("║  Email: {}", email);
        log.info("╚══════════════════════════════════════════════════╝");

        try {
            // Decodificar email si viene URL-encoded
            String decodedEmail = URLDecoder.decode(email, StandardCharsets.UTF_8);
            
            // Procesar el clic (registra interacción + deriva a Ventas)
            webhookService.procesarClicTracking(cid, decodedEmail);
            
        } catch (Exception e) {
            log.error("Error en tracking de clic: {}", e.getMessage());
            // Continuar con la redirección aunque falle el tracking
            // La experiencia del usuario es prioritaria
        }

        // Decodificar URL de destino
        String redirectUrl;
        try {
            redirectUrl = URLDecoder.decode(redirect, StandardCharsets.UTF_8);
        } catch (Exception e) {
            redirectUrl = redirect;
        }

        log.info("  → Redirigiendo a: {}", redirectUrl);

        // Redirigir al usuario a la encuesta/URL real
        RedirectView redirectView = new RedirectView(redirectUrl);
        redirectView.setStatusCode(HttpStatus.FOUND); // 302 redirect
        return redirectView;
    }

    // ========================================================================
    // TRACKING DE BAJAS (Unsubscribe)
    // ========================================================================

    /**
     * Endpoint para manejar cancelación de suscripción (unsubscribe).
     * 
     * @param cid ID de la campaña
     * @param email Email del usuario que se da de baja (URL encoded)
     * @return Página HTML de confirmación
     */
    @GetMapping("/track/unsubscribe")
    public ResponseEntity<String> trackUnsubscribe(
            @RequestParam("cid") Integer cid,
            @RequestParam("email") String email) {

        log.warn("╔══════════════════════════════════════════════════╗");
        log.warn("║  BAJA SOLICITADA                                 ║");
        log.warn("╠══════════════════════════════════════════════════╣");
        log.warn("║  Campaña: {}", cid);
        log.warn("║  Email: {}", email);
        log.warn("╚══════════════════════════════════════════════════╝");

        try {
            String decodedEmail = URLDecoder.decode(email, StandardCharsets.UTF_8);
            webhookService.procesarBajaTracking(cid, decodedEmail);
        } catch (Exception e) {
            log.error("Error procesando baja: {}", e.getMessage());
        }

        // Retornar página HTML de confirmación
        String html = """
            <!DOCTYPE html>
            <html lang="es">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Suscripción cancelada</title>
                <style>
                    * { margin: 0; padding: 0; box-sizing: border-box; }
                    body {
                        font-family: 'Segoe UI', Arial, sans-serif;
                        display: flex;
                        justify-content: center;
                        align-items: center;
                        min-height: 100vh;
                        margin: 0;
                        background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%);
                    }
                    .card {
                        background: white;
                        padding: 50px 40px;
                        border-radius: 20px;
                        text-align: center;
                        box-shadow: 0 20px 60px rgba(0,0,0,0.3);
                        max-width: 450px;
                        margin: 20px;
                    }
                    .icon {
                        width: 80px;
                        height: 80px;
                        background: linear-gradient(135deg, #84CC16 0%%, #65A30D 100%%);
                        border-radius: 50%%;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        margin: 0 auto 25px;
                        font-size: 40px;
                        color: white;
                    }
                    h1 {
                        color: #1F2937;
                        margin-bottom: 15px;
                        font-size: 24px;
                        font-weight: 600;
                    }
                    p {
                        color: #6B7280;
                        line-height: 1.6;
                        margin-bottom: 10px;
                    }
                    .footer {
                        color: #9CA3AF;
                        font-size: 12px;
                        margin-top: 30px;
                        padding-top: 20px;
                        border-top: 1px solid #E5E7EB;
                    }
                </style>
            </head>
            <body>
                <div class="card">
                    <div class="icon">✓</div>
                    <h1>Suscripción cancelada</h1>
                    <p>Tu solicitud ha sido procesada correctamente.</p>
                    <p>Ya no recibirás más correos de esta campaña.</p>
                    <div class="footer">
                        Marketing CRM - Universidad Nacional Mayor de San Marcos
                    </div>
                </div>
            </body>
            </html>
            """;

        return ResponseEntity.ok()
                .header("Content-Type", "text/html; charset=UTF-8")
                .body(html);
    }

    // ========================================================================
    // ENDPOINTS DE TESTING/DEBUG
    // ========================================================================

    /**
     * Endpoint para verificar que el webhook está funcionando.
     * Útil para:
     * - Verificar que el backend está desplegado
     * - Configurar webhook en Resend Dashboard
     */
    @GetMapping("/webhooks/test")
    public ResponseEntity<String> testWebhook() {
        log.info("Test de webhook endpoint ejecutado");
        return ResponseEntity.ok("✅ Webhook endpoint funcionando correctamente! 🚀\n" +
                "Configura este endpoint en Resend Dashboard:\n" +
                "POST /api/v1/mailing/webhooks/resend");
    }

    /**
     * Endpoint para simular un clic manualmente (para testing).
     * 
     * USO: POST /api/v1/mailing/webhooks/simulate-click?cid=1&email=test@test.com
     */
    @PostMapping("/webhooks/simulate-click")
    public ResponseEntity<String> simulateClick(
            @RequestParam("cid") Integer cid,
            @RequestParam("email") String email) {

        log.info("══════════════════════════════════════════════════");
        log.info("  SIMULANDO CLIC (Testing)");
        log.info("  Campaña: {}, Email: {}", cid, email);
        log.info("══════════════════════════════════════════════════");

        try {
            webhookService.procesarClicTracking(cid, email);
            return ResponseEntity.ok("✅ Clic simulado exitosamente para campaña " + cid + "\n" +
                    "Email: " + email);
        } catch (Exception e) {
            log.error("Error simulando clic: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("❌ Error: " + e.getMessage());
        }
    }

    /**
     * Endpoint para simular una baja manualmente (para testing).
     */
    @PostMapping("/webhooks/simulate-unsubscribe")
    public ResponseEntity<String> simulateUnsubscribe(
            @RequestParam("cid") Integer cid,
            @RequestParam("email") String email) {

        log.info("══════════════════════════════════════════════════");
        log.info("  SIMULANDO BAJA (Testing)");
        log.info("  Campaña: {}, Email: {}", cid, email);
        log.info("══════════════════════════════════════════════════");

        try {
            webhookService.procesarBajaTracking(cid, email);
            return ResponseEntity.ok("✅ Baja simulada exitosamente para campaña " + cid + "\n" +
                    "Email: " + email);
        } catch (Exception e) {
            log.error("Error simulando baja: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("❌ Error: " + e.getMessage());
        }
    }
}