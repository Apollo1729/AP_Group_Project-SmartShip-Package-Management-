package model;

import java.io.Serializable;
import java.util.Map;

/**
 * User model class representing an employee in the system
 */
public class User implements Serializable{
    private static final long serialVersionUID = 1L;
	private int userId;
    private String username;
    private String email;
    private String phone;
    private String address;
    private String role;
    
    // Additional fields that might be populated from database
    private String department;
    private String employeeId;
    private String hireDate;
    
    public User() {}
    
    /**
     * Constructor that takes a map from the database
     */
    public User(Map<String, String> userInfo) {
        if (userInfo != null) {
            this.userId = Integer.parseInt(userInfo.getOrDefault("userId", "0"));
            this.username = userInfo.get("username");
            this.email = userInfo.get("email");
            this.phone = userInfo.get("phone");
            this.address = userInfo.get("address");
            this.role = userInfo.get("role");
            
            // Optional fields
            this.department = userInfo.get("department");
            this.employeeId = userInfo.get("employeeId");
            this.hireDate = userInfo.get("hireDate");
        }
    }
    
    // Getters and Setters
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    public String getDepartment() {
        return department;
    }
    
    public void setDepartment(String department) {
        this.department = department;
    }
    
    public String getEmployeeId() {
        return employeeId;
    }
    
    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }
    
    public String getHireDate() {
        return hireDate;
    }
    
    public void setHireDate(String hireDate) {
        this.hireDate = hireDate;
    }
    
    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", role='" + role + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}