package br.com.fatec.mensageiro;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.UnknownHostException;
import org.json.JSONException;
import org.json.JSONObject;

public class Servidor implements Runnable {
	private final int SEARCH 		= -906336856; 		// "seach".hashCode()
	private final int KEEPALIVE		= -1745954712;		// "keepalive".hashCode()
	private final int SAY 			= 113643; 			// "say".hashCode()
	private final int WHISPER 		= 1316693890; 		// "whisper".hashCode()
	private final int LEAVE 		= 102846135; 		// "leave".hashCode()
	private final int REPORT 		= -934521548; 		// "report".hashCode()
	
	private Cliente cliente = null;
	
	public Servidor(Cliente cliente) {
		this.cliente = cliente;
	}
	
	@Override
	public void run() {
		executa();
	}
	
	private void executa() {
		// Cria um Socket para ficar ouvindo na porta
		DatagramSocket socket = null;
		
		try {
			 socket = new DatagramSocket(App.PORTA);
			
			 // Cria um Buffer para ler um Datagram. Se um pacote for maior que ele,
			 // o excesso será descartado!
			 byte[] buffer = new byte[2048];

			 // Cria um pacote para receber dados e armazená-los no Buffer
			 DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
			
			// Now loop forever, waiting to receive packets and printing them.
			while (true) {
				// Wait to receive a datagram
				socket.receive(packet);
				
				trataMensagem(packet, new String(buffer, 0, packet.getLength()));
				
				packet.setLength(buffer.length);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void trataMensagem(DatagramPacket packet, String strJson) {
		String host = packet.getAddress().getHostAddress();
		String IPAddress = packet.getAddress().toString().replace("/", "");
		
		try {
			JSONObject json = new JSONObject(strJson);
			
			// TODO Remover
			System.out.println(IPAddress + ": " + json.toString());
			
			if (!App.IP.equals(IPAddress)) {
				
				if (json.optString("action").equals("")) {
					cliente.reportarErro(IPAddress, "JSON Parse Error");
					return;
				}
				
				switch (json.getString("action").toLowerCase().hashCode()) {
				case SEARCH:
					if (json.optString("nickname") != null) {
						cliente.encontrouUsuario(host, json.getString("nickname"));
					}
					else {
						cliente.reportarErro(IPAddress, "JSON Parse error");
					}
					break;
					
				case KEEPALIVE:
					if (json.optString("nickname") != "" || json.optString("users") != "") {
						cliente.atualizaListaUsuarios(host, json.getString("nickname"), json.getJSONArray("users"));
					}
					else {
						cliente.reportarErro(IPAddress, "JSON Parse error");
					}
					break;
				
				case SAY:
					if (json.optString("content") != null) {
						if (json.optString("target") != "") {
							cliente.recebeMensagem(IPAddress, json.getString("target"), json.getString("content"));
						}
						else {
							cliente.recebeMensagem(IPAddress, "", json.getString("content"));
						}
					}
					else {
						cliente.reportarErro(IPAddress, "JSON Parse error");
					}
					break;
		
				case WHISPER:
					if (json.optString("content") != null) {
						cliente.recebeSussuro(IPAddress, json.getString("content"));
					}
					else {
						cliente.reportarErro(IPAddress, "JSON Parse error");
					}
					break;
				
				case LEAVE:
					cliente.removeUsuario(IPAddress);
					break;
				
				case REPORT:
					if (json.optString("message") != null) {
						cliente.recebeuReport(IPAddress, json.getString("message"));
					}
					else {
						cliente.reportarErro(IPAddress, "JSON Parse error");
					}
					break;
				
				default:
					cliente.reportarErro(IPAddress, "JSON Parse error");
				}
			}
		}
		catch (Exception e) {
 			System.out.println(e.getStackTrace());
			cliente.reportarErro(IPAddress, "JSON Parse error");
		}
	}
}
