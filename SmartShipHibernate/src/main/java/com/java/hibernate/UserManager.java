package com.java.hibernate;
import com.java.view.*;
import org.hibernate.SessionFactory;
import org.hibernate.Session;
import org.hibernate.cfg.Configuration;

public class UserManager implements UserManagement {
	
	private static SessionFactory sessionFactory = null;
	
	public static void main(String[] args) {
		UserManager manager = new UserManager();
		new Application();
		manager.exit();
	}
	
	public UserManager() {
		sessionFactory = buildSessionFactory();
	}

	private static SessionFactory buildSessionFactory() {
		try {
			return new Configuration().configure().buildSessionFactory();
		}catch(Exception e) {
			System.err.println("SessionFactory creation failed: " + e.getMessage());
			throw new ExceptionInInitializerError(e);
		}
		
		//return sessionFactory;
	}
	
	@Override
	public void exit() {

	}

	@Override
	public void signUp(Customer customer) {
		Session session = sessionFactory.openSession();
		session.beginTransaction();
		session.persist(customer);
		session.getTransaction().commit();
		session.close();
	}

	@Override
	public void login(Customer customer) {

	}

	@Override
	public void view(String cusId) {

	}

	@Override
	public void update(Customer customer) {

	}

	@Override
	public void delete(String cusId) {

	}
	

	@Override
	public void signUp(Manager manager) {
		Session session = sessionFactory.openSession();
		session.beginTransaction();
		session.persist(manager);
		session.getTransaction().commit();
		session.close();		
	}

	@Override
	public void login(Manager manager) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void update(Manager manager) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void signUp(Clerk clerk) {
		Session session = sessionFactory.openSession();
		session.beginTransaction();
		session.persist(clerk);
		session.getTransaction().commit();
		session.close();		
	}

	@Override
	public void login(Clerk clerk) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void update(Clerk clerk) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void signUp(VDriver driver) {
		Session session = sessionFactory.openSession();
		session.beginTransaction();
		session.persist(driver);
		session.getTransaction().commit();
		session.close();		
	}

	@Override
	public void login(VDriver driver) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void update(VDriver driver) {
		// TODO Auto-generated method stub
		
	}

}
