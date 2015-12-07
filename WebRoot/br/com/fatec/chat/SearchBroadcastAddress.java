package br.com.fatec.chat;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.Scanner;
import org.json.JSONException;
import org.json.JSONObject;

public class SearchBroadcastAddress {
	public static void main(String[] args) throws IOException, JSONException {
		Scanner s = new Scanner(System.in);
		Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
		
		JSONObject json = new JSONObject();
		
		while (interfaces.hasMoreElements()) {
			NetworkInterface networkInterface = interfaces.nextElement();
			
			if (networkInterface.isLoopback()) {
				continue; // Don't want to broadcast to the loopback interface
			}
			
			for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
				InetAddress broadcast = interfaceAddress.getBroadcast();
				
				if (broadcast == null) {
					continue;
				}
				
				DatagramSocket socket = new DatagramSocket();
				socket.setBroadcast(true);
				
				/*
				// Entrou na sala
				System.out.println("Digite seu nickname:");
				
				json.put("action", "greet");
				json.put("nickname", s.nextLine());
				
				byte[] buf = json.toString().getBytes();
				DatagramPacket packet = new DatagramPacket(buf, buf.length, broadcast, 9000);
				socket.send(packet);
				*/
				
				DatagramPacket packet = null;
				byte[] buf = null;
				do {
					System.out.println(broadcast);
					
					/*
					json = null;
					json = new JSONObject();
					
					json.put("action", "say");
					json.put("content", s.nextLine());
					*/
					
					System.out.println(Inet4Address.getByName("Jefferson").getHostAddress());
					buf = s.nextLine().getBytes();
					packet = new DatagramPacket(buf, buf.length, Inet4Address.getByName("Jefferson"), 9000);
					socket.send(packet);
				} while (buf != null);
				
				json = new JSONObject();
				json.put("action", "leave");
				
				buf = json.toString().getBytes();
				packet = new DatagramPacket(buf, buf.length, broadcast, 9000);
				socket.send(packet);
				socket.close();
				
				System.out.println("Você saiu da sala!");
			}
		}
		
		s.close();
	}
}
