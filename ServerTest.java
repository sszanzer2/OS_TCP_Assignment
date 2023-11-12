package ss;
import java.io.*;
import java.net.*;
import java.util.*;

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
	
	        // Define an array of 20 words
	        String[] words = {
	            "This", "project", "was", "worked", "on",
	            "by", "Shana", "and", "Miriam.", "It",
	            "took", "days", "to", "finish", "but",
	            "boruch", "hashem", "we", "did"
	        };
	        Map<String, Integer> wordIndexMap = new HashMap<>();
	        for (int i = 0; i < words.length; i++) {
	            wordIndexMap.put(words[i], i+1);
	        }
	
	        // Shuffle the array
	        List<String> wordList = Arrays.asList(words);
	        Collections.shuffle(wordList);
	        words = wordList.toArray(new String[0]);
	
	        // Generate packets and store them
	        List<String> packets = new ArrayList<>();
	        
	        // Send packets containing individual words
	        for (int packetNumber = 1; packetNumber <= totalPackets-1; packetNumber++) {
	        	 String word = words[packetNumber - 1];
	        	 String packet = createPacket(packetNumber, totalPackets, words[packetNumber-1], wordIndexMap.get(word) );
	             packets.add(packet);
	             if (random.nextDouble() < 0.8) {
	                 // Simulate packet loss with 20% probability
	                 out.println(packet);
	                 System.out.println("Sent: " + packet);
	             }
	        }
	        // Send the final "end" packet
	        String endPacket = createPacket(totalPackets, totalPackets, "it!", 20);
	        out.println(endPacket);
	        System.out.println("Sent (End): " + endPacket);
	
	        long startTime = System.currentTimeMillis();
	        long timeout = 4000; // Set a timeout (adjust as needed)
	        Set<Integer> receivedPackets = new HashSet<>();
	
	        while (System.currentTimeMillis() - startTime < timeout && receivedPackets.size() < totalPackets) {
	            // Check if there's a request from the client
	            String request = in.readLine();
	            if (request != null && request.startsWith("REQUEST:")) {
	                int missingPacket = Integer.parseInt(request.substring("REQUEST:".length()));
	                if (missingPacket >= 1 && missingPacket <= totalPackets) {
	                	//if (random.nextDouble() < 0.8) {  
		                    receivedPackets.add(missingPacket);
		                    String word = words[missingPacket - 1];
		                    String packet = createPacket(missingPacket, totalPackets, words[missingPacket-1], wordIndexMap.get(word) );
		                    out.println("MISSING:" + packet);
		                    System.out.println("Resent: " +  packet);
	                	 //}
	                }
	            }
	        }
	
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}
	private static String createPacket(int packetNumber, int totalPackets, String words, int index) {
	    return String.format("%d|%d %s %d", packetNumber, totalPackets, words, index);
	    }
	  
	}
	
