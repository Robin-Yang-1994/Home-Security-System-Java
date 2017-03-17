import ClientAndServer.*;

import org.omg.PortableServer.*;
import org.omg.CORBA.*;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

import java.io.*;
import javax.swing.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;


class RelayServant extends RelayPOA {

	private ORB orb;
	private ClientAndServer.HelloWorld server;
	private HomeHub parent;
	private boolean buttonFirstClick = true;
	private long timeDiff = 0; 
	private String messageStatus;
	private String camID;

	public RelayServant(HomeHub parentGUI, ORB orb_val) {
		// store reference to parent GUI
		parent = parentGUI;

		// store reference to ORB
		orb = orb_val;


		// look up the server
//		try {
//			// read in the 'stringified IOR'
//			BufferedReader in = new BufferedReader(new FileReader("server.ref"));
//			String stringified_ior = in.readLine();
//
//			// get object reference from stringified IOR
//			org.omg.CORBA.Object server_ref = 		
//					orb.string_to_object(stringified_ior);
//			server = ClientAndServer.HelloWorldHelper.narrow(server_ref);
//		} catch (Exception e) {
//			System.out.println("ERROR : " + e) ;
//			e.printStackTrace(System.out);
//		}
		
		try {
			// Initialize the ORB
			System.out.println("Initializing the ORB");

			// Get a reference to the Naming service
			org.omg.CORBA.Object nameServiceObj = 
					orb.resolve_initial_references ("NameService");
			if (nameServiceObj == null) {
				System.out.println("nameServiceObj = null");
				return;
			}

			// Use NamingContextExt instead of NamingContext. This is 
			// part of the Interoperable naming Service.  
			NamingContextExt nameService = NamingContextExtHelper.narrow(nameServiceObj);
			if (nameService == null) {
				System.out.println("nameService = null");
				return;
			}

			// resolve the Count object reference in the Naming service
			server = HelloWorldHelper.narrow(nameService.resolve_str("Regional Office"));
			
			} catch (Exception e) {
			System.out.println("ERROR : " + e) ;
			e.printStackTrace(System.out);
		}
		
	}

	public String switchOn(String camID){
		String on = server.switchOn(camID);
		parent.addMessage(camID + " switched on \n");
		return on;
	}

	public String switchOff(String camID){
		String off = server.switchOff(camID);
		parent.addMessage(camID + " switched off \n");
		return off;
	}

	public String fetch_message() {
		parent.addMessage("fetch_message called by client.  Calling server..\n");

		String messageFromServer = server.hello_world();

		parent.addMessage("message from server = " + messageFromServer + "\n"
				+ "   Now forwarding to client..\n\n");

		return messageFromServer;
	}

	public String sendPanicMessage(String camID){

		Timestamp time = new Timestamp(System.currentTimeMillis());
		long currTime = time.getTime();

		if((timeDiff + 5000) > currTime){
			//System.out.println("should not print this time" + timeDiff);
			this.notifyServer(camID);
			return"";
		}

		parent.addMessage("Sensor "+ camID +" called panic \n");
		Timestamp lastCurrTime = new Timestamp(System.currentTimeMillis());
		timeDiff = lastCurrTime.getTime();
		return "";
	}

	public String notifyServer(String camID){
		Timestamp panicTime = new Timestamp(System.currentTimeMillis());
		messageStatus = "Assistence needed " + panicTime + "\n";
		server.panicServer(camID);
		server.showCamStatus(messageStatus);
		return "Notified Server";
	}


}

public class HomeHub extends JFrame {
	private JPanel panel;
	private JScrollPane scrollpane;
	private JTextArea textarea;
	private RelayServant relayRef;
	private static String homeHubName;

	public HomeHub(String[] args, String homeHubName2) {
		
		homeHubName = homeHubName2;
		
		try {
		    // Initialize the ORB
		    ORB orb = ORB.init(args, null);
		    
		    // get reference to rootpoa & activate the POAManager
		    POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
		    rootpoa.the_POAManager().activate();
		    
		    // Create the Count servant object
		    relayRef = new RelayServant(this, orb);

		    // get object reference from the servant
		    org.omg.CORBA.Object ref = rootpoa.servant_to_reference(relayRef);
		    ClientAndServer.Relay cref = RelayHelper.narrow(ref);
		    
		    // Get a reference to the Naming service
		    org.omg.CORBA.Object nameServiceObj = 
			orb.resolve_initial_references ("NameService");
		    if (nameServiceObj == null) {
			System.out.println("nameServiceObj = null");
			return;
		    }

		    // Use NamingContextExt which is part of the Interoperable
		    // Naming Service (INS) specification.
		    NamingContextExt nameService = NamingContextExtHelper.narrow(nameServiceObj);
		    if (nameService == null) {
			System.out.println("nameService = null");
			return;
		    }
		    
		    // bind the Count object in the Naming service
		    String name = homeHubName;
		    NameComponent[] countName = nameService.to_name(name);
		    nameService.rebind(countName, cref);
		    
		    //  wait for invocations from clients
		 
//		try {
//			// create and initialize the ORB
//			ORB orb = ORB.init(args, null);
//
//			// get reference to rootpoa & activate the POAManager
//			POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
//			rootpoa.the_POAManager().activate();
//
//			// create servant and register it with the ORB
//			RelayServant relayRef = new RelayServant(this, orb);
//
//			// Get the 'stringified IOR'
//			org.omg.CORBA.Object ref = rootpoa.servant_to_reference(relayRef);
//			String stringified_ior = orb.object_to_string(ref);
//
//			// Save IOR to file
//			BufferedWriter out = new BufferedWriter(new FileWriter("relay.ref"));
//			out.write(stringified_ior);
//			out.close();


			// set up the GUI
			textarea = new JTextArea(20,25);
			scrollpane = new JScrollPane(textarea);
			panel = new JPanel();

			panel.add(scrollpane);
			getContentPane().add(panel, "Center");

			setSize(400, 500);
			setTitle("HomeHub");

			addWindowListener (new java.awt.event.WindowAdapter () {
				public void windowClosing (java.awt.event.WindowEvent evt) {
					System.exit(0);;
				}
			} );
	
		
	} catch(Exception e) {
	    System.err.println(e);
	}
}
	
	public void addMessage(String message){
		textarea.append(message);
	}


	public static void main(String args[]) {
		final String[] arguments = args;
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				
				JFrame frame = new JFrame();
				
				homeHubName = JOptionPane.showInputDialog(frame,"Homehub Name");
				
				HomeHub hub = new HomeHub(arguments, homeHubName);
				
				hub.setVisible(true);
			}
		});
	}

}

