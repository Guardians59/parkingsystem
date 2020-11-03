package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.sql.Timestamp;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;
    private ParkingType parkingType;
    private static Ticket ticket;
    private ParkingSpot parkingSpot;

    @Mock
    private static InputReaderUtil inputReaderUtil;

    @BeforeAll
    private static void setUp() throws Exception {

	parkingSpotDAO = new ParkingSpotDAO();
	parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
	ticketDAO = new TicketDAO();
	ticketDAO.dataBaseConfig = dataBaseTestConfig;
	dataBasePrepareService = new DataBasePrepareService();
    }

    @AfterAll
    private static void tearDown() {
	dataBasePrepareService.clearDataBaseEntries();
    }

    @Test
    @DisplayName("Entrée d'une voiture")
    public void testParkingACar() throws Exception {
	// GIVEN
	when(inputReaderUtil.readSelection()).thenReturn(1);
	when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
	ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
	parkingType = ParkingType.CAR;
	parkingSpotDAO.getNextAvailableSlot(parkingType);

	// WHEN
	parkingService.processIncomingVehicle();

	// THEN
	assertThat(parkingSpotDAO.getNextAvailableSlot(parkingType)).isNotEqualTo(1);
	assertThat(ticketDAO.getTicket("ABCDEF").getParkingSpot().getId()).isEqualTo(1);

	// TODO: check that a ticket is actualy saved in DB and Parking table is updated
	// with availability
    }

    @Test
    @DisplayName("Sortie d'une voiture, enregistrement des infos en BD correct")
    public void testParkingCarLotExit() throws SQLException, Exception {
	// GIVEN
	when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF2");
	ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
	parkingType = ParkingType.CAR;
	parkingSpotDAO.getNextAvailableSlot(parkingType);
	Timestamp inTime = new Timestamp(System.currentTimeMillis() - 3600000);
	parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
	ticket = new Ticket();
	ticket.setParkingSpot(parkingSpot);
	ticket.setVehicleRegNumber("ABCDEF2");
	ticket.setInTimestamp(inTime);
	ticketDAO.saveTicket(ticket);

	// WHEN
	parkingService.processExitingVehicle();
	ticket = ticketDAO.getTicket("ABCDEF2");

	// THEN
	assertThat(ticket.getPrice()).isEqualTo(1.5);
	assertThat(ticket.getParkingSpot().getId()).isEqualTo(1);
	assertNotNull(ticket.getOutTimestamp());
	// TODO: check that the fare generated and out time are populated correctly in
	// the database
    }

    @Test
    @DisplayName("Entrée d'une moto")
    public void testParkingABike() throws Exception {
	// GIVEN
	when(inputReaderUtil.readSelection()).thenReturn(2);
	when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("BCDEFG");
	ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
	parkingType = ParkingType.BIKE;
	parkingSpotDAO.getNextAvailableSlot(parkingType);

	// WHEN
	parkingService.processIncomingVehicle();

	// THEN
	assertThat(parkingSpotDAO.getNextAvailableSlot(parkingType)).isNotEqualTo(4);

	assertThat(ticketDAO.getTicket("BCDEFG").getParkingSpot().getId()).isEqualTo(4);
    }

    @Test
    @DisplayName("Erreur du choix de véhicule")
    public void testErrorVehicleChoice() throws Exception {
	// GIVEN
	ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

	// WHEN
	when(inputReaderUtil.readSelection()).thenThrow(new IllegalArgumentException());

	// THEN
	assertThrows(IllegalArgumentException.class, () -> parkingService.getNextParkingNumberIfAvailable());
    }

    @Test
    @DisplayName("Erreur, véhicule inconnue dans la BD")
    public void testUnknownVehicle() throws Exception {
	// GIVEN
	ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

	// WHEN
	when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("AAABBB");

	// THEN
	assertThrows(Exception.class, () -> parkingService.processExitingVehicle());
    }

    @Test
    @DisplayName("Sortie d'une voiture avec la réduction")
    public void testParkingCarLotExitReduction() throws Exception {
	// GIVEN
	when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF2");
	ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
	parkingType = ParkingType.CAR;
	parkingSpotDAO.getNextAvailableSlot(parkingType);
	Timestamp inTime = new Timestamp(System.currentTimeMillis() - 3600000);
	parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
	ticket = new Ticket();
	ticket.setParkingSpot(parkingSpot);
	ticket.setVehicleRegNumber("ABCDEF2");
	ticket.setInTimestamp(inTime);
	ticketDAO.saveTicket(ticket);

	// WHEN
	parkingService.processExitingVehicle();
	ticket = ticketDAO.getTicket("ABCDEF2");

	// THEN
	assertThat(ticket.getPrice()).isEqualTo(1.43);
	assertNotNull(ticket.getOutTimestamp());
    }

    @Test
    @DisplayName("Sortie d'une moto, enregistrement des infos en BD correct")
    public void testParkingBikeLotExit() throws Exception {

	// GIVEN
	when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("BCDEFG2");
	parkingSpot = new ParkingSpot(5, ParkingType.BIKE, false);
	ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
	Timestamp inTime = new Timestamp(System.currentTimeMillis() - 3600000);
	ticket = new Ticket();
	ticket.setParkingSpot(parkingSpot);
	ticket.setInTimestamp(inTime);
	ticket.setVehicleRegNumber("BCDEFG2");
	ticketDAO.saveTicket(ticket);

	// WHEN
	parkingService.processExitingVehicle();
	ticket = ticketDAO.getTicket("BCDEFG2");

	// THEN
	assertThat(ticket.getPrice()).isEqualTo(1.0);
	assertThat(ticket.getParkingSpot().getId()).isEqualTo(5);
	assertNotNull(ticket.getOutTimestamp());
    }

    @Test
    @DisplayName("Sortie d'une moto avec la réduction")
    public void testParkingBikeLotExitReduction() throws Exception {

	// GIVEN
	when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("BCDEFG2");
	parkingSpot = new ParkingSpot(5, ParkingType.BIKE, false);
	ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
	Timestamp inTime = new Timestamp(System.currentTimeMillis() - 3600000);
	ticket = new Ticket();
	ticket.setParkingSpot(parkingSpot);
	ticket.setInTimestamp(inTime);
	ticket.setVehicleRegNumber("BCDEFG2");
	ticketDAO.saveTicket(ticket);

	// WHEN
	parkingService.processExitingVehicle();
	ticket = ticketDAO.getTicket("BCDEFG2");
	
	//THEN
	assertThat(ticket.getPrice()).isEqualTo(0.95);
	assertNotNull(ticket.getOutTimestamp());

    }
    
    @Test
    @DisplayName("Entrée d'un utilisateur récurrent, indication de la réduction active")
    public void testRecurrentUserReductionMessage() throws Exception {
	// GIVEN
		when(inputReaderUtil.readSelection()).thenReturn(1);
		when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF2");
		ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
		parkingType = ParkingType.CAR;
		parkingSpotDAO.getNextAvailableSlot(parkingType);

		// WHEN
		parkingService.processIncomingVehicle();
		
		//THEN
		boolean result = ticketDAO.getTicketUserPresentInDB("ABCDEF2");
		assertTrue(result);
    }
    
    @Test
    @DisplayName("Nouveau utilisateur, pas de réduction")
    public void testNewUserNotReduction() throws Exception {
	//GIVEN
	boolean result = ticketDAO.getTicketUserPresentInDB("BBCCDD");
	//WHEN
	
	//THEN
	assertFalse(result);
    }
}
