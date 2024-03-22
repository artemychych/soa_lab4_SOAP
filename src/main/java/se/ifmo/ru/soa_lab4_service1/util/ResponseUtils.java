package se.ifmo.ru.soa_lab4_service1.util;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import se.ifmo.ru.soa_lab4_service1.web.model.Error;

import java.time.Instant;

@Component
public class ResponseUtils {
    @SuppressWarnings("null")
    public ResponseEntity<Error> buildResponseWithMessage(HttpStatus status, String message) {
        return ResponseEntity
                .status(status)
                .contentType(MediaType.APPLICATION_XML)
                .body(Error
                        .builder()
                        .message(message)
                        .code(String.valueOf(status.value()))
                        .date(Instant.now().toString())
                        .build());
    }
}
