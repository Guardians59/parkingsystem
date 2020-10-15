package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * La classe InteractiveShell permet l'interaction avec l'utilisateur.
 * 
 * @author Dylan
 *
 */
public class InteractiveShell {

    private static final Logger logger = LogManager.getLogger("InteractiveShell");

    /**
     * Permet de charger les fonctionnalités correspondantes au choix de
     * l'utilisateur selon l'option choisi. L'option numéro un exécute l'entrée du
     * vehicule. L'option numéro deux exécute la sortie du véhicule. L'option numéro
     * trois permet de sortir du système.
     * 
     * 
     * @throws Exception si une erreur est rencontrée lors de l'exécution du
     *                   programme.
     */
    public static void loadInterface() throws Exception {
	logger.info("App initialized!!!");
	System.out.println("Welcome to Parking System!");

	boolean continueApp = true;
	InputReaderUtil inputReaderUtil = new InputReaderUtil();
	ParkingSpotDAO parkingSpotDAO = new ParkingSpotDAO();
	TicketDAO ticketDAO = new TicketDAO();
	ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

	while (continueApp) {
	    loadMenu();
	    int option = inputReaderUtil.readSelection();
	    switch (option) {
	    case 1: {
		parkingService.processIncomingVehicle();
		break;
	    }
	    case 2: {
		parkingService.processExitingVehicle();
		break;
	    }
	    case 3: {
		System.out.println("Exiting from the system!");
		continueApp = false;
		break;
	    }
	    default:
		System.out.println("Unsupported option. Please enter a number corresponding to the provided menu");
	    }
	}
    }

    //Permet d'indiquer à l'utilisateur qu'elle option choisir.
     
    private static void loadMenu() {
	System.out.println("Please select an option. Simply enter the number to choose an action");
	System.out.println("1 New Vehicle Entering - Allocate Parking Space");
	System.out.println("2 Vehicle Exiting - Generate Ticket Price");
	System.out.println("3 Shutdown System");
    }

}
