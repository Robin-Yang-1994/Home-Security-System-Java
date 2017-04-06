import ClientAndServer.*;

import org.omg.CORBA.*;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.*;

import java.io.*;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Properties;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.ScrollPane;


class ServerRegionalOfficeServant extends ServerRegionalOfficePOA {
	private RegionalOffice parent;
	private ORB orb;
	private ClientAndServer.ClientServerHomeHub homehub;
	private JFrame contactFrame;

	public ServerRegionalOfficeServant(RegionalOffice parentGUI, ORB orb_val) {
		// store reference to parent GUI
		parent = parentGUI;
		orb = orb_val;
	}

	public void showOkayMessage(String camID, String homeHubName){ // display camera name from which home hub with okay status
		parent.addMessage("Camera " + camID + " in " + homeHubName + " is okay \n");
	}

	public String panicServer(String camID , String homeHubName, String contact){ // show panic to has been alerted twice within 5 seconds but the logic was ran in home hub
		parent.addMessage("Sensor alert activated twice within 5 seconds \n"); 
		JOptionPane.showMessageDialog(contactFrame, "A message has been sent to " + contact);
		return "Alert received";
	}

	public String switchOn(String camID){ // display which camera has been switched on 
		parent.addMessage(camID + " switched on \n");
		return"";
	}

	public String switchOff(String camID){ // display which camera has been switched on 
		parent.addMessage(camID + " switched off \n");
		return"";
	}

	public void showCamStatus(String messageStatus){ // display a status of a camera passed from the message status
		parent.addMessage(messageStatus);
	}

	public String connection(String name){ // connection method to make the home hub a server to this client
		try {
			// Initialize the ORB
			System.out.println("Initializing the ORB");
			String[] sArr = {"-ORBInitialPort","1050"};

			ORB orb = ORB.init(sArr, null);

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
			System.out.println("name is : "+ name);
			// resolve the Count object reference in the Naming service
			homehub = ClientServerHomeHubHelper.narrow(nameService.resolve_str(name));	

		} catch (Exception e) {
			System.out.println("ERROR : " + e) ;
			e.printStackTrace(System.out);
		}
		return homehub.toString();
	}

	public void resetSensor(String camID, String homeHubName){
		System.out.println("conn called by resetSensor");
		connection(parent.textFieldHub.getText());
		camID = parent.textFieldCam.getText();
		homehub.resetCamera(camID);
		parent.addMessage("Alarm " + camID + " in " + homeHubName +" has been resetted \n");

	}

	public void getStatus(String camID, String homeHubName) { // getting a status from a camera
		System.out.println("conn called by getStatus");
		connection(parent.textFieldHub.getText());
		homehub.getCameraStatus(camID);
		parent.addMessage("Calling for " + camID + " status \n");
	}

	public void showSensorStatus(String messageStatus) { // display the content held in the message status for sensor
		parent.addMessage(messageStatus);
	}

	public void showCameraStatus(String camID, String status) { // display the content held in the message status for camera
		parent.addMessage("Camera " + camID + " status = " + status + "\n");
	}

	public void sensorPanicServer(String sensorID, String roomName) { // display which sensor has been alert
		parent.addMessage("Sensor " + sensorID +" in "+ roomName +" has been alerted \n");
	}

	public void getLog(String homeHubName) {
		System.out.println("conn called by getLog");
		connection(parent.homeHubLog.getText());
		String[] logz = homehub.log();
		for (int i = 0; i < logz.length; i++){
			parent.showLog(logz[i]); 
		}
	}
}

public class RegionalOffice extends JFrame {
	private JPanel panel;
	private JScrollPane scrollpane;
	private JTextArea textarea, textAreaHBLog;
	private ServerRegionalOfficeServant officeClient;
	private JButton btnReset, btnShowHBLog;
	public static JTextField textFieldHub, textFieldCam;
	public JTextField homeHubLog;


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

			// Create the regional office servant object
			officeClient = new ServerRegionalOfficeServant(this, orb);

			// get object reference from the servant
			org.omg.CORBA.Object ref = rootpoa.servant_to_reference(officeClient);
			ClientAndServer.ServerRegionalOffice cref = ServerRegionalOfficeHelper.narrow(ref);

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
			panel = new JPanel();
			getContentPane().add(panel, "Center");

			btnReset = new JButton("Reset");
			btnReset.setBounds(179, 357, 117, 29);
			btnReset.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					officeClient.resetSensor(textFieldCam.getText(), textFieldHub.getText());
				}
			});
			panel.setLayout(null);
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
			btnGetStatus.setBounds(179, 416, 117, 29);
			btnGetStatus.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					officeClient.getStatus(textFieldCam.getText(), textFieldHub.getText());
				}
			});
			panel.add(btnGetStatus);

			JButton btnSaveLog = new JButton("Save as Log");
			btnSaveLog.setBounds(319, 357, 97, 85);
			btnSaveLog.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {

					String date = new SimpleDateFormat ("ddMMyyyy'.txt'").format(new Date());
					String path = "./" + date;

					try(FileWriter fw = new FileWriter(path, true);
							BufferedWriter bw = new BufferedWriter(fw);
							PrintWriter out = new PrintWriter(bw))
					{
						out.write(textarea.getText());
						textarea.append("Log has been saved \n");

					}catch(IOException er){
						System.err.println("ERROR: " + er);
					};

				}
			});
			scrollpane = new JScrollPane(textarea);
			scrollpane.setBounds(16, 5, 360, 324);

			panel.add(scrollpane);


			// set up the GUI
			textarea = new JTextArea(20,25);
			textarea.setBounds(16, 7, 360, 320);
			scrollpane.setViewportView(textarea);
			panel.add(btnSaveLog);

			btnShowHBLog = new JButton("Show Homehub Information");
			btnShowHBLog.setBounds(447, 381, 193, 49);
			btnShowHBLog.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					officeClient.getLog(homeHubLog.getText());;
				}
			});
			panel.add(btnShowHBLog);

			homeHubLog = new JTextField();
			homeHubLog.setBounds(447, 336, 193, 26);
			panel.add(homeHubLog);
			homeHubLog.setColumns(10);

			JLabel lblHomeHubLog = new JLabel("HomeHub Name:");
			lblHomeHubLog.setBounds(447, 313, 107, 16);
			panel.add(lblHomeHubLog);

			JScrollPane logScrollPane = new JScrollPane();
			logScrollPane.setBounds(416, 6, 241, 295);
			panel.add(logScrollPane);

			textAreaHBLog = new JTextArea();
			logScrollPane.setViewportView(textAreaHBLog);
			setSize(676, 500);
			setTitle("Regional Office Server");

			addWindowListener (new java.awt.event.WindowAdapter () {
				public void windowClosing (java.awt.event.WindowEvent evt) {
					System.exit(0);
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

	public void showLog (String logMessage){
		textAreaHBLog.append(logMessage);
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


