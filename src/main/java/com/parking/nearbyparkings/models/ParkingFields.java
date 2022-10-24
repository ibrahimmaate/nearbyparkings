package com.parking.nearbyparkings.models;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

public class ParkingFields {

    @Getter
    @Setter
    private String nom;

    @Getter
    @Setter
    @SerializedName(value = "nb_places")
    private int nbPlaces;

    @Getter
    @Setter
    @SerializedName(value = "geo_point_2d")
    private Double[] geoPoint2d;

    @Getter
    @Setter
    @SerializedName(value = "places_restantes")
    private Double placesRestantes;
}
