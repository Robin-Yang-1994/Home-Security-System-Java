package ClientAndServer;


/**
* ClientAndServer/AlarmLogs.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from Relay.idl
* Tuesday, 4 April 2017 15:10:12 o'clock BST
*/

public final class AlarmLogs implements org.omg.CORBA.portable.IDLEntity
{
  public String name = null;
  public String area = null;
  public String homehub = null;
  public String timedex = null;
  public String sent = null;

  public AlarmLogs ()
  {
  } // ctor

  public AlarmLogs (String _name, String _area, String _homehub, String _timedex, String _sent)
  {
    name = _name;
    area = _area;
    homehub = _homehub;
    timedex = _timedex;
    sent = _sent;
  } // ctor

} // class AlarmLogs
