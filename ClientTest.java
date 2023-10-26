package ss;

import java.io.*;
import java.net.*;
import java.util.*;

public class ClientTest {
    public static void main(String[] args) throws InterruptedException {
        try (Socket socket = new Socket("localhost", 12349);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
            System.out.println("Connected to server.");

            Map<Integer, String> receivedPackets = new HashMap<>();
            ArrayList<Integer> missingPackets = new ArrayList<>();
            int totalPackets = 20;

            long startTime = System.currentTimeMillis();
            long timeout = 5000; // Set a timeout (5 seconds) - adjust as needed

            while (System.currentTimeMillis() - startTime < timeout) {
                // Check if there's data available for reading
                if (in.ready()) {
                    String inputLine = in.readLine();
                    if (inputLine != null) {
                        System.out.println("Received: " + inputLine);
                        int packetNumber = extractPacketNumber(inputLine);
                        receivedPackets.put(packetNumber, inputLine);
                    }
                } else {
                    // No data available, wait for a short time before checking again
                    Thread.sleep(100); // Sleep for 100 milliseconds (adjust as needed)
                }
            }


                if (receivedPackets.size() == totalPackets) {
                    System.out.println(" All packets have been received.");
                    
            
            } else {
                for (int i = 1; i <= totalPackets; i++) {
                    if (!receivedPackets.containsKey(i)) {
                        missingPackets.add(i);
                    }
                }
                System.out.println("Not all packets were received.");

                // Request missing packets from the server
                for (int missingPacket : missingPackets) {
                    out.println("REQUEST:" + missingPacket);
                }

                // Handle missing packet responses from the server
                for (int missingPacket : missingPackets) {
                    String response = in.readLine();
                    if (response != null && response.startsWith("MISSING:" + missingPacket)) {
                        String packetData = response.substring(("MISSING:" + missingPacket).length() + 1);
                        receivedPackets.put(missingPacket, packetData);
                    }
                }
                
                // Check if all packets have been received after handling missing packets
                if (receivedPackets.size() == totalPackets) {
                    for (int i = 1; i <= totalPackets; i++) {
                        System.out.println("Packet " + i + ": " + receivedPackets.get(i));
                    }
                }
            }
        } catch (IOException e) {
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
