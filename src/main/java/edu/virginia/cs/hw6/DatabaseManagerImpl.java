package edu.virginia.cs.hw6;

import java.sql.*;
import java.util.*;

public class DatabaseManagerImpl implements DatabaseManager {
    private Connection connection;
    String url = "jdbc:sqlite:bus_stops.sqlite3";

    public static void main(String[] args){
        DatabaseManagerImpl db = new DatabaseManagerImpl();
        db.connect();
//        db.createTables();
        ApiStopReader apistop = new ApiStopReader();
        List<Stop> stops = apistop.getStops();
        ApiBusLineReader apibusline = new ApiBusLineReader();
        List<BusLine> busline = apibusline.getBusLines();
        db.addStops(stops);
        db.addBusLines(busline);
//        db.deleteTables();
//        db.clear();
    }
    @Override
    public void connect() {
        try {
            connection = DriverManager.getConnection(url);
            System.out.println("Connection to SQLite has been established.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void createTables() {
        String stopsTable = "CREATE TABLE IF NOT EXISTS Stops (\n"
                + " ID INTEGER PRIMARY KEY,\n"
                + " Name VARCHAR(50) NOT NULL,\n"
                + " Latitude DOUBLE NOT NULL,\n"
                + " Longitude DOUBLE NOT NULL\n"
                + ");";

        String busLinesTable = "CREATE TABLE IF NOT EXISTS BusLines (\n"
                + " ID INTEGER PRIMARY KEY,\n"
                + " IsActive BOOLEAN NOT NULL,\n"
                + " LongName VARCHAR(100) NOT NULL,\n"
                + " ShortName VARCHAR(50) NOT NULL\n"
                + ");";

        String routesTable = "CREATE TABLE IF NOT EXISTS Routes (\n"
                + " ID INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                + " BusLineID INTEGER NOT NULL,\n"
                + " StopID INTEGER NOT NULL,\n"
                + " 'Order' INTEGER NOT NULL,\n"
                + " FOREIGN KEY (BusLineID) REFERENCES BusLines(ID) ON DELETE CASCADE,\n"
                + " FOREIGN KEY (StopID) REFERENCES Stops(ID) ON DELETE CASCADE\n"
                + ");";

        try {
            Statement statement = connection.createStatement();
            if(doesTableExists("Stops")&& doesTableExists("BusLines")&& doesTableExists("Routes")){
                throw new IllegalStateException("ERROR: Tables already created");
            }
            if(!doesTableExists("Stops")){statement.execute(stopsTable);}
            if(!doesTableExists("BusLines")){statement.execute(busLinesTable);}
            if(!doesTableExists("Routes")){statement.execute(routesTable);}
            statement.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    boolean doesTableExists(String tableName) throws SQLException {
        DatabaseMetaData db = connection.getMetaData();
        ResultSet tables = db.getTables(null, null, tableName, null);
        boolean result = tables.next();
        tables.close();
        return result;
    }
    boolean doesManager(){
        try {
            return connection == null || connection.isClosed();
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void clear() {
        try {
            if(doesManager()){throw new IllegalStateException("ERROR: Manager not connected yet");}
            Statement statement = connection.createStatement();
            statement.execute("DELETE FROM Stops;");
            statement.execute("DELETE FROM BusLines;");
            statement.execute("DELETE FROM Routes;");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void deleteTables() {
        try {
            if(doesManager()){throw new IllegalStateException("ERROR: Manager not connected yet");}
            Statement statement = connection.createStatement();
            statement.execute("DROP TABLE IF EXISTS Routes;");
            statement.execute("DROP TABLE IF EXISTS BusLines;");
            statement.execute("DROP TABLE IF EXISTS Stops;");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void addStops(List<Stop> stopList) {
        try {
            if(doesManager()){throw new IllegalStateException("ERROR: Manager not connected yet");}
            PreparedStatement statement = connection.prepareStatement("INSERT INTO Stops (ID, Name, Latitude, Longitude) VALUES (?, ?, ?, ?);");
            for (Stop stop : stopList) {
                statement.setInt(1, stop.getId());
                statement.setString(2, stop.getName());
                statement.setDouble(3, stop.getLatitude());
                statement.setDouble(4, stop.getLongitude());
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public List<Stop> getAllStops() {
        String sql = "SELECT * FROM Stops";
        List<Stop> stops = new ArrayList<>();
        if(doesManager()){throw new IllegalStateException("ERROR: Manager not connected yet");}
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Stop stop = new Stop(
                        rs.getInt("ID"),
                        rs.getString("Name"),
                        rs.getDouble("Latitude"),
                        rs.getDouble("Longitude")
                );
                stops.add(stop);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return stops;
    }

    @Override
    public Stop getStopByID(int id) {
        String sql = "SELECT * FROM Stops WHERE ID = ?";
        Stop stop = null;
        if(doesManager()){throw new IllegalStateException("ERROR: Manager not connected yet");}
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    stop = new Stop(
                            rs.getInt("ID"),
                            rs.getString("Name"),
                            rs.getDouble("Latitude"),
                            rs.getDouble("Longitude")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return stop;
    }

    @Override
    public Stop getStopByName(String substring) {
        String sql = "SELECT * FROM Stops WHERE Name LIKE ?";
        Stop stop = null;
        if(doesManager()){throw new IllegalStateException("ERROR: Manager not connected yet");}
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + substring + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    stop = new Stop(
                            rs.getInt("ID"),
                            rs.getString("Name"),
                            rs.getDouble("Latitude"),
                            rs.getDouble("Longitude")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return stop;
    }

    @Override
    public void addBusLines(List<BusLine> busLineList) {
        String sql = "INSERT INTO BusLines(IsActive, LongName, ShortName) VALUES (?, ?, ?)";
        if(doesManager()){throw new IllegalStateException("ERROR: Manager not connected yet");}
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (BusLine busLine : busLineList) {
                pstmt.setBoolean(1, busLine.isActive());
                pstmt.setString(2, busLine.getLongName());
                pstmt.setString(3, busLine.getShortName());
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<BusLine> getBusLines() {
        String sql = "SELECT * FROM BusLines";
        List<BusLine> busLines = new ArrayList<>();
        if(doesManager()){throw new IllegalStateException("ERROR: Manager not connected yet");}
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                BusLine busLine = new BusLine(
                        rs.getInt("ID"),
                        rs.getBoolean("IsActive"),
                        rs.getString("LongName"),
                        rs.getString("ShortName")
                );
                busLines.add(busLine);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return busLines;
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url);
    }

    @Override
    public BusLine getBusLineById(int id) {
        if(doesManager()){throw new IllegalStateException("ERROR: Manager not connected yet");}
        try {
            Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM BusLines WHERE ID = ?");
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                boolean isActive = rs.getBoolean("IsActive");
                String longName = rs.getString("LongName");
                String shortName = rs.getString("ShortName");
                BusLine busLine = new BusLine(id, isActive, longName, shortName);
                conn.close();
                return busLine;
            }
            conn.close();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return null;
    }

    @Override
    public BusLine getBusLineByLongName(String longName) {
        if(doesManager()){throw new IllegalStateException("ERROR: Manager not connected yet");}
        try {
            Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM BusLines WHERE LongName = ?");
            stmt.setString(1, longName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int id = rs.getInt("ID");
                boolean isActive = rs.getBoolean("IsActive");
                String shortName = rs.getString("ShortName");
                BusLine busLine = new BusLine(id, isActive, longName, shortName);
                conn.close();
                return busLine;
            }
            conn.close();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return null;
    }

    @Override
    public BusLine getBusLineByShortName(String shortName) {
        try {
            Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM BusLines WHERE ShortName = ?");
            stmt.setString(1, shortName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int id = rs.getInt("ID");
                boolean isActive = rs.getBoolean("IsActive");
                String longName = rs.getString("LongName");
                BusLine busLine = new BusLine(id, isActive, longName, shortName);
                conn.close();
                return busLine;
            }
            conn.close();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return null;
    }

    @Override
    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }
}
