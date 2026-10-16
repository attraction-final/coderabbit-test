package com.example.attractionreservation.sample3;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Ticket {

    private Long attractionId;
    private String code;
    private int soldCount;
    private int capacity;
}
