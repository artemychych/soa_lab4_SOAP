package se.ifmo.ru.soa_lab4_service1.storage.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Sort {
    private boolean desc;
    private String fieldName;
    private String nestedName;
}
