package com.mateojohan.sistemahotel.models;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;

import com.mateojohan.sistemahotel.enums.RoomStatus;
import com.mateojohan.sistemahotel.enums.RoomType;

/**
 * Clase base abstracta de las habitaciones (HU-01 a HU-04) -> tabla rooms.
 * Herencia de tabla única: la columna type es el discriminador (SINGLE, DOUBLE, SUITE)
 * y define qué subclase instancia Hibernate.
 */
@Entity
@Table(name = "rooms")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type", discriminatorType = DiscriminatorType.STRING)
public abstract class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "room_number", nullable = false, unique = true, updatable = false)
    private Integer roomNumber;

    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    @Column(name = "price_per_night", nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerNight;

    /** Solo las suites tienen un valor mayor que cero (CHECK ck_rooms_fee_suite). */
    @Column(name = "extra_service_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal extraServiceFee = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RoomStatus status = RoomStatus.AVAILABLE;

    @Column(name = "is_active", nullable = false)
    private Boolean active = Boolean.TRUE;

    protected Room() {
    }

    protected Room(Integer roomNumber, Integer capacity, BigDecimal pricePerNight, BigDecimal extraServiceFee) {
        this.roomNumber = roomNumber;
        this.capacity = capacity;
        this.pricePerNight = pricePerNight;
        this.extraServiceFee = extraServiceFee == null ? BigDecimal.ZERO : extraServiceFee;
    }

    /** Cada subclase declara su tipo; coincide con su @DiscriminatorValue. */
    public abstract RoomType getType();

    /** RN-11: noches × tarifa + cargo extra de la habitación (cero salvo en suites). */
    public BigDecimal calculatePrice(long nights) {
        if (nights <= 0) {
            throw new IllegalArgumentException("La estadía debe ser de al menos una noche.");
        }
        return pricePerNight.multiply(BigDecimal.valueOf(nights)).add(extraServiceFee);
    }

    /** RN-13: una habitación en mantenimiento no se puede reservar. */
    public boolean isBookable() {
        return status != RoomStatus.MAINTENANCE;
    }

    public void changeStatus(RoomStatus newStatus) {
        this.status = newStatus;
    }

    /** HU-03: baja lógica. */
    public void deactivate() {
        this.active = Boolean.FALSE;
    }

    public Integer getId() {
        return id;
    }

    public Integer getRoomNumber() {
        return roomNumber;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public BigDecimal getPricePerNight() {
        return pricePerNight;
    }

    public void setPricePerNight(BigDecimal pricePerNight) {
        this.pricePerNight = pricePerNight;
    }

    public BigDecimal getExtraServiceFee() {
        return extraServiceFee;
    }

    public RoomStatus getStatus() {
        return status;
    }

    public Boolean getActive() {
        return active;
    }
}
