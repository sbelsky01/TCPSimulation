//Sarah Belsky
//TCP Simulation assignment
//November 2, 2022

import java.io.*;
import java.net.*;
import java.util.Iterator;

public class Client {
    public static void main(String[] args) throws IOException {
        
		// Hardcode in IP and Port here if required
    	args = new String[] {"127.0.0.1", "3012"};
    	
        if (args.length != 2) {
            System.err.println(
                "Usage: java EchoClient <host name> <port number>");
            System.exit(1);
        }

        String hostName = args[0];
        int portNumber = Integer.parseInt(args[1]);
        String packet;
    	int numPackets, index;
        boolean allReceived = false;

        try (
            Socket clientSocket = new Socket(hostName, portNumber);
            PrintWriter requestWriter = // stream to write text requests to server
                new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader messageReader= // stream to read text response from server
                new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        	BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        	) 
        {
        	
        	//the number of characters in the packet allocated to the packet number
        	//and the total number of packets
        	final int NUM_DIGITS = 2;
        	String lastPacketSent = ""; //generate the message that indicates when the server has finished sending packets
        	for (int i=0; i<NUM_DIGITS; i++)
			{
				lastPacketSent += "0";
			}
        	
        	//use the information from the first packet to initialize an array with
        	//a length equal to the total number of packets
        	packet = messageReader.readLine();
        	numPackets = Integer.parseInt(packet.substring(NUM_DIGITS, NUM_DIGITS*2));
        	String[] subMessages = new String[numPackets];
        	index = Integer.parseInt(packet.substring(0, NUM_DIGITS))-1;
        	
        	//add the message part of the package to the correct index in the array
        	if(index != -1) {
        		subMessages[index] = packet.substring(NUM_DIGITS*2);
        	} else {
        		allReceived = requestMissingPackets(requestWriter, subMessages);
        	}
        	
        	//keep receiving packets and requesting missing ones until all packets
        	//have been received       	
        	while(!allReceived)
        	{
        		while(!(packet = messageReader.readLine()).substring(0, NUM_DIGITS).equals(lastPacketSent))
            	{
        			//get the packet number from the first two characters of the packet
            		index = Integer.parseInt(packet.substring(0, NUM_DIGITS))-1;
            		
            		//add the message part of the package to the correct index in the array
                	subMessages[index] = packet.substring(NUM_DIGITS*2);
            	}
        		
        		//check if there are missing packets. If the only message the method sends
        		//is "00", it will return true and the client will break out of the loop
            	allReceived = requestMissingPackets(requestWriter, subMessages);
        	}
        	
        	//loop through the array to display the message
        	System.out.println("Message:");
        	for(int i=0; i<subMessages.length; i++)
        	{
        			System.out.print(subMessages[i]);
        	}
        	
        	
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + hostName);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " +
                hostName);
            System.exit(1);
        } 
    }
    /**
     * Checks for missing packets and requests them from the server
     * @param output Output stream to write text requests to the server
     * @param substrings The array of received packets
     * @return true if all the packets have been received, otherwise returns false
     */
    public static boolean requestMissingPackets(PrintWriter output, String[] substrings)
    {
    	boolean allReceived = true;
    	for(int i=0; i<substrings.length; i++)
    	{
    		//if an array position is null, request that packet from the server
    		if(substrings[i] == null)
    		{
    			allReceived = false;
    			output.println(String.format("%02d", i));
    		}
    	}
    	
    	//notify the server that all the missing packet numbers have been sent
    	output.println("00");
    	return allReceived;
    }
}