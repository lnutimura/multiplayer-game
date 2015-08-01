import java.io.*;
import java.awt.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.imageio.*;
import java.awt.event.*;
import java.awt.image.*;

class ClientGlobals{
  boolean isPlayable = true;
  int playerID = 0;
}

class Client extends JFrame implements Runnable {
    static Vector<Images> arrayImgs = new Vector<Images>();
    final int STEP = 3;
    final int STEP_FREQ = 30;
    final int SPAWN_TIME = 2000; 
    final int CIRCLE = 0;
    final int TRIANGLE = 1;
    final int SQUARE = 2;
    final int NULL = 3;
    final int LEFT = 0;
    final int CENTER = 1;
    final int RIGHT = 2;
    final int SCREEN_W = 500;
    final int SCREEN_H = 700;
    final int S_XL = 124;
    final int S_XC = 216;
    final int S_XR = 313;
    final int S_SIZE_XY = 65;
    final int S_Y = SCREEN_H - S_SIZE_XY*2;
    ClientGlobals globals = new ClientGlobals();
    int currentForm[] = new int[3];
    Image player[][] = new Image[3][3];
    Scene sc = new Scene();
    Object sync = new Object();

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
    			background = ImageIO.read(new File("images/fundo.png"));
    			player[LEFT][CIRCLE] = ImageIO.read(new File("images/bCirc.png"));
    			player[LEFT][TRIANGLE] = ImageIO.read(new File("images/bTri.png"));
    			player[LEFT][SQUARE] = ImageIO.read(new File("images/bQuad.png"));
     			player[CENTER][CIRCLE] = ImageIO.read(new File("images/bCirc.png"));
    			player[CENTER][TRIANGLE] = ImageIO.read(new File("images/bTri.png"));
    			player[CENTER][SQUARE] = ImageIO.read(new File("images/bQuad.png"));
     			player[RIGHT][CIRCLE] = ImageIO.read(new File("images/bCirc.png"));
    			player[RIGHT][TRIANGLE] = ImageIO.read(new File("images/bTri.png"));
    			player[RIGHT][SQUARE] = ImageIO.read(new File("images/bQuad.png"));
    		} catch (IOException e) {
    			System.out.println("Could not load images.");
    		}

    		currentForm[LEFT] = CIRCLE;
    		currentForm[CENTER] = TRIANGLE;
    		currentForm[RIGHT] = SQUARE;

    		arrayImgs.add(new Images(background, 0, 0, SCREEN_W, SCREEN_H));
    		arrayImgs.add(new Images(player[LEFT][currentForm[LEFT]], S_XL, S_Y, S_SIZE_XY, S_SIZE_XY));
    		arrayImgs.add(new Images(player[CENTER][currentForm[CENTER]], S_XC, S_Y, S_SIZE_XY, S_SIZE_XY));
    		arrayImgs.add(new Images(player[RIGHT][currentForm[RIGHT]], S_XR, S_Y, S_SIZE_XY, S_SIZE_XY));
    	}

    	public void paint (Graphics g) {
    		super.paint(g);
    		synchronized (sync) {
    			for (Images i : arrayImgs)
    				g.drawImage(i.graphic, i.posX, i.posY, i.sizeX, i.sizeY, this);
    		}
    	}

    	public Dimension getPreferredSize() {
    		return new Dimension (SCREEN_W, SCREEN_H);
    	}
    }

    class Enemy implements Runnable {
    	Image enemy[] = new Image[3];
    	int myIndex = 0;
    	int enemyY = 0;
    	
    	Enemy(int left, int center, int right) {
    		try {
	    		switch (left) {
	    			case TRIANGLE: enemy[LEFT] = ImageIO.read(new File("images/tri.png"));
	    				break;
	    			case CIRCLE: enemy[LEFT] = ImageIO.read(new File("images/circ.png"));
	    				break;
	    			case SQUARE: enemy[LEFT] = ImageIO.read(new File("images/quad.png"));
	    				break;
	    		}

	      		switch (center) {
	    			case TRIANGLE: enemy[CENTER] = ImageIO.read(new File("images/tri.png"));
	    				break;
	    			case CIRCLE: enemy[CENTER] = ImageIO.read(new File("images/circ.png"));
	    				break;
	    			case SQUARE: enemy[CENTER] = ImageIO.read(new File("images/quad.png"));
	    				break;
	    		}

	    		switch (right) {
	    			case TRIANGLE: enemy[RIGHT] = ImageIO.read(new File("images/tri.png"));
	    				break;
	    			case CIRCLE: enemy[RIGHT] = ImageIO.read(new File("images/circ.png"));
	    				break;
	    			case SQUARE: enemy[RIGHT] = ImageIO.read(new File("images/quad.png"));
	    				break;
	    		}
    		} catch (IOException e) {
    			System.out.println("Could not load images.");
    		}

    		synchronized (sync) {
    			myIndex = arrayImgs.size();
    			arrayImgs.add(new Images(enemy[LEFT], S_XL, enemyY, S_SIZE_XY, S_SIZE_XY));
     			arrayImgs.add(new Images(enemy[CENTER], S_XC, enemyY, S_SIZE_XY, S_SIZE_XY));
      		arrayImgs.add(new Images(enemy[RIGHT], S_XR, enemyY, S_SIZE_XY, S_SIZE_XY));
    		}
    		new Thread(this).start();
    	}

    	public void run() {
    		while (enemyY < SCREEN_H) {
  				try {
  					Thread.sleep(STEP_FREQ);
  				} catch (InterruptedException e) {}

  				enemyY += STEP;

  				arrayImgs.set(myIndex, new Images(enemy[LEFT], S_XL, enemyY, S_SIZE_XY, S_SIZE_XY));
     			arrayImgs.set(myIndex+1, new Images(enemy[CENTER], S_XC, enemyY, S_SIZE_XY, S_SIZE_XY));
      		arrayImgs.set(myIndex+2, new Images(enemy[RIGHT], S_XR, enemyY, S_SIZE_XY, S_SIZE_XY));

      		sc.repaint();
    		}
 	    }
   	}

    Client() {
    	super("Untitled Game");
    	setLocation(400,0);
    	add(sc);
    	addKeyListener(new KeyAdapter() {
    		public void keyPressed(KeyEvent e) {
	    		switch(e.getKeyCode()) {
            case KeyEvent.VK_LEFT:
	    			case KeyEvent.VK_A:
              switch(currentForm[LEFT]){
              case CIRCLE:
                  currentForm[LEFT] = TRIANGLE;
                  arrayImgs.set(1,new Images(player[LEFT][currentForm[LEFT]],S_XL,S_Y,S_SIZE_XY,S_SIZE_XY));
                  break;
              case TRIANGLE:
                  currentForm[LEFT] = SQUARE;
                  arrayImgs.set(1,new Images(player[LEFT][currentForm[LEFT]],S_XL,S_Y,S_SIZE_XY,S_SIZE_XY));
                  break;
              case SQUARE:
                  currentForm[LEFT] = CIRCLE;
                  arrayImgs.set(1,new Images(player[LEFT][currentForm[LEFT]],S_XL,S_Y,S_SIZE_XY,S_SIZE_XY));
                  break;
              }
              break;
              case KeyEvent.VK_UP:
              case KeyEvent.VK_S:
                  switch(currentForm[CENTER]){
                  case CIRCLE:
                      currentForm[CENTER] = TRIANGLE;
                      arrayImgs.set(2,new Images(player[CENTER][currentForm[CENTER]],S_XC,S_Y,S_SIZE_XY,S_SIZE_XY));
                      break;
                  case TRIANGLE:
                      currentForm[CENTER] = SQUARE;
                      arrayImgs.set(2,new Images(player[CENTER][currentForm[CENTER]],S_XC,S_Y,S_SIZE_XY,S_SIZE_XY));
                      break;
                  case SQUARE:
                      currentForm[CENTER] = CIRCLE;
                      arrayImgs.set(2,new Images(player[CENTER][currentForm[CENTER]],S_XC,S_Y,S_SIZE_XY,S_SIZE_XY));
                      break;
                  }
                  break;
              case KeyEvent.VK_RIGHT:
              case KeyEvent.VK_D:
                  switch(currentForm[RIGHT]){
                  case CIRCLE:
                      currentForm[RIGHT] = TRIANGLE;
                      arrayImgs.set(3,new Images(player[RIGHT][currentForm[RIGHT]],S_XR,S_Y,S_SIZE_XY,S_SIZE_XY));
                      break;
                  case TRIANGLE:
                      currentForm[RIGHT] = SQUARE;
                      arrayImgs.set(3,new Images(player[RIGHT][currentForm[RIGHT]],S_XR,S_Y,S_SIZE_XY,S_SIZE_XY));
                      break;
                  case SQUARE:
                      currentForm[RIGHT] = CIRCLE;
                      arrayImgs.set(3,new Images(player[RIGHT][currentForm[RIGHT]],S_XR,S_Y,S_SIZE_XY,S_SIZE_XY));
                      break;
                  }
                  break;
            }
            sc.repaint();
    		}
    	});

  		setDefaultCloseOperation(EXIT_ON_CLOSE);
  		pack();
  		setVisible(true);
    }

    public void run() {
    	Socket socket = null;
    	Scanner is = null;
    	
    	try {
    		socket = new Socket("127.0.0.1", 8080);
    		is = new Scanner(socket.getInputStream());
    	} catch (UnknownHostException e) {
      		System.err.println("Don't know about host.");
      } catch (IOException e) {
        System.err.println("Couldn't get I/O for the connection to host.");
      }

      String temp = is.nextLine();
      if (temp.startsWith("PLAYERID")){
        globals.playerID = Character.getNumericValue(temp.charAt(9));
        System.out.println("My ID: " + globals.playerID);
      }
      else{
        System.err.println("Couldn't get Player's ID.");
        endGame();
      }

      try {
      	String response;
        int enemyLeft, enemyCenter, enemyRight;

      	while (globals.isPlayable) {
      		response = is.nextLine();
          // ENEMY x:y:z
      		if (response.startsWith("ENEMY")) {
      			enemyLeft = Character.getNumericValue(response.charAt(6));
      			enemyCenter = Character.getNumericValue(response.charAt(8));
      			enemyRight = Character.getNumericValue(response.charAt(10));
      			System.out.println(">>>> " + enemyLeft + ":" + enemyCenter + ":" + enemyRight);
      			new Enemy(enemyLeft, enemyCenter, enemyRight);
      		}
      		try {
      			Thread.sleep(SPAWN_TIME);
     			} catch (InterruptedException e) {}
      	}

      	is.close();
      	socket.close();
      } catch (Exception e){
        System.err.println("Server terminated. Game will be shut down...");
      }
      endGame();
    }

    public void endGame(){
      System.out.println("Game over.");
      System.exit(0);
    }

    public static void main (String[] args) {
    	Client client = new Client();
    	new Thread(client).start();
    }
}