package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Timestamp;

public class FareCalculatorServiceTest {

    private static FareCalculatorService fareCalculatorService;
    private Ticket ticket;

    @BeforeAll
    private static void setUp() {
	fareCalculatorService = new FareCalculatorService();
    }

    @BeforeEach
    private void setUpPerTest() {
	ticket = new Ticket();
    }

    @Test
    @DisplayName("Calcul du tarif pour une durée de stationnement de 1h pour une voiture")
    public void calculateFareCar() {
	// GIVEN
	Timestamp inTime = new Timestamp(System.currentTimeMillis());
	Timestamp outTime = new Timestamp((System.currentTimeMillis() + 3600000));
	ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
	ticket.setInTimestamp(inTime);
	ticket.setOutTimestamp(outTime);
	ticket.setParkingSpot(parkingSpot);
	// WHEN
	fareCalculatorService.calculateFare(ticket);
	// THEN
	assertEquals(1.5, ticket.getPrice());
    }

    @Test
    @DisplayName("Calcul du tarif pour une durée de stationnement de 2H pour une moto")
    public void calculateFareBike() {
	// GIVEN
	Timestamp inTime = new Timestamp(System.currentTimeMillis());
	Timestamp outTime = new Timestamp((System.currentTimeMillis() + 7200000));
	ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);
	ticket.setInTimestamp(inTime);
	ticket.setOutTimestamp(outTime);
	ticket.setParkingSpot(parkingSpot);
	// WHEN
	fareCalculatorService.calculateFare(ticket);
	// THEN
	assertEquals(2.0, ticket.getPrice());
    }

    @Test
    @DisplayName("Erreur sur le choix du véhicule (inconnu)")
    public void calculateFareUnkownType() {
	// GIVEN
	Timestamp inTime = new Timestamp(System.currentTimeMillis());
	Timestamp outTime = new Timestamp((System.currentTimeMillis() + 3600000));
	ParkingSpot parkingSpot = new ParkingSpot(1, null, false);
	ticket.setInTimestamp(inTime);
	ticket.setOutTimestamp(outTime);
	ticket.setParkingSpot(parkingSpot);
	// WHEN

	// THEN
	assertThrows(NullPointerException.class, () -> fareCalculatorService.calculateFare(ticket));
    }

    @Test
    @DisplayName("Erreur temps de sortie inférieur ou égal au temps d'entrée")
    public void calculateFareBikeWithFutureInTime() {
	// GIVEN
	Timestamp inTime = new Timestamp(System.currentTimeMillis());
	Timestamp outTime = new Timestamp((System.currentTimeMillis() - 3600000));
	ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);
	ticket.setInTimestamp(inTime);
	ticket.setOutTimestamp(outTime);
	ticket.setParkingSpot(parkingSpot);
	// WHEN

	// THEN
	assertThrows(IllegalArgumentException.class, () -> fareCalculatorService.calculateFare(ticket));
    }

    @Test
    @DisplayName("Calcul du tarif pour une durée de stationnement de 45Min avec une moto")
    public void calculateFareBikeWithLessThanOneHourParkingTime() {
	// GIVEN
	Timestamp inTime = new Timestamp(System.currentTimeMillis());
	Timestamp outTime = new Timestamp((System.currentTimeMillis() + 2700000));
	ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);
	ticket.setInTimestamp(inTime);
	ticket.setOutTimestamp(outTime);
	ticket.setParkingSpot(parkingSpot);
	// WHEN
	fareCalculatorService.calculateFare(ticket);
	// THEN
	assertEquals(0.75, ticket.getPrice());
    }

    @Test
    @DisplayName("Calcul du tarif pour une durée de stationnement de 45min avec une voiture")
    public void calculateFareCarWithLessThanOneHourParkingTime() {
	// GIVEN
	Timestamp inTime = new Timestamp(System.currentTimeMillis());
	Timestamp outTime = new Timestamp((System.currentTimeMillis() + 2700000));
	ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
	ticket.setInTimestamp(inTime);
	ticket.setOutTimestamp(outTime);
	ticket.setParkingSpot(parkingSpot);
	// WHEN
	fareCalculatorService.calculateFare(ticket);
	// THEN
	assertEquals(1.13, ticket.getPrice());

    }

    @Test
    @DisplayName("Calcul du tarif pour une durée de stationnement de 25H30Min avec une voiture")
    public void calculateFareCarWithMoreThanADayParkingTime() {
	// GIVEN
	Timestamp inTime = new Timestamp(System.currentTimeMillis());
	Timestamp outTime = new Timestamp((System.currentTimeMillis() + 91800000));
	ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
	ticket.setInTimestamp(inTime);
	ticket.setOutTimestamp(outTime);
	ticket.setParkingSpot(parkingSpot);
	// WHEN
	fareCalculatorService.calculateFare(ticket);
	// THEN
	assertEquals(38.25, ticket.getPrice());

    }

    @Test
    @DisplayName("Calcul du tarif pour une durée de stationnement de 24H10Min avec une moto")
    public void calculateFareBikeWithMoreThanADayParkingTime() {
	// GIVEN
	Timestamp inTime = new Timestamp(System.currentTimeMillis());
	Timestamp outTime = new Timestamp((System.currentTimeMillis() + 87000000));
	ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);
	ticket.setInTimestamp(inTime);
	ticket.setOutTimestamp(outTime);
	ticket.setParkingSpot(parkingSpot);
	// WHEN
	fareCalculatorService.calculateFare(ticket);
	// THEN
	assertEquals(24.17, ticket.getPrice());

    }

   

    @Test
    @DisplayName("Stationnement 30min gratuit voiture")
    public void calculateParking30minOrLessCar() {
	Timestamp inTime = new Timestamp(System.currentTimeMillis());
	Timestamp outTime = new Timestamp((System.currentTimeMillis() + 1800000));
	ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
	ticket.setInTimestamp(inTime);
	ticket.setOutTimestamp(outTime);
	ticket.setParkingSpot(parkingSpot);

	fareCalculatorService.calculateFare(ticket);

	assertEquals(0.0, ticket.getPrice());
    }

    @Test
    @DisplayName("Stationnement 31min parking payant voiture")
    public void calculate31minCar() {
	Timestamp inTime = new Timestamp(System.currentTimeMillis());
	Timestamp outTime = new Timestamp((System.currentTimeMillis() + 1860000));
	ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
	ticket.setInTimestamp(inTime);
	ticket.setOutTimestamp(outTime);
	ticket.setParkingSpot(parkingSpot);

	fareCalculatorService.calculateFare(ticket);

	assertEquals(0.78, ticket.getPrice());
    }
    
    @Test
    @DisplayName("Moins de 30min moto")
    public void calculateParking30minOrLessBike() {
	Timestamp inTime = new Timestamp(System.currentTimeMillis());
	Timestamp outTime = new Timestamp((System.currentTimeMillis() + 1800000));
	ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);
	ticket.setInTimestamp(inTime);
	ticket.setOutTimestamp(outTime);
	ticket.setParkingSpot(parkingSpot);

	fareCalculatorService.calculateFare(ticket);

	assertEquals(0.0, ticket.getPrice());
    }
    
    @Test
    @DisplayName("Stationnement 31min parking payant moto")
    public void calculate31minBike() {
	Timestamp inTime = new Timestamp(System.currentTimeMillis());
	Timestamp outTime = new Timestamp((System.currentTimeMillis() + 1860000));
	ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);
	ticket.setInTimestamp(inTime);
	ticket.setOutTimestamp(outTime);
	ticket.setParkingSpot(parkingSpot);

	fareCalculatorService.calculateFare(ticket);

	assertEquals(0.52, ticket.getPrice());
}
}