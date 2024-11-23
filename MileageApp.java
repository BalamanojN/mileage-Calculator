import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

// Unit-II: Inheritance, Packages, and Interfaces
interface MileageCalculator {
    double calculateMileage(double distance, double fuelConsumed);
}

abstract class Vehicle {
    String model;
    int year;

    // Constructor
    public Vehicle(String model, int year) {
        this.model = model;
        this.year = year;
    }

    // Abstract method
    public abstract String vehicleType();
}

class Car extends Vehicle implements MileageCalculator {
    // Constructor using super
    public Car(String model, int year) {
        super(model, year);
    }

    // Overriding abstract method
    @Override
    public String vehicleType() {
        return "Car";
    }

    // Implementing interface method
    @Override
    public double calculateMileage(double distance, double fuelConsumed) {
        return distance / fuelConsumed;
    }
}

// Unit-V: Database Connectivity
class MileageDatabaseHandler {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/mileage_db";
    private static final String DB_USER = "root"; // Replace with your MySQL username
    private static final String DB_PASSWORD = ""; // Replace with your MySQL password

    // Method to initialize database
    public static void initializeDatabase() throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            if (conn != null) {
                String createTableSQL = """
                    CREATE TABLE IF NOT EXISTS VehicleMileage (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        model VARCHAR(50) NOT NULL,
                        year INT NOT NULL,
                        mileage DOUBLE NOT NULL
                    );
                """;
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(createTableSQL);
                }
            }
        }
    }

    // Method to insert mileage data
    public static void insertMileage(String model, int year, double mileage) throws SQLException {
        String insertSQL = "INSERT INTO VehicleMileage (model, year, mileage) VALUES (?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
            pstmt.setString(1, model);
            pstmt.setInt(2, year);
            pstmt.setDouble(3, mileage);
            pstmt.executeUpdate();
        }
    }

    // Method to retrieve mileage data
    public static List<String> getMileageData() throws SQLException {
        String selectSQL = "SELECT model, year, mileage FROM VehicleMileage";
        List<String> data = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(selectSQL)) {
            while (rs.next()) {
                String record = String.format("Model: %s, Year: %d, Mileage: %.2f km/l",
                        rs.getString("model"), rs.getInt("year"), rs.getDouble("mileage"));
                data.add(record);
            }
        }
        return data;
    }
}

public class MileageApp {
    public static void main(String[] args) {
        try {
            // Initialize database
            MileageDatabaseHandler.initializeDatabase();

            // Using a Collection to store Vehicles
            List<Vehicle> vehicles = new ArrayList<>();
            vehicles.add(new Car("Toyota Corolla", 2020));
            vehicles.add(new Car("Honda Civic", 2021));

            // Input data
            Scanner scanner = new Scanner(System.in);
            System.out.println("Enter distance traveled (in km):");
            double distance = scanner.nextDouble();

            System.out.println("Enter fuel consumed (in liters):");
            double fuelConsumed = scanner.nextDouble();

            for (Vehicle vehicle : vehicles) {
                double mileage = ((MileageCalculator) vehicle).calculateMileage(distance, fuelConsumed);

                if (mileage < 0) {
                    throw new IllegalArgumentException("Mileage cannot be negative!");
                }

                System.out.println(vehicle.vehicleType() + " Mileage: " + mileage + " km/l");

                // Save to database
                MileageDatabaseHandler.insertMileage(vehicle.model, vehicle.year, mileage);
            }

            // Retrieve and display mileage data from database
            List<String> mileageData = MileageDatabaseHandler.getMileageData();
            System.out.println("\nStored Mileage Data:");
            mileageData.forEach(System.out::println);

            // Using Streams to sort vehicles by model
            List<String> sortedModels = vehicles.stream()
                    .map(v -> v.model)
                    .sorted()
                    .collect(Collectors.toList());
            System.out.println("\nSorted vehicle models: " + sortedModels);

        } catch (SQLException e) {
            System.err.println("Database Error: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
        } finally {
            System.out.println("\nMileage calculation completed.");
        }
    }
}