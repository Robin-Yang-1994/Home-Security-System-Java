import ClientAndServer.*;
import ClientAndServer.Image;
import java.util.Date;

import org.omg.CORBA.*;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.*;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Properties;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

class ClientCameraServant extends ClientCameraPOA{

	private ClientAndServer.ClientServerHomeHub homehub; // home hub class   
	private ORB orb; //object request broker- middle ware which allows program to call to another computer via a network
	private CamSensor parent; // parent class
	private JFrame imageFrame; // j frame for pop up image information
	private String camID, roomName, homeHubName;

	public ClientCameraServant(CamSensor parentGUI, ORB orb_val,String camID2, String homeHubName2, String roomName2 ){
		
		// arguments passed from the client, defined on camera creation
		
		camID = camID2; // camera name 
		homeHubName = homeHubName2; // home hub name
		roomName = roomName2; // name of the room the camera is located

		parent = parentGUI;
		orb = orb_val;
		try {
			// Initialize the ORB
			System.out.println("Initializing the ORB");
			
			Properties prop = new Properties();
			prop.put("org.omg.CORBA.ORBInitialPort","1050"); // defining network ports and host name
			prop.put("org.omg.CORBA.ORBInitialPort","localhost");

			// Get a reference to the Naming service
			org.omg.CORBA.Object nameServiceObj = 
					orb.resolve_initial_references ("NameService");
			if (nameServiceObj == null) {
				System.out.println("nameServiceObj = null");
				return;
			}

			// Use NamingContextExt instead of NamingContext. This is part of the Interoperable naming Service.  
			NamingContextExt nameService = NamingContextExtHelper.narrow(nameServiceObj);
			if (nameService == null) {
				System.out.println("nameService = null");
				return;
			}
			String name = parent.homeHubName; // match name with the home hub name in home hub class
			// resolve the home hub object reference in the Naming service
			homehub = ClientServerHomeHubHelper.narrow(nameService.resolve_str(name)); // home hub server

		} catch (Exception e) {
			System.out.println("ERROR : " + e) ;
			e.printStackTrace(System.out);
		}
	}

	public void switchOn(String camID) { // calls switch on method in home hub with camera name
		homehub.switchOn(camID);

	}

	public void switchOff(String camID) { // calls switch off method in home hub with camera name
		homehub.switchOff(camID);

	}

	public void sendPanicMessage(String camID) { // calls send panic message method in home hub
		homehub.sendPanicMessage(camID, roomName); // parameter camera name and room name to determine 5 second timer 

	}

	public void setCamServer(String camID) { // calling method in home hub to make camera become server to home hub
		homehub.setCamConnection(camID);
	}

	public void resetCamStatus(){  // set status field to empty for resetting camera
		parent.statusField.setText("");
	}

	public Image currentImage() { // strut type to define images (groups data types together)

		Image i = new Image();

		DateFormat year = new SimpleDateFormat("yyyy"); // breaking down date format into individual forms
		DateFormat month = new SimpleDateFormat("MM");
		DateFormat day = new SimpleDateFormat("dd");
		DateFormat hour = new SimpleDateFormat("HH");
		DateFormat minute = new SimpleDateFormat("mm");
		DateFormat second = new SimpleDateFormat("ss");
		Date date = new Date();
		i.date = (year.format(date) + "/" + month.format(date) + "/" + day.format(date)); // set date, month and year
		i.time = (hour.format(date) + ":" + minute.format(date) + ":" + second.format(date)); // set hour minutes and seconds
		String status = i.status = parent.statusField.getText(); 
		if (status.equals("")){ // if status field equal empty, set to okay instead of null
			status = "okay";
		}

		JOptionPane.showMessageDialog(imageFrame,i.date + " " + i.time + " Status is " + status);
		// display date time and seconds in pop up j option pane
		return i;
	}

	public void sendOkayMessage(String camID) { // calling send okay message method in home hub class
		homehub.sendOkayMessage(camID); // pass camera name as parameter to home hub 
	}


	public void getCameraStatus(String camID) { // getting current camera status and passing to home hub method
		String status = parent.statusField.getText(); // getting camera status field text from camera interface
		homehub.sendCameraStatus(camID,status); // send as parameter
	}

	@Override
	public String itemName() { // return object
		return camID; 
	}

	@Override
	public String roomName() { // return object
		return roomName;
	}
}

public class CamSensor extends JFrame {
	private JPanel textpanel, buttonpanel;
	private JScrollPane scrollpane;
	private JTextArea textarea;
	public static JTextField statusField;
	public static String camID;
	private JButton btnOff, btnOn, okayButton, btnRequestImage, panicButton;
	private ClientCameraServant cameraClient; // camera
	public static String homeHubName;
	public static String roomName;

	public CamSensor(String[] args, String camID2, String homeHubName2, String roomName2) {
		
		// arguments passed from the client, defined on camera creation
		
		camID = camID2;
		homeHubName = homeHubName2;
		roomName = roomName2;

		try {
			// Initialize the ORB

			Properties prop = new Properties();
			prop.put("org.omg.CORBA.ORBInitialPort","1050");
			prop.put("org.omg.CORBA.ORBInitialPort","localhost");

			ORB orb = ORB.init(args, prop);

			// get reference to rootpoa & activate the POAManager
			POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
			rootpoa.the_POAManager().activate();

			// Create the camera servant object
			cameraClient = new ClientCameraServant(this, orb, camID, homeHubName, roomName); // camera client passing object above to servant

			// get object reference from the servant
			org.omg.CORBA.Object ref = rootpoa.servant_to_reference(cameraClient);
			ClientAndServer.ClientCamera cref = ClientCameraHelper.narrow(ref);

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

			// bind the camera object in the Naming service
			String name = camID;
			NameComponent[] countName = nameService.to_name(name);
			nameService.rebind(countName, cref);

			// set up the GUI
			textarea = new JTextArea(20,25);
			scrollpane = new JScrollPane(textarea);
			scrollpane.setBounds(61, 5, 278, 303);
			textpanel = new JPanel();

			buttonpanel = new JPanel();
			textpanel.setLayout(null);


			textpanel.add(scrollpane);

			getContentPane().add(textpanel, "Center");
			panicButton = new JButton("Send Panic");
			panicButton.setBounds(21, 320, 117, 25);
			textpanel.add(panicButton);

			statusField = new JTextField();
			statusField.setBounds(207, 378, 132, 19);
			textpanel.add(statusField);
			statusField.setColumns(10);

			JLabel lblCurrentStatus = new JLabel("Current Status:");
			lblCurrentStatus.setBounds(61, 380, 132, 15);
			textpanel.add(lblCurrentStatus);

			JButton okayButton = new JButton("Send Okay");
			okayButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					statusField.setText("Okay");	
					cameraClient.sendOkayMessage(camID); // calls method above in camera servant with camID 
				}
			});
			okayButton.setBounds(139, 318, 117, 29);
			textpanel.add(okayButton);

			btnOn = new JButton("On");
			btnOn.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					okayButton.setEnabled(true);
					btnRequestImage.setEnabled(true);
					panicButton.setEnabled(true);
					statusField.setEnabled(true);
					btnOff.setEnabled(true);

					cameraClient.switchOn(camID); // calls method above in camera servant with camID 
				}
			});
			btnOn.setBounds(61, 418, 117, 29);
			textpanel.add(btnOn);

			btnOff = new JButton("Off");
			btnOff.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					btnRequestImage.setEnabled(false);
					okayButton.setEnabled(false);
					panicButton.setEnabled(false);
					btnOff.setEnabled(false);
					statusField.setText(null);
					statusField.setEnabled(false);

					cameraClient.switchOff(camID); // calls method above in camera servant with camID 
				}
			});
			btnOff.setBounds(217, 418, 117, 29);
			textpanel.add(btnOff);

			btnRequestImage = new JButton("Request Image");
			btnRequestImage.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					cameraClient.currentImage(); // calls current image method in camera servant
				}
			});
			btnRequestImage.setBounds(254, 320, 117, 29);
			textpanel.add(btnRequestImage);
			panicButton.addActionListener (new ActionListener() {
				public void actionPerformed (ActionEvent evt) {

					statusField.setText("Assistance Needed");	
					cameraClient.sendPanicMessage(camID);
				}
			});
			getContentPane().add(buttonpanel, "South");

			setSize(400, 500);
			setTitle("Camera Sensor " + camID);

			addWindowListener (new java.awt.event.WindowAdapter () {
				public void windowClosing (java.awt.event.WindowEvent evt) {
					System.exit(0);;
				}
			} );


		} catch (Exception e) {
			System.out.println("ERROR : " + e) ;
			e.printStackTrace(System.out);
		}
	}

	public void resetCameraStatus(){ // to be called by home hub to reset camera status
		statusField.setText(null);
	}

	public void callConnectCamServer(){ // calls method in camera server class
		cameraClient.setCamServer(camID);
	}

	public static void main(String args[]) {
		final String[] arguments = args;
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {

				JFrame frame = new JFrame();

				camID = JOptionPane.showInputDialog(frame,"Camera Name");  // initialize the camera name

				JFrame frame1 = new JFrame();

				homeHubName = JOptionPane.showInputDialog(frame1,"Connect to Homehub"); // initialize the home hub name to connect to

				JFrame frame2 = new JFrame();

				roomName = JOptionPane.showInputDialog(frame2,"Room Name"); // initialize the room name it belongs too

				CamSensor cam = new CamSensor(arguments, camID, homeHubName, roomName);

				cam.setVisible(true); 
				
				cam.setTitle("name : "+ camID + " room : "+ roomName+ " connected to : "+ homeHubName);

				cam.callConnectCamServer(); // calls connection to give camera client to become server to home hub

			}
		});
	}
}
