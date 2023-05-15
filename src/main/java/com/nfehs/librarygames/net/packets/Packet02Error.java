package com.nfehs.librarygames.net.packets;

/**
 * Packets pertaining to errors
 * @author Patrick Kenney and Syed Quadri
 * @date 8/12/2018
 */

public class Packet02Error extends Packet {
  /**
   * Creates an enum to encompass all server error types
   * @author Patrick
   */
  public static enum ErrorType {
    INVALID(-1), PACKET00_INVALID_CREDENTIALS(0), PACKET01_INVALID_USERNAME(1), PACKET01_INVALID_ENCRYPTION(2),
    PACKET01_USERNAME_IN_USE(3), PACKET05_FRIEND_DOES_NOT_EXIST(4), PACKET05_ALREADY_FRIENDS(5), PACKET06_INVALID_GAMETYPE(6),
    PACKET06_OPPONENT_DOES_NOT_EXIST(7), PACKET06_DUPLICATE_GAME(8), PACKET08_INVALID_GAME_TYPE(9), PACKET08_INVALID_GAME_KEY(10),
    PACKET09_INVALID_GAME_TYPE(11), PACKET09_INVALID_GAME_KEY(12), PACKET09_GAME_ALREADY_OVER(13), PACKET09_SENDER_NOT_IN_GAME(14),
    PACKET09_ILLEGAL_MOVE(15);

    private int errorId;
    private ErrorType(int errorId) {
      this.errorId = errorId;
    }

    public int getId() {
      return errorId;
    }
  }

  // sent by server
  private int error;

  // found by client
  private ErrorType errorType;

  /**
   * Sends an error to client from server
   * @param error
   * @param serverUse boolean that serves no purpose other than to distinguish constructors
   */
  public Packet02Error(String packetKey, ErrorType error, boolean serverUse) {
    super(02);
    setUuidKey(packetKey);
    setError(error.getId());
  }

  /**
   * Used by server to retrieve data from client's data packet
   * @param data
   * @param serverUse boolean that serves no purpose other than to distinguish constructors
   */
  public Packet02Error(byte[] data, boolean serverUse) {
    super(01);

    try {
      String[] userdata = readData(data).split(":");
      setUuidKey(userdata[0]);
      setError(Integer.parseInt(userdata[1]));
      setErrorType(lookupError(getError()));
    } catch (Exception e) {
      e.printStackTrace();
      setValid(false);
    }
  }

  @Override
  public byte[] getData() {
    return ("02").getBytes();
  }

  @Override
  public byte[] getDataServer() {
    return ("02" + getUuidKey() + ":" + getError()).getBytes();
  }

  /**
   * This method returns what type of error the error is
   * @param error int
   * @return
   */
  public static ErrorType lookupError(int id) {
    for (ErrorType e : ErrorType.values()) {
      if (e.getId() == id)
        return e;
    }
    return ErrorType.INVALID;
  }

  public int getError() {
    return error;
  }

  public void setError(int error) {
    this.error = error;
  }

  public ErrorType getErrorType() {
    return errorType;
  }

  public void setErrorType(ErrorType errorType) {
    this.errorType = errorType;
  }
}
