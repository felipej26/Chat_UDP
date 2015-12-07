package br.com.fatec.mensageiro;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Cliente {
	private HashMap<String, HashMap<String, String>> usuarios = null;
	private JSONObject json = new JSONObject();
	private UI ui = null;
	private Auto auto = null;

	private String configInicial = "";
	private InetAddress broadcast;
	private DatagramSocket socket;

	public Cliente() {
		ui = new UI(this);
	}

	public void executa() {
		boolean aux = false;

		try {
			Scanner s = new Scanner(System.in);
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

			while (interfaces.hasMoreElements() && !aux) {
				NetworkInterface networkInterface = interfaces.nextElement();

				if (networkInterface.isLoopback()) {
					continue; // Don't want to broadcast to the loopback interface
				}

				for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
					broadcast = interfaceAddress.getBroadcast();

					if (broadcast == null) {
						continue;
					}

					socket = new DatagramSocket();
					socket.setBroadcast(true);

					usuarios = new HashMap<String, HashMap<String, String>>();

					// Entrou numa sala
					mostraMensagem("IP Broadcast: " + broadcast.getHostName());

					json = new JSONObject();
					json.put("nome", App.NICKNAME);
					json.put("IP", App.IP);
					json.put("timestamp", formatHora(new Date()));
					configInicial = json.toString();
					
					ui.addConfigInicial(json.toString());

					auto = new Auto(this, socket);
					auto.IP = broadcast;
					// TODO Alterar o tempo
					auto.segundos = 2000;
					auto.executar = true;
					auto.searching = true;
					Thread t1 = new Thread(auto);
					t1.start();
					
					Thread.sleep(2000);
					ui.addConfigInicial(configInicial);
					
					aux = true;
					break;
				}
			}
			s.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String formatHora(Date horas) {
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		Date hora = Calendar.getInstance().getTime(); // Ou qualquer outra forma que tem
		return sdf.format(hora);
	}

	private void mostraMensagem(String msg) {
		ui.escreveMensagens(formatHora(new Date()) + " - " + msg);
	}

	public void encontrouUsuario(String host, String nickname) {
		addUsuario(nickname, host);
		auto.searching = false;
		// TODO Alterar o tempo
		auto.segundos = 10000;

		ui.btnEnviar_enabled(true);
	}

	private void addUsuario(String nickname, String host) {
		HashMap<String, String> map = new HashMap<String, String>();
		String IP = "";

		try {
			IP = InetAddress.getByName(host).getHostAddress().replace("/", "");
		}
		catch (UnknownHostException e) {
			e.printStackTrace();
		}

		if (usuarios.get(IP) == null) {
			map.put("host", host);
			map.put("nickname", nickname);
			map.put("status", "1");
			map.put("statusAlive", "1");
			map.put("timestamp", formatHora(new Date()));
			map.put("contagem", "0");
			usuarios.put(IP, map);

			mostraMensagem(nickname + " entrou na sala!");
		}
		else {
			//mostraMensagem(nickname + " reentrou na sala!");
		}	
	}

	public void atualizaListaUsuarios(String host, String nickname, JSONArray jsonArray) {
		HashMap<String, String> map = null;
		String IP = "";

		try {
			IP = InetAddress.getByName(host).getHostAddress().replace("/", "");
		}
		catch (UnknownHostException e) {
			e.printStackTrace();
		}

		try {
			// Verifica se o usuário está na Lista
			if (usuarios.get(IP) != null) {
				map = new HashMap<String, String>();
				map = usuarios.get(IP);
				map.put("timestamp", formatHora(new Date()));
				map.put("status", "1");
				map.put("statusAlive", "1");
				usuarios.put(IP, map);
			}
			else {
				addUsuario(nickname, host);
			}

			// Atualiza a Lista
			for (int index = 0; index < jsonArray.length(); index++) {
				json = null;
				json = jsonArray.getJSONObject(index);

				if (usuarios.get(json.getString("address")) != null) {
					map = new HashMap<String, String>();
					map = usuarios.get(json.getString("address"));
					map.put("timestamp", formatHora(new Date()));
					map.put("status", "1");
					usuarios.put(json.getString("address"), map);
				}
				else {
					// TODO É passado o IP e não o host
					addUsuario(json.getString("nickname"), json.getString("address"));
				}
			}
		}
		catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public Set<String> getIPsUsuarios() {
		return this.usuarios.keySet();
	}

	public String getUsuario(String IP) {
		if (usuarios.get(IP) == null) {
			return "Desconhecido";
		}
		
		return usuarios.get(IP).get("nickname");
	}

	public HashMap<String, String> getInfoUsuario(String IP) {
		return this.usuarios.get(IP);
	}

	public void recebeSolicitacao(HttpServletRequest request, HttpServletResponse response) {
		if (request.getParameter("evento") != null) {
			switch (request.getParameter("evento")) {
			case "chat":
				if (request.getParameter("users") != null) {
					enviaMensagem(request.getParameter("msg"), "");
					mostraMensagem("Você diz à todos: " + request.getParameter("msg"));
				}
				else if (request.getParameter("target") != null) {
					enviaMensagem(request.getParameter("msg"), request.getParameter("ip"));
					mostraMensagem("Você diz à " + getUsuario(request.getParameter("target")) + ": " + request.getParameter("msg"));
				}
				else if (request.getParameter("whisper") != null) {
					sussurraMensagem(request.getParameter("msg"), request.getParameter("whisper"));
					mostraMensagem("Você sussurra à " + getUsuario(request.getParameter("whisper")) + ": " + request.getParameter("msg"));
				}
				break;

			case "sair":
				sair();
				break;

			default:
				break;
			}
		}
		else {
			ui.requestConversas(request, response);
		}
	}

	private void enviaMensagem(String mensagem, String IP) {
		try {
			json = null;
			json = new JSONObject();
			json.put("action", "say");
			json.put("content", mensagem);

			// TODO Remover
			System.out.println(json.toString());

			if (IP.equals("")) {
				for (String IP_Usuario : getIPsUsuarios()) {
					byte[] buf = json.toString().getBytes();
					DatagramPacket packet = new DatagramPacket(buf, buf.length, InetAddress.getByName(usuarios.get(IP_Usuario).get("host")), App.PORTA);
					socket.send(packet);
				}
			}
			else {
				byte[] buf = json.toString().getBytes();
				DatagramPacket packet = new DatagramPacket(buf, buf.length, InetAddress.getByName(usuarios.get(IP).get("host")), App.PORTA);
				socket.send(packet);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void sussurraMensagem(String mensagem, String IP) {
		try {
			json = null;
			json = new JSONObject();
			json.put("action", "whisper");
			json.put("content", mensagem);

			// TODO Remover
			System.out.println(json.toString());

			byte[] buf = json.toString().getBytes();
			DatagramPacket packet = new DatagramPacket(buf, buf.length, InetAddress.getByName(usuarios.get(IP).get("host")), App.PORTA);
			socket.send(packet);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void recebeMensagem(String IP, String IPAlvo, String mensagem) {
		if (IPAlvo.equals("")) {
			mostraMensagem(getUsuario(IP) + " diz à todos: " + mensagem);
		}
		else if (IPAlvo.equals(App.IP)) {
			mostraMensagem(getUsuario(IP) + " diz à você: " + mensagem);
		}
		else {
			if (getUsuario(IP) == null) {
				if (getUsuario(IPAlvo) == null) {
					mostraMensagem("Desconhecido diz à Desconhecido: " + mensagem);
				}
				else {
					mostraMensagem("Desconhecido diz à " + getUsuario(IPAlvo) + ": " + mensagem);
				}
			}
			else {
				mostraMensagem(getUsuario(IP) + " diz à " + getUsuario(IPAlvo) + ": " + mensagem);
			}
		}
	}

	public void recebeSussuro(String IP, String mensagem) {
		mostraMensagem(getUsuario(IP) + " sussura à você: " + mensagem);
	}

	private void sair() {
		byte[] buf = null;
		DatagramPacket packet = null;

		try {
			auto.executar = false;

			json = new JSONObject();
			json.put("action", "leave");

			// TODO Remover
			System.out.println(json.toString());

			for (String IP_Usuario : usuarios.keySet()) {
				buf = json.toString().getBytes();
				packet = new DatagramPacket(buf, buf.length, InetAddress.getByName(usuarios.get(IP_Usuario).get("host")), App.PORTA);
				socket.send(packet);
			}

			socket.close();
			mostraMensagem("Você saiu da sala!");	
		}
		catch (IOException | JSONException e) {
			e.printStackTrace();
		}
	}

	public void removeUsuario(String IP) {
		mostraMensagem(getUsuario(IP) + " saiu da sala!");
		usuarios.remove(IP);

		if (usuarios.size() == 0) {
			auto.segundos = 2000;
			auto.executar = true;
			auto.searching = true;
			auto.IP = broadcast;
			ui.btnEnviar_enabled(false);
		}
	}	

	public void reportarErro(String IP, String mensagem) {
		try {
			json = null;
			json = new JSONObject();
			json.put("action", "report");
			json.put("message", mensagem);

			// TODO Remover
			System.out.println(json.toString());

			byte[] buf = json.toString().getBytes();
			DatagramPacket packet = new DatagramPacket(buf, buf.length, InetAddress.getByName(IP), App.PORTA);
			socket.send(packet);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void recebeuReport(String IP, String mensagem) {
		mostraMensagem(getUsuario(IP) + " reportou um erro: " + mensagem);
	}

	public void setUsuariosAusentes() {
		HashMap<String, String> map = null;

		try {
			for (String IP_Usuario : usuarios.keySet()) {
				map = new HashMap<String, String>();
				map = usuarios.get(IP_Usuario);
				if (map.get("statusAlive").equals("0")) {
					int cont = Integer.parseInt(usuarios.get(IP_Usuario).get("contagem"));
					cont += 1;

					if (cont == 6) {
						removeUsuario(IP_Usuario);
						continue;
					}

					map.put("status", "0");
					map.put("contagem", "" + cont);
				}
				else {
					map.put("status", "1");
					map.put("contagem", "0");
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setUsuariosAusentesAux() {
		HashMap<String, String> map = null;

		for (String IP_Usuario : usuarios.keySet()) {
			map = new HashMap<String, String>();
			map = usuarios.get(IP_Usuario);
			map.put("statusAlive", "0");
		}
	}

}