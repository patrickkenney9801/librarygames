package com.nfehs.librarygames.net;

import io.grpc.Channel;
import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import io.grpc.stub.ClientCallStreamObserver;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.nfehs.librarygames.Game;
import com.nfehs.librarygames.Player;
import com.nfehs.librarygames.Player.OtherPlayer;
import com.nfehs.librarygames.games.BoardGame;
import com.nfehs.librarygames.games.BoardGame.GameMetadata;
import com.nfehs.librarygames.games.BoardGame.GameType;
import com.nfehs.librarygames.games.go.Go;
import com.nfehs.librarygames.net.packets.*;
import com.nfehs.librarygames.net.pbs.*;
import com.nfehs.librarygames.net.pbs.AccountProto.*;
import com.nfehs.librarygames.net.pbs.ChatProto.*;
import com.nfehs.librarygames.net.pbs.GameProto.*;

/**
 * This class handles receiving packets from and sending to the server
 * @author Patrick Kenney, Syed Quadri
 * @date 6/13/2018
 */

public class GameClient extends Thread {

  private InetAddress ipAddress;
  private DatagramSocket socket;
  public int port;

  private String[] lastPacketKeysSent;

  private int packetsToReceiveGetPlayers;
  private String[] friends;
  private String[] others;

  private int packetsToReceiveGetGames;
  private String[][] games;

  private int packetsToReceiveGetSpectates;
  private String[][] spectates;

  private boolean lastMoveReceived;

  private ServerToken token;
  private ManagedChannel channel;
  private LoginGrpc.LoginBlockingStub loginStub;
  private CreateAccountGrpc.CreateAccountBlockingStub createAccountStub;
  private LogoutGrpc.LogoutBlockingStub logoutStub;
  private GetUsersGrpc.GetUsersBlockingStub getUsersStub;
  private AddFriendGrpc.AddFriendBlockingStub addFriendStub;
  private CreateGameGrpc.CreateGameBlockingStub createGameStub;
  private GetGamesGrpc.GetGamesBlockingStub getGamesStub;
  private GetSpectatorGamesGrpc.GetSpectatorGamesBlockingStub getSpectatorGamesStub;

  private ChatGrpc.ChatStub chatStub;
  private StreamObserver<ChatRequest> chatWriter;
  private StreamObserver<ChatResponse> chatObserver;

  private GoGrpc.GoStub goStub;
  private StreamObserver<MoveGoRequest> goWriter;
  private StreamObserver<StateGoResponse> goObserver;

  public GameClient(byte[] ipAddress, int port) {
    setLastPacketKeysSent(new String[13]);
    try {
      this.socket = new DatagramSocket();
      this.ipAddress = InetAddress.getByAddress(ipAddress);
      this.port = port;
    } catch (SocketException e) {
      e.printStackTrace();
    } catch (UnknownHostException e) {
      e.printStackTrace();
    }

    String target = System.getenv("LIBRARY_GAMES_SERVER_ADDRESS") + ":" + port;
    token = new ServerToken(this);
    channel = Grpc.newChannelBuilder(target, InsecureChannelCredentials.create()).build();
    loginStub = LoginGrpc.newBlockingStub(channel).withWaitForReady();
    createAccountStub = CreateAccountGrpc.newBlockingStub(channel).withWaitForReady();
    logoutStub = LogoutGrpc.newBlockingStub(channel).withWaitForReady().withCallCredentials(token);
    getUsersStub = GetUsersGrpc.newBlockingStub(channel).withWaitForReady().withCallCredentials(token);
    addFriendStub = AddFriendGrpc.newBlockingStub(channel).withWaitForReady().withCallCredentials(token);
    createGameStub = CreateGameGrpc.newBlockingStub(channel).withWaitForReady().withCallCredentials(token);
    getGamesStub = GetGamesGrpc.newBlockingStub(channel).withWaitForReady().withCallCredentials(token);
    getSpectatorGamesStub = GetSpectatorGamesGrpc.newBlockingStub(channel).withWaitForReady().withCallCredentials(token);
    chatStub = ChatGrpc.newStub(channel).withWaitForReady().withCallCredentials(token);

    goStub = GoGrpc.newStub(channel).withWaitForReady().withCallCredentials(token);
  }

  public void login(final String username, final String password) {
    new Thread (new Runnable () {
      public void run() {
        String t = loginSync(username, password);
        if (t == null) {
          return;
        }
        System.out.println("Res: " + t);

        // verify that user is still on the login screen, if not exit
        if (Game.gameState != Game.LOGIN)
          return;

        // create player from packet
        Game.setPlayer(new Player(username, ""));

        // update server token
        token.SetToken(username, password, t);

        // open active games screen
        Game.openActiveGamesScreen();
      }
    }).start();
  }

  public String loginSync(final String username, final String password) {
    LoginRequest req = LoginRequest.newBuilder().setUsername(username).setPassword(password).build();
    LoginResponse resp;
    try {
      resp = loginStub.login(req);
    } catch (StatusRuntimeException e) {
      System.out.println("login RPC failed: " + e.getStatus());
      return null;
    }
    return resp.getUserToken();
  }

  public void createAccount(String username, String password, String email) {
    new Thread (new Runnable () {
      public void run() {
        CreateAccountRequest req = CreateAccountRequest.newBuilder().setUsername(username).setPassword(password).setEmail(email).build();
        CreateAccountResponse resp;
        try {
          resp = createAccountStub.createAccount(req);
        } catch (StatusRuntimeException e) {
          System.out.println("create account RPC failed: " + e.getStatus());
          return;
        }

        // verify that user is still on the create account screen, if not exit
        if (Game.gameState != Game.CREATE_ACCOUNT)
          return;
        Game.openLoginScreen();
      }
    }).start();
  }

  public void logout() {
    new Thread (new Runnable () {
      public void run() {
        LogoutRequest req = LogoutRequest.newBuilder().build();
        LogoutResponse resp;
        try {
          resp = logoutStub.logout(req);
        } catch (StatusRuntimeException e) {
          System.out.println("logout RPC failed: " + e.getStatus());
          return;
        }

        token.SetToken(null, null, null);
      }
    }).start();
  }

  public void getUsers() {
    new Thread (new Runnable () {
      public void run() {
        GetUsersRequest req = GetUsersRequest.newBuilder().build();
        GetUsersResponse resp;
        try {
          resp = getUsersStub.getUsers(req);
        } catch (StatusRuntimeException e) {
          System.out.println("get users RPC failed: " + e.getStatus());
          return;
        }

        // verify that user is still on the create game screen, if not exit
        if (Game.gameState != Game.CREATE_GAME)
          return;

        List<GetUsersResponse.PeerUser> rawUsers = resp.getUsersList();
        ArrayList<OtherPlayer> friends = new ArrayList<OtherPlayer>();
        ArrayList<OtherPlayer> users = new ArrayList<OtherPlayer>();
        for (GetUsersResponse.PeerUser user : rawUsers) {
          OtherPlayer player = new OtherPlayer(user.getUsername(), user.getFriend(), user.getOnline());
          if (user.getFriend()) {
            friends.add(player);
          } else {
            users.add(player);
          }
        }

        Game.getPlayer().setFriends(friends);
        Game.getPlayer().setOtherPlayers(users);

        // refresh friends and players on create game screen
        Game.updatePlayersList();
      }
    }).start();
  }

  public void addFriend(String friend) {
    new Thread (new Runnable () {
      public void run() {
        AddFriendRequest req = AddFriendRequest.newBuilder().setFriend(friend).build();
        AddFriendResponse resp;
        try {
          resp = addFriendStub.addFriend(req);
        } catch (StatusRuntimeException e) {
          System.out.println("add friend RPC failed: " + e.getStatus());
          return;
        }

        // verify that user is still on the create game screen, if not exit
        if (Game.gameState != Game.CREATE_GAME)
          return;
        getUsers();
      }
    }).start();
  }

  public void createGame(String otherUser, boolean creatorGoesFirst, GameType gameType) {
    new Thread (new Runnable () {
      public void run() {
        var reqBuilder = CreateGameRequest.newBuilder().setOtherUser(otherUser).setGameType(GameProto.GameType.forNumber(gameType.getType()));
        switch (gameType) {
          case GO9x9:
          case GO13x13:
          case GO19x19:   reqBuilder.setGo(CreateGoGameRequest.newBuilder().setCreatorGoesFirst(creatorGoesFirst).build());
                          break;
          default:        // handle invalid game type
                          System.out.println("ERROR INVALID GAME TYPE");
                          return;
        }
        CreateGameRequest req = reqBuilder.build();

        CreateGameResponse resp;
        try {
          resp = createGameStub.createGame(req);
        } catch (StatusRuntimeException e) {
          System.out.println("create game RPC failed: " + e.getStatus());
          return;
        }

        // verify that user is still on the create game screen, if not exit
        if (Game.gameState != Game.CREATE_GAME)
          return;
        Game.openActiveGamesScreen();
      }
    }).start();
  }

  public void getGames() {
    new Thread (new Runnable () {
      public void run() {
        GetGamesRequest req = GetGamesRequest.newBuilder().build();
        GetGamesResponse resp;
        try {
          resp = getGamesStub.getGames(req);
        } catch (StatusRuntimeException e) {
          System.out.println("get games RPC failed: " + e.getStatus());
          return;
        }

        // verify that user is still on the active games screen, if not exit
        if (Game.gameState != Game.ACTIVE_GAMES)
          return;

        List<GameProto.GameMetadata> rawGames = resp.getGamesList();
        ArrayList<GameMetadata> finished = new ArrayList<GameMetadata>();
        ArrayList<GameMetadata> userTurn = new ArrayList<GameMetadata>();
        ArrayList<GameMetadata> opponentTurn = new ArrayList<GameMetadata>();

        for (GameProto.GameMetadata game : rawGames) {
          GameType gameType = GameType.values()[game.getGameType().getNumber()];
          GameMetadata gameData = new GameMetadata(gameType, game.getGameKey(), game.getUser1(), game.getUser2(), game.getMoves(), game.getWinner(), Game.getPlayer().getUsername());

          if (gameData.finished) {
            finished.add(gameData);
          } else if (gameData.userTurn) {
            userTurn.add(gameData);
          } else {
            opponentTurn.add(gameData);
          }
        }

        // set board game Strings in Player
        Game.getPlayer().setFinishedBoardGames(finished);
        Game.getPlayer().setYourTurnBoardGames(userTurn);
        Game.getPlayer().setOpponentTurnBoardGames(opponentTurn);

        // refresh games list on ActiveGamesScreen
        Game.updateActiveGamesList();
      }
    }).start();
  }

  public void getSpectatorGames() {
    new Thread (new Runnable () {
      public void run() {
        GetSpectatorGamesRequest req = GetSpectatorGamesRequest.newBuilder().build();
        GetSpectatorGamesResponse resp;
        try {
          resp = getSpectatorGamesStub.getSpectatorGames(req);
        } catch (StatusRuntimeException e) {
          System.out.println("get spectator games RPC failed: " + e.getStatus());
          return;
        }

        // verify that user is still on the active games screen, if not exit
        if (Game.gameState != Game.SPECTATOR_GAMES)
          return;

        List<GameProto.GameMetadata> rawGames = resp.getGamesList();
        ArrayList<GameMetadata> spectates = new ArrayList<GameMetadata>();

        for (GameProto.GameMetadata game : rawGames) {
          GameType gameType = GameType.values()[game.getGameType().getNumber()];
          GameMetadata gameData = new GameMetadata(gameType, game.getGameKey(), game.getUser1(), game.getUser2(), game.getMoves(), game.getWinner(), Game.getPlayer().getUsername());
          spectates.add(gameData);
        }

        // set board game Strings in Player
        Game.getPlayer().setSpectatorBoardGames(spectates);

        // refresh games list on SpectatorGamesScreen
        Game.updateSpectatorGamesList();
      }
    }).start();
  }

  public void startGo(GameMetadata gameMetadata) {
    new Thread (new Runnable () {
      public void run() {
        try {
          setGoObserver(gameMetadata);
          goWriter = goStub.playGo(goObserver);
          sendGoMove(gameMetadata.gameKey, -3, -3);
        } catch (StatusRuntimeException e) {
          System.out.println("go play game RPC failed: " + e.getStatus());
          return;
        }
      }
    }).start();
  }

  public void sendGoMove(String gameKey, int moveFrom, int moveTo) {
    MoveGoRequest req = MoveGoRequest.newBuilder().setGameKey(gameKey).setMoveFrom(moveFrom).setMoveTo(moveTo).build();
    goWriter.onNext(req);
  }

  public void chat(GameMetadata gameMetadata) {
    new Thread (new Runnable () {
      public void run() {
        try {
          setChatObserver(gameMetadata);
          chatWriter = chatStub.chat(chatObserver);
          sendChat(gameMetadata.gameKey, "", false);
        } catch (StatusRuntimeException e) {
          System.out.println("chat RPC failed: " + e.getStatus());
          return;
        }
      }
    }).start();
  }

  public void sendChat(String gameKey, String message, boolean isPublic) {
    ChatRequest req = ChatRequest.newBuilder().setGameKey(gameKey).setMessage(message).setPublic(isPublic).build();
    chatWriter.onNext(req);
  }

  public void stopGoStream() {
    if (goWriter != null) {
      goWriter.onCompleted();
      goObserver = null;
      goWriter = null;
    }
  }

  public void setGoObserver(GameMetadata gameMetata) {
    stopGoStream();
    goObserver = new StreamObserver<StateGoResponse>() {
        @Override
        public void onNext(StateGoResponse goState) {
          String board = goState.getGameState().getBoard();
          int moves = goState.getGameState().getMoves();
          int winner = goState.getGameState().getWinner();
          int lastMove = goState.getGameState().getLastMove();
          int penultMove = goState.getGameState().getPenultMove();
          boolean player1Online = goState.getGameState().getPlayer1Online();
          boolean player2Online = goState.getGameState().getPlayer2Online();

          int player1StonesCaptured = goState.getP1StonesCaptured();
          int player2StonesCaptured = goState.getP2StonesCaptured();
          int player1Score = goState.getP1Score();
          int player2Score = goState.getP2Score();

          // check to see if user is trying to access game from ActiveGamesScreen or CreateGameScreen or SpectatorGamesScreen
          if (Game.gameState == Game.ACTIVE_GAMES || Game.gameState == Game.CREATE_GAME || Game.gameState == Game.SPECTATOR_GAMES) {
            // if so, set GameBoard and open GameScreen
            Game.setBoardGame(new Go(gameMetata.gameKey, gameMetata.gameType, gameMetata.user1, gameMetata.user2,
                                     moves, penultMove, lastMove, winner, player1Online, player2Online, board,
                                     player1StonesCaptured, player2StonesCaptured, player1Score, player2Score));

            // open GameScreen
            Game.openGameScreen();
          }
          // check to see if user is receiving packet while on GameScreen, if it is the same game, update screen
          else if (Game.gameState == Game.PLAYING_GAME) {
            if (((Go) Game.getBoardGame()).update(gameMetata.gameKey, board,
                                                  penultMove, lastMove, moves, winner, player1Online, player2Online,
                                                  player1StonesCaptured, player2StonesCaptured, player1Score, player2Score)) {
              Game.updateGameBoard();
            }
          }
        }

        @Override
        public void onError(Throwable t) {
          if (t != null) {
            System.out.println("go receiving state RPC failed: " + t.getCause().getMessage());
          }
        }

        @Override
        public void onCompleted() {
        }
      };
  }

  public void stopChatStream() {
    if (chatWriter != null) {
      chatWriter.onCompleted();
      chatObserver = null;
      chatWriter = null;
    }
  }

  public void setChatObserver(GameMetadata gameMetata) {
    stopChatStream();
    chatObserver = new StreamObserver<ChatResponse>() {
        @Override
        public void onNext(ChatResponse chatMessage) {
          String sender = chatMessage.getSender();
          String message = chatMessage.getMessage();

          // verify that user is on game screen, if not exit
          if (Game.gameState != Game.PLAYING_GAME)
            return;

          Game.updateGameChat(sender, message);
        }

        @Override
        public void onError(Throwable t) {
          if (t != null) {
            System.out.println("chat receiving message RPC failed: " + t.getCause().getMessage());
          }
        }

        @Override
        public void onCompleted() {
        }
      };
  }

  /**
   * Receives incoming packets from server
   */
  public void run() {
    while (true) {
      byte[] data = new byte[1024];
      DatagramPacket packet = new DatagramPacket(data, data.length);

      try {
        socket.receive(packet);
      } catch (IOException e) {
        e.printStackTrace();
      }
      System.out.println("SERVER > " + new String(packet.getData()));

      handle(packet);
    }
  }

  /**
   * Determines what to do with incoming packets
   * @param packet
   */
  private void handle(final DatagramPacket packet) {
    // create new thread to handle packet
    new Thread (new Runnable () {
      public void run() {
        switch (Packet.lookupPacket(new String(packet.getData()).trim().substring(0, 2))) {
          case INVALID:        break;
          case LOGIN:          loginUser(packet.getData());
                        break;
          case CREATEACCOUNT:      createAccountLogin(packet.getData());
                        break;
          case ERROR:          handleError(packet.getData());
                        break;
          case LOGOUT:        break;
          case GETPLAYERS:      getPlayers(packet.getData());
                        break;
          case CREATEGAME:      break;
          case GETGAMES:        getGamesOld(packet.getData());
                        break;
          case GETBOARD:        getBoard(packet.getData());
                        break;
          case SENDMOVE:        updateBoard(packet.getData());
                        break;
          case SENDCHAT:        updateChat(packet.getData());
                        break;
          case ONGAME:        updateOnGame(packet.getData());
                        break;
          case GETSPECTATES:      getSpectates(packet.getData());
          default:          break;
        }
      }
    }).start();
  }

  /**
   * Sends data to server
   * @param data
   */
  public void sendData(byte[] data) {
    DatagramPacket packet = new DatagramPacket(data, data.length, ipAddress, port);
    try {
      socket.send(packet);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Handles a user successfully logging to server
   * @param data
   */
  private void loginUser(byte[] data) {
    // verify that user is still on the login screen, if not exit
    if (Game.gameState != Game.LOGIN)
      return;
    Packet00Login packet = new Packet00Login(data, true);
    if (!packet.isValid())
      return;
    // verify that this packet is responding to the last one sent
    if (!packet.getUuidKey().equals(getLastPacketKeysSent()[0]))
      return;

    // create player from packet
    Game.setPlayer(new Player(Security.decrypt(packet.getUsername()), packet.getUserKey()));

    // open active games screen
    Game.openActiveGamesScreen();
  }

  /**
   * Handles a user successfully creating an account and then logging into server
   * @param data
   */
  private void createAccountLogin(byte[] data) {
    // verify that user is still on the create account screen, if not exit
    if (Game.gameState != Game.CREATE_ACCOUNT)
      return;

    Packet01CreateAcc packet = new Packet01CreateAcc(data, true);
    if (!packet.isValid())
      return;
    // verify that this packet is responding to the last one sent
    if (!packet.getUuidKey().equals(getLastPacketKeysSent()[1]))
      return;

    // create player from packet
    Game.setPlayer(new Player(Security.decrypt(packet.getUsername()), packet.getUserKey()));

    // open active games screen
    Game.openActiveGamesScreen();
  }

  /**
   * Handles an error sent by the server
   * @param data
   */
  protected void handleError(byte[] data) {
    Packet02Error errorPacket = new Packet02Error(data, true);
    if (!errorPacket.isValid())
      return;

    // verify error first, then set it on screen through Game class
    // errors left not handled are errors that do not show up when using the client
    switch (errorPacket.getErrorType()) {
      case PACKET00_INVALID_CREDENTIALS:      if (errorPacket.getUuidKey().equals(getLastPacketKeysSent()[0]))
                              Game.setErrorLoginScreen("ERROR: INCORRECT USERNAME OR PASSWORD");
                            break;
      case PACKET01_INVALID_USERNAME:        if (errorPacket.getUuidKey().equals(getLastPacketKeysSent()[1]))
                              Game.setErrorCreateAccountScreen("ERROR: USERNAMES CANNOT USE :, or ~");
                            break;
      case PACKET01_INVALID_ENCRYPTION:      if (errorPacket.getUuidKey().equals(getLastPacketKeysSent()[1]))
                              Game.setErrorCreateAccountScreen("ERROR: PASSWORD ENCRYPTED IMPROPERLY");
                            break;
      case PACKET01_USERNAME_IN_USE:        if (errorPacket.getUuidKey().equals(getLastPacketKeysSent()[1]))
                              Game.setErrorCreateAccountScreen("ERROR: USERNAME IS ALREADY IN USE");
                            break;
      case PACKET05_FRIEND_DOES_NOT_EXIST:
      case PACKET05_ALREADY_FRIENDS:
      case PACKET06_INVALID_GAMETYPE:
      case PACKET06_OPPONENT_DOES_NOT_EXIST:    break;
      case PACKET06_DUPLICATE_GAME:        if (errorPacket.getUuidKey().equals(getLastPacketKeysSent()[6]))
                              Game.setErrorCreateGameScreen("ERROR: A GAME OF THIS TYPE ALREADY EXISTS");
                            break;
      case PACKET08_INVALID_GAME_TYPE:
      case PACKET08_INVALID_GAME_KEY:
      case PACKET09_INVALID_GAME_TYPE:
      case PACKET09_INVALID_GAME_KEY:
      case PACKET09_GAME_ALREADY_OVER:
      case PACKET09_SENDER_NOT_IN_GAME:
      case PACKET09_ILLEGAL_MOVE:
      default:                  break;
    }
  }

  /**
   * Handles a successful request for players
   * @param data
   */
  private void getPlayers(byte[] data) {
    // verify that user is still on the create game screen, if not exit
    if (Game.gameState != Game.CREATE_GAME)
      return;

    Packet04GetPlayers packet = new Packet04GetPlayers(data, true);
    if (!packet.isValid())
      return;
    // verify that this packet is responding to the last one sent
    if (!packet.getUuidKey().equals(getLastPacketKeysSent()[4]))
      return;

    /*
    if (getFriends() == null || getFriends().length != packet.getPacketsSent()) {
      setPacketsToReceiveGetPlayers(packet.getPacketsSent());
      setFriends(new String[packet.getPacketsSent()]);
      setOthers(new String[packet.getPacketsSent()]);
    }

    // set friends and other players lists in class in right row
    getFriends()[packet.getPacketNumber()-1] = packet.getFriends();
    getOthers()[packet.getPacketNumber()-1] = packet.getOtherPlayers();

    // when all packets have been received,
    if (listIsFull(getFriends())) {
      // build strings for friends and others
      String friends = "";
      String others = "";

      for (int i = 0; i < getPacketsToReceiveGetPlayers(); i++) {
        friends += getFriends()[i];
        others += getOthers()[i];
      }

      if (friends.length() < 1)
        friends = null;
      if (others.length() < 1)
        others = null;
      Game.getPlayer().setFriends(friends);
      Game.getPlayer().setOtherPlayers(others);
      // refresh friends and players on create game screen
      Game.updatePlayersList();

      setFriends(null);
    }
    */
  }

  /**
   * Handles a successful request for games
   * @param data
   */
  private void getGamesOld(byte[] data) {
    // verify that user is still on the active games screen, if not exit
    if (Game.gameState != Game.ACTIVE_GAMES)
      return;

    Packet07GetGames packet = new Packet07GetGames(data, true);
    if (!packet.isValid())
      return;
    // verify that this packet is responding to the last one sent
    if (!packet.getUuidKey().equals(getLastPacketKeysSent()[7]))
      return;

/*
    if (getGames() == null || getGames().length != packet.getPacketsSent()) {
      setPacketsToReceiveGetGames(packet.getPacketsSent());
      setGames(new String[packet.getPacketsSent()][]);
    }

    // set games lists in class in right row
    getGames()[packet.getPacketNumber()-1] = packet.getGameInfo();
    // when all packets have been received,
    if (listIsFull(getGames())) {
      ArrayList<String> finished = new ArrayList<String>();
      ArrayList<String> userTurn = new ArrayList<String>();
      ArrayList<String> opponentTurn = new ArrayList<String>();

      for (String[] packetData : getGames())
        for (String info : packetData) {
          if (info != null) {
            String gameData = BoardGame.getGameInfo(info.split(","));

            if (info.split(",").length == 6)
              finished.add(gameData);
            else if (Boolean.parseBoolean(gameData.split("~")[2]))
              userTurn.add(gameData);
            else
              opponentTurn.add(gameData);
          }
        }

      // set board game Strings in Player
      Game.getPlayer().setFinishedBoardGames(finished);
      Game.getPlayer().setYourTurnBoardGames(userTurn);
      Game.getPlayer().setOpponentTurnBoardGames(opponentTurn);

      // refresh games list on ActiveGamesScreen
      Game.updateActiveGamesList();

      setGames(null);
    }
    */
  }

  /**
   * Handles a successful request for a game
   * Also handles server sent games when an action is made
   * Also handles server sent new games
   * @param data
   */
  private void getBoard(byte[] data) {
    Packet08GetBoard packet = new Packet08GetBoard(data, true);
    if (!packet.isValid())
      return;
    // send receipt
    Packet13Receipt receipt = new Packet13Receipt(packet.getUuidKey());
    receipt.writeData(this);
    System.out.println(new String(receipt.getData()));

    GameType gameType = GameType.values()[packet.getGameType()];
/*
    // check to see if user is trying to access game from ActiveGamesScreen or CreateGameScreen or SpectatorGamesScreen
    if ((Game.gameState == Game.ACTIVE_GAMES || Game.gameState == Game.CREATE_GAME || Game.gameState == Game.SPECTATOR_GAMES)
          && (packet.getUuidKey().equals(getLastPacketKeysSent()[8]) || packet.getUuidKey().equals(getLastPacketKeysSent()[6]))
            || packet.getUuidKey().equals(getLastPacketKeysSent()[12])) {

      // if so, set GameBoard and open GameScreen
      Game.setBoardGame(BoardGame.createGame(packet.getGameKey(), gameType, packet.getPlayer1(), packet.getPlayer2(), packet.getMoves(),
          packet.getPenultMove(), packet.getLastMove(), packet.getWinner(), packet.isPlayer1OnGame(), packet.isPlayer2OnGame(), packet.getBoard(), packet.getExtraData()));

      // open GameScreen
      Game.openGameScreen();
    }
    // check to see if user is receiving packet while on GameScreen, if it is the same game, update screen
    else if (Game.gameState == Game.PLAYING_GAME && Game.getBoardGame().update(packet.getGameKey(), packet.getBoard(),
        packet.getPenultMove(), packet.getLastMove(), packet.getMoves(), packet.getWinner(), packet.isPlayer1OnGame(), packet.isPlayer2OnGame(), packet.getExtraData()))
      Game.updateGameBoard();
    // if none of the above, notify the client
    else {
      // if the user is on the active games screen, update it
      if (Game.gameState == Game.ACTIVE_GAMES)
        Game.getActiveGames();
      // notify the user
      Game.notifyUser(BoardGame.createGame(packet.getGameKey(), gameType, packet.getPlayer1(), packet.getPlayer2(), packet.getMoves(),
          packet.getPenultMove(), packet.getLastMove(), packet.getWinner(), true, true, packet.getBoard(), packet.getExtraData()));
    }
    */
  }

  /**
   * Handles a successful move, updates the game board
   * @param data
   */
  private void updateBoard(byte[] data) {
    // verify that user is still on game screen, if not exit
    if (Game.gameState != Game.PLAYING_GAME)
      return;

    Packet09SendMove packet = new Packet09SendMove(data, true);
    if (!packet.isValid())
      return;
    // send receipt
    Packet13Receipt receipt = new Packet13Receipt(packet.getUuidKey());
    receipt.writeData(this);
    System.out.println(new String(receipt.getData()));
    // verify that this packet is responding to the last one sent
    if (!packet.getUuidKey().equals(getLastPacketKeysSent()[9]))
      return;
/*
    // set last move receipt true
    setLastMoveReceived(true);

    // update current board game, returns false if wrong game
    // if successful update, update the game board
    if (Game.getBoardGame().update(  packet.getGameKey(), packet.getBoard(), packet.getPenultMove(), packet.getLastMove(), packet.getMoves(),
                    packet.getWinner(), packet.isPlayer1OnGame(), packet.isPlayer2OnGame(), packet.getExtraData()))
      Game.updateGameBoard();
      */
  }

  /**
   * Handles a server sent chat, updates the game chat
   * @param data
   */
  private void updateChat(byte[] data) {
    // verify that user is on game screen, if not exit
    if (Game.gameState != Game.PLAYING_GAME)
      return;

    Packet10SendChat packet = new Packet10SendChat(data, true);
    if (!packet.isValid())
      return;
    // send receipt
    Packet13Receipt receipt = new Packet13Receipt(packet.getUuidKey());
    receipt.writeData(this);

    // check that client is on correct game, if so update the chat
    if (packet.getGameKey().equals(Game.getBoardGame().getGameKey())) {
      Game.getBoardGame().setPlayer1OnGame(packet.isPlayer1OnGame());
      Game.getBoardGame().setPlayer2OnGame(packet.isPlayer2OnGame());
      Game.updateGameChat(packet.getText(), packet.getSenderKey());
    }
  }

  /**
   * Handles a server onGame packet, updates opponent is on same game
   * @param data
   */
  private void updateOnGame(byte[] data) {
    // verify that user is on game screen, if not exit
    if (Game.gameState != Game.PLAYING_GAME)
      return;

    Packet11OnGame packet = new Packet11OnGame(data, true);
    if (!packet.isValid())
      return;

    // check that client is on correct game, if so update the opponent is on game
    if (packet.getGameKey().equals(Game.getBoardGame().getGameKey())) {
      if (Game.getBoardGame().getPlayer1().equals(Security.decrypt(packet.getPlayer())))
        Game.getBoardGame().setPlayer1OnGame(packet.isOnGame());
      else if (Game.getBoardGame().getPlayer2().equals(Security.decrypt(packet.getPlayer())))
        Game.getBoardGame().setPlayer2OnGame(packet.isOnGame());
    }
    Game.updatePlayersOnGame();
  }

  /**
   * Handles a successful request for spectator games
   * @param data
   */
  private void getSpectates(byte[] data) {
    // verify that user is still on the spectator games screen, if not exit
    if (Game.gameState != Game.SPECTATOR_GAMES)
      return;

    Packet12GetSpectates packet = new Packet12GetSpectates(data, true);
    if (!packet.isValid())
      return;
    // verify that this packet is responding to the last one sent
    if (!packet.getUuidKey().equals(getLastPacketKeysSent()[12]))
      return;
/*
    if (getSpectates() == null || getSpectates().length != packet.getPacketsSent()) {
      setPacketsToReceiveGetSpectates(packet.getPacketsSent());
      setSpectates(new String[packet.getPacketsSent()][]);
    }

    // set spectates lists in class in right row
    getSpectates()[packet.getPacketNumber()-1] = packet.getGameInfo();

    // when all packets have been received,
    if (listIsFull(getSpectates())) {
      ArrayList<String> spectates = new ArrayList<String>();

      for (String[] packetData : getSpectates())
        for (String info : packetData) {
          if (info != null) {
            String gameData = BoardGame.getGameInfo(info.split(","));
            spectates.add(gameData);
          }
        }

      // set board game Strings in Player
      Game.getPlayer().setSpectatorBoardGames(spectates);

      // refresh games list on SpectatorGamesScreen
      Game.updateSpectatorGamesList();

      setSpectates(null);
    }
    */
  }

  /**
   * Returns true if a given string array has no null values
   * @param array
   * @return
   */
  private boolean listIsFull(String[] array) {
    for (String s : array)
      if (s == null)
        return false;
    return true;
  }

  /**
   * Returns true if a given 2d string array has no null values
   * @param array
   * @return
   */
  private boolean listIsFull(String[][] array) {
    for (String[] s : array)
      if (s == null)
        return false;
    return true;
  }

  /**
   * @return the lastPacketKeysSent
   */
  public String[] getLastPacketKeysSent() {
    return lastPacketKeysSent;
  }

  /**
   * @param lastPacketKeysSent the lastPacketKeysSent to set
   */
  public void setLastPacketKeysSent(String[] lastPacketKeysSent) {
    this.lastPacketKeysSent = lastPacketKeysSent;
  }

  public int getPacketsToReceiveGetPlayers() {
    return packetsToReceiveGetPlayers;
  }

  public void setPacketsToReceiveGetPlayers(int packetsToReceiveGetPlayers) {
    this.packetsToReceiveGetPlayers = packetsToReceiveGetPlayers;
  }

  public String[] getFriends() {
    return friends;
  }

  public void setFriends(String[] friends) {
    this.friends = friends;
  }

  public String[] getOthers() {
    return others;
  }

  public void setOthers(String[] others) {
    this.others = others;
  }

  public int getPacketsToReceiveGetGames() {
    return packetsToReceiveGetGames;
  }

  public void setPacketsToReceiveGetGames(int packetsToReceiveGetGames) {
    this.packetsToReceiveGetGames = packetsToReceiveGetGames;
  }
/*
  public String[][] getGames() {
    return games;
  }
*/
  public void setGames(String[][] games) {
    this.games = games;
  }

  public int getPacketsToReceiveGetSpectates() {
    return packetsToReceiveGetSpectates;
  }

  public void setPacketsToReceiveGetSpectates(int packetsToReceiveGetSpectates) {
    this.packetsToReceiveGetSpectates = packetsToReceiveGetSpectates;
  }

  public String[][] getSpectates() {
    return spectates;
  }

  public void setSpectates(String[][] spectates) {
    this.spectates = spectates;
  }

  public boolean isLastMoveReceived() {
    return lastMoveReceived;
  }

  public void setLastMoveReceived(boolean lastMoveReceived) {
    this.lastMoveReceived = lastMoveReceived;
  }
}
