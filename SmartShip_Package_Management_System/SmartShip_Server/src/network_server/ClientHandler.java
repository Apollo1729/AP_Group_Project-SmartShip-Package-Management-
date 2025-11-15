package network_server;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    
    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }
    
    @Override
    public void run() {
        try {
            ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
            ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
            
            String action = (String) in.readObject();
            System.out.println("Action: " + action);
            
            Command command = CommandRegistry.getCommand(action);
            
            if (command != null) {
                Object result = command.execute(in);
                out.writeObject(result);
                out.flush();
            } else {
                out.writeObject(null);
                out.flush();
            }
            
            clientSocket.close();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
