package com.java.hibernate;
import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "Clerk") //name of the table in the database
public class Clerk extends User implements Serializable {

	private static final long serialVersionUID = 1L;
	@Id
	@Column(name= "clerkId")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	
	private long clerkId;
	
	//default constructor
	public Clerk() {
		super();
		this.clerkId = 0L;
	}
			
	//primary constructor
	public Clerk(String firstName, String lastName, Date dob, int age, String nationalId, Address address, String telNum, String username, String password, String email, long clerkId) {
		super(firstName,lastName, dob, age, nationalId, address, telNum, username, password, email);
		this.clerkId = clerkId;
	}
	//primary constructor - 2
	public Clerk(String firstName, String lastName, Date dob, int age, String nationalId, Address address, String telNum, String username, String password, String email) {
		super(firstName,lastName, dob, age, nationalId, address, telNum,  username,  password,  email);
	}
			
	//Copy constructor
	public Clerk(Clerk clk) {
		super(clk);
		this.clerkId = clk.clerkId;
	}
	
	//setters and getters
			
	public long getClerkId() {
		return this.clerkId;
	}

	public void setClerkId(int clerkId) {
		this.clerkId = clerkId;
	}

	@Override
	public String toString() {
		return "Clerk ID: " + clerkId + super.toString() + "\n";
	}
	
}//end of class 
