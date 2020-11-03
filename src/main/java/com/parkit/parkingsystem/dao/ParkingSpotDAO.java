package com.parkit.parkingsystem.dao;

import com.parkit.parkingsystem.config.DataBaseConfig;
import com.parkit.parkingsystem.constants.DBConstants;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * La classe ParkingSpotDAO permet de communiquer avec la base de donnée,
 * vérifier et mettre à jour la disponibilité des places de parking.
 * 
 * @author Dylan
 *
 */
public class ParkingSpotDAO {
    private static final Logger logger = LogManager.getLogger("ParkingSpotDAO");
    /*
     * DataBaseConfig est la configuration qui permet la connection à la base de
     * donnée.
     */
    public DataBaseConfig dataBaseConfig = new DataBaseConfig();

    /**
     * Permet de vérifier si il y a une place de parking disponible, pour le type de
     * véhicule spécifié par l'utilisateur.
     * 
     * Connexion à la base de donnée. PrepareStatement avec la requête SQL
     * next_parking_slot présente dans la DBConstants.
     * 
     * @see DBConstants
     * 
     * @param parkingType le type de véhicule voulant stationner.
     * @return result le numéro de la place disponible.
     * @throws Exception si une erreur est rencontrée lors de la verification dans
     *                   la base de donnée.
     */
    public int getNextAvailableSlot(ParkingType parkingType) throws Exception {
	Connection con = null;
	PreparedStatement ps = null;
	ResultSet rs = null;
	int result = -1;
	try {
	    con = dataBaseConfig.getConnection();
	    ps = con.prepareStatement(DBConstants.GET_NEXT_PARKING_SPOT);
	    ps.setString(1, parkingType.toString());
	    rs = ps.executeQuery();
	    if (rs.next()) {
		result = rs.getInt(1);
	    }

	} catch (Exception ex) {
	    logger.error("Error fetching next available slot", ex);
	    throw ex;
	} finally {
	    dataBaseConfig.closeResultSet(rs);
	    dataBaseConfig.closePreparedStatement(ps);
	    dataBaseConfig.closeConnection(con);
	}
	return result;
    }

    /**
     * Permet de mettre à jour la disponibilité de la place de parking dans la base
     * de donnée.
     * 
     * Connexion à la base donnée. PrepareStatement avec la requête SQL
     * update_parking_spot présente dans DBConstants.
     * 
     * @see DBConstants
     * 
     * @param parkingSpot les informations de la place de parking avec le numéro et
     *                    le type de véhicule.
     * @return true si les informations ont bien été mis à jour, else dans le cas
     *         contraire.
     * @throws Exception si une erreur est rencontrée lors de la mise à jour.
     */
    public boolean updateParking(ParkingSpot parkingSpot) throws Exception {

	Connection con = null;
	PreparedStatement ps = null;
	
	try {
	    con = dataBaseConfig.getConnection();
	    ps = con.prepareStatement(DBConstants.UPDATE_PARKING_SPOT);
	    ps.setBoolean(1, parkingSpot.isAvailable());
	    ps.setInt(2, parkingSpot.getId());
	    ps.executeUpdate();
	    
	    return true;
	} catch (Exception ex) {
	    logger.error("Error updating parking info", ex);
	    return false;
	} finally {
	    dataBaseConfig.closePreparedStatement(ps);
	    dataBaseConfig.closeConnection(con);
	}
    }
}
