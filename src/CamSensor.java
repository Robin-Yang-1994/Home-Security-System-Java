import ClientAndServer.*;

import org.omg.CORBA.*;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.*;
import org.omg.PortableServer.POA;

import java.io.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;


public class CamSensor extends JFrame {
	private JPanel textpanel, buttonpanel;
	private JScrollPane scrollpane;
	private JTextArea textarea;
	private JButton panicButton;
	private JTextField statusField;
	private static String camID;
	private JButton btnOff, btnOn;


	public CamSensor(String[] args, String camID2) {
		
		camID = camID2;
		
		try {
			// Initialize the ORB
			System.out.println("Initializing the ORB");
			ORB orb = ORB.init(args, null);

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
			ClientAndServer.Relay relay = RelayHelper.narrow(nameService.resolve_str(camID));	

			//	try {
			//	    // create and initialize the ORB
			//	    ORB orb = ORB.init(args, null);
			//	    
			//	    // read in the 'stringified IOR' of the HomeHub
			//      	    BufferedReader in = new BufferedReader(new FileReader("relay.ref"));
			//      	    String stringified_ior = in.readLine();
			//	    
			//	    // get object reference from stringified IOR
			//      	    org.omg.CORBA.Object server_ref = 		
			//		orb.string_to_object(stringified_ior);
			//	    
			//	    final ClientAndServer.Relay relay = 
			//		ClientAndServer.RelayHelper.narrow(server_ref);
			//	    

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
			panicButton.setBounds(132, 320, 132, 25);
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
					relay.switchOn(camID);
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
					relay.switchOff(camID);
				}
			});
			btnOff.setBounds(217, 418, 117, 29);
			textpanel.add(btnOff);
			panicButton.addActionListener (new ActionListener() {
				public void actionPerformed (ActionEvent evt) {

					statusField.setText("Assistance Needed");	
					relay.sendPanicMessage(camID);
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



	public static void main(String args[]) {
		final String[] arguments = args;
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				
				JFrame frame = new JFrame();
				
				camID = JOptionPane.showInputDialog(frame,"Camera Name");
				
				CamSensor cam = new CamSensor(arguments, camID);
				
				cam.setVisible(true);
				
			}
		});
	}
}
