package com.smartparkinglot.backend.configuration;

import com.google.maps.GeoApiContext;
import com.google.maps.PlacesApi;
import com.google.maps.model.LatLng;
import com.google.maps.model.PlacesSearchResponse;
import com.google.maps.model.PlacesSearchResult;
import com.smartparkinglot.backend.entity.ParkingLot;
import com.smartparkinglot.backend.entity.ParkingSpot;
import com.smartparkinglot.backend.entity.User;
import com.smartparkinglot.backend.repository.ParkingLotRepository;
import com.smartparkinglot.backend.repository.ParkingSpotRepository;
import com.smartparkinglot.backend.repository.UserRepository;
import com.smartparkinglot.backend.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.data.domain.Example;

import java.math.RoundingMode;
import java.util.Random;


import java.math.BigDecimal;
import java.util.List;


@Configuration
@Order(5)
public class ParkingLotConfig {
    @Bean
    CommandLineRunner parkingLotCommandLineRunner(ParkingLotRepository parkingLotRepository, ParkingSpotRepository parkingSpotRepository, UserService userService) {
        return args -> {
            GeoApiContext context = new GeoApiContext.Builder()
                    .apiKey("AIzaSyCQnveA3osqanDT7SHtzkVYXyOypc__sKk")
                    .build();

            // Need to use multiple isolated locations in Iasi because places api paging limit is 60 locations for one call
            // right now only one small part of iasi is added(one location), because it takes a long time to add all of
            // them, in production app add the other locations
            List<LatLng> locations = List.of(new LatLng(47.181745, 27.566722));
//                    new LatLng(47.173882, 27.549240), new LatLng(47.162795, 27.564862),
//                    new LatLng(47.170673, 27.579796), new LatLng(47.154157, 27.576620),
//                    new LatLng(47.161570, 27.601597), new LatLng(47.143737, 27.598325),
//                    new LatLng(47.153248, 27.620373), new LatLng(47.144022, 27.633400));

            int locationIndex = 0;
            for(LatLng location : locations) {
                try {
                    PlacesSearchResponse response = PlacesApi
                            .textSearchQuery(context, "parcare") // Cautare dupa cuv. cheie
                            .location(locations.get(locationIndex)) // Locatia aleasa
                            .radius(2000) // Distanta maxima in metri
                            .await();

                    locationIndex++;
                    // Process first page of results


                    addToDatabase(response, parkingLotRepository, parkingSpotRepository, userService);

                    // UNCOMMENT IN PRODUCTION
//                    // Handling pagination
//                    while (response.nextPageToken != null && !response.nextPageToken.isEmpty()) {
//                        Thread.sleep(2000); // Pause to ensure the token is valid; Google recommends this delay
//                        response = PlacesApi.nearbySearchNextPage(context, response.nextPageToken).await();
//                        addToDatabase(response, parkingLotRepository, parkingSpotRepository);
//                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }


    private static void addToDatabase(PlacesSearchResponse response, ParkingLotRepository parkingLotRepository, ParkingSpotRepository parkingSpotRepository, UserService userService) {
        for (PlacesSearchResult result : response.results) {
            Random rand = new Random();
            Integer nrOfSpots = rand.nextInt(10, 30);
            Float price = rand.nextFloat(2, 10);

            User admin = userService.getUserByEmail("baciu_elena@gmail.com");

            if(admin == null) {
                return;
            }

            if(parkingLotRepository.existsByLatitudeAndLongitude(new BigDecimal(result.geometry.location.lat).setScale(7, RoundingMode.HALF_UP), new BigDecimal(result.geometry.location.lng).setScale(7, RoundingMode.HALF_UP))) {
                continue;
            }

            ParkingLot parkingLotToSave = new ParkingLot(userService.getUserByEmail("baciu_elena@gmail.com"), result.name, nrOfSpots, price, new BigDecimal(result.geometry.location.lat), new BigDecimal(result.geometry.location.lng));
            parkingLotRepository.save(parkingLotToSave);

            for(int i = 0; i < nrOfSpots; i++) {
                ParkingSpot spotForThisParkingLot = new ParkingSpot(parkingLotToSave);
                parkingSpotRepository.save(spotForThisParkingLot);
                System.out.println(spotForThisParkingLot);
            }

        }
    }
}
