package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

/**
 * La classe FareCalculatorService permet de calculer le prix du ticket selon le
 * temps passé dans le parking et le type de véhicule
 * 
 * @author Dylan
 *
 */

public class FareCalculatorService {

    private double priceTicket;
    private double priceTicketConvert;

    /**
     * 
     * @param ticket le ticket de l'utilisateur
     * 
     */

    public void calculateFare(Ticket ticket) {
	// inMinutes est la date d'entrée en minutes
	long inMinutes = ((ticket.getInTimestamp().getTime() / 1000) / 60);
	// outMinutes est la date de sortie en minutes
	long outMinutes = ((ticket.getOutTimestamp().getTime() / 1000) / 60);
	// Si le temps de sortie est inférieur ou égal au temps d'entrée, nous avons une
	// erreur

	if ((outMinutes < inMinutes) || (outMinutes == inMinutes)) {
	    throw new IllegalArgumentException("Out time provided is incorrect:" + ticket.getOutTimestamp());
	}

	/* duration est la différence des deux dates en minutes, pour permettre le
	 calcul du tarif*/
	double duration = (outMinutes - inMinutes);
	// Si la durée de stationnement est inférieure ou égal à 30min, c'est gratuit.
	if (duration <= 30) {

	    ticket.setPrice(0.0);
	    System.out.println("You are staying less than 30 minutes, it's free");

	}

	/*
	 * Le switch nous permet d'arriver dans le bon service de calcul selon le type
	 * de véhicule récupéré sur le ticket.
	 * 
	 * Le priceTicket est le prix du ticket calculé à partir de la durée et du tarif
	 * à la minute. Le priceTicketConvert est le prix arrondi au centième. On initie
	 * le prix du ticket avec le priceTicketConvert.
	 * 
	 * 
	 */
	else {

	    switch (ticket.getParkingSpot().getParkingType()) {

	    case CAR: {
		priceTicket = duration * Fare.CAR_RATE_PER_MINUTES;
		priceTicketConvert = (double) Math.round(priceTicket * 100) / 100;
		ticket.setPrice(priceTicketConvert);
		break;
	    }
	    case BIKE: {
		priceTicket = duration * Fare.BIKE_RATE_PER_MINUTES;
		priceTicketConvert = (double) Math.round(priceTicket * 100) / 100;
		ticket.setPrice(priceTicketConvert);
		break;
	    }
	    default:
		throw new NullPointerException("Unkown Parking Type");
	    }
	}
    }
}
