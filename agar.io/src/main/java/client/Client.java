package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import server.PlayerConnection;
import server.SSLConnection;
import javafx.scene.Parent;

public class Client extends Application{
	public static final String TRUSTTORE_LOCATION = "C:/Users/99031510240/alv";
	public static final int OFFLINE = 0;
	public static final int READY = 1;
	public static final int PLAYING = 2;
	
	public static PrintWriter writerC;
	private static LoginController logInC;
	
	private int sessionStatus;
	private int posPlayer;
	private String nickName;
	private String[] enemies;
	private String player;
	private String[] food;
	
	public String getNickName() {
		return nickName;
	}
	@Override
	public void start(final Stage primaryStage) {
		try {
			FXMLLoader loader = new FXMLLoader();
			Parent root = loader.load(getClass().getResource("login.fxml").openStream());
			Scene scene = new Scene(root);
			primaryStage.setScene(scene);
			primaryStage.show();
			primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
				
				public void handle(WindowEvent event) {
					// TODO Auto-generated method stub
					primaryStage.close();
				}
			});
			logInC = loader.getController();
			logInC.putClient(this);
			connectToServer();
			sessionStatus = OFFLINE;
			posPlayer = 0;
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}

	public void sendToServer(String parameter) {
		writerC.println(parameter);
	}

	public Object getlogInC() {
		return logInC;
	}

	public void putLogInC(LoginController loginController) {
		logInC = loginController;

	}

	public void connectToServer() {
				final SSLSocket client;
				System.setProperty("javax.net.ssl.trustStore", TRUSTTORE_LOCATION);
				SSLSocketFactory sf = (SSLSocketFactory) SSLSocketFactory.getDefault();
				
				try {
					client = (SSLSocket) sf.createSocket("Voyager", 8030);
					String[] supported = client.getSupportedCipherSuites();
					client.setEnabledCipherSuites(supported);
					writerC = new PrintWriter(client.getOutputStream(), true);
					
			Thread tServer = new Thread(new Runnable() {

				public void run() {
					BufferedReader readerC;
					try {
						readerC = new BufferedReader(new InputStreamReader(client.getInputStream()));
						while (client.isConnected()) {
							final String serverAnswer = readerC.readLine();
							logInC.showMessage(serverAnswer);
							if(serverAnswer.endsWith(SSLConnection.WAITING_MATCH)) {
								nickName = serverAnswer.split(" ")[1];
								connectToGame();
								sessionStatus = READY;
							}
						}
					} catch (IOException e) {
						try {
							client.shutdownInput();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
						e.printStackTrace();
					}
				}
			});
					
			tServer.start();
					
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
}
	
	public void connectToGame() {
		final Socket client;
		SocketFactory sf = SocketFactory.getDefault();
		
		try {
			client = sf.createSocket("Voyager", 8040);
			writerC = new PrintWriter(client.getOutputStream(), true);
			writerC.println(nickName);
			
	Thread tServer = new Thread(new Runnable() {

		public void run() {
			BufferedReader readerC;
			try {
				readerC = new BufferedReader(new InputStreamReader(client.getInputStream()));
				System.out.println("Se conecta al juego");
				while (client.isConnected()) {
					final String infoGame = readerC.readLine();
//					System.out.println(infoGame);
					if(!infoGame.startsWith(PlayerConnection.STARTING_MATCH)) {
						updateGame(infoGame);
						sendToServer(getInfoPlayer());
					}
					else {
						posPlayer = Integer.parseInt(infoGame.substring(infoGame.length()-1));
						sessionStatus = PLAYING;
					}
//					aquí debería llenar la información del cliente sobre el juego
				}
			} catch (IOException e) {
				try {
					client.shutdownInput();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				e.printStackTrace();
			}
		}

//		private String setFormatToCommas(String[] infoPlayer) {
//			StringBuilder sb = new StringBuilder();
//			for (int i = 0; i < infoPlayer.length; i++) {
//				sb.append(infoPlayer[i] + ",");
//			}
//			System.out.println(sb.toString());
//			return sb.toString();
//		}
	});
			
	tServer.start();
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
}
	/**
	 * 
	 * @return  Array of strings that represent the position x,y and mass (width and height) of every food in the game
	 * Example : food[0] -> x,y,w,h
	 */
	public String[] getFoodFromGame() {
		return food;
	}
	/**
	 * 
	 * @return Array of strings that represent the position x,y and mass (width and height) of every player in the game
	 * Example : player[0] -> x,y,w,h,id
	 */
	public String[] getPlayersFromGame() {
		return enemies;
	}
	/**
	 * 
	 * @return Array of strings that represent the initial info of the user. Initial position, initial mass
	 * (width and height) and id
	 * infoPlayer[0]-> x , infoPlayer[1]-> y, infoPlayer[2]-> w, infoPlayer[3]-> h, infoPlayer[4]-> id
	 */
	public String getInfoPlayer() {
//		System.out.println(player);
		return player;
	}
	/**
	 * 
	 * @param state position x,y and mass that is represented by a ball
	 */
	public void updatePlayer(String state) {
		player = state;
	}
	
	public int getPosPlayer() {
		return posPlayer;
	}
	public void updateGame(String entry) {
		String[] arreglos = entry.split("/");
		enemies = arreglos[0].split(" ");
		food = arreglos[1].split(" ");
		if(player == null)
			player = enemies[posPlayer];
	}
	
	public int getStatus() {
		return sessionStatus;
	}
	
	public void setSession(int status) {
		this.sessionStatus = status;
	}

}
