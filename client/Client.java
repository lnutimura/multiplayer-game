import java.io.*;
import java.awt.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.imageio.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

class ClientGlobals{
  static boolean isPlayable = true;
  static boolean isSetting = true;
  static boolean hasScored = false;
  static int playerID = 0;
  static int currentLine;
  static int portValue = 8080;
  static String ipAddress = "localhost";
  static Client.Enemy hitZone = null;
}

class Client extends JFrame implements Runnable {
  static Vector<Images> arrayImgs = new Vector<Images>();

  int currentForm[] = new int[3];
  Image player[][] = new Image[3][3];
  Object sync = new Object();
  Scanner serverIS;
  PrintStream serverOS;
  static EnemyManager eMgr;
  Score playerScore = new Score();
  Scene sc = new Scene();
  BGM bgm = new BGM();

  public static void main (String[] args) {
    Socket socket = null;
    Scanner is = null;
    PrintStream os = null;

    /*new UserPreferences();
    while (ClientGlobals.isSetting)
      try { Thread.sleep(10); }
      catch (InterruptedException e) {}
    */

    try {
      socket = new Socket(ClientGlobals.ipAddress, ClientGlobals.portValue);
      is = new Scanner(socket.getInputStream());
      os = new PrintStream(socket.getOutputStream());
    } catch (UnknownHostException e) {
        System.err.println("Don't know about host.");
    } catch (IOException e) {
      System.err.println("Couldn't get I/O for the connection to host.");
    }

    System.out.println("Client connected to: " + ClientGlobals.ipAddress + "/" + ClientGlobals.portValue);

    String temp = is.nextLine();
    if (temp.startsWith("PLAYERID")){
      ClientGlobals.playerID = Character.getNumericValue(temp.charAt(9));
      System.out.println(">> MY ID: " + ClientGlobals.playerID);
    }
    else{
      System.err.println("Couldn't get Player's ID.");
      endGame();
    }

    Client client = new Client(is,os);
    new Thread(client).start();
  }

  Client(Scanner i, PrintStream o) {
    super("Untitled Game");
    
    this.serverIS = i;
    this.serverOS = o;

    setLocation(400,0);

    add(sc);
    addKeyListener(new KeyAdapter() {
      public void keyPressed(KeyEvent e) {
        
        switch(e.getKeyCode()) {
        case KeyEvent.VK_LEFT:
        case KeyEvent.VK_A:
          serverOS.println("KEYPRESS A"+":"+currentForm[GameGlobals.LEFT]);
          if (ClientGlobals.playerID == 1){
            changeForm('A',currentForm[GameGlobals.LEFT]);
          }
          break;
        case KeyEvent.VK_DOWN:
        case KeyEvent.VK_S:
          serverOS.println("KEYPRESS S"+":"+currentForm[GameGlobals.CENTER]);
          if (ClientGlobals.playerID == 1){
            changeForm('S',currentForm[GameGlobals.CENTER]);
          }
          break;
        case KeyEvent.VK_RIGHT:
        case KeyEvent.VK_D:
          serverOS.println("KEYPRESS D"+":"+currentForm[GameGlobals.RIGHT]);
          if (ClientGlobals.playerID == 1){
            changeForm('D',currentForm[GameGlobals.RIGHT]);
          }
          break;
        }
      }
    });

    setDefaultCloseOperation(EXIT_ON_CLOSE);
    pack();
    setVisible(true); 
    bgm.playTitleMusic();
  }

  public void changeForm(Character position, int actualShape){
    int newShape = -1;
    switch(actualShape){
    case GameGlobals.TRIANGLE:
      newShape = GameGlobals.SQUARE;
      break;
    case GameGlobals.SQUARE:
      newShape = GameGlobals.CIRCLE;
      break;
    case GameGlobals.CIRCLE:
      newShape = GameGlobals.TRIANGLE;
      break;
    default:
      System.err.println("Error: Unindentified value in variable 'actualShape'.");
    }
    switch (position){
    case 'A':
      currentForm[GameGlobals.LEFT] = newShape;
      arrayImgs.set(1,new Images(player[GameGlobals.LEFT][currentForm[GameGlobals.LEFT]],GameGlobals.S_XL,GameGlobals.S_Y,GameGlobals.S_SIZE_XY,GameGlobals.S_SIZE_XY));
      break;
    case 'S':
      currentForm[GameGlobals.CENTER] = newShape;
      arrayImgs.set(2,new Images(player[GameGlobals.CENTER][currentForm[GameGlobals.CENTER]],GameGlobals.S_XC,GameGlobals.S_Y,GameGlobals.S_SIZE_XY,GameGlobals.S_SIZE_XY));
      break;
    case 'D':
      currentForm[GameGlobals.RIGHT] = newShape;
      arrayImgs.set(3,new Images(player[GameGlobals.RIGHT][currentForm[GameGlobals.RIGHT]],GameGlobals.S_XR,GameGlobals.S_Y,GameGlobals.S_SIZE_XY,GameGlobals.S_SIZE_XY));
      break;
    }
    sc.repaint();
  }

  public void run() {
    try {
      String response;
      Character position;
      int newShape;
      Matcher m;

      eMgr = new EnemyManager();
      eMgr.start();

      while (ClientGlobals.isPlayable) {
        response = serverIS.nextLine();
        // ENEMY x:y:z
        if (response.startsWith("ENEMY")) {
          eMgr.queueEnemy(response);
        }
        // FORM x:y -- x is which position, y is what form it'll become
        else if (response.startsWith("FORM")) {
          position = response.charAt(5);
          newShape = Character.getNumericValue(response.charAt(7));
          changeForm(position, newShape);
        }
        // SCORED x >> x is position hit
        else if (response.startsWith("SCORED")) {
          m = Pattern.compile("\\w+\\s(\\d)").matcher(response);
          m.find();
          ClientGlobals.hitZone.enemyImg[Integer.parseInt(m.group(1))] = null;
          playerScore.winPoints(10);
        }
        else if (response.startsWith("LOST")) {
          playerScore.losePoints(20);
        }
      }

    } catch (Exception e){
      //System.out.println("Server terminated. Game will be shut down: " + e);
      e.printStackTrace();
    }
    serverIS.close();
    serverOS.close();
    endGame();
  }

  public static void endGame(){
    System.out.println("Game over.");
    System.exit(0);
  } 

  class Images {
    public Image graphic;
    public int posX, posY, sizeX, sizeY;
    Images (Image graphic, int posX, int posY, int sizeX, int sizeY) {
      this.graphic = graphic;
      this.posX = posX;
      this.posY = posY;
      this.sizeX = sizeX;
      this.sizeY = sizeY;
    }
  }

  class Scene extends JPanel {
    Image background;
    Scene() {
      try {
        background = ImageIO.read(GameGlobals.BG_IMG);
        player[GameGlobals.LEFT][GameGlobals.CIRCLE] = ImageIO.read(GameGlobals.BCIRC_IMG);
        player[GameGlobals.LEFT][GameGlobals.TRIANGLE] = ImageIO.read(GameGlobals.BTRI_IMG);
        player[GameGlobals.LEFT][GameGlobals.SQUARE] = ImageIO.read(GameGlobals.BSQU_IMG);

        player[GameGlobals.CENTER][GameGlobals.CIRCLE] = ImageIO.read(GameGlobals.BCIRC_IMG);
        player[GameGlobals.CENTER][GameGlobals.TRIANGLE] = ImageIO.read(GameGlobals.BTRI_IMG);
        player[GameGlobals.CENTER][GameGlobals.SQUARE] = ImageIO.read(GameGlobals.BSQU_IMG);

        player[GameGlobals.RIGHT][GameGlobals.CIRCLE] = ImageIO.read(GameGlobals.BCIRC_IMG);
        player[GameGlobals.RIGHT][GameGlobals.TRIANGLE] = ImageIO.read(GameGlobals.BTRI_IMG);
        player[GameGlobals.RIGHT][GameGlobals.SQUARE] = ImageIO.read(GameGlobals.BSQU_IMG);
      } catch (IOException e) {
        System.err.println("Could not load images.");
      }

      currentForm[GameGlobals.LEFT] = GameGlobals.CIRCLE;
      currentForm[GameGlobals.CENTER] = GameGlobals.TRIANGLE;
      currentForm[GameGlobals.RIGHT] = GameGlobals.SQUARE;

      arrayImgs.add(new Images(background, 0, 0, GameGlobals.SCREEN_W, GameGlobals.SCREEN_H));
      arrayImgs.add(new Images(player[GameGlobals.LEFT][currentForm[GameGlobals.LEFT]], GameGlobals.S_XL, GameGlobals.S_Y, GameGlobals.S_SIZE_XY, GameGlobals.S_SIZE_XY));
      arrayImgs.add(new Images(player[GameGlobals.CENTER][currentForm[GameGlobals.CENTER]], GameGlobals.S_XC, GameGlobals.S_Y, GameGlobals.S_SIZE_XY, GameGlobals.S_SIZE_XY));
      arrayImgs.add(new Images(player[GameGlobals.RIGHT][currentForm[GameGlobals.RIGHT]], GameGlobals.S_XR, GameGlobals.S_Y, GameGlobals.S_SIZE_XY, GameGlobals.S_SIZE_XY));
    }

    public void paint (Graphics g) {
      synchronized (sync) {
        for (Images i : arrayImgs){
          g.drawImage(i.graphic, i.posX, i.posY, i.sizeX, i.sizeY, this);
        }
        g.setColor(playerScore.stringColor);
        g.setFont(new Font("OCR A Extended", Font.PLAIN, 36));
        g.drawString(playerScore.scoreString.toString(), GameGlobals.SCORE_X, GameGlobals.SCORE_Y);
  		}
  	}

  	public Dimension getPreferredSize() {
  		return new Dimension (GameGlobals.SCREEN_W, GameGlobals.SCREEN_H);
  	}
  }

  class Score{
    int actualScore = 0;
    StringBuffer scoreString = null;
    Image[] scoreImgArray = new Image[GameGlobals.SCORE_STRING_SIZE];
    Color stringColor = Color.GRAY;

    Score(){
      scoreString = new StringBuffer("");
      for (int i = 0; i < GameGlobals.SCORE_STRING_SIZE; i++) {
        scoreString.append("0");
      }
      updateScoreString();
    }

    public void updateScoreString(){
      scoreString.replace(0, scoreString.length(), String.format("%0"+GameGlobals.SCORE_STRING_SIZE+"d",actualScore));
    }

    public void winPoints(int pointsWon){
      stringColor = Color.CYAN;
      actualScore += pointsWon;
      if (actualScore > 9999)
        actualScore = 9999;
      updateScoreString();
    }

    public void losePoints(int pointsLost){
      stringColor = Color.RED;
      actualScore -= pointsLost;
      if (actualScore < 0)
        actualScore = 0;
      updateScoreString();
    }
  }

  class Enemy implements Runnable {
    Image enemyImg[] = new Image[3];
    int[] enemy = new int[3];
    int myIndex = 0;
    int enemyY = 0;
    boolean alreadyIn = false;
    
    Enemy(int left, int center, int right) {
      try {
        switch (left) {
        case GameGlobals.TRIANGLE: enemyImg[GameGlobals.LEFT] = ImageIO.read(GameGlobals.TRI_IMG);
          break;
        case GameGlobals.CIRCLE: enemyImg[GameGlobals.LEFT] = ImageIO.read(GameGlobals.CIRC_IMG);
          break;
        case GameGlobals.SQUARE: enemyImg[GameGlobals.LEFT] = ImageIO.read(GameGlobals.SQU_IMG);
          break;
        }

        switch (center) {
        case GameGlobals.TRIANGLE: enemyImg[GameGlobals.CENTER] = ImageIO.read(GameGlobals.TRI_IMG);
          break;
        case GameGlobals.CIRCLE: enemyImg[GameGlobals.CENTER] = ImageIO.read(GameGlobals.CIRC_IMG);
          break;
        case GameGlobals.SQUARE: enemyImg[GameGlobals.CENTER] = ImageIO.read(GameGlobals.SQU_IMG);
          break;
        }

        switch (right) {
        case GameGlobals.TRIANGLE: enemyImg[GameGlobals.RIGHT] = ImageIO.read(GameGlobals.TRI_IMG);
          break;
        case GameGlobals.CIRCLE: enemyImg[GameGlobals.RIGHT] = ImageIO.read(GameGlobals.CIRC_IMG);
          break;
        case GameGlobals.SQUARE: enemyImg[GameGlobals.RIGHT] = ImageIO.read(GameGlobals.SQU_IMG);
          break;
        }
      } catch (IOException e) {
        System.err.println("Could not load images.");
      }

      enemy[GameGlobals.LEFT] = left;
      enemy[GameGlobals.CENTER] = center;
      enemy[GameGlobals.RIGHT] = right;

      synchronized (sync) {
        myIndex = arrayImgs.size();
        arrayImgs.add(new Images(enemyImg[GameGlobals.LEFT], GameGlobals.S_XL, enemyY, GameGlobals.S_SIZE_XY, GameGlobals.S_SIZE_XY));
        arrayImgs.add(new Images(enemyImg[GameGlobals.CENTER], GameGlobals.S_XC, enemyY, GameGlobals.S_SIZE_XY, GameGlobals.S_SIZE_XY));
        arrayImgs.add(new Images(enemyImg[GameGlobals.RIGHT], GameGlobals.S_XR, enemyY, GameGlobals.S_SIZE_XY, GameGlobals.S_SIZE_XY));
      }
    }

    public void run() {
      while (enemyY < GameGlobals.SCREEN_H) {
        try {
          Thread.sleep(GameGlobals.STEP_FREQ);
        } catch (InterruptedException e) {}

        if (!alreadyIn && enemyY > (GameGlobals.HITZONE_UPPER_BOUND) && enemyY < (GameGlobals.HITZONE_LOWER_BOUND)) {
          ClientGlobals.hitZone = this;
          if (ClientGlobals.playerID == 2){
            //HITZONE x:y:z:i >> x, y & z are the symbols, i is index
            serverOS.println("HITZONE " + ClientGlobals.hitZone + ":" + ClientGlobals.hitZone.myIndex);
          }
          alreadyIn = true;
        }
        else if (alreadyIn && enemyY > (GameGlobals.HITZONE_LOWER_BOUND)) {
          serverOS.println("HITZONE 9:9:9:9");
          alreadyIn = false;
        }

        enemyY += GameGlobals.STEP;

        arrayImgs.set(myIndex, new Images(enemyImg[GameGlobals.LEFT], GameGlobals.S_XL, enemyY, GameGlobals.S_SIZE_XY, GameGlobals.S_SIZE_XY));
        arrayImgs.set(myIndex+1, new Images(enemyImg[GameGlobals.CENTER], GameGlobals.S_XC, enemyY, GameGlobals.S_SIZE_XY, GameGlobals.S_SIZE_XY));
        arrayImgs.set(myIndex+2, new Images(enemyImg[GameGlobals.RIGHT], GameGlobals.S_XR, enemyY, GameGlobals.S_SIZE_XY, GameGlobals.S_SIZE_XY));

        sc.repaint();
      }
    }

    public String toString(){
      return enemy[GameGlobals.LEFT] + ":" + enemy[GameGlobals.CENTER] + ":" + enemy[GameGlobals.RIGHT];
    }
  }

  class EnemyManager extends Thread{      
    Vector<String> enemyVector = new Vector<String>();
    int index = 0;
    int enemySigns[] = new int[3];

    public void queueEnemy(String nextEnemy){
      enemyVector.add(nextEnemy);
    }

    private void createEnemy(String nextEnemy){
      enemySigns[GameGlobals.LEFT] = Character.getNumericValue(nextEnemy.charAt(6));
      enemySigns[GameGlobals.CENTER] = Character.getNumericValue(nextEnemy.charAt(8));
      enemySigns[GameGlobals.RIGHT] = Character.getNumericValue(nextEnemy.charAt(10));
      new Thread(new Enemy(enemySigns[GameGlobals.LEFT], enemySigns[GameGlobals.CENTER], enemySigns[GameGlobals.RIGHT])).start();
    }

    public void run(){
      while (ClientGlobals.isPlayable){
        if (index < enemyVector.size()){
          createEnemy(enemyVector.elementAt(index));
          index++;

          try {Thread.sleep(GameGlobals.SPAWN_TIME);}
          catch (InterruptedException e) {}
        }
      }
    }
  }
}

class UserPreferences extends JFrame implements ActionListener {
  JLabel text1 = new JLabel("Server Address:");
  JLabel text2 = new JLabel("Server Port:");
  JTextField ipText = new JTextField(15);
  JTextField portText = new JTextField(8);
  JButton setButton = new JButton("Set");

  UserPreferences() {
    JPanel mainPanel = new JPanel();
    mainPanel.setLayout(new FlowLayout());

    mainPanel.add(text1);
    mainPanel.add(ipText);
    mainPanel.add(text2);
    mainPanel.add(portText);
    mainPanel.add(setButton);

    setButton.addActionListener(this);

    add(mainPanel);

    pack();

    setVisible(true);
    setLocationRelativeTo(null);
    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
  }

  public void actionPerformed (ActionEvent e) {
    if(portText.getText().equals("")) {
      JOptionPane.showMessageDialog(this, "You must fill both fields!", "Error", JOptionPane.ERROR_MESSAGE);
    } else {
      try {
        ClientGlobals.ipAddress = ipText.getText();
        ClientGlobals.portValue = Integer.parseInt(portText.getText());
        ClientGlobals.isSetting = false;
        setVisible(false);
      } catch (Exception ex) {
        System.out.println("Invalid Port, " + ex);
        System.exit(0);
      }
    }
  }
}