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
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;


class ClientServerHomeHubServant extends ClientServerHomeHubPOA {

	private ORB orb;
	private ClientAndServer.ServerRegionalOffice server;
	private HomeHub parent;
	private boolean buttonFirstClick = true;
	private long timeDiff = 0; 
	private String messageStatus;
	private String camID;
	private ClientAndServer.ClientCamera relay2;


	public ClientServerHomeHubServant(HomeHub parentGUI, ORB orb_val) {
		// store reference to parent GUI
		parent = parentGUI;

		// store reference to ORB
		orb = orb_val;
		
		try {
			// Initialize the ORB
			System.out.println("Initializing the ORB");
			Properties prop = new Properties();
			prop.put("org.omg.CORBA.ORBInitialPort","1050");
			prop.put("org.omg.CORBA.ORBInitialPort","localhost");

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
			String name = "Office";
			server = ServerRegionalOfficeHelper.narrow(nameService.resolve_str(name));
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
	
	public void sendOkayMessage(String camID){
		
		String homeHubName = parent.getTitle();
		
		parent.addMessage("Camera " + camID + " in " + homeHubName + " is okay \n");

		server.showOkayMessage(camID, homeHubName);
	}

	public String sendPanicMessage(String camID){

		Timestamp time = new Timestamp(System.currentTimeMillis());
		long currTime = time.getTime();

		if((timeDiff + 5000) > currTime){
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
	
	public void sendSensorPanicMessage(String sensorID, String roomName){
		Timestamp panicTime = new Timestamp(System.currentTimeMillis());
		parent.addMessage("Sensor " + sensorID +" in "+ roomName +" has been alerted \n");
		messageStatus = "Assistence needed " + panicTime + "\n";		
		server.sensorPanicServer(sensorID, roomName);
		server.showSensorStatus(messageStatus);
	}
	
	public void setConnection(String name){
		server.connection(name);
	}
	
	public String setCamConnection(String name){
		try {
			// Initialize the ORB
			System.out.println("Initializing the ORB");
			
			Properties prop = new Properties();
			prop.put("org.omg.CORBA.ORBInitialPort","1050");
			prop.put("org.omg.CORBA.ORBInitialPort","localhost");
			
			//ORB orb = ORB.init(args, null);

			// Get a reference to the Naming service
			org.omg.CORBA.Object nameServiceObj = 
					orb.resolve_initial_references ("NameService");
			if (nameServiceObj == null) {
				System.out.println("nameServiceObj = null");
				
			}

			// Use NamingContextExt instead of NamingContext. This is 
			// part of the Interoperable naming Service.  
			NamingContextExt nameService = NamingContextExtHelper.narrow(nameServiceObj);
			if (nameService == null) {
				System.out.println("nameService = null");
				
			}
			
			// resolve the Count object reference in the Naming service
			relay2 = ClientCameraHelper.narrow(nameService.resolve_str(name));	
			
		} catch (Exception e) {
			System.out.println("ERROR : " + e) ;
			e.printStackTrace(System.out);
		}
		return relay2.toString();
	}
	
	public void resetCamera(String c){
		setConnection(c);
		relay2.resetCamStatus();
		
	}

	@Override
	public void getCameraStatus(String camID) {
		setConnection(camID);
		relay2.getCameraStatus(camID);
	}

	public void sendCameraStatus(String camID, String status) {
		
		parent.addMessage("Camera " + camID + " status = " + status + "\n");

		server.showCameraStatus(camID, status);
	}
}

public class HomeHub extends JFrame {
	private JPanel panel;
	private JScrollPane scrollpane;
	private JTextArea textarea;
	private ClientServerHomeHubServant relayRef;
	public static String homeHubName;

	public HomeHub(String[] args, String homeHubName2) {
		
		homeHubName = homeHubName2;
		
		try {
		    // Initialize the ORB
			Properties prop = new Properties();
			prop.put("org.omg.CORBA.ORBInitialPort","1050");
			prop.put("org.omg.CORBA.ORBInitialPort","localhost");
			
		    ORB orb = ORB.init(args, prop);
		    
		    // get reference to rootpoa & activate the POAManager
		    POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
		    rootpoa.the_POAManager().activate();
		    
		    // Create the Count servant object
		    relayRef = new ClientServerHomeHubServant(this, orb);

		    // get object reference from the servant
		    org.omg.CORBA.Object ref = rootpoa.servant_to_reference(relayRef);
		    ClientAndServer.ClientServerHomeHub cref = ClientServerHomeHubHelper.narrow(ref);
		    
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
			scrollpane = new JScrollPane();
			scrollpane.setBounds(350, 163, 4, 4);
			panel = new JPanel();
			panel.setLayout(null);
			
			//  wait for invocations from client

			// set up the GUI
			textarea = new JTextArea(20,25);
			textarea.setBounds(6, 5, 388, 467);
			panel.add(textarea);

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
	
	public void callConnect(){
		relayRef.setConnection(homeHubName);
	}


	public static void main(String args[]) {
		final String[] arguments = args;
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				
				JFrame frame = new JFrame();
				
				homeHubName = JOptionPane.showInputDialog(frame,"Homehub Name");
				
				HomeHub hub = new HomeHub(arguments, homeHubName);
				
				hub.setVisible(true);
				
				hub.setTitle(homeHubName);
				
				hub.callConnect();
			}
		});
	}

}

