package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Timestamp;

/**
 * La classe ParkingService permet l'entrée et sortie des véhicules, en
 * communiquant avec la base de données.
 * 
 * @author Dylan
 *
 */
public class ParkingService {

    private static final Logger logger = LogManager.getLogger("ParkingService");

    private static FareCalculatorService fareCalculatorService = new FareCalculatorService();

    private InputReaderUtil inputReaderUtil;
    private ParkingSpotDAO parkingSpotDAO;
    private TicketDAO ticketDAO;

    /**
     * 
     * Constructeur ParkingService
     * 
     * @param inputReaderUtil l'option choisi par l'utilisateur si c'est une voiture
     *                        ou moto.
     * @param parkingSpotDAO  est l'enregistrement dans la base de donnée du type de
     *                        parking et son numéro.
     * @param ticketDAO       est l'enregistrement du ticket dans la base de donnée.
     */
    public ParkingService(InputReaderUtil inputReaderUtil, ParkingSpotDAO parkingSpotDAO, TicketDAO ticketDAO) {
	this.inputReaderUtil = inputReaderUtil;
	this.parkingSpotDAO = parkingSpotDAO;
	this.ticketDAO = ticketDAO;
    }

    /**
     * Permet l'entrée du véhicule.
     * 
     * @throws Exception si une erreur est rencontrée lors de la vérification des
     *                   places disponibles.
     */
    public void processIncomingVehicle() throws Exception {
	/*
	 * Vérifie si une place est disponible selon le type de véhicule, si cela est
	 * correct nous mettons à jour la base de donnée, afin d'indiquer que cette
	 * place est désormais prise. Nous initions le ticket avec le type de parking,
	 * la plaque d'immatriculation et le temps d'entrée afin de l'enregistrer dans
	 * la base de donnée. Si l'utilisateur est déjà venue alors nous lui indiquons
	 * qu'il bénéficera d'une réduction à sa sortie.
	 */
	try {
	    ParkingSpot parkingSpot = getNextParkingNumberIfAvailable();

	    if (parkingSpot != null && parkingSpot.getId() > 0) {
		String vehicleRegNumber = getVehichleRegNumber();
		Ticket ticket = new Ticket();
		parkingSpot.setAvailable(false);
		parkingSpotDAO.updateParking(parkingSpot);
		Timestamp inTime = new Timestamp(System.currentTimeMillis());

		ticket.setParkingSpot(parkingSpot);
		ticket.setVehicleRegNumber(vehicleRegNumber);
		ticket.setInTimestamp(inTime);
		ticketDAO.saveTicket(ticket);
		System.out.println("Generated Ticket and saved in DB");
		System.out.println("Please park your vehicle in spot number: " + parkingSpot.getId());
		System.out.println("Recorded in-time for vehicle number: " + vehicleRegNumber + " is: " + inTime);
		if (ticketDAO.getTicketUserPresentInDB(vehicleRegNumber)) {
		    System.out.println(
			    "You have already come at least once, if you stay more than 30min you will benefit from a 5% discount when you go out");
		}
	    }

	} catch (Exception e) {
	    logger.error("Unable to process incoming vehicle", e);
	    throw e;
	}
    }

    /**
     * Récupère la plaque d'immatriculation du véhicule.
     * 
     * @return la lecture de la composition de la plaque d'immatriculation de
     *         l'utilisateur.
     * @throws Exception si la lecture de la plaque rencontre un problème.
     */
    private String getVehichleRegNumber() throws Exception {
	System.out.println("Please type the vehicle registration number and press enter key");
	return inputReaderUtil.readVehicleRegistrationNumber();
    }

    /**
     * Vérifie dans la base de donnée si une place est disponible selon le type de
     * véhicule.
     * 
     * @return La place disponible, ou un message si le parking est complet.
     * @throws Exception                si le parking est complet.
     * @throws IllegalArgumentException si le type de véhicule entrée par
     *                                  l'utilisateur est incorrect.
     */
    public ParkingSpot getNextParkingNumberIfAvailable() throws Exception, IllegalArgumentException {
	int parkingNumber = 0;
	ParkingSpot parkingSpot = null;
	try {
	    ParkingType parkingType = getVehichleType();
	    parkingNumber = parkingSpotDAO.getNextAvailableSlot(parkingType);
	    if (parkingNumber > 0) {
		parkingSpot = new ParkingSpot(parkingNumber, parkingType, true);
	    } else {
		throw new Exception("Error fetching parking number from DB. Parking slots might be full");
	    }
	} catch (IllegalArgumentException ie) {
	    logger.error("Error parsing user input for type of vehicle", ie);
	    throw ie;
	} catch (Exception e) {
	    logger.error("Error fetching next available parking slot", e);
	    throw e;
	}
	return parkingSpot;
    }

    /**
     * Lis le type de véhicule choisi par l'utilisateur.
     * 
     * @return le type de véhicule.
     * @throws Exception si une erreur est rencontrée lors de la sélection.
     */
    private ParkingType getVehichleType() throws Exception {
	System.out.println("Please select vehicle type from menu");
	System.out.println("1 CAR");
	System.out.println("2 BIKE");
	int input = inputReaderUtil.readSelection();
	switch (input) {
	case 1: {
	    return ParkingType.CAR;
	}
	case 2: {
	    return ParkingType.BIKE;
	}
	default: {
	    System.out.println("Incorrect input provided");
	    throw new IllegalArgumentException("Entered input is invalid");
	}
	}
    }

    /**
     * Permet la sortie d'un véhicule en récupérant ses informations d'entrée via
     * son ticket enregistré en base de donnée. Lis le numéro d'immatriculation
     * indiqué par l'utilisateur. Récupère les informations d'entrée sur le ticket
     * correspondant dans la base de donnée. Initie le temps de sortie sur le
     * ticket. Fait appel au service de calcul afin de calculer le tarif en fonction
     * du ticket. Si l'utilisateur est déjà venue, alors la réduction de 5% est
     * appliquée. Mets à jour le ticket dans la base de donnée. Mets à jour le
     * parking dans la base de donnée. Indique à l'utilisateur le tarif à payer.
     * Envoi un message d'erreur si le ticket ne se mets pas à jour.
     * 
     * @throws Exception si on arrive pas à vérifier l'existence d'un ticket pour ce
     *                   véhicule.
     */
    public void processExitingVehicle() throws Exception {
	try {
	    String vehicleRegNumber = getVehichleRegNumber();
	    Timestamp outTime = new Timestamp(System.currentTimeMillis());
	    Ticket ticket = ticketDAO.getTicket(vehicleRegNumber);
	    ticket.setOutTimestamp(outTime);
	    fareCalculatorService.calculateFare(ticket);
	    if (ticketDAO.getTicketUserPresentInDB(vehicleRegNumber) && ticket.getPrice() > 0.0) {
		double reduction = ticket.getPrice() * 5 / 100;
		double applyReduction = ticket.getPrice() - reduction;
		double convertPrice = (double) Math.round(applyReduction * 100) / 100;
		ticket.setPrice(convertPrice);
		System.out.println("You are entitled to a 5% discount applied immediately for your recurring use of our parking");
	    }
	    if (ticketDAO.updateTicket(ticket) == true) {

		ParkingSpot parkingSpot = ticket.getParkingSpot();
		parkingSpot.setAvailable(true);
		parkingSpotDAO.updateParking(parkingSpot);
		System.out.println("Please pay the parking fare: " + ticket.getPrice() + "€");
		System.out.println(
			"Recorded out-time for vehicle number: " + ticket.getVehicleRegNumber() + " is: " + outTime);
	    } else {
		System.out.println("Unable to update ticket information. Error occurred");
	    }

	} catch (Exception e) {
	    logger.error("Unable to process exiting vehicle", e);
	    throw e;
	}
    }
}