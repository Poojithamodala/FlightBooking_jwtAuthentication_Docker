package com.flightapp.model;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BookingHistoryResponse {

    private String id;
    private String pnr;
    private FLIGHTTYPE tripType;
    private LocalDateTime bookingTime;
    private String seatsBooked;
    private String mealType;
    private Double totalPrice;
    private boolean canceled;

    // Flight details
    private String airline;
    private String fromPlace;
    private String toPlace;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;

    // Passengers
    private List<Passenger> passengers;

    // ✅ EXPLICIT CONSTRUCTOR (fixes error)
    public BookingHistoryResponse(
            String id,
            String pnr,
            FLIGHTTYPE tripType,
            LocalDateTime bookingTime,
            String seatsBooked,
            String mealType,
            Double totalPrice,
            boolean canceled,
            String airline,
            String fromPlace,
            String toPlace,
            LocalDateTime departureTime,
            LocalDateTime arrivalTime,
            List<Passenger> passengers
    ) {
        this.id = id;
        this.pnr = pnr;
        this.tripType = tripType;
        this.bookingTime = bookingTime;
        this.seatsBooked = seatsBooked;
        this.mealType = mealType;
        this.totalPrice = totalPrice;
        this.canceled = canceled;
        this.airline = airline;
        this.fromPlace = fromPlace;
        this.toPlace = toPlace;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.passengers = passengers;
    }
}
