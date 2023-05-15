package com.nfehs.librarygames.net.packets;

/**
 * Packets pertaining to logging out, this class does not send a response package from server
 * @author Patrick Kenney and Syed Quadri
 * @date 8/12/2018
 */

public class Packet03Logout extends Packet {
  public Packet03Logout(String senderKey) {
    super(03, senderKey);
  }

  public Packet03Logout(byte[] data) {
    super(03);

    try {
      String[] userpass = readData(data).split(":");
      setUuidKey(userpass[0]);
      setSenderKey(userpass[1]);
    } catch (Exception e) {
      e.printStackTrace();
      setValid(false);
    }
  }

  @Override
  public byte[] getData() {
    return ("03" + getUuidKey() + ":" + getSenderKey()).getBytes();
  }

  /**
   * @Override
   */
  public byte[] getDataServer() {
    return getData();
  }
}
