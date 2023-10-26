package ss;

import java.io.*;
import java.net.*;
import java.util.*;

public class ServerTest {
	public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(12345)) {
            System.out.println("Server started. Waiting for connections...");
            Socket clientSocket = serverSocket.accept();
            System.out.println("Client connected.");

            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            Random random = new Random();
            int totalPackets = 20;

            for (int packetNumber = 1; packetNumber <= totalPackets; packetNumber++) {
                if (random.nextDouble() < 0.8) {
                    // Simulate packet loss with 20% probability
                    String packet = createPacket(packetNumber, totalPackets);
                    out.println(packet);
                    System.out.println("Sent: " + packet);
                }
            }

            // Send the final "end" packet
            String endPacket = createPacket(totalPackets + 1, totalPackets);
            out.println(endPacket);
            System.out.println("Sent (End): " + endPacket);

            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String createPacket(int packetNumber, int totalPackets) {
       // int checksum = packetNumber; // Simple checksum
        return String.format(" %d|%d Packet Data", packetNumber, totalPackets);
	    }
	}



