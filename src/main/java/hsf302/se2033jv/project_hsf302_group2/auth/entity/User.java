package hsf302.se2033jv.project_hsf302_group2.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
import hsf302.se2033jv.project_hsf302_group2.ordering.entity.Order;
import hsf302.se2033jv.project_hsf302_group2.ordering.entity.Cart;
import hsf302.se2033jv.project_hsf302_group2.customer.entity.Review;
import hsf302.se2033jv.project_hsf302_group2.customer.entity.CustomerAddress;
import hsf302.se2033jv.project_hsf302_group2.reservation.entity.Reservation;
import hsf302.se2033jv.project_hsf302_group2.admin.entity.SystemLog;
import hsf302.se2033jv.project_hsf302_group2.customer.entity.LoyaltyPoint;

/**
 * Entity class ánh xạ tới bảng 'users' trong database.
 * Đại diện cho một người dùng trong hệ thống.
 */
@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(name = "username", length = 50, unique = true)
    private String username;

    @Column(name = "email", length = 150, unique = true)
    private String email;

    @Column(name = "phone", length = 20, unique = true)
    private String phone;

    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Column(name = "status")
    private Boolean status;

    @Column(name = "avatar_url", length = 255)
    private String avatarUrl;

    @Column(name = "created_at", nullable = false, columnDefinition = "DATETIME2")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false, columnDefinition = "DATETIME2")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Order> orders;

    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Reservation> reservations;

    @OneToMany(mappedBy = "cancelledBy", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Reservation> cancelledReservations;

    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Review> reviews;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<SystemLog> systemLogs;

    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<LoyaltyPoint> loyaltyPoints;

    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<CustomerAddress> customerAddresses;

    @OneToOne(mappedBy = "customer", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Cart cart;

    /**
     * Lifecycle callback - set createdAt khi insert
     */
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }

    /**
     * Lifecycle callback - update updatedAt khi update
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

