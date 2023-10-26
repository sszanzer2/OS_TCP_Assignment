package ss;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.Random;

public class ServerTest {
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
            long timeout = 5000; // Set a timeout (5 seconds) - adjust as needed

            while (System.currentTimeMillis() - startTime < timeout) {
                // Check if there's a request from the client
                String request = in.readLine();
                if (request != null && request.startsWith("REQUEST:")) {
                    int missingPacket = Integer.parseInt(request.substring("REQUEST:".length()));

                    if (missingPacket >= 1 && missingPacket <= totalPackets) {
                        String missingPacketData = packets.get(missingPacket - 1);

                        // Simulate packet loss with 20% probability when resending
                        if (random.nextDouble() < 0.8) {
                            out.println(missingPacketData); // Send the missing packet directly
                            System.out.println("Resent (with drop probability): " + missingPacketData);
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
