package ClientAndServer;

/**
* ClientAndServer/ClientHolder.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from Relay.idl
* Wednesday, 29 March 2017 16:23:14 o'clock BST
*/

public final class ClientHolder implements org.omg.CORBA.portable.Streamable
{
  public ClientAndServer.Client value = null;

  public ClientHolder ()
  {
  }

  public ClientHolder (ClientAndServer.Client initialValue)
  {
    value = initialValue;
  }

  public void _read (org.omg.CORBA.portable.InputStream i)
  {
    value = ClientAndServer.ClientHelper.read (i);
  }

  public void _write (org.omg.CORBA.portable.OutputStream o)
  {
    ClientAndServer.ClientHelper.write (o, value);
  }

  public org.omg.CORBA.TypeCode _type ()
  {
    return ClientAndServer.ClientHelper.type ();
  }

}
