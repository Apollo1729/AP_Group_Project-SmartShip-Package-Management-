package model;

import java.io.Serializable;

public class Customer extends User implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private String customerId;
	
	
    
    public Customer(String customerId, String userId, String username, String password, String email, String phone, String address, String role ) {
    	super( userId,  username, email, phone,  address, role);
    	this.customerId =customerId;
    }



	@Override
	public String toString() {
		return "Customer [customerId=" + customerId + ", getUserId()=" + getUserId() + ", getUsername()="
				+ getUsername() + ", getPassword()=" + getPassword() + ", getEmail()=" + getEmail() + ", getPhone()="
				+ getPhone() + ", getAddress()=" + getAddress() + ", getRole()=" + getRole() + ", toString()="
				+ super.toString() + ", getClass()=" + getClass() + ", hashCode()=" + hashCode() + "]";
	}



	public String getCustomerId() {
		return customerId;
	}



	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}
}
