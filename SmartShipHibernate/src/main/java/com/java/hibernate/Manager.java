package com.java.hibernate;
import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "Manager") //name of the table in the database
public class Manager extends User implements Serializable{

		private static final long serialVersionUID = 1L;
		@Id
		@Column(name= "mngId")
		@GeneratedValue(strategy = GenerationType.IDENTITY)
		private long mngId;
		//default
		public Manager() {
			super();
			this.mngId = 0L;
		}
		
		//primary
		public Manager(String firstName, String lastName, Date dob, int age, String nationalId, Address address, String telNum, String username, String password, String email, long mngId) {
			super(firstName,lastName, dob, age, nationalId, address, telNum, username, password, email);
			this.mngId = mngId;
		}
		
		//primary constructor - 2
		public Manager(String firstName, String lastName, Date dob, int age, String nationalId, Address address, String telNum, String username, String password, String email) {
			super(firstName,lastName, dob, age, nationalId, address, telNum,  username,  password,  email);
		}
		
		//copy
		public Manager(Manager mng) {
			super(mng);
			this.mngId = mng.mngId;
		}
		
		//setters and getters
		
		public long getMngId() {
			return this.mngId;
		}

		public void setMngId(int mngId) {
			this.mngId = mngId;
		}
		
		@Override
		public String toString() {
			return "Manager ID: " + mngId + super.toString() + "\n";
		}
		
		
		
}//end of class
