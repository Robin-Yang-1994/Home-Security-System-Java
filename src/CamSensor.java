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

class ClientServant extends ClientPOA{
	
	private ClientAndServer.Relay relay;
	private ORB orb;
	private CamSensor parent;
	
	public ClientServant(CamSensor parentGUI, ORB orb_val){
		parent = parentGUI;
		orb = orb_val;
		try {
			// Initialize the ORB
			System.out.println("Initializing the ORB");
			//ORB orb = ORB.init(args, null);
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
			String name = parent.homeHubName;
			// resolve the Count object reference in the Naming service
			relay = RelayHelper.narrow(nameService.resolve_str(name));
			
		} catch (Exception e) {
			System.out.println("ERROR : " + e) ;
			e.printStackTrace(System.out);
		}
		
	}

	public void switchOn(String camID) {
		relay.switchOn(camID);
		
	}

	public void switchOff(String camID) {
		relay.switchOff(camID);
		
	}

	public void sendPanicMessage(String camID) {
		relay.sendPanicMessage(camID);
		
	}

	public void setCamServer(String camID) {
		relay.setCamConnection(camID);
	}
	
	public void resetCamStatus(){
		parent.statusField.setText("");
	}

	public Image currentImage() {
		Image i = new Image();
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date format = new Date();
		i.time = format.getHours();
		i.date = format.getDate();
		i.status = parent.statusField.getText();
		
		System.out.println(i.date + i.time + i.status);
		return i;
	}

	public void sendOkayMessage(String camID) {
		relay.sendOkayMessage(camID);
	}

	@Override
	public void getCameraStatus(String camID) {
		String status = parent.statusField.getText();
		relay.sendCameraStatus(camID,status);
	}
}

public class CamSensor extends JFrame {
	private JPanel textpanel, buttonpanel;
	private JScrollPane scrollpane;
	private JTextArea textarea;
	private JButton panicButton;
	public static JTextField statusField;
	public static String camID;
	private JButton btnOff, btnOn;
	private ClientServant clientRef;
	public static String homeHubName;
	private JButton btnSendImage;

	public CamSensor(String[] args, String camID2, String homeHubName2) {
		
		camID = camID2;
		
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
		    clientRef = new ClientServant(this, orb);

		    // get object reference from the servant
		    org.omg.CORBA.Object ref = rootpoa.servant_to_reference(clientRef);
		    ClientAndServer.Client cref = ClientHelper.narrow(ref);
		    
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

			btnOn = new JButton("On");
			btnOn.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					panicButton.setEnabled(true);
					statusField.setEnabled(true);
					btnOff.setEnabled(true);
					clientRef.switchOn(camID);
				}
			});
			btnOn.setBounds(61, 418, 117, 29);
			textpanel.add(btnOn);

			btnOff = new JButton("Off");
			btnOff.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					
					panicButton.setEnabled(false);
					btnOff.setEnabled(false);
					statusField.setText(null);
					statusField.setEnabled(false);
					clientRef.switchOff(camID);
				}
			});
			btnOff.setBounds(217, 418, 117, 29);
			textpanel.add(btnOff);
			
			JButton okayButton = new JButton("Send Okay");
			okayButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					statusField.setText("Okay");	
					clientRef.sendOkayMessage(camID);
				}
			});
			okayButton.setBounds(139, 318, 117, 29);
			textpanel.add(okayButton);
			
			btnSendImage = new JButton("Send Image");
			btnSendImage.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					clientRef.currentImage();
				}
			});
			btnSendImage.setBounds(254, 320, 117, 29);
			textpanel.add(btnSendImage);
			panicButton.addActionListener (new ActionListener() {
				public void actionPerformed (ActionEvent evt) {

					statusField.setText("Assistance Needed");	
					clientRef.sendPanicMessage(camID);
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
	
	public void resetCameraStatus(){
		statusField.setText(null);
	}
	
	public void callConnectCamServer(){
		clientRef.setCamServer(camID);
	}

	public static void main(String args[]) {
		final String[] arguments = args;
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				
				JFrame frame = new JFrame();
				
				camID = JOptionPane.showInputDialog(frame,"Camera Name");
				
				JFrame frame1 = new JFrame();
				
				homeHubName = JOptionPane.showInputDialog(frame1,"Connect to Homehub");
				
				CamSensor cam = new CamSensor(arguments, camID, homeHubName);
				
				cam.setVisible(true);
				
				cam.callConnectCamServer();
				
			}
		});
	}
}
