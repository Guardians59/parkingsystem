package com.parkit.parkingsystem.model;

import java.sql.Timestamp;

/**
 * La classe Ticket permet d'indiquer l'ID du ticket, le numéro de parking avec
 * son type d'emplacement, la plaque d'immatriculation du véhicule, le prix à
 * payer, le temps d'entrée dans le parking ainsi que le temps de sortie.
 * 
 * @author Dylan
 *
 */
public class Ticket {
    private int id;
    private ParkingSpot parkingSpot;
    private String vehicleRegNumber;
    private double price;
    private Timestamp inTimestamp;
    private Timestamp outTimestamp;
    

   

    /**
     * Récupère l'ID du ticket.
     * 
     * @return l'ID du ticket.
     */
    public int getId() {
	return id;
    }

    /**
     * Initie l'ID du ticket.
     * 
     * @param id l'ID du ticket.
     */
    public void setId(int id) {
	this.id = id;
    }

    /**
     * Récupère le parkingSpot avec le numéro de parking, son type d'emplacement
     * ainsi que sa disponibilité.
     * 
     * @return le parkingSpot.
     * @see ParkingSpot
     */
    public ParkingSpot getParkingSpot() {
	return parkingSpot;
    }

    /**
     * Initie le parkingSpot avec le numéro de parking, son type d'emplacement ainsi
     * que sa disponibilité.
     * 
     * @param parkingSpot le parkingSpot.
     * @see ParkingSpot
     */
    public void setParkingSpot(ParkingSpot parkingSpot) {
	this.parkingSpot = parkingSpot;
    }

    /**
     * Récupère le numéro d'immatriculation du véhicule.
     * 
     * @return le numéro d'immatriculation.
     */
    public String getVehicleRegNumber() {
	return vehicleRegNumber;
    }

    /**
     * Initie le numéro d'immatriculation du véhicule.
     * 
     * @param vehicleRegNumber le numéro d'immatriculation du véhicule.
     */
    public void setVehicleRegNumber(String vehicleRegNumber) {
	this.vehicleRegNumber = vehicleRegNumber;
    }

    /**
     * Récupère le prix du ticket.
     * 
     * @return le prix du ticket.
     */
    public double getPrice() {
	return price;
    }

    /**
     * Initie le prix du ticket.
     * 
     * @param price le prix du ticket.
     */
    public void setPrice(double price) {
	this.price = price;
    }

    /**
     * Récupère le temps d'entrée dans le parking.
     * 
     * @return le temps d'entrée le parking.
     */
    public Timestamp getInTimestamp() {
	return inTimestamp;
    }

    /**
     * Initie le temps d'entrée dans le parking.
     * 
     * @param inTimestamp le temps d'entrée dans le parking.
     */
    public void setInTimestamp(Timestamp inTimestamp) {
	this.inTimestamp = inTimestamp;
    }

    /**
     * Récupère le temps de sortie du parking.
     * 
     * @return le temps de sortie du parking.
     */
    public Timestamp getOutTimestamp() {
	return outTimestamp;
    }

    /**
     * Initie le temps de sortie du parking.
     * 
     * @param outTimestamp le temps de sortie du parking.
     */
    public void setOutTimestamp(Timestamp outTimestamp) {
	this.outTimestamp = outTimestamp;
    }
    
}