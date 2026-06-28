package hsf302.se2033jv.project_hsf302_group2.common.entity;

import hsf302.se2033jv.project_hsf302_group2.common.enums.TableStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
// Reservation is in same package; explicit import removed

/**
 * Entity class ánh xạ tới bảng 'tables' trong database.
 * Đại diện cho một bàn/table trong quán cà phê.
 */
@Entity
@Table(name = "tables")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoffeeTable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "table_id")
    private Integer tableId;

    @Column(name = "capacity")
    private Integer capacity;

    @Column(name = "status", length = 15)
    @Enumerated(EnumType.STRING)
    private TableStatus status;

    @Column(name = "is_active")
    private Boolean isActive;

    @OneToMany(mappedBy = "table", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Order> orders;

    /**
     * Many-to-many convenience mapping to Reservation via reservation_tables join table
     */
    @ManyToMany(mappedBy = "tables")
    private Set<Reservation> reservations = new HashSet<>();

    /**
     * Lifecycle callback - set default values khi insert
     */
    @PrePersist
    protected void onCreate() {
        if (status == null) {
            status = TableStatus.AVAILABLE;
        }
        if (isActive == null) {
            isActive = true;
        }
    }
}


