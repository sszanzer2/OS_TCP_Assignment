package ss;

import java.io.*;
import java.net.*;
import java.util.*;

public class Client {
	public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 12349);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
            System.out.println("Connected to the server.");

            Map<Integer, String> receivedPackets = new HashMap<>();
            List<Integer> missingPackets = new ArrayList<>();
            int totalPackets = 20;

            long startTime = System.currentTimeMillis();
            long timeout = 3000; // Set a timeout (adjust as needed)

            while (System.currentTimeMillis() - startTime < timeout) {
                // Check if there's data available for reading
                if (in.ready()) {
                    String inputLine = in.readLine();
                    if (inputLine != null) {
                        System.out.println("Received: " + inputLine);
                        int packetNumber = extractNumberInPacket(inputLine);
                        receivedPackets.put(packetNumber, inputLine);
                    }
                } else {
                    // No data available, wait for a short time before checking again
                    Thread.sleep(50); // Sleep for 50 milliseconds (adjust as needed)
                }
            }

            if (receivedPackets.size() == totalPackets) {
                System.out.println("All packets have been received.");

                // Sort and print the received packets by the numbers in the packets
                List<Map.Entry<Integer, String>> sortedPackets = new ArrayList<>(receivedPackets.entrySet());
                sortedPackets.sort(Comparator.comparingInt(entry -> extractNumberInPacket(entry.getValue())));

                for (Map.Entry<Integer, String> entry : sortedPackets) {
                    int packetNumber = entry.getKey();
                    String packetData = entry.getValue();
                    System.out.println("Packet with number " + packetNumber + " received: " + packetData);
                }
            } else {
                System.out.println("Not all packets were received.");
                for (int i = 1; i <= totalPackets; i++) {
                    if (!receivedPackets.containsKey(i)) {
                        missingPackets.add(i);
                        System.out.print("Missing: " + i + "|" + totalPackets + "\n");
                    }
                }
                // Handle missing packet responses from the server
                while (!missingPackets.isEmpty()) {
                    // Request missing packets from the server
                    for (int missingPacket : missingPackets) {
                        out.println("REQUEST:" + missingPacket);
                        System.out.println("REQUEST:" + missingPacket);
                    }
                    String response;
                    while ((response = in.readLine()) != null && response.startsWith("MISSING:")) {
                        String[] parts = response.substring("MISSING:".length()).split(" ");
                        //for(int i = 0; i<parts.length;i++)
                       // System.out.print(parts[0] + " ");
                    	String packetNumber = parts[0];
                        String packetData = parts[1];
                        int index = Integer.parseInt(parts[2]);
                        receivedPackets.put(index, packetData);
                        String [] packetNumberParts = packetNumber.split("|");
                        missingPackets.remove(Integer.parseInt(packetNumberParts[0]));
                        System.out.println("Received resent packet: " + packetNumber + " " + packetData + " " + index);
                       
                    
                
                
                if (receivedPackets.size() == totalPackets) {
                    System.out.println("All packets have been received.");
                    break;
                	}
                }
                    //List<Map.Entry<Integer, String>> sortedPackets = new ArrayList<>(receivedPackets.entrySet());
                    //sortedPackets.sort(Comparator.comparingInt(entry -> extractNumberInPacket(entry.getValue())));
                    
                    for(int i = 1; i<receivedPackets.size()+1; i++) {
                    	if(receivedPackets.containsKey(i)) {
                    		System.out.print(receivedPackets.get(i) + " ");
                    	}
                    }

                  /*  int index = 1;
                    for (Map.Entry<Integer, String> entry : sortedPackets) {
              
                        System.out.print(entry.getValue() + " ");
                    }*/
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static int extractPacketNumber(String packet) {
        String[] parts = packet.split(" ");
        if (parts.length >= 1) {
            return Integer.parseInt(parts[0]);
        }
        return -1;
    }

    private static int extractNumberInPacket(String packet) {
        String[] parts = packet.split(" ");
        return Integer.parseInt(parts[2]);
        
    }
}

