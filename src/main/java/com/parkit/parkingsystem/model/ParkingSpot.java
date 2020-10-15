package com.parkit.parkingsystem.model;

import com.parkit.parkingsystem.constants.ParkingType;

/**
 * La classe ParkingSpot permet d'indiquer le numéro de parking, le type de
 * véhicule et la disponibilité de cet emplacement.
 * 
 * @author Dylan
 *
 */
public class ParkingSpot {
    private int number;
    private ParkingType parkingType;
    private boolean isAvailable;

    /**
     * 
     * @param number      le numéro de parking.
     * @param parkingType le type d'emplacement de parking, si c'est pour une
     *                    voiture ou moto.
     * @param isAvailable si l'emplacement est disponible.
     */
    public ParkingSpot(int number, ParkingType parkingType, boolean isAvailable) {
	this.number = number;
	this.parkingType = parkingType;
	this.isAvailable = isAvailable;
    }

    /**
     * Récupère le numéro de parking.
     * 
     * @return le numéro de parking.
     */
    public int getId() {
	return number;
    }

    /**
     * Initie le numéro de parking.
     * 
     * @param number le numéro de parking.
     */
    public void setId(int number) {
	this.number = number;
    }

    /**
     * Récupère le type d'emplacement du parking.
     * 
     * @return le type d'emplacement du parking.
     */
    public ParkingType getParkingType() {
	return parkingType;
    }

    /**
     * Initie le type d'emplacement du parking.
     * 
     * @param parkingType le type d'emplacement du parking.
     */
    public void setParkingType(ParkingType parkingType) {
	this.parkingType = parkingType;
    }

    /**
     * Récupère la disponibilité du numéro de parking.
     * 
     * @return la disponibilité du numéro de parking.
     */
    public boolean isAvailable() {
	return isAvailable;
    }

    /**
     * Initie la disponibilité du numéro de parking.
     * 
     * @param available la disponibilité du numéro de parking.
     */
    public void setAvailable(boolean available) {
	isAvailable = available;
    }

    @Override
    public boolean equals(Object o) {
	if (this == o) {
	    return true;
	}
	if (o == null || getClass() != o.getClass()) {
	    return false;
	}
	ParkingSpot that = (ParkingSpot) o;
	return number == that.number;
    }

    @Override
    public int hashCode() {
	return number;
    }
}
