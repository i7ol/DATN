package com.datn.shopdatabase.entity;

import com.datn.shopdatabase.entity.UserEntity;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "carts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Giỏ hàng dành cho user đăng nhập
    @OneToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    // Giỏ hàng dành cho guest
    @Column(name = "guest_id")
    private String guestId;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<CartItemEntity> items = new ArrayList<>();

}
