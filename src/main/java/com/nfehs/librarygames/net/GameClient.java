package com.nfehs.librarygames.net;

import com.nfehs.librarygames.Game;
import com.nfehs.librarygames.Player;
import com.nfehs.librarygames.Player.OtherPlayer;
import com.nfehs.librarygames.games.BoardGame.GameMetadata;
import com.nfehs.librarygames.games.BoardGame.GameType;
import com.nfehs.librarygames.games.go.Go;
import com.nfehs.librarygames.net.pbs.*;
import com.nfehs.librarygames.net.pbs.AccountProto.*;
import com.nfehs.librarygames.net.pbs.ChatProto.*;
import com.nfehs.librarygames.net.pbs.GameProto.*;
import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import java.util.ArrayList;
import java.util.List;

/**
 * This class handles receiving packets from and sending to the server
 *
 * @author Patrick Kenney, Syed Quadri
 * @date 6/13/2018
 */
public class GameClient {

  private String serverAddress;

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

  public GameClient(String serverAddress) {
    this.serverAddress = serverAddress;
    token = new ServerToken(this);
    channel = Grpc.newChannelBuilder(serverAddress, InsecureChannelCredentials.create()).build();
    loginStub = LoginGrpc.newBlockingStub(channel).withWaitForReady();
    createAccountStub = CreateAccountGrpc.newBlockingStub(channel).withWaitForReady();
    logoutStub = LogoutGrpc.newBlockingStub(channel).withWaitForReady().withCallCredentials(token);
    getUsersStub =
        GetUsersGrpc.newBlockingStub(channel).withWaitForReady().withCallCredentials(token);
    addFriendStub =
        AddFriendGrpc.newBlockingStub(channel).withWaitForReady().withCallCredentials(token);
    createGameStub =
        CreateGameGrpc.newBlockingStub(channel).withWaitForReady().withCallCredentials(token);
    getGamesStub =
        GetGamesGrpc.newBlockingStub(channel).withWaitForReady().withCallCredentials(token);
    getSpectatorGamesStub =
        GetSpectatorGamesGrpc.newBlockingStub(channel)
            .withWaitForReady()
            .withCallCredentials(token);
    chatStub = ChatGrpc.newStub(channel).withWaitForReady().withCallCredentials(token);

    goStub = GoGrpc.newStub(channel).withWaitForReady().withCallCredentials(token);
  }

  public void login(final String username, final String password) {
    new Thread(
            new Runnable() {
              public void run() {
                String t = loginSync(username, password);
                if (t == null) {
                  return;
                }

                // verify that user is still on the login screen, if not exit
                if (Game.gameState != Game.LOGIN) return;

                // create player from packet
                Game.setPlayer(new Player(username, ""));

                // update server token
                token.SetToken(username, password, t);

                // open active games screen
                Game.openActiveGamesScreen();
              }
            })
        .start();
  }

  public String loginSync(final String username, final String password) {
    LoginRequest req =
        LoginRequest.newBuilder().setUsername(username).setPassword(password).build();
    LoginResponse resp;
    try {
      resp = loginStub.login(req);
    } catch (StatusRuntimeException e) {
      System.out.println("login RPC failed: " + e.getStatus());
      if (Game.gameState == Game.LOGIN) {
        Game.setErrorLoginScreen(e.getStatus().getDescription());
      }
      return null;
    }
    return resp.getUserToken();
  }

  public void createAccount(String username, String password, String email) {
    new Thread(
            new Runnable() {
              public void run() {
                CreateAccountRequest req =
                    CreateAccountRequest.newBuilder()
                        .setUsername(username)
                        .setPassword(password)
                        .setEmail(email)
                        .build();
                CreateAccountResponse resp;
                try {
                  resp = createAccountStub.createAccount(req);
                } catch (StatusRuntimeException e) {
                  System.out.println("create account RPC failed: " + e.getStatus());
                  if (Game.gameState == Game.CREATE_ACCOUNT) {
                    Game.setErrorCreateAccountScreen(e.getStatus().getDescription());
                  }
                  return;
                }

                // verify that user is still on the create account screen, if not exit
                if (Game.gameState != Game.CREATE_ACCOUNT) return;
                Game.openLoginScreen();
              }
            })
        .start();
  }

  public void logout() {
    new Thread(
            new Runnable() {
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
            })
        .start();
  }

  public void getUsers() {
    new Thread(
            new Runnable() {
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
                if (Game.gameState != Game.CREATE_GAME) return;

                List<GetUsersResponse.PeerUser> rawUsers = resp.getUsersList();
                ArrayList<OtherPlayer> friends = new ArrayList<OtherPlayer>();
                ArrayList<OtherPlayer> users = new ArrayList<OtherPlayer>();
                for (GetUsersResponse.PeerUser user : rawUsers) {
                  OtherPlayer player =
                      new OtherPlayer(user.getUsername(), user.getFriend(), user.getOnline());
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
            })
        .start();
  }

  public void addFriend(String friend) {
    new Thread(
            new Runnable() {
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
                if (Game.gameState != Game.CREATE_GAME) return;
                getUsers();
              }
            })
        .start();
  }

  public void createGame(String otherUser, boolean creatorGoesFirst, GameType gameType) {
    new Thread(
            new Runnable() {
              public void run() {
                var reqBuilder =
                    CreateGameRequest.newBuilder()
                        .setOtherUser(otherUser)
                        .setGameType(GameProto.GameType.forNumber(gameType.getType()));
                switch (gameType) {
                  case GO9x9:
                  case GO13x13:
                  case GO19x19:
                    reqBuilder.setGo(
                        CreateGoGameRequest.newBuilder()
                            .setCreatorGoesFirst(creatorGoesFirst)
                            .build());
                    break;
                  default: // handle invalid game type
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
                if (Game.gameState != Game.CREATE_GAME) return;
                Game.openActiveGamesScreen();
              }
            })
        .start();
  }

  public void getGames() {
    new Thread(
            new Runnable() {
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
                if (Game.gameState != Game.ACTIVE_GAMES) return;

                List<GameProto.GameMetadata> rawGames = resp.getGamesList();
                ArrayList<GameMetadata> finished = new ArrayList<GameMetadata>();
                ArrayList<GameMetadata> userTurn = new ArrayList<GameMetadata>();
                ArrayList<GameMetadata> opponentTurn = new ArrayList<GameMetadata>();

                for (GameProto.GameMetadata game : rawGames) {
                  GameType gameType = GameType.values()[game.getGameType().getNumber()];
                  GameMetadata gameData =
                      new GameMetadata(
                          gameType,
                          game.getGameKey(),
                          game.getUser1(),
                          game.getUser2(),
                          game.getMoves(),
                          game.getWinner(),
                          Game.getPlayer().getUsername());

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
            })
        .start();
  }

  public void getSpectatorGames() {
    new Thread(
            new Runnable() {
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
                if (Game.gameState != Game.SPECTATOR_GAMES) return;

                List<GameProto.GameMetadata> rawGames = resp.getGamesList();
                ArrayList<GameMetadata> spectates = new ArrayList<GameMetadata>();

                for (GameProto.GameMetadata game : rawGames) {
                  GameType gameType = GameType.values()[game.getGameType().getNumber()];
                  GameMetadata gameData =
                      new GameMetadata(
                          gameType,
                          game.getGameKey(),
                          game.getUser1(),
                          game.getUser2(),
                          game.getMoves(),
                          game.getWinner(),
                          Game.getPlayer().getUsername());
                  spectates.add(gameData);
                }

                // set board game Strings in Player
                Game.getPlayer().setSpectatorBoardGames(spectates);

                // refresh games list on SpectatorGamesScreen
                Game.updateSpectatorGamesList();
              }
            })
        .start();
  }

  public void startGo(GameMetadata gameMetadata) {
    new Thread(
            new Runnable() {
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
            })
        .start();
  }

  public void sendGoMove(String gameKey, int moveFrom, int moveTo) {
    MoveGoRequest req =
        MoveGoRequest.newBuilder()
            .setGameKey(gameKey)
            .setMoveFrom(moveFrom)
            .setMoveTo(moveTo)
            .build();
    goWriter.onNext(req);
  }

  public void chat(GameMetadata gameMetadata) {
    new Thread(
            new Runnable() {
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
            })
        .start();
  }

  public void sendChat(String gameKey, String message, boolean isPublic) {
    ChatRequest req =
        ChatRequest.newBuilder()
            .setGameKey(gameKey)
            .setMessage(message)
            .setPublic(isPublic)
            .build();
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
    goObserver =
        new StreamObserver<StateGoResponse>() {
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

            // check to see if user is trying to access game from ActiveGamesScreen or
            // CreateGameScreen or SpectatorGamesScreen
            if (Game.gameState == Game.ACTIVE_GAMES
                || Game.gameState == Game.CREATE_GAME
                || Game.gameState == Game.SPECTATOR_GAMES) {
              // if so, set GameBoard and open GameScreen
              Game.setBoardGame(
                  new Go(
                      gameMetata.gameKey,
                      gameMetata.gameType,
                      gameMetata.user1,
                      gameMetata.user2,
                      moves,
                      penultMove,
                      lastMove,
                      winner,
                      player1Online,
                      player2Online,
                      board,
                      player1StonesCaptured,
                      player2StonesCaptured,
                      player1Score,
                      player2Score));

              // open GameScreen
              Game.openGameScreen();
            }
            // check to see if user is receiving packet while on GameScreen, if it is the same game,
            // update screen
            else if (Game.gameState == Game.PLAYING_GAME) {
              if (((Go) Game.getBoardGame())
                  .update(
                      gameMetata.gameKey,
                      board,
                      penultMove,
                      lastMove,
                      moves,
                      winner,
                      player1Online,
                      player2Online,
                      player1StonesCaptured,
                      player2StonesCaptured,
                      player1Score,
                      player2Score)) {
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
          public void onCompleted() {}
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
    chatObserver =
        new StreamObserver<ChatResponse>() {
          @Override
          public void onNext(ChatResponse chatMessage) {
            String sender = chatMessage.getSender();
            String message = chatMessage.getMessage();

            // verify that user is on game screen, if not exit
            if (Game.gameState != Game.PLAYING_GAME) return;

            Game.updateGameChat(sender, message);
          }

          @Override
          public void onError(Throwable t) {
            if (t != null) {
              System.out.println("chat receiving message RPC failed: " + t.getCause().getMessage());
            }
          }

          @Override
          public void onCompleted() {}
        };
  }
}
