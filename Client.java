//Richard Molina
//COP4338 U01 - Programming Assignment 1

import java.net.Socket;

public class Client
{
	private String id;				// client's ID
	private Socket clientSocket;	// client's Socket
	private int chatRoom;			// client's chat room number

	public Client(String id, Socket clientSocket, int chatRoom)
	{
		this.id = id;
		this.clientSocket = clientSocket;
		this.chatRoom = chatRoom;
	}
	
	// returns client's Socket
	public Socket getClientSocket()
	{
		return clientSocket;
	}

	// returns client's ID
	public String getId()
	{
		return id;
	}

	// returns client's chat room number
	public int getChatRoom()
	{
		return chatRoom;
	}
}
