package com.nfehs.librarygames;

import java.net.InetAddress;
import java.util.ArrayList;

import com.nfehs.librarygames.games.BoardGame.GameMetadata;

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

  private ArrayList<GameMetadata> finishedBoardGames;
  private ArrayList<GameMetadata> yourTurnBoardGames;
  private ArrayList<GameMetadata> opponentTurnBoardGames;
  private ArrayList<GameMetadata> spectatorBoardGames;
  private ArrayList<OtherPlayer> friends;
  private ArrayList<OtherPlayer> otherPlayers;

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
  public ArrayList<GameMetadata> getYourTurnBoardGames() {
    return yourTurnBoardGames;
  }

  /**
   * @param yourTurnBoardGames the yourTurnBoardGames to set
   */
  public void setYourTurnBoardGames(ArrayList<GameMetadata> yourTurn) {
    this.yourTurnBoardGames = yourTurn;
  }

  /**
   * @return the opponentTurnBoardGames
   */
  public ArrayList<GameMetadata> getOpponentTurnBoardGames() {
    return opponentTurnBoardGames;
  }

  /**
   * @param opponentTurnBoardGames the opponentTurnBoardGames to set
   */
  public void setOpponentTurnBoardGames(ArrayList<GameMetadata> opponentTurn) {
    this.opponentTurnBoardGames = opponentTurn;
  }

  public ArrayList<OtherPlayer> getFriends() {
    return friends;
  }

  public void setFriends(ArrayList<OtherPlayer> friends) {
    if (friends == null) {
      return;
    }
    this.friends = friends;
  }

  public ArrayList<OtherPlayer> getOtherPlayers() {
    return otherPlayers;
  }

  public void setOtherPlayers(ArrayList<OtherPlayer> otherPlayers) {
    if (otherPlayers == null) {
      return;
    }
    this.otherPlayers = otherPlayers;
  }

  /**
   * @return the finishedBoardGames
   */
  public ArrayList<GameMetadata> getFinishedBoardGames() {
    return finishedBoardGames;
  }

  /**
   * @param finishedBoardGames the finishedBoardGames to set
   */
  public void setFinishedBoardGames(ArrayList<GameMetadata> finishedBoardGames) {
    this.finishedBoardGames = finishedBoardGames;
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

  public ArrayList<GameMetadata> getSpectatorBoardGames() {
    return spectatorBoardGames;
  }

  public void setSpectatorBoardGames(ArrayList<GameMetadata> spectatorBoardGames) {
    this.spectatorBoardGames = spectatorBoardGames;
  }

  public static class OtherPlayer {
    private String username;
    private boolean friend;
    private boolean online;

    public OtherPlayer(String username, boolean friend, boolean online) {
      this.username = username;
      this.friend = friend;
      this.online = online;
    }

    public String getUsername() {
      return username;
    }

    public boolean getFriend() {
      return friend;
    }

    public boolean getOnline() {
      return online;
    }
  }
}
