package se.ifmo.ru.soa_lab4_service1.service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Location {
    private Float x; //Поле не может быть null
    private long y;
    private double z;
}
