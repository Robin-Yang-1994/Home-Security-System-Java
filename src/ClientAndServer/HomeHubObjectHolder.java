package ClientAndServer;

/**
* ClientAndServer/HomeHubObjectHolder.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from Relay.idl
* Wednesday, 29 March 2017 16:23:14 o'clock BST
*/

public final class HomeHubObjectHolder implements org.omg.CORBA.portable.Streamable
{
  public ClientAndServer.HomeHubObject value = null;

  public HomeHubObjectHolder ()
  {
  }

  public HomeHubObjectHolder (ClientAndServer.HomeHubObject initialValue)
  {
    value = initialValue;
  }

  public void _read (org.omg.CORBA.portable.InputStream i)
  {
    value = ClientAndServer.HomeHubObjectHelper.read (i);
  }

  public void _write (org.omg.CORBA.portable.OutputStream o)
  {
    ClientAndServer.HomeHubObjectHelper.write (o, value);
  }

  public org.omg.CORBA.TypeCode _type ()
  {
    return ClientAndServer.HomeHubObjectHelper.type ();
  }

}
