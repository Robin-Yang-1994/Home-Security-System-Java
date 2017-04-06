package ClientAndServer;


/**
* ClientAndServer/ClientServerHomeHubOperations.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from Relay.idl
* Thursday, 6 April 2017 23:57:23 o'clock BST
*/

public interface ClientServerHomeHubOperations 
{
  String switchOn (String camID);
  String switchOff (String camID);
  void sendSensorPanicMessage (String sensorID, String roomName);
  String sendPanicMessage (String camID, String roomName);
  void sendOkayMessage (String camID);
  String notifyServer (String camID, String homeHubName, String contact);
  void setConnection (String name);
  void resetCamera (String c);
  String setCamConnection (String name);
  void getCameraStatus (String camID);
  void sendCameraStatus (String camID, String status);
  String[] log ();
} // interface ClientServerHomeHubOperations
