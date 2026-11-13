/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cafeteriaapp;

/**
 *
 * @author DELL
 */
public class User {

    private int id;
    private String firstName;
    private String lastName;
    private String email;
    private double accountBalance;
    private String role; // "customer" or "manager"

    public User(int id, String firstName, String lastName, String email, double accountBalance, String role) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.accountBalance = accountBalance;
        this.role = role;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public double getAccountBalance() { return accountBalance; }
    public void setAccountBalance(double accountBalance) { this.accountBalance = accountBalance; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isManager() { return "manager".equalsIgnoreCase(role); }
}
