package com.dineahead.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

import com.dineahead.enums.ReservationStatus;


@Entity
@Table(name = "reservations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reservation {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;

    private String customerName;

    private LocalDateTime reservationTime;

    private Integer partySize;


    // ==========================================
    // ADDED FOR OWNERSHIP SECURITY
    // ==========================================
    @ManyToOne
    @JoinColumn(
            name = "user_id"
            //nullable = false
    )
    private User user;


    @ManyToOne
    @JoinColumn(
            name = "restaurant_id",
            nullable = false
    )
    private Restaurant restaurant;


    @ManyToOne
    @JoinColumn(
            name = "restaurant_table_id",
            nullable = false
    )
    private RestaurantTable restaurantTable;


    @Enumerated(EnumType.STRING)
    private ReservationStatus status;


    @OneToOne(
            mappedBy = "reservation"
    )
    private Order order;


    @OneToMany(
            mappedBy = "reservation",
            cascade = CascadeType.ALL
    )
    private List<Notification> notifications;

}