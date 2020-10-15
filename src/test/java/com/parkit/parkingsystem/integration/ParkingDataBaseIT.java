package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.sql.Timestamp;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;
    private ParkingType parkingType;
    private static Ticket ticket;
	

    @Mock
    private static InputReaderUtil inputReaderUtil;

    @BeforeAll
    private static void setUp() throws Exception{
       
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
    }

	

    @BeforeEach
    private void setUpPerTest() throws Exception {
    	
    	//when(inputReaderUtil.readSelection()).thenReturn(1);
        //when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        
    }

    @AfterAll
    private static void tearDown(){
    	dataBasePrepareService.clearDataBaseEntries();
    }
    
   

    @Test
    @DisplayName("Entrée d'une voiture")
    public void testParkingACar() throws Exception{
    	//GIVEN
    	when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingType = ParkingType.CAR;
        parkingSpotDAO.getNextAvailableSlot(parkingType);
        
        //WHEN
        parkingService.processIncomingVehicle();
        ticket = ticketDAO.getTicket("ABCDEF");
        
        //THEN
        assertThat(parkingSpotDAO.getNextAvailableSlot(parkingType)).isNotEqualTo(1);
        assertThat(ticketDAO.saveTicket(ticket)).isNotEqualTo(null);
        
        
        //TODO: check that a ticket is actualy saved in DB and Parking table is updated with availability
    }
    
    @Test
    @DisplayName("Entrée, sortie d'une voiture")
    public void testParkingCarExit() throws SQLException, Exception{
    	//GIVEN
    	when(inputReaderUtil.readSelection()).thenReturn(1);
    	when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF2");
    	ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
    	parkingType = ParkingType.CAR;
        parkingSpotDAO.getNextAvailableSlot(parkingType);
        Timestamp inTime = new Timestamp (System.currentTimeMillis()-3600000);
        
        //WHEN
        parkingService.processIncomingVehicle();
        ticket = ticketDAO.getTicket("ABCDEF2");
        ticket.setInTimestamp(inTime);
        ticketDAO.saveTicket(ticket);
        parkingService.processExitingVehicle();
        ticket = ticketDAO.getTicket("ABCDEF2");
        
        //THEN
        assertThat(ticket.getPrice()).isEqualTo(1.5);
        assertThat(ticket.getParkingSpot().getId()).isEqualTo(1);
        //TODO: check that the fare generated and out time are populated correctly in the database
    }
    
    @Test
    @DisplayName("Entrée d'une moto")
    public void testParkingABike() throws Exception {
    	//GIVEN
    	when(inputReaderUtil.readSelection()).thenReturn(2);
    	when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("BCDEFG");
    	ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
    	parkingType = ParkingType.BIKE;
    	parkingSpotDAO.getNextAvailableSlot(parkingType);
    	
    	//WHEN
    	parkingService.processIncomingVehicle();
    	ticket = ticketDAO.getTicket("BCDEFG");
    	
    	//THEN
    	assertThat(parkingSpotDAO.getNextAvailableSlot(parkingType)).isNotEqualTo(4);
    	assertThat(ticketDAO.saveTicket(ticket)).isNotEqualTo(null);
    	assertThat(ticket.getParkingSpot().getId()).isEqualTo(4);
    	}
    
    
    @Test
    @DisplayName("Erreur du choix de véhicule")
    public void testErrorVehicleChoice() throws Exception {
    	//GIVEN
    	ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
    	
    	//WHEN
    	when(inputReaderUtil.readSelection()).thenThrow(new IllegalArgumentException());
    	
    	//THEN
    	assertThrows(IllegalArgumentException.class,()->parkingService.getNextParkingNumberIfAvailable());
    }

    @Test
    @DisplayName("Erreur, véhicule inconnue dans la BD")
    public void testUnknownVehicle() throws Exception {
    	//GIVEN
    	ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
    	
    	//WHEN
    	when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("AAABBB");
    	
    	//THEN
    	assertThrows(Exception.class,()->parkingService.processExitingVehicle());
    }
    @Test
    @DisplayName("Entrée, sortie d'une moto")
    public void testParkingBikeExit() throws Exception {
    	//GIVEN
    	when(inputReaderUtil.readSelection()).thenReturn(2);
    	when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("BCDEFG2");
    	ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
    	Timestamp inTime = new Timestamp (System.currentTimeMillis()-3600000);
    	
    	//WHEN
    	parkingService.processIncomingVehicle();
    	ticket = ticketDAO.getTicket("BCDEFG2");
        ticket.setInTimestamp(inTime);
        ticketDAO.saveTicket(ticket);
        parkingService.processExitingVehicle();
        ticket = ticketDAO.getTicket("BCDEFG2");
        
        //THEN
        assertThat(ticket.getPrice()).isEqualTo(1.0);
        assertThat(ticket.getParkingSpot().getId()).isEqualTo(5);
       
    }
    
    @Test
    @DisplayName("Erreur, parking moto complet")
    public void testParkingBikeFull() throws Exception {
    	//GIVEN
    	ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
    	
    	//WHEN
    	when(inputReaderUtil.readSelection()).thenReturn(2);
    	when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("BCDEFG3");
    	parkingService.processIncomingVehicle();
    	
    	//THEN
    	assertThrows(Exception.class,()-> parkingService.processIncomingVehicle());
    }
    
}
