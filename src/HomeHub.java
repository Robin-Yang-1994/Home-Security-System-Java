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
import java.util.List;
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
	private String homeHubName;
	private String contact;
	private ClientAndServer.ClientCamera camera;
	private ArrayList <String> homeHubList  = new ArrayList<String>();
	private static String hitRoom, hitCam; 

	public ClientServerHomeHubServant(HomeHub parentGUI, ORB orb_val, String contactNew, String newHubName) {
		// store reference to parent GUI
		parent = parentGUI;

		contact = contactNew;
		homeHubName = newHubName;

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

			// set naming services connections for home hub to become server to regional office
			String name = "Office";
			server = ServerRegionalOfficeHelper.narrow(nameService.resolve_str(name));
		} catch (Exception e) {
			System.out.println("ERROR : " + e) ;
			e.printStackTrace(System.out);
		}

	}

	public String switchOn(String camID){ // camera switch on
		String on = server.switchOn(camID);
		parent.addMessage(camID + " switched on \n");
		return on;
	}

	public String switchOff(String camID){  // camera switch off
		String off = server.switchOff(camID);
		parent.addMessage(camID + " switched off \n");
		return off;
	}

	//	public String fetch_message() {
	//		parent.addMessage("fetch_message called by client.  Calling server..\n");
	//
	//		String messageFromServer = server.hello_world();
	//
	//		parent.addMessage("message from server = " + messageFromServer + "\n"
	//				+ "   Now forwarding to client..\n\n");
	//
	//		return messageFromServer;
	//	}

	public void sendOkayMessage(String camID){ // home hub sends okay message to the regional office

		String homeHubName = parent.getTitle();

		parent.addMessage("Camera " + camID + " in " + homeHubName + " is okay \n");

		server.showOkayMessage(camID, homeHubName); // calls method in the regional office with passing arguement
	}

	public String sendPanicMessage(String camID, String roomName){ // panic message with method to send panic server if pressed twice within 5 seconds

		Timestamp time = new Timestamp(System.currentTimeMillis()); // define system time
		long currTime = time.getTime(); // get system time 

		if((timeDiff + 5000) > currTime){ // uses last time set to compare with new time to clarify the difference
			if (hitRoom.equals(roomName) || hitCam.equals(camID)){
				//hitRoom = "";
				//hitCam = "";
				String add = "false alarm \n";
				homeHubList.add(add); 
				return "";
			}else{
				hitRoom = "";
				this.notifyServer(camID, homeHubName, contact); // if time is within 5 seconds, it will call method below
				String add = "Camera " + camID + " has been panic twice within 5 seconds \n";
				homeHubList.add(add); 
				return"";}
		}
		hitRoom = roomName;
		hitCam = camID;
		parent.addMessage("Sensor "+ camID +" called panic \n");
		Timestamp lastCurrTime = new Timestamp(System.currentTimeMillis());
		timeDiff = lastCurrTime.getTime(); // set new time for use in the if statement above
		String add = "Camera " + camID + " in " + homeHubName + " has sent sent panic \n";
		homeHubList.add(add); 
		return "";
	}

	public String notifyServer(String camID, String homeHubName, String contact){ // this method is called from the if statement
		Timestamp panicTime = new Timestamp(System.currentTimeMillis());
		messageStatus = "Assistence needed " + panicTime + "\n";
		server.panicServer(camID, homeHubName, contact); // calls panic method in the server (regional office class)
		server.showCamStatus(messageStatus);
		return "Notified Server";
	}

	public String[] log() {
		ArrayList<String> tempLogs = homeHubList;
		String[] tempLogs2 = new String[tempLogs.size()];
		tempLogs2 = tempLogs.toArray(tempLogs2);
		return tempLogs2;
	}

	public void sendSensorPanicMessage(String sensorID, String roomName){ // sensor calls panic method
		Timestamp panicTime = new Timestamp(System.currentTimeMillis()); // initialize time
		parent.addMessage("Sensor " + sensorID +" in "+ roomName +" has been alerted \n");
		messageStatus = "Assistence needed " + panicTime + "\n";		
		server.sensorPanicServer(sensorID, roomName); // calls method in the regional office class with passing parameters
		server.showSensorStatus(messageStatus);
	}

	public void setConnection(String name){ // calls method make home hub to become server to the regional office
		server.connection(name);
	}

	public String setCamConnection(String name){ // method to be called by the camera class to become server to home hub
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

			// resolve the client object reference in the Naming service
			camera = ClientCameraHelper.narrow(nameService.resolve_str(name));	

		} catch (Exception e) {
			System.out.println("ERROR : " + e) ;
			e.printStackTrace(System.out);
		}
		return camera.toString();
	}

	public void resetCamera(String c){ // connection must be made to reach method in camera class
		setCamConnection(c);
		camera.resetCamStatus();
	}

	public void getCameraStatus(String camID) { // connection must be made to reach method in camera class
		setCamConnection(camID);
		camera.getCameraStatus(camID);
	}

	public void sendCameraStatus(String camID, String status) { // pass camera status to regional office, source from camera sensor

		parent.addMessage("Camera " + camID + " status = " + status + "\n");

		server.showCameraStatus(camID, status);
	}
}

public class HomeHub extends JFrame {
	private JPanel panel;
	private JScrollPane scrollpane;
	private JTextArea textarea;
	private ClientServerHomeHubServant homehubClient;
	public static String homeHubName;
	public static String contact;

	public HomeHub(String[] args, String homeHubName2, String contact2) {

		homeHubName = homeHubName2;

		contact = contact2;

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
			homehubClient = new ClientServerHomeHubServant(this, orb, contact, homeHubName);

			// get object reference from the servant
			org.omg.CORBA.Object ref = rootpoa.servant_to_reference(homehubClient);
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

			// bind the home hub object in the Naming service
			String name = homeHubName;
			NameComponent[] countName = nameService.to_name(name);
			nameService.rebind(countName, cref);
			scrollpane = new JScrollPane();
			scrollpane.setBounds(350, 163, 4, 4);
			panel = new JPanel();
			panel.setLayout(null);

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
		homehubClient.setConnection(homeHubName);
	}


	public static void main(String args[]) {
		final String[] arguments = args;
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {

				JFrame frame = new JFrame();

				homeHubName = JOptionPane.showInputDialog(frame,"Homehub Name"); // initialize home hub name

				JFrame frame1 = new JFrame();

				contact = JOptionPane.showInputDialog(frame1,"Mobile Number"); // initialize contact

				HomeHub hub = new HomeHub(arguments, homeHubName, contact);

				hub.setVisible(true);

				hub.setTitle("Homehub Name " + homeHubName);

				hub.callConnect(); // calls the connection method upon creation of the class 
			}
		});
	}

}

