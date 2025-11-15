package network_server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class SmartShipServer {
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(5000)) {
            System.out.println("SmartShip Server started on port 5000...");
            System.out.println("Waiting for clients...");
            
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected!");
                
                ClientHandler handler = new ClientHandler(clientSocket);
                Thread thread = new Thread(handler);
                thread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
