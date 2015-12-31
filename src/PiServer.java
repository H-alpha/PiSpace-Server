import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;

public class PiServer {

	private int port = 6789;
	private int backlog = 1;
	private ServerSocket server;
	private Socket connection;
	private ObjectOutputStream output;
	private ObjectInputStream input;
	private DataOutputStream doutput;
	private Object message;
	
	@SuppressWarnings("unused")
	private byte[] lpass;
	private String clientIP;
	private String clientName;
	private long connection_time;
	
	public PiServer(){
		port = 6789;
	}
	
	public PiServer(int port){
		this.port = port;
	}
	
	public PiServer(int port, int backlog){
		this.port = port;
		this.backlog = backlog;
	}
	
	public PiServer(int port, String password){
		this.port = port;
		try {
			lpass = Passwords.getHash(password);
		} catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void startRunning(){ //bootstrap method
		if(port==80||port==8080){
			port = 6789;
			System.out.println("Port changed to 6789 because entered port is forbidden"); //don't wanna use those ports O.o
		}
		try {
			server = new ServerSocket(port,backlog); //creates server
			while(true){
				waitForConnection(); //method that is running while nobody is connected
				setupStreams(); //sets up streams with the client
				whileConnected(); //speaks for itself
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void whileConnected() { //most code goes here
		do{
			try{
				message = (String) input.readObject();
				System.out.println("USER: "+message); //reads and displays anything the client sends to the server
				if(((String)message).equals("list")){ //lists the available files
					File f = new File("files\\");
					String[] names = f.list();
					String list = "";
					for(String s : names){
						list+=s+"\n";
					}
					sendMessage(list); //sends the list to the client
				}
				if(((String)message).length()>=4){
					if(((String)message).substring(0, 4).equals("give")){
						System.out.println("Waiting to recieve files");
						recieveFiles(); //gets files from client
					}
				}
				if(((String)message).length()>=3){
					if(((String)message).substring(0, 3).equals("get")){
						System.out.println("Waiting to send files");
						donateFiles(); //gives files to client. this method causes me great mental anguish
					}
				}
			}catch(IOException | ClassNotFoundException e){
				System.out.println("Ended connection!\n"); //idk
				break;
			}
		}while(!message.equals("end"));
	}

	private void donateFiles() {
		// TODO Auto-generated method stub
		//sendMessage("Enter file name and extension"); client now does this
		System.out.println("Told user to enter file name");
		try {
			String name =  (String) input.readObject();
			System.out.println("User wants "+name);
			File f = new File("files\\"+name);
			Path path = Paths.get("files\\"+name);
			byte[] data = Files.readAllBytes(path);
			if(f.exists()){
				System.out.println("Sending data");
				output.writeObject(Integer.toString(data.length));
				System.out.println("Sent data length of " + data.length);
				doutput.write(data);
				System.out.println("Sent file data");
			}
			else{
				System.out.println("File does not exist");
				sendMessage("Error in sending file; file does not exist");
			}
		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void recieveFiles() {
		// TODO Auto-generated method stub
		String filename = "";
		byte[] filedata = {};
		//for some reason this works
		try {
			filename = (String) input.readObject(); //reads file name
			filedata = (byte[]) input.readObject(); //reads file data
			createLocalFile(filename, filedata); //writes a copy of the data to the disk under "files"
			System.out.println("Wrote file to disk");
			sendMessage("File has been saved");
		} catch (ClassNotFoundException | IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Error in writing file to disk!");
			sendMessage("Failed to save files");
		}
		
	}

	private void createLocalFile(String name, byte[] data) throws IOException {
		// TODO Auto-generated method stub
		FileOutputStream fileOuputStream = new FileOutputStream("files\\"+name); 
	    fileOuputStream.write(data);
	    fileOuputStream.close();
	}
	

	private void setupStreams() {
		// TODO Auto-generated method stub
		try{
			output = new ObjectOutputStream(connection.getOutputStream());
			output.flush();
			doutput = new DataOutputStream(connection.getOutputStream());
			input = new ObjectInputStream(connection.getInputStream());
		}catch(Exception e){
			
		}
		System.out.println("Streams established");
		System.out.println("Ready for data transfer");
	}

	
	private void waitForConnection() throws IOException {
		// TODO Auto-generated method stub
		System.out.println("Waiting for a connection...");
		long start = System.currentTimeMillis();
		connection = server.accept(); //waits for a client to connect
		long end = System.currentTimeMillis();
		System.out.println("Connected to a client!");
		System.out.println("Time waiting: "+((end-start)/1000)+" seconds");
		gatherClientId(); //gets client's ip address and records it to a log file
	}

	private void gatherClientId() {
		// TODO Auto-generated method stub
		connection_time = System.currentTimeMillis();
		clientIP = connection.getInetAddress().getHostAddress();
		clientName = connection.getInetAddress().getHostName();
		
		String filename = "logs\\"+Long.toString(connection_time)+".txt";
		PrintWriter writer = null; //writes client data to a local file
		try {
			writer = new PrintWriter(filename, "UTF-8");
			writer.println("Client IP Address: "+clientIP);
			writer.println("Client Name: "+clientName);
			writer.println("Time connected: "+connection_time);
			writer.close();
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Unable to write client data to file");
		}
		
		System.out.println("Collected client data and wrote to file "+Long.toString(connection_time)+".txt");
		
	}
	

	private void sendMessage(String m){
		try {
			output.writeObject(m);
			output.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
