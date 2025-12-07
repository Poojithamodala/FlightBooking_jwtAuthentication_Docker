package com.flightapp.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FlightSearchRequest {
    private String fromPlace;
    private String toPlace;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
