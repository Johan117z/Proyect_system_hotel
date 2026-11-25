package com.mateojohan.sistemahotel.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.mateojohan.sistemahotel.enums.ReservationStatus;
import com.mateojohan.sistemahotel.enums.RoomType;
import com.mateojohan.sistemahotel.models.Reservation;

/** Reserva con los datos del huésped, la habitación y los servicios consumidos. */
public record ReservationResponseDTO(
        Long id,
        String reservationCode,
        String guestDocument,
        String guestName,
        Integer roomNumber,
        RoomType roomType,
        LocalDate checkInDate,
        LocalDate checkOutDate,
        long stayDays,
        ReservationStatus status,
        List<AdditionalServiceResponseDTO> services,
        BigDecimal servicesTotal) {

    public static ReservationResponseDTO from(Reservation reservation) {
        return new ReservationResponseDTO(
                reservation.getId(),
                reservation.getReservationCode(),
                reservation.getGuest().getDocument(),
                reservation.getGuest().getFullName(),
                reservation.getRoom().getRoomNumber(),
                reservation.getRoom().getType(),
                reservation.getCheckInDate(),
                reservation.getCheckOutDate(),
                reservation.getStayDays(),
                reservation.getStatus(),
                reservation.getServices().stream().map(AdditionalServiceResponseDTO::from).toList(),
                reservation.getServicesTotal());
    }
}
