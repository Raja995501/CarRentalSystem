
        package org.rain;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

class Car {
    private String carId;
    private String brand;
    private String model;
    private double basePricePerDay;
    private boolean isAvailable;

    public Car(String carId, String brand, String model, double basePricePerDay) {
        this.carId = carId;
        this.brand = brand;
        this.model = model;
        this.basePricePerDay = basePricePerDay;
        this.isAvailable = true;
    }

    public String getCarId() {
        return carId;
    }

    public String getBrand() {
        return brand;
    }

    public String getModel() {
        return model;
    }

    public double calculatePrice(int rentalDays) {
        return basePricePerDay * rentalDays;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void rent() {
        isAvailable = false;
    }

    public void returnCar() {
        isAvailable = true;
    }
}

class Customer {
    private String customerId;
    private String name;

    public Customer(String customerId, String name) {
        this.customerId = customerId;
        this.name = name;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getName() {
        return name;
    }
}

class CarRentalSystem {
    private Connection connection;

    public CarRentalSystem() {
        try {
            connection = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/carRentalSystem", "root", "Raja@9955"
            );
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addCar(Car car) {
        try {
            String query = "INSERT INTO cars (car_id, brand, model, base_price_per_day, is_available) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, car.getCarId());
            stmt.setString(2, car.getBrand());
            stmt.setString(3, car.getModel());
            stmt.setDouble(4, car.calculatePrice(1));
            stmt.setBoolean(5, car.isAvailable());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addCustomer(Customer customer) {
        try {
            String query = "INSERT INTO customers (customer_id, name) VALUES (?, ?)";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, customer.getCustomerId());
            stmt.setString(2, customer.getName());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void rentCar(Car car, Customer customer, int days) {
        if (car.isAvailable()) {
            try {
                car.rent();
                String updateCarQuery = "UPDATE cars SET is_available = ? WHERE car_id = ?";
                PreparedStatement updateStmt = connection.prepareStatement(updateCarQuery);
                updateStmt.setBoolean(1, false);
                updateStmt.setString(2, car.getCarId());
                updateStmt.executeUpdate();

                String rentalQuery = "INSERT INTO rentals (car_id, customer_id, days) VALUES (?, ?, ?)";
                PreparedStatement rentalStmt = connection.prepareStatement(rentalQuery);
                rentalStmt.setString(1, car.getCarId());
                rentalStmt.setString(2, customer.getCustomerId());
                rentalStmt.setInt(3, days);
                rentalStmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Car is not available for rent.");
        }
    }

    public void returnCar(Car car) {
        try {
            String updateCarQuery = "UPDATE cars SET is_available = ? WHERE car_id = ?";
            PreparedStatement updateStmt = connection.prepareStatement(updateCarQuery);
            updateStmt.setBoolean(1, true);
            updateStmt.setString(2, car.getCarId());
            updateStmt.executeUpdate();

            String deleteRentalQuery = "DELETE FROM rentals WHERE car_id = ?";
            PreparedStatement deleteStmt = connection.prepareStatement(deleteRentalQuery);
            deleteStmt.setString(1, car.getCarId());
            deleteStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Car> getCars() {
        List<Car> cars = new ArrayList<>();
        try {
            String query = "SELECT * FROM cars";
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                Car car = new Car(
                        rs.getString("car_id"),
                        rs.getString("brand"),
                        rs.getString("model"),
                        rs.getDouble("base_price_per_day")
                );
                if (!rs.getBoolean("is_available")) {
                    car.rent();
                }
                cars.add(car);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cars;
    }

    public Car getCarById(String carId) {
        Car car = null;
        try {
            String query = "SELECT * FROM cars WHERE car_id = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, carId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                car = new Car(
                        rs.getString("car_id"),
                        rs.getString("brand"),
                        rs.getString("model"),
                        rs.getDouble("base_price_per_day")
                );
                if (!rs.getBoolean("is_available")) {
                    car.rent();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return car;
    }

    public void menu() {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("===== Car Rental System =====");
            System.out.println("1. Rent a Car");
            System.out.println("2. Return a Car");
            System.out.println("3. Exit");
            System.out.print("Enter your choice: ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            if (choice == 1) {
                System.out.println("\n== Rent a Car ==\n");
                System.out.print("Enter your name: ");
                String customerName = scanner.nextLine();

                System.out.println("\nAvailable Cars:");
                List<Car> cars = getCars();
                for (Car car : cars) {
                    if (car.isAvailable()) {
                        System.out.println(car.getCarId() + " - " + car.getBrand() + " " + car.getModel());
                    }
                }

                System.out.print("\nEnter the car ID you want to rent: ");
                String carId = scanner.nextLine();

                System.out.print("Enter the number of days for rental: ");
                int rentalDays = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                Customer newCustomer = new Customer("CUS" + (int)(Math.random() * 10000), customerName);  // Generating a random customer ID
                addCustomer(newCustomer);

                Car selectedCar = getCarById(carId);

                if (selectedCar != null && selectedCar.isAvailable()) {
                    double totalPrice = selectedCar.calculatePrice(rentalDays);
                    System.out.println("\n== Rental Information ==\n");
                    System.out.println("Customer ID: " + newCustomer.getCustomerId());
                    System.out.println("Customer Name: " + newCustomer.getName());
                    System.out.println("Car: " + selectedCar.getBrand() + " " + selectedCar.getModel());
                    System.out.println("Rental Days: " + rentalDays);
                    System.out.printf("Total Price: $%.2f%n", totalPrice);

                    System.out.print("\nConfirm rental (Y/N): ");
                    String confirm = scanner.nextLine();

                    if (confirm.equalsIgnoreCase("Y")) {
                        rentCar(selectedCar, newCustomer, rentalDays);
                        System.out.println("\nCar rented successfully.");
                    } else {
                        System.out.println("\nRental canceled.");
                    }
                } else {
                    System.out.println("\nInvalid car selection or car not available for rent.");
                }
            } else if (choice == 2) {
                System.out.println("\n== Return a Car ==\n");
                System.out.print("Enter the car ID you want to return: ");
                String carId = scanner.nextLine();

                Car carToReturn = getCarById(carId);

                if (carToReturn != null && !carToReturn.isAvailable()) {
                    returnCar(carToReturn);
                    System.out.println("Car returned successfully.");
                } else {
                    System.out.println("Invalid car ID or car is not rented.");
                }
            } else if (choice == 3) {
                break;
            } else {
                System.out.println("Invalid choice. Please enter a valid option.");
            }
        }

        System.out.println("\nThank you for using the Car Rental System!");
    }
}

public class Main {
    public static void main(String[] args) {
        CarRentalSystem rentalSystem = new CarRentalSystem();
        Car car1 = new Car("C001", "Toyota", "Camry", 60.0);
        Car car2 = new Car("C002", "Honda", "Accord", 70.0);
        Car car3 = new Car("C003", "Mahindra", "Thar", 150.0);
        rentalSystem.addCar(car1);
        rentalSystem.addCar(car2);
        rentalSystem.addCar(car3);

        rentalSystem.menu();


    }
}
