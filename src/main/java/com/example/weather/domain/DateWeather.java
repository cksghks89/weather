package com.example.weather.domain;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class DateWeather {
    @Id
    private LocalDate date;
    private String weather;
    private String icon;
    private double temperature;
}
