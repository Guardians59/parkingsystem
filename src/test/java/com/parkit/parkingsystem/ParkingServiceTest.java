package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ParkingServiceTest {

    @Mock
    private static InputReaderUtil inputReaderUtil;
    @Mock
    private static ParkingSpotDAO parkingSpotDAO;
    @Mock
    private static TicketDAO ticketDAO;

    private static ParkingService parkingService;
    private ParkingSpot parkingSpot;
    private Ticket ticket;

    @BeforeEach
    private void setUpPerTest() {
	ticket = new Ticket();
	parkingSpot = new ParkingSpot(17, ParkingType.CAR, false);
	parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
    }

    @Test
    @DisplayName("Entrée place N°17 disponible pour une voiture")
    public void processIncomingVehicleCarTest() throws IllegalArgumentException, Exception {
	// GIVEN
	when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
	when(inputReaderUtil.readSelection()).thenReturn(1);
	when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(17);
	ticket.setInTimestamp(new Timestamp(System.currentTimeMillis()));
	ticket.setParkingSpot(parkingSpot);
	ticket.setVehicleRegNumber("ABCDEF");
	when(ticketDAO.saveTicket(any(Ticket.class))).thenReturn(ticket);
	when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
	// WHEN
	parkingService.processIncomingVehicle();
	// THEN
	verify(inputReaderUtil, Mockito.times(1)).readSelection();
	verify(parkingSpotDAO, Mockito.times(1)).getNextAvailableSlot(any(ParkingType.class));
	assertThat(parkingService.getNextParkingNumberIfAvailable().getId()).isEqualTo(parkingSpot.getId());
	assertThat(parkingService.getNextParkingNumberIfAvailable().isAvailable()).isEqualTo(true);

    }

    @Test
    @DisplayName("Entrée place N°17 disponible pour une moto")
    public void processIncomingVehicleBikeTest() throws Exception {
	// GIVEN
	when(inputReaderUtil.readSelection()).thenReturn(2);
	when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
	when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(17);
	ticket.setInTimestamp(new Timestamp(System.currentTimeMillis()));
	ticket.setParkingSpot(parkingSpot);
	ticket.setVehicleRegNumber("ABCDEF");
	when(ticketDAO.saveTicket(any(Ticket.class))).thenReturn(ticket);
	when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
	// WHEN
	parkingService.processIncomingVehicle();
	// THEN
	assertThat(parkingService.getNextParkingNumberIfAvailable().getId()).isEqualTo(parkingSpot.getId());
	assertThat(parkingService.getNextParkingNumberIfAvailable().isAvailable()).isEqualTo(true);

    }

    @Test
    @DisplayName("Erreur place indisponible")
    public void processIncomingVehicleDontSpotTest() throws Exception {
	// GIVEN
	when(inputReaderUtil.readSelection()).thenReturn(1);
	when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(0);
	ticket.setInTimestamp(new Timestamp(System.currentTimeMillis()));
	ticket.setParkingSpot(parkingSpot);
	ticket.setVehicleRegNumber("ABCDEF");
	// WHEN

	// THEN
	assertThrows(Exception.class, () -> parkingService.getNextParkingNumberIfAvailable());
	assertThrows(Exception.class, () -> parkingService.processIncomingVehicle());

    }

    @Test
    @DisplayName("Erreur du choix du type de véhicule")
    public void getVehicleTypeErrorTest() throws Exception {
	// GIVEN
	when(inputReaderUtil.readSelection()).thenThrow(new Exception());
	// WHEN

	// THEN
	assertThrows(Exception.class, () -> parkingService.getNextParkingNumberIfAvailable());

    }

    @Test
    @DisplayName("Sortie du véhicule avec le bon tarif")
    public void processExitingVehicleTest() throws Exception {
	// GIVEN
	Timestamp inTime = new Timestamp(System.currentTimeMillis() - 3600000);
	Timestamp outTimes = new Timestamp(System.currentTimeMillis());
	when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
	ticket.setInTimestamp(inTime);
	ticket.setParkingSpot(parkingSpot);
	ticket.setVehicleRegNumber("ABCDEF");
	when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
	ticket.setOutTimestamp(outTimes);
	when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
	when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
	// WHEN
	parkingService.processExitingVehicle();
	// THEN
	assertThat(ticket.getPrice()).isEqualTo(1.5);

    }

    @Test
    @DisplayName("Sortie d'une moto avec le bon tarif")
    public void processExitingBikeTest() throws Exception {
	// GIVEN
	ParkingSpot parkingSpot = new ParkingSpot(3, ParkingType.BIKE, false);
	Timestamp inTime = new Timestamp(System.currentTimeMillis() - 3600000);
	Timestamp outTimes = new Timestamp(System.currentTimeMillis());
	when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
	ticket.setInTimestamp(inTime);
	ticket.setParkingSpot(parkingSpot);
	ticket.setVehicleRegNumber("ABCDEF");
	when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
	ticket.setOutTimestamp(outTimes);
	when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
	when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
	// WHEN
	parkingService.processExitingVehicle();
	// THEN
	assertThat(ticket.getPrice()).isEqualTo(1.0);

    }

    @Test
    @DisplayName("Erreur de lecture de la plaque lors de la sortie du véhicule")
    public void processExitingVehicleReadErrorTest() throws Exception {
	// GIVEN
	when(inputReaderUtil.readVehicleRegistrationNumber()).thenThrow(new IllegalArgumentException());
	// WHEN

	// THEN
	assertThrows(Exception.class, () -> parkingService.processExitingVehicle());

    }

    @Test
    @DisplayName("Erreur lors de la sortie du véhicule")
    public void processExitingVehicleErrorTest() throws Exception {
	// GIVEN
	Timestamp inTime = new Timestamp(System.currentTimeMillis() - 3600000);
	Timestamp outTimes = new Timestamp(System.currentTimeMillis());
	when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
	ticket.setInTimestamp(inTime);
	ticket.setParkingSpot(parkingSpot);
	ticket.setVehicleRegNumber("ABCDEF");
	when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
	ticket.setOutTimestamp(outTimes);
	when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(false);
	// WHEN
	parkingService.processExitingVehicle();
	// THEN
	assertThat(ticketDAO.updateTicket(ticket)).isEqualTo(false);

    }

}
