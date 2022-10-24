package com.parking.nearbyparkings;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.parking.nearbyparkings.models.Parking;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.parking.nearbyparkings.Utils.getJsonElement;

@Service
public class ParkingService {

    @Value("${parking.parkingUrl}")
    private String parkingUrl;

    @Value("${parking.availablePlacesParkingUrl}")
    private String availablePlacesParkingUrl;

    @Value("${parking.path}")
    @Getter
    private String parkingJsonPath;

    @Autowired
    private ParkingCaller parkingCaller;
    @Autowired
    Gson gson;

    private final double earthRadius = 6371.01; //Kilometers

    public String getParkingsData(){
        return parkingCaller.getParkings(parkingUrl);
    }

    public String getAvailablePlacesParkingData(){
        return parkingCaller.getParkings(availablePlacesParkingUrl);
    }

    /**
     * transform JSON to List
     * Important : if data structure change, the Parking Model and parkingJsonPath should be modified !
     * @param jsonParkingString
     * @return
     */
    private List<Parking> parkingJsonToList(String jsonParkingString){
        Type listType = new TypeToken<ArrayList<Parking>>(){}.getType();
        JsonElement jsonObject = gson.fromJson(jsonParkingString, JsonElement.class);
        JsonArray jsonElements = (JsonArray) getJsonElement(jsonObject, parkingJsonPath);
        List<Parking> parking = gson.fromJson(jsonElements.toString(), listType);
        return parking;
    }

    /**
     * search for nearby parkings using user position (longitude, latitude)
     * @param userLatitude
     * @param userLongitude
     * @param searchRadius in meters
     * @return
     */
    public List<Parking> getNearbyParkings(Double userLatitude, Double userLongitude, int searchRadius){
        List<Parking> parkingList = parkingJsonToList(getParkingsData());

        List<Parking> nearbyParking = parkingList
                .stream()
                .filter(parking -> isLocationNearby(userLatitude,userLongitude,parking.getFields().getGeoPoint2d()[0],parking.getFields().getGeoPoint2d()[1],searchRadius))
                .collect(Collectors.toList());

        resolveAvailablePlaces(nearbyParking);

        return nearbyParking;
    }

    /**
     * add information about available places if it exists
     * @param parkingList
     * @return
     */
    private List<Parking> resolveAvailablePlaces(List<Parking> parkingList){

        List<Parking> availablePlacesParkingList = parkingJsonToList(getAvailablePlacesParkingData());

        for(Parking parking: parkingList){
            Optional<Parking> optional = availablePlacesParkingList.stream().filter(parking1 -> parking1.getFields().getNom().equals(parking.getFields().getNom())).findAny();
            if(!optional.isEmpty()){
                parking.getFields().setPlacesRestantes(optional.get().getFields().getPlacesRestantes());
            }
        }

        return parkingList;
    }

    /**
     * define if a parking is near to user
     * @param userLatitude
     * @param userLongitude
     * @param parkingLatitude
     * @param parkingLongitude
     * @param searchRadius
     * @return
     */
    private boolean isLocationNearby(Double userLatitude, Double userLongitude, Double parkingLatitude, Double parkingLongitude, int searchRadius){

        userLatitude = Math.toRadians(userLatitude);
        userLongitude = Math.toRadians(userLongitude);
        parkingLatitude = Math.toRadians(parkingLatitude);
        parkingLongitude = Math.toRadians(parkingLongitude);

        // distance in meters between user and current processed parking
        double distance = 1000 * earthRadius * Math.acos(Math.sin(userLatitude)*Math.sin(parkingLatitude) + Math.cos(userLatitude)*Math.cos(parkingLatitude)*Math.cos(userLongitude - parkingLongitude));
        return distance < searchRadius;
    }


}
