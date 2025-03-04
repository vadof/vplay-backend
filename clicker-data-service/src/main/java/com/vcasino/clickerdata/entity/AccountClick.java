package com.vcasino.clickerdata.entity;

import com.vcasino.clickerdata.entity.key.ClickKey;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Entity
@Table(name = "account_clicks")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@IdClass(ClickKey.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AccountClick {

    @Id
    @Column(name = "accountId")
    Long accountId;

    @Id
    @Column(name = "date")
    LocalDate date;

    @Column(name = "amount", nullable = false)
    Integer amount;

}
