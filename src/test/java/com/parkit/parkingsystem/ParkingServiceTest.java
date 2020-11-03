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
    public void testProcessIncomingVehicleCar() throws Exception {
	// GIVEN
	when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEFC");
	when(inputReaderUtil.readSelection()).thenReturn(1);
	when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(17);
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
    public void testProcessIncomingVehicleBike() throws Exception {
	// GIVEN
	when(inputReaderUtil.readSelection()).thenReturn(2);
	when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEFM");
	when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(17);
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
    public void testProcessIncomingVehicleDontSpot() throws Exception {
	// GIVEN
	when(inputReaderUtil.readSelection()).thenReturn(1);
	when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(0);
	// WHEN

	// THEN
	assertThrows(Exception.class, () -> parkingService.getNextParkingNumberIfAvailable());
	assertThrows(Exception.class, () -> parkingService.processIncomingVehicle());

    }

    @Test
    @DisplayName("Erreur du choix du type de véhicule")
    public void testGetVehicleTypeError() throws Exception {
	// GIVEN
	when(inputReaderUtil.readSelection()).thenThrow(new Exception());
	// WHEN

	// THEN
	assertThrows(Exception.class, () -> parkingService.getNextParkingNumberIfAvailable());

    }

    @Test
    @DisplayName("Sortie du véhicule avec le bon tarif")
    public void testProcessExitingVehicleCar() throws Exception {
	// GIVEN
	Timestamp inTime = new Timestamp(System.currentTimeMillis() - 3600000);
	when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEFC2");
	ticket.setInTimestamp(inTime);
	ticket.setParkingSpot(parkingSpot);
	ticket.setVehicleRegNumber("ABCDEFC2");
	when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
	when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
	when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
	// WHEN
	parkingService.processExitingVehicle();
	// THEN
	assertThat(ticket.getPrice()).isEqualTo(1.5);

    }

    @Test
    @DisplayName("Sortie d'une moto avec le bon tarif")
    public void testProcessExitingBike() throws Exception {
	// GIVEN
	ParkingSpot parkingSpot = new ParkingSpot(3, ParkingType.BIKE, false);
	Timestamp inTime = new Timestamp(System.currentTimeMillis() - 3600000);
	when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF2");
	ticket.setInTimestamp(inTime);
	ticket.setParkingSpot(parkingSpot);
	ticket.setVehicleRegNumber("ABCDEF2");
	when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
	when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
	when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
	// WHEN
	parkingService.processExitingVehicle();
	// THEN
	assertThat(ticket.getPrice()).isEqualTo(1.0);

    }

    @Test
    @DisplayName("Erreur de lecture de la plaque lors de la sortie du véhicule")
    public void testProcessExitingVehicleReadError() throws Exception {
	// GIVEN
	when(inputReaderUtil.readVehicleRegistrationNumber()).thenThrow(new IllegalArgumentException());
	// WHEN

	// THEN
	assertThrows(Exception.class, () -> parkingService.processExitingVehicle());

    }

    @Test
    @DisplayName("Erreur lors de la mis à jour du ticket")
    public void testProcessExitingVehicleError() throws Exception {
	// GIVEN
	Timestamp inTime = new Timestamp(System.currentTimeMillis() - 3600000);
	when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEFE2");
	ticket.setInTimestamp(inTime);
	ticket.setParkingSpot(parkingSpot);
	ticket.setVehicleRegNumber("ABCDEFE2");
	when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
	when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(false);
	// WHEN
	parkingService.processExitingVehicle();
	// THEN
	assertThat(ticketDAO.updateTicket(ticket)).isEqualTo(false);

    }

    @Test
    @DisplayName("Réduction pour une voiture")
    public void testCarReduction() throws Exception {
	// GIVEN
	Timestamp inTime = new Timestamp(System.currentTimeMillis() - 3600000);
	when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEFC2");
	ticket.setInTimestamp(inTime);
	ticket.setParkingSpot(parkingSpot);
	ticket.setVehicleRegNumber("ABCDEFC2");
	when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
	when(ticketDAO.getTicketUserPresentInDB("ABCDEFC2")).thenReturn(true);
	when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
	when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
	// WHEN
	parkingService.processExitingVehicle();
	// THEN
	assertThat(ticket.getPrice()).isEqualTo(1.43);
    }

    @Test
    @DisplayName("Réduction pour une moto")
    public void testBikeReduction() throws Exception {
	// GIVEN
	ParkingSpot parkingSpot = new ParkingSpot(3, ParkingType.BIKE, false);
	Timestamp inTime = new Timestamp(System.currentTimeMillis() - 3600000);
	when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF2");
	ticket.setInTimestamp(inTime);
	ticket.setParkingSpot(parkingSpot);
	ticket.setVehicleRegNumber("ABCDEF2");
	when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
	when(ticketDAO.getTicketUserPresentInDB("ABCDEF2")).thenReturn(true);
	when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
	when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
	// WHEN
	parkingService.processExitingVehicle();
	//THEN
	assertThat(ticket.getPrice()).isEqualTo(0.95);
    }
    
}
