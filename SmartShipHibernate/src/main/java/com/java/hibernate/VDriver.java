package com.java.hibernate;
import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="Driver")
public class VDriver extends User implements Serializable {
		private static final long serialVersionUID = 1L;
		
		@Id
		@Column(name="driverId")
		@GeneratedValue(strategy = GenerationType.IDENTITY)
		
		private long driverId;
		//private Route route;
		@Embedded
		private Vehicle assignedVehicle;
		@Embedded
		private DriverLicense license;
		@Embedded
		private Delivery assignedDeliveries;
		
		
		//default constructor
		public VDriver() {
			super();
			this.driverId = 0;
			this.assignedVehicle = new Vehicle();
			this.license = new DriverLicense();
		}
		
		//primary constructor
		public VDriver(String firstName, String lastName, Date dob, int age, String nationalId, Address address, String telNum, String username, String password, String email, long driverId, Vehicle assignedVehicle, DriverLicense license) {
			super( firstName,  lastName,  dob,  age,  nationalId,  address,  telNum,  username,  password,  email);
			this.driverId = driverId;
			this.assignedVehicle = assignedVehicle;
			this.license = license;
		}
		
		//primary constructor - 2
		public VDriver(String firstName, String lastName, Date dob, int age, String nationalId, Address address, String telNum, String username, String password, String email) {
			super(firstName,lastName, dob, age, nationalId, address, telNum,  username,  password,  email);
		}

		//COPY constructor
		public VDriver(VDriver v) {
			super(v);
			this.driverId = v.driverId;
			this.assignedVehicle = v.assignedVehicle;
			this.license = v.license;
		}

		//Setter and getters
		public long getDriverId() {
			return this.driverId;
		}

		public void setDriverId(long driverId) {
			this.driverId = driverId;
		}

		public Vehicle getAssignedVehicle() {
			return this.assignedVehicle;
		}

		public void setAssignedVehicle(Vehicle assignedVehicle) {
			this.assignedVehicle = assignedVehicle;
		}

		public DriverLicense getLicense() {
			return this.license;
		}

		public void setLicense(DriverLicense license) {
			this.license = license;
		}
		
		
		@Override
		public String toString() {
			return "Driver ID: " + driverId +
					"\nFirst Name: " + this.firstName + 
					"\nLast Name: " + this.lastName + 
					"\nDob: " + this.dob + 
					"\nAge:" + this.age + 
					"\nNational Id: " + this.nationalId + 
					"\nAddress: " + this.address + 
					"\nTel Number: " + this.telNum + 
					"\nAssigned Vehicle: " + assignedVehicle.toString() +
					"\nDriver's License: " + license.toString() + "\n";
		}
}
