package com.parkit.parkingsystem.dao;

import com.parkit.parkingsystem.config.DataBaseConfig;
import com.parkit.parkingsystem.constants.DBConstants;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

/**
 * La classe TicketDAO permet de communiquer avec la base de donnée, enregistrer
 * un ticket, mettre à jour un ticket ainsi que de récupérer les informations
 * d'un ticket.
 * 
 * @author Dylan
 *
 */
public class TicketDAO {

    private static final Logger logger = LogManager.getLogger("TicketDAO");
    /*
     * DataBaseConfig est la configuration qui permet la connection à la base de
     * donnée.
     */
    public DataBaseConfig dataBaseConfig = new DataBaseConfig();

    /**
     * Permet de sauvegarder un ticket dans la base de donnée.
     * 
     * Connection à la base de donnée. PrepareStatement avec la requête SQL
     * save_ticket présente dans la DBConstants. Enregistre le numéro de parking, le
     * numéro d'immatriculation, Une erreur si le ticket ne se sauvegarde pas. Ferme
     * les connections dans le finally afin de s'assurer de l'exécution de
     * celles-ci.
     * 
     * @see DBConstants
     * 
     * @param ticket le ticket de l'utilisateur à sauvegarder.
     * @return le ticket enregistré.
     * @throws Exception si une erreur lors de la sauvegarde du ticket.
     */
    public Ticket saveTicket(Ticket ticket) throws Exception {
	Connection con = null;
	PreparedStatement ps = null;
	try {
	    con = dataBaseConfig.getConnection();
	    ps = con.prepareStatement(DBConstants.SAVE_TICKET);
	    ps.setInt(1, ticket.getParkingSpot().getId());
	    ps.setString(2, ticket.getVehicleRegNumber());
	    ps.setDouble(3, ticket.getPrice());
	    ps.setTimestamp(4, ticket.getInTimestamp());
	    ps.setTimestamp(5,
		    (ticket.getOutTimestamp() == null) ? null : (new Timestamp(ticket.getOutTimestamp().getTime())));
	    ps.execute();
	} catch (Exception ex) {
	    logger.error("Error fetching next available slot", ex);
	} finally {
	    dataBaseConfig.closePreparedStatement(ps);
	    dataBaseConfig.closeConnection(con);
	}
	return ticket;
    }

    /**
     * Permet de mettre à jour un ticket.
     * 
     * Connection à la base de donnée. PrepareStatement avec la requête SQL
     * update_ticket présente dans la DBConstants Mets à jour le prix du ticket.
     * Mets à jour le temps de sortie du véhicule. Prends en condition le numéro
     * d'immatriculation. Execute la mise à jour. Une erreur si la mise à jour
     * échoue. Ferme les connections dans le finally afin de s'assurer de
     * l'exécutions de celles-ci. Si les deux informations sont bien à jour alors
     * true est indiqué, sinon il renvoie false pour indiquer que la mise à jour ne
     * s'est pas faite.
     * 
     *
     * @see DBConstants
     * 
     * @param ticket le ticket de l'utilisateur retrouvé en base de donnée avec le
     *               numéro d'immatriculation.
     * @return le ticket mise à jour.
     * @throws Exception si une erreur est rencontrée lors de la mise à jour du
     *                   ticket.
     */
    public boolean updateTicket(Ticket ticket) throws Exception {
	Connection con = null;
	PreparedStatement ps = null;
	int result = 0;
	try {
	    con = dataBaseConfig.getConnection();
	    ps = con.prepareStatement(DBConstants.UPDATE_TICKET);
	    ps.setDouble(1, ticket.getPrice());
	    ps.setTimestamp(2, ticket.getOutTimestamp());
	    ps.setString(3, ticket.getVehicleRegNumber());
	    ps.executeUpdate();
	    result = ps.executeUpdate();
	} catch (Exception ex) {
	    logger.error("Error saving ticket info", ex);
	} finally {
	    dataBaseConfig.closePreparedStatement(ps);
	    dataBaseConfig.closeConnection(con);
	}
	if (result == 2) {
	    return true;
	} else {
	    return false;
	}
    }

    /**
     * Permet de récuperer un ticket. Connection à la base de donnée.
     * 
     * PrepareStatement avec la requête SQL get_ticket présente dans la DBConstants.
     * Prépare la requête de recherche avec le numéro d'immatriculation. Récupère
     * les informations du ticket lié au numéro d'immatriculation. Indique sur le
     * ticket les informations du parking avec le numéro et le type de véhicule, la
     * plaque d'immatriculation, le prix à payer, le temps d'entrée ainsi que le
     * temps de sortie. Une erreur si le ticket n'est pas trouvé. Ferme les
     * connections dans le finally afin de s'assurer de l'exécution de celles-ci.
     * 
     * @see DBConstants
     * 
     * @param vehicleRegNumber le numéro d'immatriculation du véhicule.
     * @return le ticket avec toutes les informations.
     * @throws Exception si une erreur est rencontrée lors de la recherche du
     *                   ticket.
     */
    public Ticket getTicket(String vehicleRegNumber) throws Exception {
	Connection con = null;
	PreparedStatement ps = null;
	ResultSet rs = null;
	Ticket ticket = new Ticket();
	try {
	    con = dataBaseConfig.getConnection();
	    ps = con.prepareStatement(DBConstants.GET_TICKET);
	    ps.setString(1, vehicleRegNumber);
	    rs = ps.executeQuery();
	    if (rs.next()) {
		ParkingSpot parkingSpot = new ParkingSpot(rs.getInt(2), ParkingType.valueOf(rs.getString(6)), false);
		ticket.setParkingSpot(parkingSpot);
		ticket.setVehicleRegNumber(vehicleRegNumber);
		ticket.setPrice(rs.getDouble(3));
		ticket.setInTimestamp(rs.getTimestamp(4));
		ticket.setOutTimestamp(rs.getTimestamp(5));
	    }
	} catch (Exception ex) {
	    logger.error("Error unable to retrieve the ticket corresponding to the registration number", ex);
	} finally {
	    dataBaseConfig.closeResultSet(rs);
	    dataBaseConfig.closePreparedStatement(ps);
	    dataBaseConfig.closeConnection(con);
	}
	return ticket;
    }
}