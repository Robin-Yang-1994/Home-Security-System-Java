package ClientAndServer;

/**
* ClientAndServer/ClientCameraHolder.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from Relay.idl
* Thursday, 6 April 2017 23:57:23 o'clock BST
*/

public final class ClientCameraHolder implements org.omg.CORBA.portable.Streamable
{
  public ClientAndServer.ClientCamera value = null;

  public ClientCameraHolder ()
  {
  }

  public ClientCameraHolder (ClientAndServer.ClientCamera initialValue)
  {
    value = initialValue;
  }

  public void _read (org.omg.CORBA.portable.InputStream i)
  {
    value = ClientAndServer.ClientCameraHelper.read (i);
  }

  public void _write (org.omg.CORBA.portable.OutputStream o)
  {
    ClientAndServer.ClientCameraHelper.write (o, value);
  }

  public org.omg.CORBA.TypeCode _type ()
  {
    return ClientAndServer.ClientCameraHelper.type ();
  }

}
