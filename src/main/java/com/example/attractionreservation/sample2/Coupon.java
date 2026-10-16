package com.example.attractionreservation.sample2;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
public class Coupon implements Serializable {

    private String code;
    private String type;
    private int value;
    private int remaining;
    private LocalDateTime expiredAt;

    @Override
    public boolean equals(Object o) {
        Coupon other = (Coupon) o;
        return code.equals(other.code);
    }
}
