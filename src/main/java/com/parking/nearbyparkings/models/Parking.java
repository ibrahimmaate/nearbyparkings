package com.parking.nearbyparkings.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public class Parking {

    @Getter
    @Setter
    private String recordid;

    @Getter
    @Setter
    private ParkingFields fields;

}
