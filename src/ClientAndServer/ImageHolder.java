package ClientAndServer;

/**
* ClientAndServer/ImageHolder.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from Relay.idl
* Tuesday, 4 April 2017 15:10:12 o'clock BST
*/

public final class ImageHolder implements org.omg.CORBA.portable.Streamable
{
  public ClientAndServer.Image value = null;

  public ImageHolder ()
  {
  }

  public ImageHolder (ClientAndServer.Image initialValue)
  {
    value = initialValue;
  }

  public void _read (org.omg.CORBA.portable.InputStream i)
  {
    value = ClientAndServer.ImageHelper.read (i);
  }

  public void _write (org.omg.CORBA.portable.OutputStream o)
  {
    ClientAndServer.ImageHelper.write (o, value);
  }

  public org.omg.CORBA.TypeCode _type ()
  {
    return ClientAndServer.ImageHelper.type ();
  }

}
