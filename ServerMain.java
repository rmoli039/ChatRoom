//Richard Molina
//COP4338 U01 - Programming Assignment 1

import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class ServerMain
{
	public static void main(String[] args)
	{
		ArrayList<Client> clientList = new ArrayList<Client>();		// initializes an ArrayList of Clients
		final int PORT = 8888;										// sets the port number for the server
		
		try {
			ServerSocket serverSocket = new ServerSocket(PORT);		// creates a ServerScoket at PORT #
			while(true)												// infinite loop to keep the server open and accepting new connections
			{
				System.out.println("Waiting for client connection...");
				Socket clientSocket = serverSocket.accept();							// creates a client Socket for each new connection
				System.out.println("Client connected: " + clientSocket);
				ClientHandler handler = new ClientHandler(clientSocket, clientList);	// creates a ClientHandler for the new connection
				handler.start();														// starts the ClientHandler for the new connection
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

}
