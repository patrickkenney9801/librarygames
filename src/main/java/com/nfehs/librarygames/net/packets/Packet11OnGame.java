package com.nfehs.librarygames.net.packets;

/**
 * Handles sending data for whether opponent is on the client's game or not
 * Sent only by server
 * @author Patrick Kenney and Syed Quadri
 * @date 8/21/2018
 */

public class Packet11OnGame extends Packet {
  // client data
  private String gameKey;

  // server data
  // gameKey
  private String player;
  private boolean onGame;

  /**
   * Used by client to send data to server
   * @param senderKey
   * @param gameKey
   */
  public Packet11OnGame(String senderKey, String gameKey) {
    super(11, senderKey);
    setGameKey(gameKey);
  }

  /**
   * Used by server to retrieve data from client's packet
   * @param data
   */
  public Packet11OnGame(byte[] data) {
    super(11);

    try {
      String[] userdata = readData(data).split(":");
      setUuidKey(userdata[0]);
      setSenderKey(userdata[1]);
      setGameKey(userdata[2]);
    } catch (Exception e) {
      e.printStackTrace();
      setValid(false);
    }
  }

  /**
   * Used by server to send data to client
   * @param gameKey
   * @param player
   * @param onGame
   * @param serverUse boolean that serves no purpose other than to distinguish constructors
   */
  public Packet11OnGame(String gameKey, String player, boolean onGame, boolean serverUse) {
    super(11);
    setGameKey(gameKey);
    setPlayer(player);
    setOnGame(onGame);
  }

  /**
   * Used by server to retrieve data from client's data packet
   * @param data
   * @param serverUse boolean that serves no purpose other than to distinguish constructors
   */
  public Packet11OnGame(byte[] data, boolean serverUse) {
    super(11);

    try {
      String[] userdata = readData(data).split(":");
      setUuidKey(userdata[0]);
      setGameKey(userdata[1]);
      setPlayer(userdata[2]);
      setOnGame(Boolean.parseBoolean(userdata[3]));
    } catch (Exception e) {
      e.printStackTrace();
      setValid(false);
    }
  }

  @Override
  public byte[] getData() {
    return ("11" + getUuidKey() + ":" + getSenderKey() + ":" + getGameKey()).getBytes();
  }

  @Override
  public byte[] getDataServer() {
    return ("11" + getUuidKey() + ":" + getGameKey() + ":" + getPlayer() + ":" + isOnGame()).getBytes();
  }

  public String getGameKey() {
    return gameKey;
  }

  public void setGameKey(String gameKey) {
    this.gameKey = gameKey;
  }

  public String getPlayer() {
    return player;
  }

  public void setPlayer(String player) {
    this.player = player;
  }

  public boolean isOnGame() {
    return onGame;
  }

  public void setOnGame(boolean onGame) {
    this.onGame = onGame;
  }
}
