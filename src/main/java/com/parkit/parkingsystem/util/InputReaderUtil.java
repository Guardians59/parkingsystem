package com.parkit.parkingsystem.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Scanner;

/**
 * La classe InputReaderUtil permet de lire les données entrées par
 * l'utilisateur via le scanner.
 * 
 * @author Dylan
 *
 */
public class InputReaderUtil {

    private static Scanner scan = new Scanner(System.in);
    private static final Logger logger = LogManager.getLogger("InputReaderUtil");

    /**
     * Permet de lire la sélection de l'utilisateur.
     * 
     * @return input la sélection inscrit par l'utilisateur.
     * @throws Exception si la lecture échoue.
     */
    public int readSelection() throws Exception {
	try {
	    int input = Integer.parseInt(scan.nextLine());
	    return input;
	} catch (Exception e) {
	    logger.error("Error while reading user input from Shell", e);
	    System.out.println("Error reading input. Please enter valid number for proceeding further");
	    return -1;
	}
    }

    /**
     * Permet de lire le numéro d'immatriculation inscrit par l'utilisateur.
     * 
     * @return vehicleRegNumber le numéro d'immatriculation.
     * @throws Exception si la lecture échoue.
     */
    public String readVehicleRegistrationNumber() throws Exception {
	try {
	    String vehicleRegNumber = scan.nextLine();
	    if (vehicleRegNumber == null || vehicleRegNumber.trim().length() == 0) {
		throw new IllegalArgumentException("Invalid input provided");
	    }
	    return vehicleRegNumber;
	} catch (Exception e) {
	    logger.error("Error while reading user input from Shell", e);
	    System.out.println("Error reading input. Please enter a valid string for vehicle registration number");
	    throw e;
	}
    }

}
