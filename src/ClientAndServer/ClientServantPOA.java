package ClientAndServer;


/**
* ClientAndServer/ClientServantPOA.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from Relay.idl
* Wednesday, 22 March 2017 14:00:55 o'clock GMT
*/

public abstract class ClientServantPOA extends org.omg.PortableServer.Servant
 implements ClientAndServer.ClientServantOperations, org.omg.CORBA.portable.InvokeHandler
{

  // Constructors

  private static java.util.Hashtable _methods = new java.util.Hashtable ();
  static
  {
  }

  public org.omg.CORBA.portable.OutputStream _invoke (String $method,
                                org.omg.CORBA.portable.InputStream in,
                                org.omg.CORBA.portable.ResponseHandler $rh)
  {
    org.omg.CORBA.portable.OutputStream out = null;
    java.lang.Integer __method = (java.lang.Integer)_methods.get ($method);
    if (__method == null)
      throw new org.omg.CORBA.BAD_OPERATION (0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);

    return out;
  } // _invoke

  // Type-specific CORBA::Object operations
  private static String[] __ids = {
    "IDL:ClientAndServer/ClientServant:1.0"};

  public String[] _all_interfaces (org.omg.PortableServer.POA poa, byte[] objectId)
  {
    return (String[])__ids.clone ();
  }

  public ClientServant _this() 
  {
    return ClientServantHelper.narrow(
    super._this_object());
  }

  public ClientServant _this(org.omg.CORBA.ORB orb) 
  {
    return ClientServantHelper.narrow(
    super._this_object(orb));
  }


} // class ClientServantPOA
