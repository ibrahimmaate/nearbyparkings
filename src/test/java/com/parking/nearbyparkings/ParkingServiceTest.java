package com.parking.nearbyparkings;

import com.google.gson.Gson;
import com.parking.nearbyparkings.models.Parking;
import com.parking.nearbyparkings.models.ParkingFields;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties ={ "parking.path=test.records" })
class ParkingServiceTest {

    @Autowired
    ParkingService parkingService;

    @Test
    void parkingJsonToListTest() throws IOException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Path filePath = Path.of("src/test/resources/jsonParking.json");
        String content = Files.readString(filePath);

        ParkingFields parkingFields = new ParkingFields();
        parkingFields.setNom("PALAIS DE JUSTICE");
        parkingFields.setGeoPoint2d(new Double[]{46.58595804860371,0.3512954265806957});
        parkingFields.setNbPlaces(140);
        Parking parking = new Parking("e24d0474ce8282db403e8cbf61c94069c1b89460",parkingFields);

        Method methodParkingJsonToList = ParkingService.class.getDeclaredMethod("parkingJsonToList", String.class);
        methodParkingJsonToList.setAccessible(true);
        List<Parking> parkingList = (List<Parking>) methodParkingJsonToList.invoke(parkingService, content);

        assertEquals(parkingList.size(),1);
        assertThat(parkingList.get(0)).usingRecursiveComparison().isEqualTo(parking);
        methodParkingJsonToList.setAccessible(false);
    }

    @Test
    void getNearbyParkingsTest() throws IOException {
        String content = Files.readString(Path.of("src/test/resources/jsonParkingFullData.json"));
        String content2 = Files.readString(Path.of("src/test/resources/jsonAvailableParkingPlaces.json"));

        ParkingService parkingServiceMock = Mockito.mock(ParkingService.class);

        ReflectionTestUtils.setField(parkingServiceMock, "parkingJsonPath", "test.records");
        ReflectionTestUtils.setField(parkingServiceMock, "gson", new Gson());

        Mockito.when(parkingServiceMock.getParkingsData()).thenReturn(content);
        Mockito.when(parkingServiceMock.getAvailablePlacesParkingData()).thenReturn(content2);
        Mockito.when(parkingServiceMock.getNearbyParkings(Mockito.anyDouble(), Mockito.anyDouble(), Mockito.anyInt())).thenCallRealMethod();

        List<Parking> nearbyParking = parkingServiceMock.getNearbyParkings(46.58448367959357, 0.34844930500842264, 200);
        List<Parking> nearbyParking2 = parkingServiceMock.getNearbyParkings(46.57448367959357, 0.34744930500842264, 1000);
        List<Parking> nearbyParking3 = parkingServiceMock.getNearbyParkings(47.58448367959357, 1.34844930500842264, 200);

        assertEquals(1, nearbyParking.size());
        assertEquals(8, nearbyParking2.size());
        assertEquals(0,nearbyParking3.size());

    }

    @Test
    void resolveAvailablePlacesTest() throws NoSuchMethodException, IOException, InvocationTargetException, IllegalAccessException {
        String content2 = Files.readString(Path.of("src/test/resources/jsonAvailableParkingPlaces.json"));

        ParkingService parkingServiceMock = Mockito.mock(ParkingService.class);

        Mockito.when(parkingServiceMock.getAvailablePlacesParkingData()).thenReturn(content2);
        ReflectionTestUtils.setField(parkingServiceMock, "parkingJsonPath", "test.records");
        ReflectionTestUtils.setField(parkingServiceMock, "gson", new Gson());

        ParkingFields parkingFields = new ParkingFields();
        parkingFields.setNbPlaces(665);
        parkingFields.setNom("BLOSSAC TISON");
        parkingFields.setGeoPoint2d(new Double[]{46.57505317559496,0.337126307915689});
        Parking parking = new Parking("d16fcc8493a83b76f29199365fb94772755f4131", parkingFields);
        List<Parking> parkingList = new ArrayList<>();
        parkingList.add(parking);

        Method resolveAvailablePlaces = ParkingService.class.getDeclaredMethod("resolveAvailablePlaces", List.class);
        resolveAvailablePlaces.setAccessible(true);

        assertEquals(parkingList.get(0).getFields().getPlacesRestantes(), null);

        List<Parking> parkingListWithResolvedAvailablePlaces = (List<Parking>) resolveAvailablePlaces.invoke(parkingServiceMock, parkingList);

        assertEquals(parkingListWithResolvedAvailablePlaces.get(0).getFields().getPlacesRestantes(),357);

    }

    @Test
    void isLocationNearbyTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method methodIsLocationNearby = ParkingService.class.getDeclaredMethod("isLocationNearby", Double.class, Double.class, Double.class, Double.class, int.class);
        methodIsLocationNearby.setAccessible(true);

        boolean expected1 = (boolean) methodIsLocationNearby.invoke(parkingService, 46.58448367959357, 0.34844930500842264, 46.58348367959357, 0.34744930500842264, 200);
        boolean expected2 = (boolean) methodIsLocationNearby.invoke(parkingService, 46.58448367959357, 0.34844930500842264, 46.58348367959357, 0.34744930500842264, 50);
        boolean expected3 = (boolean) methodIsLocationNearby.invoke(parkingService, 46.59448367959357, 0.35844930500842264, 46.58448367959357, 0.34844930500842264, 200);

        assertTrue(expected1);
        assertFalse(expected2);
        assertFalse(expected3);

        methodIsLocationNearby.setAccessible(false);
    }

}