package com.nfehs.librarygames.net.packets;

/**
 * Packets pertaining to CreateGameScreen, gets players from server
 * Retrieves friends and other users separately
 * @author Patrick Kenney and Syed Quadri
 * @date 8/12/2018
 */

public class Packet04GetPlayers extends Packet {
  // data to send to server

  // data to send to client
  private int packetsSent;
  private int packetNumber;
  private String friends;
  private String otherPlayers;

  /**
   * Used by client to send data to server
   * @param senderKey
   */
  public Packet04GetPlayers(String senderKey) {
    super(04, senderKey);
  }

  /**
   * Used by server to retrieve data from client's packet
   * @param data
   */
  public Packet04GetPlayers(byte[] data) {
    super(04);

    try {
      String[] userdata = readData(data).split(":");
      setUuidKey(userdata[0]);
      setSenderKey(userdata[1]);
    } catch (Exception e) {
      e.printStackTrace();
      setValid(false);
    }
  }

  /**
   *
   * Used by server to send data to client
   * @param packetKey
   * @param packetsSent
   * @param packetNumber
   * @param friends
   * @param otherPlayers
   * @param serverUse boolean that serves no purpose other than to distinguish constructors
   */
  public Packet04GetPlayers(String packetKey, int packetsSent, int packetNumber, String friends, String otherPlayers, boolean serverUse) {
    super(04);
    setUuidKey(packetKey);
    setPacketsSent(packetsSent);
    setPacketNumber(packetNumber);
    setFriends(friends);
    setOtherPlayers(otherPlayers);
  }

  /**
   * Used by server to retrieve data from client's data packet
   * @param data
   * @param serverUse boolean that serves no purpose other than to distinguish constructors
   */
  public Packet04GetPlayers(byte[] data, boolean serverUse) {
    super(04);

    try {
      String[] userdata = readData(data).split(":");
      setUuidKey(userdata[0]);
      setPacketsSent(Integer.parseInt(userdata[1]));
      setPacketNumber(Integer.parseInt(userdata[2]));
      setFriends(userdata[3]);
      if (userdata.length == 5)
        setOtherPlayers(userdata[4]);
      else
        setOtherPlayers("");
    } catch (Exception e) {
      e.printStackTrace();
      setValid(false);
    }
  }

  @Override
  public byte[] getData() {
    return ("04" + getUuidKey() + ":" + getSenderKey()).getBytes();
  }

  @Override
  public byte[] getDataServer() {
    return ("04" + getUuidKey() + ":" + getPacketsSent() + ":" + getPacketNumber() + ":" + getFriends() + ":" + getOtherPlayers()).getBytes();
  }

  public String getFriends() {
    return friends;
  }

  public void setFriends(String friends) {
    this.friends = friends;
  }

  public String getOtherPlayers() {
    return otherPlayers;
  }

  public void setOtherPlayers(String otherPlayers) {
    this.otherPlayers = otherPlayers;
  }

  /**
   * @return the packetsSent
   */
  public int getPacketsSent() {
    return packetsSent;
  }

  /**
   * @param packetsSent the packetsSent to set
   */
  public void setPacketsSent(int packetsSent) {
    this.packetsSent = packetsSent;
  }

  /**
   * @return the packetNumber
   */
  public int getPacketNumber() {
    return packetNumber;
  }

  /**
   * @param packetNumber the packetNumber to set
   */
  public void setPacketNumber(int packetNumber) {
    this.packetNumber = packetNumber;
  }
}
