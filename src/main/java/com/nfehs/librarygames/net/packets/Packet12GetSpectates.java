package com.nfehs.librarygames.net.packets;

/**
 * Packets pertaining to SpectatorGamesScreen, gets spectator games
 * @author Patrick Kenney and Syed Quadri
 * @date 8/23/2018
 */

public class Packet12GetSpectates extends Packet {
  // data to send to server

  // data to send to client
  private int packetsSent;
  private int packetNumber;
  private String[] gameInfo;

  /**
   * Used by client to send data to server
   * @param senderKey
   */
  public Packet12GetSpectates(String senderKey) {
    super(12, senderKey);
  }

  /**
   * Used by server to retrieve data from client's packet
   * @param data
   */
  public Packet12GetSpectates(byte[] data) {
    super(12);

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
   * @param gameInfo
   * @param serverUse boolean that serves no purpose other than to distinguish constructors
   */
  public Packet12GetSpectates(String packetKey, int packetsSent, int packetNumber, String[] gameInfo, boolean serverUse) {
    super(12);
    setUuidKey(packetKey);
    setPacketsSent(packetsSent);
    setPacketNumber(packetNumber);
    setGameInfo(gameInfo);
  }

  /**
   * Used by server to retrieve data from client's data packet
   * @param data
   * @param serverUse boolean that serves no purpose other than to distinguish constructors
   */
  public Packet12GetSpectates(byte[] data, boolean serverUse) {
    super(12);

    try {
      String[] userdata = readData(data).split(":");
      setUuidKey(userdata[0]);
      setPacketsSent(Integer.parseInt(userdata[1]));
      setPacketNumber(Integer.parseInt(userdata[2]));

      String[] gameInfo = new String[userdata.length-3];
      for (int i = 3; i < userdata.length; i++)
        gameInfo[i-3] = userdata[i];
      setGameInfo(gameInfo);
    } catch (Exception e) {
      e.printStackTrace();
      setValid(false);
    }
  }

  @Override
  public byte[] getData() {
    return ("12" + getUuidKey() + ":" + getSenderKey()).getBytes();
  }

  @Override
  public byte[] getDataServer() {
    String data = "12" + getUuidKey() + ":" + getPacketsSent() + ":" + getPacketNumber();
    for (String info : getGameInfo())
      if (info != null)
        data += ":" + info;
    return (data).getBytes();
  }

  public int getPacketsSent() {
    return packetsSent;
  }

  public void setPacketsSent(int packetsSent) {
    this.packetsSent = packetsSent;
  }

  public int getPacketNumber() {
    return packetNumber;
  }

  public void setPacketNumber(int packetNumber) {
    this.packetNumber = packetNumber;
  }

  public String[] getGameInfo() {
    return gameInfo;
  }

  public void setGameInfo(String[] gameInfo) {
    this.gameInfo = gameInfo;
  }
}
