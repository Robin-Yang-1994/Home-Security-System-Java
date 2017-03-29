package ClientAndServer;


/**
* ClientAndServer/ClientOperations.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from Relay.idl
* Wednesday, 29 March 2017 16:23:14 o'clock BST
*/

public interface ClientOperations 
{
  void switchOn (String camID);
  void switchOff (String camID);
  void sendPanicMessage (String camID);
  void sendOkayMessage (String camID);
  void setCamServer (String camID);
  void resetCamStatus ();
  ClientAndServer.Image currentImage ();
  void getCameraStatus (String camID);
} // interface ClientOperations
