package ClientAndServer;


/**
* ClientAndServer/HelloWorldOperations.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from Relay.idl
* Thursday, 16 March 2017 17:36:51 o'clock GMT
*/

public interface HelloWorldOperations 
{
  String hello_world ();
  String panicServer (String camID);
  String switchOn (String camID);
  String switchOff (String camID);
  void showCamStatus (String messageStatus);
} // interface HelloWorldOperations
