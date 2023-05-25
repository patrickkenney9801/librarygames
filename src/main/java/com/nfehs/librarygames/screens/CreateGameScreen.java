package com.nfehs.librarygames.screens;

import com.nfehs.librarygames.Game;
import com.nfehs.librarygames.GameFrame;
import com.nfehs.librarygames.Player;
import com.nfehs.librarygames.games.BoardGame.GameType;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

/**
 * Handles the user creating a game,
 *
 * @author Patrick Kenney and Syed Quadri
 * @date 8/11/2018
 */
public class CreateGameScreen extends Screen {
  private final String[] choices = {"Go 9x9", "Go 13x13", "Go 19x19"};
  private JComboBox<String> gameChoices;

  private ButtonGroup startingPlayer;
  private JRadioButton userGoesFirst;
  private JRadioButton opponentGoesFirst;
  private JButton createGame;
  private JLabel error;
  private JButton back;
  private JButton refresh;

  private JLabel chooseOpponent;
  private JLabel chooseFriend;
  private JLabel chooseRandomPlayer;
  private ButtonGroup players;

  private JPanel friendsPanel;
  private JScrollPane friendsTab;
  private JRadioButton[] friends;

  private JPanel randomPlayersPanel;
  private JScrollPane randomPlayersTab;
  private JRadioButton[] randomPlayers;
  private JButton[] addFriends;

  public CreateGameScreen() {
    super(false);

    gameChoices = new JComboBox<String>(choices);
    Game.mainWindow.add(gameChoices);
    gameChoices.setBounds(
        (int) Game.screenSize.getWidth() / 2 - 225,
        (int) Game.screenSize.getHeight() / 10,
        150,
        30);
    gameChoices.setSelectedIndex(0);

    userGoesFirst = new JRadioButton("I go first");
    Game.mainWindow.add(userGoesFirst);
    userGoesFirst.setBounds(
        (int) Game.screenSize.getWidth() / 2 + 30, (int) Game.screenSize.getHeight() / 10, 120, 30);
    userGoesFirst.setSelected(true);

    opponentGoesFirst = new JRadioButton("Opponent goes first");
    Game.mainWindow.add(opponentGoesFirst);
    opponentGoesFirst.setBounds(
        (int) Game.screenSize.getWidth() / 2 + 160,
        (int) Game.screenSize.getHeight() / 10,
        150,
        30);

    startingPlayer = new ButtonGroup();
    startingPlayer.add(userGoesFirst);
    startingPlayer.add(opponentGoesFirst);

    chooseOpponent = new JLabel("Choose opponent:");
    Game.mainWindow.add(chooseOpponent);
    chooseOpponent.setBounds(
        (int) Game.screenSize.getWidth() / 2 - 75,
        (int) Game.screenSize.getHeight() / 10 + 75,
        150,
        30);

    chooseFriend = new JLabel("Friends:");
    Game.mainWindow.add(chooseFriend);
    chooseFriend.setBounds(
        (int) Game.screenSize.getWidth() / 2 - 275,
        (int) Game.screenSize.getHeight() / 10 + 125,
        150,
        30);

    chooseRandomPlayer = new JLabel("All players:");
    Game.mainWindow.add(chooseRandomPlayer);
    chooseRandomPlayer.setBounds(
        (int) Game.screenSize.getWidth() / 2 + 10,
        (int) Game.screenSize.getHeight() / 10 + 125,
        150,
        30);

    friendsPanel = new JPanel();
    friendsPanel.setLayout(null);
    friendsPanel.setBackground(GameFrame.background);
    friendsTab = new JScrollPane(friendsPanel);
    friendsTab.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    friendsTab.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    friendsTab.setAutoscrolls(true);
    friendsTab.getVerticalScrollBar().setUnitIncrement(15);

    randomPlayersPanel = new JPanel();
    randomPlayersPanel.setLayout(null);
    randomPlayersPanel.setBackground(GameFrame.background);
    randomPlayersTab = new JScrollPane(randomPlayersPanel);
    randomPlayersTab.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    randomPlayersTab.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    randomPlayersTab.setAutoscrolls(true);
    randomPlayersTab.getVerticalScrollBar().setUnitIncrement(15);

    players = new ButtonGroup();
    friends = new JRadioButton[0];
    randomPlayers = new JRadioButton[0];
    addFriends = new JButton[0];

    refresh = new JButton("REFRESH");
    Game.mainWindow.add(refresh);
    refresh.setBounds(
        (int) Game.screenSize.getWidth() / 2 - 75,
        (int) Game.screenSize.getHeight() / 5 * 4 - 40,
        150,
        30);
    refresh.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            // goes back to active games list
            System.out.println("Refresh clicked from create game");
            Game.getOtherPlayers();
          }
        });

    createGame = new JButton("CREATE GAME");
    Game.mainWindow.add(createGame);
    createGame.setBounds(
        (int) Game.screenSize.getWidth() / 2 - 75,
        (int) Game.screenSize.getHeight() / 5 * 4,
        150,
        30);
    createGame.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            // attempts to create a new game
            System.out.println("Create game clicked");

            // get other user from selected JRadioButton
            String otherUser = null;
            for (JRadioButton button : friends)
              if (button.isSelected()) otherUser = button.getText();
            for (JRadioButton button : randomPlayers)
              if (button.isSelected()) otherUser = button.getText();

            // if no opponent is selected to play against, exit and send error to screen
            if (otherUser == null) {
              setError("ERROR: NO OPPONENT SELECTED");
              System.out.println("No opponent selected");
              return;
            }

            // get creator goes first from startingPlayer
            boolean creatorGoesFirst = false;
            if (userGoesFirst.isSelected()) creatorGoesFirst = true;

            // get gameType from selected index of gameChoices
            int type = gameChoices.getSelectedIndex();

            // if the no gameType is selected or there is some other error, exit and send message to
            // screen
            if (type < 0) {
              setError("ERROR: NO GAME TYPE SELECTED");
              System.out.println("No game type selected");
              return;
            }
            GameType gameType = GameType.values()[type + 1];
            Game.createGame(otherUser, creatorGoesFirst, gameType);
          }
        });

    error = new JLabel();
    Game.mainWindow.add(error);
    error.setBounds(
        (int) Game.screenSize.getWidth() / 2 - 150,
        (int) Game.screenSize.getHeight() / 5 * 4 + 30,
        300,
        20);
    error.setForeground(Color.RED);
    error.setHorizontalAlignment(SwingConstants.CENTER);

    back = new JButton("BACK");
    Game.mainWindow.add(back);
    back.setBounds(
        (int) Game.screenSize.getWidth() / 2 - 75,
        (int) Game.screenSize.getHeight() / 5 * 4 + 60,
        150,
        30);
    back.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            // goes back to active games list
            System.out.println("Back clicked from create game");
            Game.openActiveGamesScreen();
          }
        });
  }

  /** This method is called once the client has received player data from the server */
  public void loadPlayers() {
    // remove current players
    removeAndNullPlayers();

    // add friends
    friends = new JRadioButton[Game.getPlayer().getFriends().size()];

    friendsPanel.setPreferredSize(new Dimension(285, friends.length * 30));
    friendsPanel.setBounds(5, 5, 285, friends.length * 30);

    friendsTab.setBounds(
        (int) Game.screenSize.getWidth() / 2 - 325,
        (int) Game.screenSize.getHeight() / 10 + 155,
        300,
        (int) Game.screenSize.getHeight() / 5 * 4
            - ((int) Game.screenSize.getHeight() / 10 + 155)
            - 50);
    Game.mainWindow.add(friendsTab);

    for (int i = 0; i < friends.length; i++) {
      Player.OtherPlayer friend = Game.getPlayer().getFriends().get(i);
      friends[i] = new JRadioButton(friend.getUsername());
      players.add(friends[i]);
      friends[i].setBounds(5, 5 + i * 30, friendsPanel.getWidth() - 10, 30);
      friends[i].setForeground(friend.getOnline() ? Color.GREEN : Color.RED);
      friendsPanel.add(friends[i]);
    }

    // add other players
    randomPlayers = new JRadioButton[Game.getPlayer().getOtherPlayers().size()];

    randomPlayersPanel.setPreferredSize(new Dimension(285, randomPlayers.length * 30 + 10));
    randomPlayersPanel.setBounds(5, 0, 285, randomPlayers.length * 30 + 10);

    randomPlayersTab.setBounds(
        (int) Game.screenSize.getWidth() / 2 + 25,
        (int) Game.screenSize.getHeight() / 10 + 155,
        300,
        (int) Game.screenSize.getHeight() / 5 * 4
            - ((int) Game.screenSize.getHeight() / 10 + 155)
            - 50);
    Game.mainWindow.add(randomPlayersTab);

    for (int i = 0; i < randomPlayers.length; i++) {
      Player.OtherPlayer player = Game.getPlayer().getOtherPlayers().get(i);
      randomPlayers[i] = new JRadioButton(player.getUsername());
      players.add(randomPlayers[i]);
      randomPlayers[i].setBounds(5, 5 + i * 30, randomPlayersPanel.getWidth() - 85, 30);
      randomPlayers[i].setForeground(player.getOnline() ? Color.GREEN : Color.RED);
      randomPlayersPanel.add(randomPlayers[i]);
    }

    // add 'add friend' buttons
    addFriends = new JButton[randomPlayers.length];
    for (int i = 0; i < addFriends.length; i++) {
      addFriends[i] = new JButton("ADD");
      addFriends[i].setBounds(200, 5 + i * 30, randomPlayersPanel.getWidth() - 215, 30);
      randomPlayersPanel.add(addFriends[i]);
      addFriends[i].addActionListener(
          new ActionListener() {
            public void actionPerformed(ActionEvent e) {
              // get the element number for the username
              int elemNumber = -1;
              for (int i = 0; i < addFriends.length; i++)
                if (addFriends[i].equals(e.getSource())) elemNumber = i;
              System.out.println(
                  "Add friend button clicked: " + randomPlayers[elemNumber].getText());
              ((JButton) e.getSource()).setEnabled(false);

              // send friend request to server
              Game.addFriend(randomPlayers[elemNumber].getText());
            }
          });
    }
    friendsTab.revalidate();
    randomPlayersTab.revalidate();
    Game.mainWindow.repaint();
  }

  /**
   * Sets error text to the errorMessage called locally and by Game class
   *
   * @param errorMessage
   */
  public void setError(String errorMessage) {
    error.setText(errorMessage);
  }

  /** Removes and nulls all current players in JRadioButtons */
  private void removeAndNullPlayers() {
    for (JRadioButton friend : friends) {
      // Game.mainWindow.remove(friend);
      friendsPanel.remove(friend);
      friend = null;
    }
    for (JRadioButton randomPlayer : randomPlayers) {
      // Game.mainWindow.remove(randomPlayer);
      randomPlayersPanel.remove(randomPlayer);
      randomPlayer = null;
    }
    for (JButton addFriend : addFriends) {
      // Game.mainWindow.remove(addFriend);
      randomPlayersPanel.remove(addFriend);
      addFriend = null;
    }

    Game.mainWindow.remove(friendsTab);
    Game.mainWindow.remove(randomPlayersTab);
  }

  @Override
  public void exit() {
    exitParentGUI();

    Game.mainWindow.remove(gameChoices);
    Game.mainWindow.remove(userGoesFirst);
    Game.mainWindow.remove(opponentGoesFirst);
    Game.mainWindow.remove(createGame);
    Game.mainWindow.remove(chooseOpponent);
    Game.mainWindow.remove(chooseFriend);
    Game.mainWindow.remove(chooseRandomPlayer);
    Game.mainWindow.remove(back);
    Game.mainWindow.remove(refresh);
    Game.mainWindow.remove(error);

    gameChoices = null;
    userGoesFirst = null;
    opponentGoesFirst = null;
    startingPlayer = null;
    createGame = null;
    chooseOpponent = null;
    chooseFriend = null;
    chooseRandomPlayer = null;
    back = null;
    refresh = null;
    error = null;

    removeAndNullPlayers();
    players = null;
    addFriends = null;
    friendsTab = null;
    randomPlayersTab = null;

    Game.mainWindow.repaint();
  }
}
