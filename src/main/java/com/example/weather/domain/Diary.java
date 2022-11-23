package com.example.weather.domain;

import com.sun.istack.NotNull;
import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Diary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;

    @NotNull
    private String weather;

    @NotNull
    private String icon;

    @NotNull
    private double temperature;

    @NotNull
    private String text;

    @NotNull
    private LocalDate date;
}
