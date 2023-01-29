//Sarah Belsky
//TCP Simulation assignment
//November 2, 2022

import java.net.*;
import java.io.*;
import java.util.*;

public class Server
{
	public static void main(String[] args) throws IOException {

		//Hard code in port number if necessary:
		args = new String[] { "3012" };
		
		if (args.length != 1) {
			System.err.println("Usage: java EchoServer <port number>");
			System.exit(1);
		}

		int portNumber = Integer.parseInt(args[0]);
        final int SUBLENGTH = 16;
        ArrayList<Integer> packetsToSend= new ArrayList<Integer>();
        boolean allReceived = false;
        String toAdd;

		try (ServerSocket serverSocket = new ServerSocket(Integer.parseInt(args[0]));
				Socket clientSocket = serverSocket.accept();
				PrintWriter messageWriter = new PrintWriter(clientSocket.getOutputStream(), true);
				BufferedReader requestReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				) {
			
			//define the message to send
	    	String message = "<!DOCTYPE html> <html lang='en'> <head> <meta charset='utf-8'> <title> TCPSimulation</title>"
	    			+ "<style> body { margin: 0px; height: 100%; background-color: beige; font-family: 'Berlin sans FB', 'Calibri', sans-serif; } h1 { color: green; font-family: 'Showcard Gothic', 'Snap ITC', fantasy; font-size: 40px; }"
	    			+ "header, footer { background-color: green; margin: 0px; padding: 50px; width: 100%; border: solid medium black } p { font-size: 17px; }"
	    			+ ".character { font-family: 'Webdings'; color: green; font-size: 150px; margin: 10px; padding-left: 50px; } footer { position: absolute; bottom: 0; } div { padding: 15px; } </style> </head>"
	    			+ "<body> <header></header> <div> <h1>Success!</h1> <p>This document was successfully sent from server to client. We let nothing stand in our way! Dropped packets, out of order... (maybe a few unintentional bugs as well...) "
	    			+ "We've conquered it all!</p> <p>In fact, if the client was a browser, it would now be able to display a webpage based on this text. </p> <p class='character'>a</p> </div> <footer></footer> </body> </html>";
			
	    	//split the message into packets
	    	int numPackets = (int)Math.ceil((double)message.length()/SUBLENGTH);
	    	String finishedMessage = String.format("00%02d", numPackets); //message to indicate that the server finished sending packets
	    	String[] packets = new String[numPackets];
	    	makePackets(message, packets, SUBLENGTH);
	    	
	    	//initially, all the packets will be sent, so the ArrayList should hold all
	    	//the values until the last index
	    	for(int i=0; i<numPackets; i++)
	    	{
	    		packetsToSend.add(i);
	    	}
	    	
	    	//resend the packets requested by the client until the client
	    	//received all the packets
	    	do
	    	{
	    		allReceived = true;
	    		
	    		//send the packets specified in the packetsToSend list
	    		sendPackets(packets, packetsToSend, messageWriter, finishedMessage);
	    		
	    		//get the missing packet numbers from the client, adding each one
	    		//to the packetsToSend list as it is received
	    		//if the first number received is "00", allReceived will remain true
	    		//and the loop will end
	    		while(!(toAdd = requestReader.readLine()).equals("00"))
	    		{
	    			allReceived = false;
	    			packetsToSend.add(Integer.parseInt(toAdd));
	    		}
	    		
	    	}while(!allReceived);
	    	
    		
    		
		} catch (IOException e) {
			System.out.println(
					"Exception caught when trying to listen on port " + portNumber + " or listening for a connection");
			System.out.println(e.getMessage());
		}

	}
	
	/**
	 * Splits the message into equally sized packets
	 * @param message The message to split
	 * @param packets The array to hold the packets
	 * @param SUBLENGTH The length of each substring of the message
	 */
	public static void makePackets(String message, String[] packets, int SUBLENGTH)
	{
		int index;
    	String substring, packet;
    	for(index=0; index<packets.length-1; index++)
    	{
    		substring = message.substring(SUBLENGTH*index, SUBLENGTH*(index+1));
    		packet = String.format("%02d%02d%s", index+1, packets.length, substring);
    		packets[index] = packet;
    	}
    	if(packets.length > 0) {
    		substring = message.substring(SUBLENGTH*index);
    		packet = String.format("%02d%02d%s", index+1, packets.length, substring);
    		packets[index] = packet;
    	}
	}
	
	/**
	 * Sends packets to the client in a random order. Each packet has a
	 * 20% chance of being dropped.
	 * @param packets An array containing the packets to be sent
	 * @param toSend An ArrayList that holds the index numbers of the packets to be sent
	 * @param output Output stream to write text responses to the client
	 */
	public static void sendPackets(String[] packets, ArrayList<Integer> toSend, PrintWriter output, String finishedMessage)
	{
		Integer packetNum;
		Random rand = new Random();
    	while(toSend.size() > 0)
    	{
    		//pick a random packet number
    		packetNum = rand.nextInt(packets.length);
    		if(toSend.contains(packetNum))
    		{
    			//send the packet to the client with an 80% probability
    			if(rand.nextDouble() < .8)
    			{
    				output.println(packets[packetNum]);
    			}
    			//remove the packet number from the list of packets to send
    			toSend.remove(packetNum);
    		}
    	}
    	
    	//indicate to the client that the server has finished sending the packets
    	output.println(finishedMessage);
	}

}