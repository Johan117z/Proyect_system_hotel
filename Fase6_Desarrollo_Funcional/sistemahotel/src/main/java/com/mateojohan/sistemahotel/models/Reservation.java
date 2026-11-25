package com.mateojohan.sistemahotel.models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import com.mateojohan.sistemahotel.enums.ReservationStatus;
import com.mateojohan.sistemahotel.enums.RoomStatus;
import com.mateojohan.sistemahotel.exceptions.BusinessRuleException;
import com.mateojohan.sistemahotel.utils.FormatUtils;

/**
 * Reserva de una habitación (HU-09 a HU-13) -> tabla reservations.
 * La entidad controla sus propias transiciones de estado; el servicio solo las orquesta.
 */
@Entity
@Table(name = "reservations")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Formato RES-AAAA-NNN (CHECK ck_reservations_code). */
    @Column(name = "reservation_code", nullable = false, unique = true, length = 15, updatable = false)
    private String reservationCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "guest_id", nullable = false, updatable = false)
    private Guest guest;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false, updatable = false)
    private Room room;

    @Column(name = "check_in_date", nullable = false, updatable = false)
    private LocalDate checkInDate;

    @Column(name = "check_out_date", nullable = false, updatable = false)
    private LocalDate checkOutDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReservationStatus status = ReservationStatus.PENDING;   // RF-09: estado inicial

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "reservation")
    @OrderBy("id ASC")
    private List<AdditionalService> services = new ArrayList<>();

    protected Reservation() {
    }

    public Reservation(String reservationCode, Guest guest, Room room, LocalDate checkInDate, LocalDate checkOutDate) {
        this.reservationCode = reservationCode;
        this.guest = guest;
        this.room = room;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
    }

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public long getStayDays() {
        return ChronoUnit.DAYS.between(checkInDate, checkOutDate);
    }

    /**
     * HU-12 / RN-08: solo una reserva PENDING, dentro de su periodo [entrada, salida)
     * y con la habitación disponible. La reserva pasa a ACTIVE y la habitación a OCCUPIED.
     */
    public void checkIn(LocalDate today) {
        if (status != ReservationStatus.PENDING) {
            throw new BusinessRuleException("RN-08",
                    "Solo se puede hacer check-in de reservas pendientes. Estado actual: " + status + ".");
        }
        if (today.isBefore(checkInDate)) {
            throw new BusinessRuleException("RN-08",
                    "El check-in de esta reserva es a partir del " + FormatUtils.formatDate(checkInDate) + ".");
        }
        if (!today.isBefore(checkOutDate)) {
            throw new BusinessRuleException("RN-08",
                    "El periodo de la reserva terminó el " + FormatUtils.formatDate(checkOutDate) + ".");
        }
        if (room.getStatus() != RoomStatus.AVAILABLE) {
            throw new BusinessRuleException("RN-08",
                    "La habitación " + room.getRoomNumber() + " no está disponible (" + room.getStatus() + ").");
        }
        status = ReservationStatus.ACTIVE;
        room.changeStatus(RoomStatus.OCCUPIED);
    }

    /**
     * HU-13 / RN-10: ACTIVE -> COMPLETED y la habitación pasa a MAINTENANCE.
     * Que la cuenta esté saldada (RN-09) lo verifica ReservationService antes de llamar este método.
     */
    public void checkOut() {
        ensureActive("RF-13", "Solo se puede hacer check-out de reservas activas.");
        status = ReservationStatus.COMPLETED;
        room.changeStatus(RoomStatus.MAINTENANCE);
    }

    /** HU-10 / RN-06: solo se cancelan reservas pendientes; sus fechas quedan libres. */
    public void cancel() {
        if (status != ReservationStatus.PENDING) {
            throw new BusinessRuleException("RN-06",
                    "Solo se pueden cancelar reservas pendientes. Estado actual: " + status + ".");
        }
        status = ReservationStatus.CANCELLED;
    }

    /** Los servicios adicionales solo se registran durante la estadía (reserva ACTIVE). */
    public void addService(AdditionalService service) {
        ensureActive("RF-14", "Solo se registran servicios durante la estadía (reserva activa).");
        services.add(service);
    }

    public void ensureActive(String ruleCode, String message) {
        if (status != ReservationStatus.ACTIVE) {
            throw new BusinessRuleException(ruleCode, message + " Estado actual: " + status + ".");
        }
    }

    public BigDecimal getServicesTotal() {
        return services.stream().map(AdditionalService::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Long getId() {
        return id;
    }

    public String getReservationCode() {
        return reservationCode;
    }

    public Guest getGuest() {
        return guest;
    }

    public Room getRoom() {
        return room;
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public List<AdditionalService> getServices() {
        return List.copyOf(services);
    }
}
