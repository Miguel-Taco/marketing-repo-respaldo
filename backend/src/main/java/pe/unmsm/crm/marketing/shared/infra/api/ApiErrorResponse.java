package pe.unmsm.crm.marketing.shared.infra.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiErrorResponse {

    private String code;
    private String message;
    private String path;

    @Builder.Default
    private Instant timestamp = Instant.now();

    public static ApiErrorResponse of(String code, String message, String path) {
        return ApiErrorResponse.builder()
                .code(code)
                .message(message)
                .path(path)
                .build();
    }
}
