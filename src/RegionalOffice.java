import ClientAndServer.*;

import org.omg.CORBA.*;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.*;

import java.io.*;
import java.util.Properties;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;


class HelloServant extends HelloWorldPOA {
	private RegionalOffice parent;
	private ORB orb;
	private ClientAndServer.Relay relay2;

	public HelloServant(RegionalOffice parentGUI, ORB orb_val) {
		// store reference to parent GUI
		parent = parentGUI;
		
		orb = orb_val;
	}

	public String hello_world() {
		parent.addMessage("hello_world called by relay.\n    Replying with message...\n\n");

		return "Hello World!!";
	}
	
	public void showOkayMessage(String camID, String homeHubName){
		parent.addMessage("Camera " + camID + " in " + homeHubName + " is okay \n");
	}

	public String panicServer(String camID){

		parent.addMessage("Sensor alert activated twice within 5 seconds \n"); 

		return "Alert received";
	}

	public String switchOn(String camID){
		parent.addMessage(camID + " switched on \n");
		return"";
	}

	public String switchOff(String camID){
		parent.addMessage(camID + " switched off \n");
		return"";
	}

	public void showCamStatus(String messageStatus){
		parent.addMessage(messageStatus);
	}
	
	public String connection(String name){
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
			relay2 = RelayHelper.narrow(nameService.resolve_str(name));	
			
		} catch (Exception e) {
			System.out.println("ERROR : " + e) ;
			e.printStackTrace(System.out);
		}
		return relay2.toString();
	}
	
	public void resetSensor(String camID, String homeHubName){ // only deletes one sensor
		connection(parent.textFieldHub.getText());
		relay2.resetCamera(camID);
		parent.addMessage("Alarm " + camID + " in " + homeHubName +" has been resetted \n");
		
	}

	public void getStatus(String camID, String homeHubName) {
		connection(parent.textFieldHub.getText());
		relay2.getCameraStatus(camID);
		parent.addMessage("Calling for " + camID + " status \n");
		
	}

	public void showCameraStatus(String camID, String status) {
		parent.addMessage("Camera " + camID + " status = " + status + "\n");
	}
}


public class RegionalOffice extends JFrame {
	private JPanel panel;
	private JScrollPane scrollpane;
	private JTextArea textarea;
	private HelloServant helloRef;
	private JButton btnReset;
	public static JTextField textFieldHub;
	public static JTextField textFieldCam;

	public RegionalOffice(String[] args){
		
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
		    helloRef = new HelloServant(this, orb);

		    // get object reference from the servant
		    org.omg.CORBA.Object ref = rootpoa.servant_to_reference(helloRef);
		    ClientAndServer.HelloWorld cref = HelloWorldHelper.narrow(ref);
		    
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
		    String name = "Office";
		    NameComponent[] countName = nameService.to_name(name);
		    nameService.rebind(countName, cref);


			// set up the GUI
			textarea = new JTextArea(20,25);
			scrollpane = new JScrollPane(textarea);
			scrollpane.setBounds(48, 5, 304, 324);
			panel = new JPanel();
			panel.setLayout(null);

			panel.add(scrollpane);
			getContentPane().add(panel, "Center");
			
			btnReset = new JButton("Reset");
			btnReset.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					helloRef.resetSensor(textFieldCam.getText(), textFieldHub.getText());
				}
			});
			btnReset.setBounds(234, 357, 117, 29);
			panel.add(btnReset);
			
			textFieldHub = new JTextField();
			textFieldHub.setBounds(48, 357, 130, 26);
			panel.add(textFieldHub);
			textFieldHub.setColumns(10);
			
			textFieldCam = new JTextField();
			textFieldCam.setBounds(48, 416, 130, 26);
			panel.add(textFieldCam);
			textFieldCam.setColumns(10);
			
			JLabel lblHomehubName = new JLabel("HomeHub Name:");
			lblHomehubName.setBounds(48, 341, 107, 16);
			panel.add(lblHomehubName);
			
			JLabel lblCameraName = new JLabel("Camera Name:");
			lblCameraName.setBounds(48, 396, 107, 16);
			panel.add(lblCameraName);
			
			JButton btnGetStatus = new JButton("Get Status");
			btnGetStatus.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					helloRef.getStatus(textFieldCam.getText(), textFieldHub.getText());
				}
			});
			btnGetStatus.setBounds(235, 416, 117, 29);
			panel.add(btnGetStatus);

			setSize(400, 500);
			setTitle("Regional Office Server");

			addWindowListener (new java.awt.event.WindowAdapter () {
				public void windowClosing (java.awt.event.WindowEvent evt) {
					System.exit(0);;
				}
			} );


		} catch (Exception e) {
			System.err.println("ERROR: " + e);
			e.printStackTrace(System.out);
		}

	}

	public void addMessage(String message){
		textarea.append(message);
	}

	public static void main(String args[]) {
		final String[] arguments = args;
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				new  RegionalOffice(arguments).setVisible(true);
			}
		});
	}   
}


