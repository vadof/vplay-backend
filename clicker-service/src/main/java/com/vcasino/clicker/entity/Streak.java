package com.vcasino.clicker.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Entity
@Table(name = "streak")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Streak {

    @Id
    @Column(name = "account_id")
    Long accountId;

    @Column(name = "day", nullable = false)
    Integer day;

    @Column(name = "last_received_date")
    LocalDate lastReceivedDate;

}
