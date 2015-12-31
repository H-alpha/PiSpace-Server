public class Initializer {

	private static String version = "v1.0.1";
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		/*
		Scanner scan = new Scanner(System.in);
		int port;
		int backlog;
		String pass;
		System.out.print("Enter port: ");
		port = scan.nextInt();
		System.out.println("Enter password: ");
		pass = scan.nextLine();*/
		
		//code above is unused for now
		
		System.out.println("PiSpace Server "+version);
		PiServer pi = new PiServer();
		pi.startRunning();
		
	}

}
