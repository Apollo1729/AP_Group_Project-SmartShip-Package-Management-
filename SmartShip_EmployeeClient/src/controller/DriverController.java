package controller;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;


public class DriverController {

	private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 5000;
    
    
    
    public List<Map<String,String>> getDeliveriesArray(int userID)
    {
    	// Connect to server and authenticate
        Socket socket = null;
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        
        try {
        	socket = new Socket(SERVER_HOST, SERVER_PORT);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            
            out.writeObject("GET_DRIVER_DELIVERIES");
            out.writeObject(userID);
            out.flush();
           
            
            Object response = in.readObject();
            
            if (response instanceof List) {
            	@SuppressWarnings("unchecked")
				List<Map<String,String>> deliveryinfo = (List<Map<String,String>>) response;
            	return deliveryinfo;
            }

        	} catch (Exception e){
        	e.printStackTrace();
        	JOptionPane.showMessageDialog(null, 
        			"Error retrieving delivery data", 
        			"Retrieval Error", 
        			JOptionPane.ERROR_MESSAGE);
        	}finally {
        		try {
        			if (in != null) in.close();
                    if (out != null) out.close();
                    if (socket != null) socket.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
        	}
		return new ArrayList<>();
    }
    
    public boolean updateDelivStatus(String trackingNumber, String status) {
        Socket socket = null;
        ObjectOutputStream out = null;
        ObjectInputStream in = null;

        try {
            socket = new Socket(SERVER_HOST, SERVER_PORT);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            out.writeObject("UPDATE_DELIVERY_STATUS");
            out.writeObject(trackingNumber);
            out.writeObject(status);
            out.flush();

            Object response = in.readObject();
            return response instanceof Boolean && (Boolean) response;

        } catch (Exception e) {
            e.printStackTrace();
            return false;

        } finally {
            try {
                if (in != null) in.close();
                if (out != null) out.close();
                if (socket != null) socket.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }



	
 
    
    
    
}

