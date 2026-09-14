package com.dineahead.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "menu_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String description;

    private BigDecimal price;

    private Boolean available;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private MenuCategory category;

    @OneToMany(mappedBy = "menuItem")
    private List<OrderItem> orderItems;
}
