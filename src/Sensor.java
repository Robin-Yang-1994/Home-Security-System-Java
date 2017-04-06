import ClientAndServer.*;
import org.omg.CORBA.*;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.*;
import java.util.Properties;
import javax.swing.*;
import java.awt.event.*;

class ClientSensorServant extends ClientSensorPOA{
	
	private ClientAndServer.ClientServerHomeHub homehub;
	private ORB orb;
	private Sensor parent;
	private String sensorID, roomName, homeHubName;
	
	public ClientSensorServant(Sensor parentGUI, ORB orb_val, String sensorID2, String homeHubName2, String roomName2){
		sensorID = sensorID2;
		homeHubName = homeHubName2;
		roomName = roomName2;
		parent = parentGUI;
		orb = orb_val;
		
		try {
			// Initialize the ORB
			System.out.println("Initializing the ORB");
			//ORB orb = ORB.init(args, null);
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

			// Use NamingContextExt instead of NamingContext. This is 
			// part of the Interoperable naming Service.  
			NamingContextExt nameService = NamingContextExtHelper.narrow(nameServiceObj);
			if (nameService == null) {
				System.out.println("nameService = null");
				return;
			}
			String name = parent.homeHubName;
			// resolve the Count object reference in the Naming service
			homehub = ClientServerHomeHubHelper.narrow(nameService.resolve_str(name)); // home hub server
			
		} catch (Exception e) {
			System.out.println("ERROR : " + e) ;
			e.printStackTrace(System.out);
		}
		
	}

	public void sendSensorPanicMessage(String sensorID, String roomName) { // calls panic message for sensor in home hub class
		homehub.sendPanicMessage(sensorID, roomName);
	}

}

public class Sensor extends JFrame {
	private JPanel textpanel;
	private JButton panicButton;
	public static String sensorID;
	private ClientSensorServant sensorClient;
	public static String homeHubName;
	public static String roomName;
	public static JTextField statusField;

	public Sensor(String[] args, String sensorID2, String homeHubName2, String roomName2) {
		
		sensorID = sensorID2;
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
		    
		    // Create the sensor servant object
		    sensorClient = new ClientSensorServant(this, orb, sensorID, homeHubName, roomName ); // camera sensor client

		    // get object reference from the servant
		    org.omg.CORBA.Object ref = rootpoa.servant_to_reference(sensorClient);
		    ClientAndServer.ClientSensor cref = ClientSensorHelper.narrow(ref);
		    
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
		    String name = sensorID;
		    NameComponent[] countName = nameService.to_name(name);
		    nameService.rebind(countName, cref);
		    
			textpanel = new JPanel();
			textpanel.setLayout(null);

			getContentPane().add(textpanel, "Center");
			panicButton = new JButton("Send Panic");
			panicButton.setBounds(6, 16, 388, 456);
			textpanel.add(panicButton);
			panicButton.addActionListener (new ActionListener() {
				public void actionPerformed (ActionEvent evt) {
					sensorClient.sendSensorPanicMessage(sensorID, roomName);
				}
			});

			setSize(400, 500);
			setTitle("Sensor " + sensorID);

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
				
				sensorID = JOptionPane.showInputDialog(frame,"Sensor Name"); // initialize the sensor name
				
				JFrame frame1 = new JFrame();
				
				homeHubName = JOptionPane.showInputDialog(frame1,"Connect to Homehub"); // initialize the home hub name
				
				JFrame frame2 = new JFrame();
				
				roomName = JOptionPane.showInputDialog(frame2,"Room Name");
				
				Sensor sensor = new Sensor(arguments, sensorID, homeHubName, roomName);
				sensor.setTitle("name : "+ sensorID + " room : "+ roomName+ " connected to : "+ homeHubName);
				
				sensor.setVisible(true);
				
			}
		});
	}
}
