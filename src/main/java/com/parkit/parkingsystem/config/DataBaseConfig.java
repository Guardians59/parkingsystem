package com.parkit.parkingsystem.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;

/**
 * La classe DataBaseConfig configure la connection à la base de donnée.
 * 
 * @author Dylan
 *
 */
public class DataBaseConfig {

    private static final Logger logger = LogManager.getLogger("DataBaseConfig");

    /**
     * Permet la connection à la base de donnée, avec l'url de la base de donnée, le
     * nom d'utilisateur et le mot de passe.
     * 
     * @return la connexion à la base de donnée.
     * @throws ClassNotFoundException si le chemin de la classe est introuvable.
     * @throws SQLException           si la liaison avec la base de donnée échoue.
     */
    public Connection getConnection() throws ClassNotFoundException, SQLException {
	logger.info("Create DB connection");
	Class.forName("com.mysql.cj.jdbc.Driver");
	return DriverManager.getConnection("jdbc:mysql://localhost:3306/prod?useTimezone=true&serverTimezone=UTC", "root", "rootroot");
    }

    /**
     * Permet de fermer la connection à la base de donnée.
     * 
     * @param con la connexion à fermer.
     * @throws SQLException si la liaison avec la base de donnée pour fermer la
     *                      connexion échoue.
     */
    public void closeConnection(Connection con) throws SQLException {
	if (con != null) {
	    try {
		con.close();
		logger.info("Closing DB connection");
	    } catch (SQLException e) {
		logger.error("Error while closing connection", e);
	    }
	}
    }

    /**
     * Permet de fermer le PreparedStatement.
     * 
     * @param ps la requête SQL paramétrée qui à été exécutée.
     */
    public void closePreparedStatement(PreparedStatement ps) {
	if (ps != null) {
	    try {
		ps.close();
		logger.info("Closing Prepared Statement");
	    } catch (SQLException e) {
		logger.error("Error while closing prepared statement", e);
	    }
	}
    }

    /**
     * Permet de fermer le ResultSet.
     * 
     * @param rs le résultat de la requête SQL appelée.
     * @throws SQLException si la connexion à la base de donnée échoue.
     */
    public void closeResultSet(ResultSet rs) throws SQLException {
	if (rs != null) {
	    try {
		rs.close();
		logger.info("Closing Result Set");
	    } catch (SQLException e) {
		logger.error("Error while closing result set", e);
	    }
	}
    }
}
