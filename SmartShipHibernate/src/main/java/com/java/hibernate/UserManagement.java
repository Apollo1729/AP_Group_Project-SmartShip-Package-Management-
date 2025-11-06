package com.java.hibernate;

public interface UserManagement {
	public abstract void exit();
	public abstract void view(String cusId);
	public abstract void delete(String cusId);
	
	//Customer
	public abstract void signUp(Customer customer);
	public abstract void login(Customer customer);
	public abstract void update(Customer customer);
	
	//Manager
	public abstract void signUp(Manager manager);
	public abstract void login(Manager manager);
	public abstract void update(Manager manager);
	
	//Clerk
	public abstract void signUp(Clerk clerk);
	public abstract void login(Clerk clerk);
	public abstract void update(Clerk clerk);
	
	//Driver
	public abstract void signUp(VDriver driver);
	public abstract void login(VDriver driver);
	public abstract void update(VDriver driver);
}
