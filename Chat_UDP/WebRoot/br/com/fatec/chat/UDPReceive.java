package br.com.fatec.chat;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

public class UDPReceive {
	private final int SEARCH 		= -906336856; 		// "seach".hashCode()
	private final int GREET 		= 98619145; 		// "greet".hashCode()
	private final int KEEPALIVE 	= -1775507384; 		// "keepAlive".hashCode()
	private final int SAY 			= 113643; 			// "say".hashCode()
	private final int WHISPER 		= 1316693890; 		// "whisper".hashCode()
	private final int LEAVE 		= 102846135; 		// "leave".hashCode()
	private final int REPORT 		= -934521548; 		// "report".hashCode()
	
	private HashMap<String, HashMap<String, String>> usuarios = null;
	private final int porta = 9000;
	
	public UDPReceive() {
		
	}
	
	private void executa() {
		// Cria um Socket para ficar ouvindo na porta
		DatagramSocket dsocket = null;
		
		try {
			 dsocket = new DatagramSocket(porta);
			
			 // Cria um Buffer para ler um Datagram. Se um pacote for maior que ele,
			 // o excesso será descartado!
			 byte[] buffer = new byte[2048];

			 // Cria um pacote para receber dados e armazená-los no Buffer
			 DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
			
			usuarios = new HashMap<String, HashMap<String, String>>();
			
			// Now loop forever, waiting to receive packets and printing them.
			while (true) {
				// Wait to receive a datagram
				dsocket.receive(packet);
				
				trataMensagem(packet, new String(buffer, 0, packet.getLength()));
				
				packet.setLength(buffer.length);
			}
		}
		catch (Exception e) {
			System.err.println(e);
		}
	}
	
	private void trataMensagem(DatagramPacket packet, String strJson) throws JSONException {
		JSONObject json = new JSONObject(strJson);
		HashMap<String, String> map = new HashMap<String, String>();
		
		switch (json.get("action").hashCode()) {
		case SEARCH:
			break;
			
		case GREET:
			map.put("nickname", json.getString("nickname"));
			usuarios.put(packet.getAddress().toString(), map);
			System.out.println(json.getString("nickname") + " entro na sala!");
			break;
			
		case KEEPALIVE:
			break;
		
		case SAY:
			map = usuarios.get(packet.getAddress().toString());
			System.out.println(map.get("nickname") + ": " + json.get("content"));
			break;
		
		case WHISPER:
			break;
		
		case LEAVE:
			System.out.println(usuarios.get(packet.getAddress().toString()).get("nickname") + " saiu da sala!");
			usuarios.remove(packet.getAddress().toString());
			break;
		
		case REPORT:
			break;
		
		default:
			new JSONException("Parametro {\"action\" : \"" + json.getString("content") + "\"} inválido");
		}
	}
	
	public static void main(String args[]) {
		new UDPReceive().executa();
		
//		System.out.println("private final int SEARCH = " + "search".hashCode() + "; // \"seach\".hashCode()");
//		System.out.println("private final int GREET = " + "greet".hashCode() + "; // \"greet\".hashCode()");
//		System.out.println("private final int KEEPALIVE = " + "keepAlive".hashCode() + "; // \"keepAlive\".hashCode()");
//		System.out.println("private final int WHISPER = " + "whisper".hashCode() + "; // \"whisper\".hashCode()");
//		System.out.println("private final int LEAVE = " + "leave".hashCode() + "; // \"leave\".hashCode()");
//		System.out.println("private final int REPORT = " + "report".hashCode() + "; // \"report\".hashCode()");
	}
}
