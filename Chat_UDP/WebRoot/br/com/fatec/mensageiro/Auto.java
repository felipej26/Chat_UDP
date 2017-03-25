package br.com.fatec.mensageiro;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Auto implements Runnable{
	private Cliente cliente = null;
	private DatagramSocket socket = null;
	
	public InetAddress IP = null;
	public long segundos = 0;
	public boolean executar = false;
	public boolean searching = false;
	
	public Auto(Cliente cliente, DatagramSocket socket) {
		this.cliente = cliente;
		this.socket = socket;
	}

	@Override
	public void run() {
		JSONObject json = null;
		byte[] buf = null;
		DatagramPacket packet = null;
		
		try {
			while (executar) {
				Thread.sleep(segundos);
				
				json = null;
				json = new JSONObject();
				
				if (searching) {
					json.put("action", "search");
					json.put("nickname", App.NICKNAME);
					
					// TODO Remover
					System.out.println(json.toString());
					
					buf = json.toString().getBytes();
					packet = new DatagramPacket(buf, buf.length, InetAddress.getByAddress(IP.getAddress()), App.PORTA);
					socket.send(packet);
				}
				else {
					// Verifica todos os usuarios que não responderam nos ultimos 10 segundos
					// E muda os status dele para Ausente
					cliente.setUsuariosAusentes();
					
					JSONArray jsonArray = new JSONArray();
					
					for (String IP_Usuario : cliente.getIPsUsuarios()) {
						json = new JSONObject();
						json.put("nickname", cliente.getUsuario(IP_Usuario));
						json.put("address", IP_Usuario);
						
						jsonArray.put(json);
					}
					
					json = null;
					json = new JSONObject();
					json.put("action", "keepAlive");
					json.put("nickname", App.NICKNAME);
					json.put("users", jsonArray);
					
					// TODO Remover
					System.out.println(json.toString());
				
					for (String IP_Usuario : cliente.getIPsUsuarios()) {
						buf = json.toString().getBytes();
						packet = new DatagramPacket(buf, buf.length, InetAddress.getByName(IP_Usuario), App.PORTA);
						socket.send(packet);
					}
					
					cliente.setUsuariosAusentesAux();
				}
			}
		}
		catch (InterruptedException | IOException | JSONException e) {
			e.printStackTrace();
		}
	}
}
