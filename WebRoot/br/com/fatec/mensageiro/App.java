package br.com.fatec.mensageiro;

import java.io.IOException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
@WebServlet("/chat")
public class App extends HttpServlet {
	private Servidor servidor = null;
	private Cliente cliente = null;
	public static final int PORTA = 9000;
	public static final String IP = "192.168.3.60";
	public static final String NICKNAME = "Jefferson Felipe";
	
	public App() {
		cliente = new Cliente();
		servidor = new Servidor(cliente);
		
		iniciaProcessos();
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		cliente.recebeSolicitacao(request, response);
	}
	
	private void iniciaProcessos() {
		Thread t1 = new Thread(servidor);
		t1.start();
		
		cliente.executa();
	}
}
