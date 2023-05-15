package com.nfehs.librarygames;

import java.net.InetAddress;
import java.util.ArrayList;

/**
 * This class holds data for players
 * Used by both client player and by server to hold data
 * @author Patrick Kenney and Syed Quadri
 * @date 8/10/2018
 */

public class Player {
  private String username;
  private String user_key;
  private String game_key;
  private InetAddress ipAddress;
  private int port;

  private String[] finishedBoardGames;
  private String[] yourTurnBoardGames;
  private String[] opponentTurnBoardGames;
  private String[] friends;
  private String[] otherPlayers;
  private String[] spectatorBoardGames;

  /**
   * For use by client
   * @param user
   * @param key
   */
  public Player(String user, String key) {
    setUsername(user);
    setUser_key(key);
  }

  /**
   * For use by server
   * @param user
   * @param key
   * @param ip
   * @param p
   */
  public Player(String user, String key, InetAddress ip, int p) {
    setUsername(user);
    setUser_key(key);
    setIpAddress(ip);
    setPort(p);
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
   * @return the user_key
   */
  public String getUser_key() {
    return user_key;
  }
  /**
   * @param user_key the user_key to set
   */
  public void setUser_key(String user_key) {
    this.user_key = user_key;
  }
  /**
   * @return the ipAddress
   */
  public InetAddress getIpAddress() {
    return ipAddress;
  }
  /**
   * @param ipAddress the ipAddress to set
   */
  public void setIpAddress(InetAddress ipAddress) {
    this.ipAddress = ipAddress;
  }
  /**
   * @return the port
   */
  public int getPort() {
    return port;
  }
  /**
   * @param port the port to set
   */
  public void setPort(int port) {
    this.port = port;
  }

  /**
   * @return the yourTurnBoardGames
   */
  public String[] getYourTurnBoardGames() {
    return yourTurnBoardGames;
  }

  /**
   * @param yourTurnBoardGames the yourTurnBoardGames to set
   */
  public void setYourTurnBoardGames(ArrayList<String> yourTurn) {
    String[] yourTurnBoardGames = new String[yourTurn.size()];
    for (int i = 0; i < yourTurn.size(); i++)
      yourTurnBoardGames[i] = yourTurn.get(i);
    this.yourTurnBoardGames = yourTurnBoardGames;
  }

  /**
   * @return the opponentTurnBoardGames
   */
  public String[] getOpponentTurnBoardGames() {
    return opponentTurnBoardGames;
  }

  /**
   * @param opponentTurnBoardGames the opponentTurnBoardGames to set
   */
  public void setOpponentTurnBoardGames(ArrayList<String> opponentTurn) {
    String[] opponentTurnBoardGames = new String[opponentTurn.size()];
    for (int i = 0; i < opponentTurnBoardGames.length; i++)
      opponentTurnBoardGames[i] = opponentTurn.get(i);
    this.opponentTurnBoardGames = opponentTurnBoardGames;
  }

  public String[] getFriends() {
    return friends;
  }

  public void setFriends(String friends) {
    if (friends == null || friends.length() < 1) {
      this.friends = new String[0];
      return;
    }
    this.friends = friends.split(",");
  }

  public String[] getOtherPlayers() {
    return otherPlayers;
  }

  public void setOtherPlayers(String otherPlayers) {
    if (otherPlayers == null || otherPlayers.length() < 1) {
      this.otherPlayers = new String[0];
      return;
    }
    this.otherPlayers = otherPlayers.split(",");
  }

  /**
   * @return the finishedBoardGames
   */
  public String[] getFinishedBoardGames() {
    return finishedBoardGames;
  }

  /**
   * @param finishedBoardGames the finishedBoardGames to set
   */
  public void setFinishedBoardGames(ArrayList<String> finishedBoardGames) {
    String[] finBoardGames = new String[finishedBoardGames.size()];
    for (int i = 0; i < finBoardGames.length; i++)
      finBoardGames[i] = finishedBoardGames.get(i);
    this.finishedBoardGames = finBoardGames;
  }

  /**
   * @return the game_key
   */
  public String getGame_key() {
    return game_key;
  }

  /**
   * @param game_key the game_key to set
   */
  public void setGame_key(String game_key) {
    this.game_key = game_key;
  }

  public String[] getSpectatorBoardGames() {
    return spectatorBoardGames;
  }

  public void setSpectatorBoardGames(ArrayList<String> spectatorBoardGames) {
    String[] specBoardGames = new String[spectatorBoardGames.size()];
    for (int i = 0; i < specBoardGames.length; i++)
      specBoardGames[i] = spectatorBoardGames.get(i);
    this.spectatorBoardGames = specBoardGames;
  }
}
