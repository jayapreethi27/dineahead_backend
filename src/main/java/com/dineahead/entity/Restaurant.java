package com.dineahead.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "restaurants")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Restaurant {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;

    private String name;

    private String address;

    private String phone;

    private String email;


    // ==========================================
    // ADDED: RESTAURANT OWNER
    // ==========================================
    @ManyToOne
    @JoinColumn(
            name = "owner_id"
    )
    private User owner;


    @OneToMany(
            mappedBy = "restaurant"
    )
    private List<RestaurantTable>
            restaurantTables;


    @OneToMany(
            mappedBy = "restaurant"
    )
    private List<MenuCategory>
            menuCategories;
}