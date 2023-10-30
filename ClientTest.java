package ss;

import java.io.*;
import java.net.*;
import java.util.*;

public class ClientTest2 {
    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 12349);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
            System.out.println("Connected to server.");

            Map<Integer, String> receivedPackets = new HashMap<>();
            ArrayList<Integer> missingPackets = new ArrayList<>();
            ArrayList<String> words = new ArrayList<>();
            int totalPackets = 20;

            long startTime = System.currentTimeMillis();
            long timeout = 20000; // Set a timeout (adjust as needed)

            while (System.currentTimeMillis() - startTime < timeout) {
                // Check if there's data available for reading
                if (in.ready()) {
                    String inputLine = in.readLine();
                    if (inputLine != null) {
                        System.out.println("Received: " + inputLine);
                        int packetNumber = extractPacketNumber(inputLine);
                        words.add(inputLine);
                        receivedPackets.put(packetNumber,inputLine);
                    }
                } else {
                    // No data available, wait for a short time before checking again
                    Thread.sleep(50); // Sleep for 50 milliseconds (adjust as needed)
                }
            }
            if (receivedPackets.size() == totalPackets) {
                System.out.println("All packets have been received.");
            } else {
            	System.out.println("Not all packets were received.");
                for (int i = 1; i <= totalPackets; i++) {
                    if (!receivedPackets.containsKey(i)) {
                        missingPackets.add(i);
                        System.out.print("Missing: " + i + "|" + totalPackets + "\n");
                    }
                }
                List<String> receivedWords = new ArrayList<>(Collections.nCopies(totalPackets, ""));

             // Handle missing packet responses from the server
                while (!missingPackets.isEmpty()) {
                	// Request missing packets from the server
                    for (int missingPacket : missingPackets) {                    	
                        out.println("REQUEST:" + missingPacket);
                        System.out.println("REQUEST:" + missingPacket);
                    }
                    String response = in.readLine();
                    if (response.startsWith("MISSING:")) {
                    	String[] parts = response.substring("MISSING:".length()).split("\\|");
                    	 if (parts.length >= 2) {
                    	        
                    	    int packetNumber = Integer.parseInt(parts[0]);
                            String packetData = parts[1];
                            int index = Integer.parseInt(parts[2]);
                            receivedWords.set(index, packetData);
                            receivedPackets.put(packetNumber, packetData);
                            missingPackets.remove((Integer)packetNumber);
                            System.out.println("Received resent packet: " + packetNumber + " " + packetData); // Print the resent packet
                        //}
                    }
                

                // Check if all packets have been received after handling missing packets
                if (receivedPackets.size() == totalPackets) {
                    for (int i = 1; i <= totalPackets; i++) {
                        System.out.println("Packet " + i + " recieved. " );
                       
                    }
                    System.out.println("All packets were recieved!");
                 // Sort and print the received words based on the index
                    Collections.sort(receivedWords, Comparator.comparingInt(Integer::parseInt));

                    for (String receivedPacket : receivedWords) {
                        System.out.print(receivedPacket + " ");
                    }


                }

                    }    }}  

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    private static int extractPacketNumber(String packet) {
        String[] parts = packet.split("\\|");
        if (parts.length >= 1) {
            return Integer.parseInt(parts[0]);
        }
        return -1;
    }
}
