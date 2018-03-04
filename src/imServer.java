import java.io.*;
import java.net.*;
import java.util.*;

import javafx.application.*;
import javafx.concurrent.*;
import javafx.event.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.text.*;
import javafx.stage.*;

public class imServer extends Application{
	
	private boolean runThreads;
	
	private VBox mainBox;
	private HBox buttonBox;
	private Button startButton, stopButton;
	private Text info;
	
	private static final int PORT_NUMBER = 26666;
	
	private int serverPort;
	private List<ClientTask> clientTasks = new ArrayList<>();
	
	public void start(Stage primaryStage)
	{
		mainBox = new VBox();
		buttonBox = new HBox();
		
		startButton = new Button("start");
		startButton.setOnAction(this::startServer);
		stopButton = new Button("stop");
		
		buttonBox.getChildren().addAll(startButton, stopButton);
		
		info = new Text();
		info.prefHeight(100);
		info.prefWidth(200);
		
		mainBox.getChildren().addAll(buttonBox, info);
		
		Scene scene = new Scene(mainBox, 500, 500, Color.ALICEBLUE);
		primaryStage.setTitle("Maim Server");
		primaryStage.setScene(scene);
		primaryStage.show();
	}
	
	public void startServer(ActionEvent event)
	{
		startButton.setDisable(true);
		runThreads = true;
		clientTasks = new ArrayList<ClientTask>();
		ServerSocket serverSocket = null;
		try
		{
			serverSocket = new ServerSocket(PORT_NUMBER);
			acceptClients(serverSocket);
		}
		catch (IOException ex)
		{
			info.setText("Could not listen on port #" + PORT_NUMBER + "\n");
		}
	}
	
	private void acceptClients(ServerSocket serverSocket) throws UnknownHostException
	{
		

		info.setText("Server activated on port #" + serverSocket.getLocalPort() + " on address " + InetAddress.getLocalHost() + "\n");
		ServerTask serverTask = new ServerTask(serverSocket);
		Thread serverThread = new Thread(serverTask);
        serverThread.setDaemon(true);
        serverThread.start();
        
        
        
	}
	

	public static void main(String[] args) {
		launch(args);

	}
	
	private class ServerTask extends Task<Void>
	{
		private ServerSocket serverSocket;
		
		public ServerTask(ServerSocket serverSocket)
		{
			this.serverSocket = serverSocket;
		}
		
		@Override
		protected Void call()
		{
			Platform.runLater(new Runnable() {
                @Override public void run() {
                    info.setText(info.getText() + "Running server task\n");
                }
           });
			while(runThreads)
			{

				try
				{
					Socket socket = serverSocket.accept();
					Platform.runLater(new Runnable() {
	                     @Override public void run() {
	                         info.setText(info.getText() + "Accepting connection from " + socket.getRemoteSocketAddress() + "\n");
	                     }
	                });
					ClientTask clientTask = new ClientTask(socket);
					Thread clientThread = new Thread(clientTask);
					clientThread.setDaemon(true);
					clientThread.start();
					clientTasks.add(clientTask);
				}
				catch(IOException ex)
				{
					Platform.runLater(new Runnable() {
	                     @Override public void run() {
	                         info.setText(info.getText() + "Accept failed\n");
	                     }
	                });
				}
				
				
				
			}
			
			
			
			
			
			return null;
		}
		
		
	}
	
	private class ClientTask extends Task<Void> 
	{
		Socket socket;
		PrintWriter clientOut;
		String userName;
		
		
		ClientTask(Socket socket)
		{
			this.socket = socket;
			this.userName = "";
		}
		
		
		
		@Override
		protected Void call()
		{
			try
			{
				this.clientOut = new PrintWriter(socket.getOutputStream(), false);
				Scanner in = new Scanner(socket.getInputStream());
				while(!socket.isClosed())
				{
					if (socket.getInputStream().read() == -1)
					{
						System.out.println("the socket is closing");
						socket.close();
					}
					if (in.hasNextLine())
					{
						System.out.println("Scanner has line from client");
						String input = in.nextLine();
						System.out.println("Line received from client: " + "\"" + input + "\"");
						Scanner inputScan = new Scanner(input);
						inputScan.useDelimiter(":");
						int actionCode = Integer.parseInt(inputScan.next());
						if(actionCode == 0)
						{
							userName = input.substring(2);
							Platform.runLater(new Runnable() {
			                     @Override public void run() {
			                         info.setText(info.getText() + "User logged in as " + userName + "\n");
			                     }
			                });
						}
						if(actionCode == 1)
						{
							System.out.println("Actioncode 1 recognized");
							String buddyName = inputScan.next();
							for(ClientTask aClient : clientTasks)
							{
								PrintWriter clientOutput = aClient.clientOut;
								if (clientOutput != null && (aClient.userName.equalsIgnoreCase(buddyName) || aClient.userName.equalsIgnoreCase(userName)))
								{
									clientOutput.write(input.substring(3 + buddyName.length()) + "\r\n");
									clientOutput.flush();
									System.out.println("Message sent to client");
								}
							}
						}

					}

					
					
				}
				Platform.runLater(new Runnable() {
					@Override public void run() {
                    	info.setText(info.getText() + "Socket closed! " + userName + " logged out!\n");
                    }
				});
				
				
				
			}
			catch(IOException ex)
			{
				
			}
			
			
			
			return null;
		}
	}

}
