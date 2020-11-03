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
     * Permet de récuperer un ticket. Connection à la base de donnée.
     * 
     * Connexion à la base de donnée. PrepareStatement avec la requête SQL
     * get_ticket présente dans la DBConstants. Prépare la requête de recherche avec
     * le numéro d'immatriculation. Récupère les informations du ticket lié au
     * numéro d'immatriculation. Indique sur le ticket les informations du parking
     * avec le numéro et le type de véhicule, l'ID généré par la base de donnée, la
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
	    ps = con.prepareStatement(DBConstants.GET_TICKET, ResultSet.TYPE_SCROLL_SENSITIVE,
		    ResultSet.CONCUR_READ_ONLY);
	    ps.setString(1, vehicleRegNumber);
	    rs = ps.executeQuery();
	    if (rs.last()) {
		ParkingSpot parkingSpot = new ParkingSpot(rs.getInt(2), ParkingType.valueOf(rs.getString(6)), false);
		ticket.setId(rs.getInt(1));
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
	try {
	    con = dataBaseConfig.getConnection();
	    ps = con.prepareStatement(DBConstants.UPDATE_TICKET);
	    ps.setDouble(1, ticket.getPrice());
	    ps.setTimestamp(2, ticket.getOutTimestamp());
	    ps.setInt(3, ticket.getId());
	    ps.executeUpdate();
	    return true;
	} catch (Exception ex) {
	    logger.error("Error saving ticket info", ex);
	    return false;
	} finally {
	    dataBaseConfig.closePreparedStatement(ps);
	    dataBaseConfig.closeConnection(con);
	}

    }

    /**
     * Permet de vérifier si l'utilisateur est présent en base de donnée, via son
     * numéro d'immatriculation.
     * 
     * Connexion à la base de donnée. PreparedStatement avec la requête SQL
     * GET_VEHICLE_REG_NUMBER présente dans la DBConstants. Prépare la requête de
     * recherche avec le numéro d'immatriculation. Récupère le nombre de ligne
     * correspondante au nombre de ticket enregistré avec le numéro
     * d'immatriculation. Si il y a au moins une ligne de trouvée cela renvoie true
     * pour confirmer que l'utilisateur est bien présent. Une erreur si la recherche
     * échoue. Ferme les connections dans le finally afin de s'assurer de
     * l'exécution de celles-ci.
     * 
     * @see DBConstants
     * 
     * @param vehicleRegNumber le numéro d'immatriculation de l'utilisateur.
     * @return result true si l'utilisateur est déjà venue, false si c'est la
     *         première fois.
     * @throws Exception si une erreur est rencontrée lors de la recherche.
     */

    public boolean getTicketUserPresentInDB(String vehicleRegNumber) throws Exception {
	Connection con = null;
	PreparedStatement ps = null;
	ResultSet rs = null;
	boolean result = false;
	int numberRow = -1;
	try {
	    con = dataBaseConfig.getConnection();
	    ps = con.prepareStatement(DBConstants.GET_VEHICLE_REG_NUMBER);
	    ps.setString(1, vehicleRegNumber);
	    rs = ps.executeQuery();

	    while (rs.next()) {
		numberRow = rs.getInt("count(*)");
	    }

	    if (numberRow >= 1) {
		result = true;
	    }

	} catch (Exception ex) {
	    logger.error("Error when looking for an old ticket", ex);

	} finally {
	    dataBaseConfig.closeResultSet(rs);
	    dataBaseConfig.closePreparedStatement(ps);
	    dataBaseConfig.closeConnection(con);
	}
	return result;

    }
}
