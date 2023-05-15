package com.nfehs.librarygames.net.packets;

/**
 * Packets pertaining to login
 * @author Patrick Kenney and Syed Quadri
 * @date 8/11/2018
 */

public class Packet00Login extends Packet {
  // data to send to server
  private String username;
  private String password;

  // data to send to client
  // username
  private String userKey;

  /**
   * Used by client to send data to server
   * @param username
   * @param password
   */
  public Packet00Login(String username, String password) {
    super(00);
    setUsername(username);
    setPassword(password);
  }

  /**
   * Used by server to retrieve data from client's packet
   * @param data
   */
  public Packet00Login(byte[] data) {
    super(00);

    try {
      String[] userpass = readData(data).split(":");
      setUuidKey(userpass[0]);
      setUsername(userpass[1]);
      setPassword(userpass[2]);
    } catch (Exception e) {
      e.printStackTrace();
      setValid(false);
    }
  }

  /**
   * Used by server to send data to client
   * @param packetKey
   * @param username
   * @param userKey
   * @param serverUse boolean that serves no purpose other than to distinguish constructors
   */
  public Packet00Login(String packetKey, String username, String userKey, boolean serverUse) {
    super(00);
    setUuidKey(packetKey);
    setUsername(username);
    setUserKey(userKey);
  }

  /**
   * Used by server to retrieve data from client's data packet
   * @param data
   * @param serverUse boolean that serves no purpose other than to distinguish constructors
   */
  public Packet00Login(byte[] data, boolean serverUse) {
    super(00);

    try {
      String[] userdata = readData(data).split(":");
      setUuidKey(userdata[0]);
      setUsername(userdata[1]);
      setUserKey(userdata[2]);
    } catch (Exception e) {
      e.printStackTrace();
      setValid(false);
    }
  }

  @Override
  public byte[] getData() {
    return ("00" + getUuidKey() + ":" + getUsername() + ":" + getPassword()).getBytes();
  }

  @Override
  public byte[] getDataServer() {
    return ("00" + getUuidKey() + ":" + getUsername() + ":" + getUserKey()).getBytes();
  }

  /**
   * @return the username
   */
  public String getUsername() {
    return username;
  }

  /**
   * @param username the username to set
   */
  public void setUsername(String username) {
    this.username = username;
  }

  /**
   * @return the password
   */
  public String getPassword() {
    return password;
  }

  /**
   * @param password the password to set
   */
  public void setPassword(String password) {
    this.password = password;
  }

  public String getUserKey() {
    return userKey;
  }

  public void setUserKey(String userKey) {
    this.userKey = userKey;
  }
}
