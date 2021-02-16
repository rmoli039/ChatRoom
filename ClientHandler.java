//Richard Molina
//COP4338 U01 - Programming Assignment 1

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.concurrent.locks.*;

public class ClientHandler extends Thread
{
	private Socket clientSocket;
	private Client client;
	private ArrayList<Client> clientList; 
	private Lock clientListLock;
	private InputStream inputStream;
	private BufferedReader reader;
	private OutputStream outputStream;
	private PrintWriter serverWriter;
	private final String EXIT_KEYWORD = "LOGOUT";	// keyword to exit server

	public ClientHandler(Socket clientSocket, ArrayList<Client> clientList)
	{
		this.clientSocket = clientSocket;
		this.clientList = clientList;
		this.clientListLock = new ReentrantLock();	// initializes the clientList lock
	}
	
	// opens a new thread to handle the client's connection
	public void run()
	{
		try
		{
			try
			{
				inputStream = clientSocket.getInputStream();						// initailizes the clientHandler's inputStream
				reader = new BufferedReader(new InputStreamReader(inputStream));	// initailizes the clientHandler's BufferedReader
				outputStream = clientSocket.getOutputStream();						// initailizes the clientHandler's outputStream
				serverWriter = new PrintWriter(outputStream);						// initailizes the clientHandler's PrintWriter
				handleClientSocket();												// begins processing the client's connection to the server
			}
			finally
			{
				clientListLock.lock();														// locks the clientList
				clientList.remove(client);													// removes the client from the clientList
				postAnnouncement("User " + client.getId() + " has left the server!\n\r");	// sends announcement to chat room that client has left
				serverWriter.close();														// closes the clientHandler's PrintWriter
				reader.close();																// closes the clientHandler's BufferedReader
				clientSocket.close();														// closes the client's Socket
			}
		} 
		catch (IOException | InterruptedException e)
		{
			e.printStackTrace();
		}
		finally
		{
			clientListLock.unlock();	// unlocks the clientList after removing and disconnecting the client
		}
	}
	
	// processes the client's ongoing connection to the server
	private void handleClientSocket() throws IOException, InterruptedException
	{		
		int chatRoom = getChatRoom(reader, serverWriter);			// gets the client's chat room number
		String inputLine = getId(reader, serverWriter, chatRoom);	// gets the client's ID
		client = new Client(inputLine, clientSocket, chatRoom);		// initializes the client
		
		clientListLock.lock();			// locks the clientList
		try
		{
			clientList.add(client);		// adds the client to the clientList
		}
		finally
		{
			clientListLock.unlock();	// unlocks the clientList
		}
		
		
		postAnnouncement("User " + client.getId() + " has joined the server!\n\r");		// sends announcement to chat room that client has joined
		printWelcome();																	// prints welcome message to the client
		
		while((inputLine = reader.readLine()) != null)		// infinite loop that reads client input (breaks if null input received)
		{			
			if (inputLine.equals(EXIT_KEYWORD))				// breaks loop if client enters exit keyword
			{
				break;
			}
			postMessage(inputLine + "\n\r");				// sends client input as message
		}
	}

	// prints a welcome message to the client with instructions on how to exit the server
	private void printWelcome()
	{
		serverWriter.print("~~Welcome to chat room " + client.getChatRoom() + "! Enter " + EXIT_KEYWORD + " at any time to exit.\n\r");
		serverWriter.flush();
	}

	// sends client input as message to all clients in the chat room
	private void postMessage(String message) throws IOException
	{
		OutputStream post; 
		PrintWriter postWriter; 
		for (Client c: clientList)									// loops through all clients in clientList
		{
			if (c.getChatRoom() == client.getChatRoom())			// checks if the client is in the same chat room
			{
				post = c.getClientSocket().getOutputStream();		// initializes the client's OutputStream
				postWriter = new PrintWriter(post);					// initializes the client's PrintWriter
				postWriter.print(client.getId() + ": " + message);  // prints the message to the client
				postWriter.flush();
			}
		}
	}
	
	// sends an announcement to all clients in the chat room
	private void postAnnouncement(String announcement) throws IOException
	{
		OutputStream post; 
		PrintWriter postWriter; 
		for (Client c: clientList)								// loops through all clients in clientList
		{
			if (c.getChatRoom() == client.getChatRoom())			// checks if the client is in the same chat room
			{
				post = c.getClientSocket().getOutputStream();		// initializes the client's OutputStream
				postWriter = new PrintWriter(post);					// initializes the client's PrintWriter
				postWriter.print("~~" + announcement);				// prints the announcement to the client
				postWriter.flush();
			}
		}
	}
	
	// prompts the client to select a chat room and returns it
	private int getChatRoom(BufferedReader reader, PrintWriter serverWriter) throws IOException
	{
		String input = "";
		int chatRoom = 0;
		while(true)																	// infinite loop to prevent blank answer
		{
			serverWriter.print("~~Which chat room would you like to join?\n\r");	// prompts client to enter a chat room number
			serverWriter.flush();
			serverWriter.print("~~Enter a number from 1 to 4: ");					// prompts client to enter a number from 1 to 4
			serverWriter.flush();
			input = reader.readLine();						// reads client input
			try
			{
				chatRoom = Integer.parseInt(input);			// parses client input as integer
			}
			catch (NumberFormatException e)					// catches exception if client inputs non-integer
			{
				serverWriter.print("~~Invalid input!\n\r");	// warns client input is invalid (not a number)
				serverWriter.flush();
				continue;
			}
			
			if (chatRoom >= 1 && chatRoom <= 4)				// breaks loop if client enters a number from 1 to 4
			{
				break;
			}
			serverWriter.print("~~Invalid input!\n\r");		// warns client input is invalid (blank or not 1 to 4)
			serverWriter.flush();
		}
		serverWriter.print("\n\r");
		serverWriter.flush();
		return chatRoom;
	}

	// prompts the client to enter an ID and returns it
	private String getId(BufferedReader reader, PrintWriter serverWriter, int chatRoom) throws IOException
	{
		String id = "";
		while(true)										// infinite loop to prevent blank answer
		{
			serverWriter.print("~~Enter your ID: ");	// prompts client to enter an ID
			serverWriter.flush();
			id = reader.readLine();						// reads client input
			boolean taken = false;
			if (!id.equals(""))							// checks that client did not enter a blank answer
			{
				for (Client c: clientList)				// loops through all clients in clientList
				{
					if (c.getChatRoom() == chatRoom && c.getId().equals(id))	// checks for matching ID in the same chat room
					{
						serverWriter.print("~~That ID is already taken!\n\r");	// warns client ID is already taken
						serverWriter.flush();
						taken = true;
						continue;												// continues loop to reprompt client
					}
				}
				if (!taken)		// breaks loop if ID is not taken
				{
					break;
				}
				continue;		// continues loop since ID is taken
			}
			serverWriter.print("~~ID cannot be blank!\n\r");	// warns client ID cannot be blank
			serverWriter.flush();
		}
		serverWriter.print("\n\r");
		serverWriter.flush();
		return id;
	}
}
