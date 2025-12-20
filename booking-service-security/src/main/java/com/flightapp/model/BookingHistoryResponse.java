package com.flightapp.model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BookingHistoryResponse {

    private String ticketId;
    private String pnr;
    private FLIGHTTYPE tripType;
    private LocalDateTime bookingTime;
    private String seatsBooked;
    private String mealType;
    private Double totalPrice;
    private boolean canceled;

    private String airline;
    private String fromPlace;
    private String toPlace;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;

    private ReturnFlightDTO returnFlight;
}
