package com.casino.authentication.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "currency")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Currency {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String value;

}
