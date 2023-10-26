package ss;
import java.io.*;
import java.net.*;
import java.util.*;
public class ClientTest {
	
    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 12345);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            System.out.println("Connected to server.");

            Map<Integer, String> receivedPackets = new HashMap<>();
            int totalPackets = -1;

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                System.out.println("Received: " + inputLine);
                int packetNumber = extractPacketNumber(inputLine);

                if (packetNumber == totalPackets + 1) {
                    // Special "end" packet indicating all packets have been sent
                    totalPackets = packetNumber;
                } else {
                    receivedPackets.put(packetNumber, inputLine);
                }

                if (totalPackets != -1 && receivedPackets.size() == totalPackets) {
                    // All packets have been received
                    break;
                }
            }

            if (receivedPackets.size() == totalPackets) {
                // Reassemble and display the complete message
                for (int i = 1; i <= totalPackets; i++) {
                    System.out.println("Packet " + i + ": " + receivedPackets.get(i));
                }
            } else {
                System.out.println("Not all packets were received. Request missing packets if needed.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static int extractPacketNumber(String packet) {
        String[] parts = packet.split("\\|");
        if (parts.length >= 1) {
            return Integer.parseInt(parts[0].substring(1)); // Remove '['
        }
        return -1;
    }
}


