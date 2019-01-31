package DBWriter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
	public static void main(String[] args) throws Exception
	{
		ServerSocket serverSocket = new ServerSocket(8002, 100,
				InetAddress.getByName("0.0.0.0"));
		System.out.println("Server started	at:	" + serverSocket);

		while (true) {
			System.out.println("Waiting for a	connection...");

			final Socket activeSocket = serverSocket.accept();

			System.out.println("Received a	connection from	" + activeSocket);
			Runnable runnable = () -> handleClientRequest(activeSocket);
			new Thread(runnable).start(); // start a new thread
		}
	}

	public static void handleClientRequest(Socket socket) {
		System.out.println("connection: setup");
		try{
			BufferedReader socketReader = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			
			InputInterpreter inp = new InputInterpreter();
			
			String inMsg = null;
			while ((inMsg = socketReader.readLine()) != null) {
				inp.interpretLine(inMsg);
			
				/*String outMsg = inMsg;
				socketWriter.write(outMsg);
				socketWriter.write("\n");
				socketWriter.flush();*/
			}
			socket.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
		System.out.println("connection: close");
	}
}
