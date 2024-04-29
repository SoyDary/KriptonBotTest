package me.Dary.KriptonBotBungee.Managers.storage;

import java.sql.*;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import me.Dary.KriptonBotBungee.KriptonBot;

public class SQLConnection {
	
	KriptonBot plugin = KriptonBot.getInstance();
	
    public Connection connection;
    private String host;
    private String database;
    private String username;
    private String password;
    private long connectionTime = System.currentTimeMillis();;
    private HikariDataSource dataSource;


    public SQLConnection(String host, String database, String username, String password) {
        this.host = host;
        this.database = database;
        this.username = username;
        this.password = password;   
        connect();
    }
    
    public Connection getConnection() {
    	return connection;
    }

    public void connect() {
    	try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:mysql://" + host + "/" + database + "?autoReconnect=true");
            config.setUsername(username);
            config.setPassword(password);
            config.setMinimumIdle(1);
            config.setMaximumPoolSize(3);
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.addDataSourceProperty("useServerPrepStmts", "true");
            dataSource = new HikariDataSource(config);
            if (dataSource != null) {
            	connection = dataSource.getConnection();
                plugin.getLogger().info("Base de datos conectada en " + (System.currentTimeMillis() - connectionTime) + "ms");     
                checkTables();
            }
    	} catch (SQLException e) {
    		plugin.getLogger().severe("Hubo un error al conectar la base de datos.");
    		e.printStackTrace();
    	}
    }
    
    public void disconnect() {
        if (dataSource != null) {
        	try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
        	dataSource.close();        
        }
    }

    public void checkTables() {
        try  {
            Statement statement = connection.createStatement();
            statement.executeUpdate(
            		"CREATE TABLE IF NOT EXISTS roles " +
                    "(ID INT(11) NOT NULL AUTO_INCREMENT, " +
                    "UserID BIGINT(20), " +
                    "RoleID BIGINT(20), " +
                    "ExpirationDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "PRIMARY KEY (ID))");
            statement.executeUpdate(
            	    "CREATE TABLE IF NOT EXISTS levels " +
            	    "(ID BIGINT(20) PRIMARY KEY, " +
            	    "Messages INT(11), " +
            	    "Level INT(11), " +
            	    "Experience INT(11), " +
            	    "Color VARCHAR(16), " +
            	    "Background VARCHAR(255), " +
            	    "LevelNotifications BOOLEAN DEFAULT true)");
            statement.executeUpdate(
            		"CREATE TABLE IF NOT EXISTS commands " +
            		"(Name varchar(16) NOT NULL PRIMARY KEY, " +
                    "ID BIGINT UNSIGNED)");
            statement.executeUpdate(
            		"CREATE TABLE IF NOT EXISTS accounts " +
            		"(ID BIGINT(20) UNSIGNED NOT NULL PRIMARY KEY, " +
                    "Name VARCHAR(16) NOT NULL, " +
            		"UUID VARCHAR(16) NOT NULL)");                    
            statement.executeUpdate(
            		"CREATE TABLE IF NOT EXISTS interactions " +
            		"(Hash VARCHAR(75) NOT NULL PRIMARY KEY, " +
            		"Response TEXT, " +
            		"Functions TEXT, " +
            		"Timestamp TIMESTAMP default CURRENT_TIMESTAMP," +
            		"Used TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
            		"Ephemeral BOOLEAN DEFAULT FALSE)");
        } catch (SQLException e) {
        	plugin.getLogger().severe("Hubo un error al preparar tablas de la base de datos.");
            e.printStackTrace();
        }
    }
    
    public void setData(String table, String column1, String value) throws SQLException {
        String query = "INSERT INTO " + table + " (" + column1 + ") VALUES (?)";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setString(1, value);
        statement.executeUpdate();
    }
    
    public void setData(String table, String column1, String value1, String column2, String value2) throws SQLException {
        String query = "INSERT INTO " + table + " (" + column1 + ", " + column2 + ") VALUES (?, ?)";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setString(1, value1);
        statement.setString(2, value2);
        statement.executeUpdate();
    }
    
    public void setData(String table, String column1, String value1, String column2, String value2, String column3, String value3) throws SQLException {
        String query = "INSERT INTO " + table + " (" + column1 + ", " + column2 + ", " + column3 + ") VALUES (?, ?, ?)";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setString(1, value1);
        statement.setString(2, value2);
        statement.setString(3, value3);
        statement.executeUpdate();
    }
    
    public void setData(String table, String column1, String value1, String column2, String value2, String column3, String value3, String column4, String value4) throws SQLException {
        String query = "INSERT INTO " + table + " (" + column1 + ", " + column2 + ", " + column3 + ", " +column4 + ") VALUES (?, ?, ?, ?)";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setString(1, value1);
        statement.setString(2, value2);
        statement.setString(3, value3);
        statement.setString(4, value4);
        statement.executeUpdate();
    }
    

    public ResultSet getData(String table, String column, String value) throws SQLException {
        String query = "SELECT * FROM " + table + " WHERE " + column + "=?";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setString(1, value);
        return statement.executeQuery();
    }
    
    public String getValue(String table, String find, String column, String condition) throws SQLException {
        String query = "SELECT " + find + " FROM " + table + " WHERE " + column + "=?";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setString(1, condition);
        ResultSet resultSet = statement.executeQuery();
        if(resultSet.next()) {
            return resultSet.getString(find);
        } else {
            return "";
        }
    }

    public ResultSet getColumn(String table, String column) throws SQLException {
    	PreparedStatement statement = connection.prepareStatement("SELECT "+column+" FROM "+table);
    	return statement.executeQuery();
    }
    
    public void deleteData(String table, String column, String value) throws SQLException {
        String query = "DELETE FROM " + table + " WHERE " + column + "=?";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setString(1, value);
        statement.executeUpdate();
    }

    public void reloadTable(String table) throws SQLException {
        String query = "SELECT * FROM " + table;
        Statement statement = connection.createStatement();
        statement.executeQuery(query);
    }

}
