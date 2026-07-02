package hsf302.se2033jv.project_hsf302_group2.common.entity;

import hsf302.se2033jv.project_hsf302_group2.common.enums.ReservationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
// classes ReservationDeposit and CoffeeTable are in the same package; explicit imports removed

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Entity ánh xạ tới bảng 'reservations'
 */
@Entity
@Table(name = "reservations")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservation_id")
    private Integer reservationId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cancelled_by")
    private User cancelledBy;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "order_id", unique = true)
    private Order order;

    @Column(name = "party_size", nullable = false)
    private Integer partySize;

    @Column(name = "reservation_date")
    private LocalDate reservationDate;

    @Column(name = "reservation_time")
    private LocalTime reservationTime;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "status", length = 15)
    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    @Column(name = "cancellation_reason", columnDefinition = "NVARCHAR(MAX)")
    private String cancellationReason;

    @Column(name = "cancelled_at", columnDefinition = "DATETIME2")
    private LocalDateTime cancelledAt;

    @Column(name = "note", columnDefinition = "NVARCHAR(MAX)")
    private String note;

    @Column(name = "created_at", columnDefinition = "DATETIME2")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "DATETIME2")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "reservation", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<ReservationDeposit> deposits;

    /**
     * Many-to-many relationship to CoffeeTable via join table 'reservation_tables'
     */
    @ManyToMany
    @JoinTable(
        name = "reservation_tables",
        joinColumns = @JoinColumn(name = "reservation_id"),
        inverseJoinColumns = @JoinColumn(name = "table_id")
    )
    private Set<CoffeeTable> tables = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (updatedAt == null) updatedAt = LocalDateTime.now();
        if (status == null) status = ReservationStatus.PENDING;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

