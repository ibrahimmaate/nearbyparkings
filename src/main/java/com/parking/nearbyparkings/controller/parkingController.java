package com.parking.nearbyparkings.controller;

import com.parking.nearbyparkings.ParkingService;
import com.parking.nearbyparkings.models.Parking;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/parkings/*")
public class parkingController {

    @Autowired
    ParkingService parkingService;

    /**
     * exposed endpoint to get nearby parkings
     * @param userLatitude
     * @param userLongitude
     * @param searchRadius
     * @return
     */
    @GetMapping(value = "/nearbyparkings")
    @ResponseBody
    public List<Parking> getNearbyParkings(@RequestParam(name = "lat") Double userLatitude, @RequestParam(name = "long") Double userLongitude, @RequestParam(name = "searchradius") int searchRadius) {
        return parkingService.getNearbyParkings(userLatitude,userLongitude,searchRadius);
    }

}
