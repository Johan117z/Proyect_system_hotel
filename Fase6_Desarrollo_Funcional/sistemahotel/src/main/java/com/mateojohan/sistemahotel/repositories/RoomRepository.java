package com.mateojohan.sistemahotel.repositories;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mateojohan.sistemahotel.enums.ReservationStatus;
import com.mateojohan.sistemahotel.enums.RoomStatus;
import com.mateojohan.sistemahotel.models.Room;

@Repository
public interface RoomRepository extends JpaRepository<Room, Integer> {

    /** HU-02: busca una habitación activa por número. */
    Optional<Room> findByRoomNumberAndActiveTrue(Integer roomNumber);

    /** RN-01: el número es único incluso frente a habitaciones dadas de baja. */
    boolean existsByRoomNumber(Integer roomNumber);

    /** HU-04 */
    List<Room> findByActiveTrueOrderByRoomNumberAsc();

    /** HU-04: filtro por estado. */
    List<Room> findByActiveTrueAndStatusOrderByRoomNumberAsc(RoomStatus status);

    /**
     * HU-11 / RN-07: habitaciones activas, que no están en mantenimiento (RN-13) y sin reservas
     * PENDING/ACTIVE que se crucen con [checkIn, checkOut). La salida es exclusiva.
     */
    @Query("""
            SELECT r FROM Room r
             WHERE r.active = true
               AND r.status <> :maintenance
               AND NOT EXISTS (SELECT x FROM Reservation x
                                WHERE x.room = r
                                  AND x.status IN :blocking
                                  AND x.checkInDate < :checkOut
                                  AND x.checkOutDate > :checkIn)
             ORDER BY r.roomNumber
            """)
    List<Room> findAvailable(@Param("checkIn") LocalDate checkIn,
                             @Param("checkOut") LocalDate checkOut,
                             @Param("maintenance") RoomStatus maintenance,
                             @Param("blocking") List<ReservationStatus> blocking);

    /**
     * HU-02: cambio de tipo. El tipo es el discriminador de la herencia y JPA no permite cambiar la
     * subclase de una entidad, así que se actualiza la fila con SQL nativo. Conserva el id y,
     * con él, el historial de reservas de la habitación.
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = """
            UPDATE rooms
               SET type = :type, capacity = :capacity, price_per_night = :price, extra_service_fee = :fee
             WHERE id = :id
            """, nativeQuery = true)
    int changeType(@Param("id") Integer id,
                   @Param("type") String type,
                   @Param("capacity") Integer capacity,
                   @Param("price") BigDecimal pricePerNight,
                   @Param("fee") BigDecimal extraServiceFee);
}
