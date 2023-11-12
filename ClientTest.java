package ss;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.Map.Entry;

public class ClientTest {
    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 12349);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
            System.out.println("Connected to the server.");

            Map<Integer, String> receivedPackets = new HashMap<>();
            Map<Integer, String> sortedPackets = new HashMap<>();
            Map<Integer, String> missingPackets = new HashMap<>(); // Change to HashMap

            String inputLine = " ";
            int totalPackets = 20;

            long startTime = System.currentTimeMillis();
            long timeout = 3000; // Set a timeout (adjust as needed)

            while (System.currentTimeMillis() - startTime < timeout) {
                // Check if there's data available for reading
                if (in.ready()) {
                    inputLine = in.readLine();
                    if (inputLine != null) {
                        System.out.println("Received: " + inputLine);
                        int packetIndex = extractNumberInPacket(inputLine);
                        int packetNumber = extractPacketNumber(inputLine);
                        receivedPackets.put(packetNumber, inputLine);
                        sortedPackets.put(packetIndex, inputLine);
                    }
                } else {
                    // No data available, wait for a short time before checking again
                    Thread.sleep(50); // Sleep for 50 milliseconds (adjust as needed)
                }
            }

            if (receivedPackets.size() == totalPackets) {
                System.out.println("All packets have been received.");
                for (int i = 1; i < sortedPackets.size() + 1; i++) {
                    if (sortedPackets.containsKey(i)) {
                        System.out.print(sortedPackets.get(i) + " ");
                    }
                }
            } else {
            	
                System.out.println("Not all packets were received.");
                for (int x = 1; x <= totalPackets; x++) {
                    if (!receivedPackets.containsKey(x)) {
                        missingPackets.put(x, null); // Add missing packet to HashMap
                        System.out.println("Missing: " + x + "|" + totalPackets);
                    }
                }

                // Handle missing packet responses from the server
                while (!missingPackets.isEmpty()) {
                    // Request missing packets from the server
                    for (int missingPacket : new ArrayList<>(missingPackets.keySet())) { // Create a copy of missing packets
                        out.println("REQUEST:" + missingPacket);
                        System.out.println("REQUEST:" + missingPacket);
                    }

                    String response;
                    boolean allPacketsReceived = false; // Flag to track whether all packets have been received

                    while ((response = in.readLine()) != null && response.startsWith("MISSING:") && !missingPackets.isEmpty()) {
                        String[] parts = response.substring("MISSING:".length()).split(" ");
                        if (parts.length >= 3) {
                            String packetNumber = parts[0];
                            String packetData = parts[1];
                            int index = Integer.parseInt(parts[2]);
                            int packetKey = Integer.parseInt(packetNumber.split("\\|")[0]);

                            missingPackets.remove(packetKey); // Update the value from null to the resent packet data

                           // int beforeSize = receivedPackets.size();
                            receivedPackets.put(packetKey, packetData);
                            sortedPackets.put(index, packetData);
                            //int afterSize = receivedPackets.size();
                            //System.out.println("ReceivedPackets size before: " + beforeSize);
                            System.out.println("Received resent packet: " + packetNumber + " " + packetData + " " + index);
                            //System.out.println("ReceivedPackets size after: " + afterSize);

                            if (receivedPackets.size() == totalPackets) {
                                allPacketsReceived = true;
                                //System.out.println("All packets have been received.");
                                break;
                            }
                        }
                    }
                    // Check if all packets have been received and exit the outer loop
                    if (allPacketsReceived) {
                        break;
                    }
                }
            	
            }
            if (receivedPackets.size() == totalPackets) {
                System.out.println("All packets have been received.");
                for (int i = 1; i < sortedPackets.size() + 1; i++) {
                    if (sortedPackets.containsKey(i)) {
                        System.out.print(sortedPackets.get(i) + " ");
                    }
                }
            }

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

    private static int extractNumberInPacket(String packet) {
        String[] parts = packet.split(" ");
        return Integer.parseInt(parts[2]);
    }
}
