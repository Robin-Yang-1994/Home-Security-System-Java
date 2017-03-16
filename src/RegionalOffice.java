import ClientAndServer.*;

import org.omg.CORBA.*;
import org.omg.PortableServer.*;
import org.omg.PortableServer.POA;

import java.io.*;
import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;


class HelloServant extends HelloWorldPOA {
	private RegionalOffice parent;

	public HelloServant(RegionalOffice parentGUI) {
		// store reference to parent GUI
		parent = parentGUI;
	}

	public String hello_world() {
		parent.addMessage("hello_world called by relay.\n    Replying with message...\n\n");

		return "Hello World!!";
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

}


public class RegionalOffice extends JFrame {
	private JPanel panel;
	private JScrollPane scrollpane;
	private JTextArea textarea;

	public RegionalOffice(String[] args){
		try {
			// create and initialize the ORB
			ORB orb = ORB.init(args, null);

			// get reference to rootpoa & activate the POAManager
			POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
			rootpoa.the_POAManager().activate();

			// create servant and register it with the ORB
			HelloServant helloRef = new HelloServant(this);

			// get the 'stringified IOR'
			org.omg.CORBA.Object ref = rootpoa.servant_to_reference(helloRef);
			String stringified_ior = orb.object_to_string(ref);

			// Save IOR to file
			BufferedWriter out = new BufferedWriter(new FileWriter("server.ref"));
			out.write(stringified_ior);
			out.close();


			// set up the GUI
			textarea = new JTextArea(20,25);
			scrollpane = new JScrollPane(textarea);
			scrollpane.setBounds(48, 5, 304, 324);
			panel = new JPanel();
			panel.setLayout(null);

			panel.add(scrollpane);
			getContentPane().add(panel, "Center");

			setSize(400, 500);
			setTitle("Regional Office Server");

			addWindowListener (new java.awt.event.WindowAdapter () {
				public void windowClosing (java.awt.event.WindowEvent evt) {
					System.exit(0);;
				}
			} );

			// remove the "orb.run()" command,
			// or the server will run but the GUI will not be visible
			// orb.run();

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


