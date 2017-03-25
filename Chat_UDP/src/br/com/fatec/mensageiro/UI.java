package br.com.fatec.mensageiro;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class UI{
	private Cliente cliente;
	private JSONObject json = new JSONObject();
	
	private String mensagens = "";
	private String config = "";
	private String btnEnabled = "";
	
	public UI(Cliente cliente) {
		this.cliente = cliente;
	}
	
	public void escreveMensagens(String mensagem) {
		this.mensagens += mensagem + "<br>";
	}
	
	public void addConfigInicial(String config) {
		this.config = config;
	}
	
	public void btnEnviar_enabled(boolean enabled) {
		if (enabled) {
			btnEnabled = "true";
		}
		else {
			btnEnabled = "false";
		}
	}

	public void requestConversas(HttpServletRequest request, HttpServletResponse response) {
		try {
			json = null;
			json = new JSONObject();
			
			if (cliente.getIPsUsuarios().size() > 0) {
				JSONArray jsonArray = new JSONArray();
				
				for (String IP_Usuarios : cliente.getIPsUsuarios()) {
					HashMap<String, String> map = new HashMap<String, String>();
					json = new JSONObject();
					
					map = cliente.getInfoUsuario(IP_Usuarios);
					json.put("nickname", map.get("nickname"));
					json.put("address", IP_Usuarios);
					json.put("timestamp", map.get("timestamp"));
					json.put("status", map.get("status"));
					
					jsonArray.put(json);
				}
				
				json = new JSONObject();
				json.put("users", jsonArray);
			}
			
			if (!config.equals("")) {
				json.put("config", config);
			}
			
			if(!btnEnabled.equals("")) {
				json.put("libera", btnEnabled);
				btnEnabled = "";
			}
			
			json.put("msg", mensagens);
			
			// TODO Remover
			System.out.println("INTERNAL: " + json.toString());
			
			response.getWriter().println(json.toString());
			mensagens = "";
		}
		catch (IOException | JSONException e) {
			e.printStackTrace();
		}
	}
}
