package ss;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.Random;

public class ServerTest1 {
		
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(12349)) {
            System.out.println("Server started. Waiting for connections...");
            Socket clientSocket = serverSocket.accept();
            System.out.println("Client connected.");

            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            Random random = new Random();
            int totalPackets = 20;

            // Generate packets and store them
            List<String> packets = new ArrayList<>();
            for (int packetNumber = 1; packetNumber <= totalPackets - 1; packetNumber++) {
                String packet = createPacket(packetNumber, totalPackets);
                packets.add(packet);
                if (random.nextDouble() < 0.8) {
                    // Simulate packet loss with 20% probability
                    out.println(packet);
                    System.out.println("Sent: " + packet);
                }
            }

            // Send the final "end" packet
            String endPacket = createPacket(totalPackets, totalPackets);
            out.println(endPacket);
            System.out.println("Sent (End): " + endPacket);

            long startTime = System.currentTimeMillis();
            long timeout = 20000; // Set a timeout (15 seconds) - adjust as needed
            Set<Integer> receivedPackets = new HashSet<>();

            while (System.currentTimeMillis() - startTime < timeout && receivedPackets.size() < totalPackets) {
                // Check if there's a request from the client
                String request = in.readLine();
                if (request != null && request.startsWith("REQUEST:")) {
                    int missingPacket = Integer.parseInt(request.substring("REQUEST:".length()));

                    if(missingPacket >= 1 && missingPacket <= totalPackets) {
                        String missingPacketData = createPacket(missingPacket, totalPackets);

                        // Simulate packet loss with 20% probability when resending
                        if (!receivedPackets.contains(missingPacket)){
                        		if(random.nextDouble() < 0.8) {
	                        	out.println("MISSING:" + missingPacket + "|" + missingPacketData); // Send the missing packet directly
	                            receivedPackets.add(missingPacket);
	                            System.out.println("Resent (with drop probability): " + missingPacketData);
                        		}
	                        }
                        
                    }
                           
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String createPacket(int packetNumber, int totalPackets) {
        return String.format("%d|%d Packet Data", packetNumber, totalPackets);
	    }
}
