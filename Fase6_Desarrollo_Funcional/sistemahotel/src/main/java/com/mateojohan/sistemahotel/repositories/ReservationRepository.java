package com.mateojohan.sistemahotel.repositories;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mateojohan.sistemahotel.enums.ReservationStatus;
import com.mateojohan.sistemahotel.models.Guest;
import com.mateojohan.sistemahotel.models.Reservation;
import com.mateojohan.sistemahotel.models.Room;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    Optional<Reservation> findByReservationCode(String reservationCode);

    List<Reservation> findAllByOrderByIdDesc();

    List<Reservation> findByStatusOrderByCheckInDateAsc(ReservationStatus status);

    /** RN-02 / RN-14: la habitación tiene reservas pendientes o activas. */
    boolean existsByRoomAndStatusIn(Room room, List<ReservationStatus> statuses);

    /** RN-04: el huésped tiene hospedaje activo o reservas vigentes. */
    boolean existsByGuestAndStatusIn(Guest guest, List<ReservationStatus> statuses);

    /** RN-05: hay cruce de fechas con otra reserva PENDING/ACTIVE de la misma habitación. */
    @Query("""
            SELECT COUNT(x) > 0 FROM Reservation x
             WHERE x.room = :room
               AND x.status IN :blocking
               AND x.checkInDate < :checkOut
               AND x.checkOutDate > :checkIn
            """)
    boolean existsOverlap(@Param("room") Room room,
                          @Param("checkIn") LocalDate checkIn,
                          @Param("checkOut") LocalDate checkOut,
                          @Param("blocking") List<ReservationStatus> blocking);
}
