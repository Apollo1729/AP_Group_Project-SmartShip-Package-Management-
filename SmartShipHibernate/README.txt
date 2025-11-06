Date: November 6, 2025 @5:11 pm (Thursday)
#-------------------------------------------------------------------------------------------------------------------------------------

Need fixing:

The Remaining tries are always displaying 0
The test cases for the email extensions are empty, it wont allow us to register a customer in the database
#-------------------------------------------------------------------------------------------------------------------------------------

Havent Tested:

<!--The methods are there but just haven't been tested. They Should work though.-->
1. Creating a manager in its table
2. Creating a driver in its table
#-------------------------------------------------------------------------------------------------------------------------------------

To Do:

1. User login authentication has not yet been implemented
2. Create a customer interface (should go in the com.java.view package)
3. Create a staff interface - *preview idea below* (should go in the com.java.view package)
4. The Retrieve, delete and update methods have not been implemented for the  User sub classes (Customer, Manager, Clerk, Driver)
5. Add Vehicle, Driver's License, Package, Delivery to the database (Their tables need to be created and methods created)
#-------------------------------------------------------------------------------------------------------------------------------------

Ideas:
1. For the interface, we could make one main UI but depending on who is logged into the system, different parts of the system is closed(hidden) to unauthorized persons.
	This will prevent us from needing to create more than one UIs. 
2.