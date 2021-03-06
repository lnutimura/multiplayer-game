import java.net.*;
import java.io.*;
import java.util.*;

class GameGlobals{
	static boolean isPlayable = true;
	static int playerLeft, playerCenter, playerRight;
	static int clientCount = 0;
}

class Server{
	public static void main (String[] args) {
		ServerSocket serverSocket = null;

		try {
			serverSocket = new ServerSocket(8080);
		} catch (IOException e) {
			System.out.println("Could not listen on port 8080, " + e);
			System.exit(1);
		}

		Socket clientSocket1 = null;
		Socket clientSocket2 = null;

		try {
			System.out.println("Waiting for players (0/2).");
			clientSocket1 = serverSocket.accept();
			System.out.println("Waiting for players (1/2).");
			clientSocket2 = serverSocket.accept();
			System.out.println("All players connected (2/2).");
		} catch (IOException e) {
			System.out.println("Accept failed, " + e);
			System.exit(1);
		}

		Serving serving1 = new Serving(clientSocket1);
		Serving serving2 = new Serving(clientSocket2);
		GameSetup game = new GameSetup();
		GameSetup.Enemy currEnemy = null;
		
		currEnemy = game.new Enemy();

		serving1.start();
		serving2.start();

		boolean isPlayable = true;

		while (GameGlobals.isPlayable) {
			try {
				Thread.sleep(game.SPAWN_TIME);
				currEnemy = game.new Enemy();				
				for(int i = 0; i < GameGlobals.clientCount; i++) {
					System.out.println("ENEMY " + currEnemy);
					serving1.os[i].println("ENEMY " + currEnemy);
				}
			} catch (InterruptedException e) {}
		}

		try {
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

class Serving extends Thread {
	Socket clientSocket;
	static boolean isPlayable = true;
	public static PrintStream os[] = new PrintStream[2];

	Serving (Socket clientSocket) {
		this.clientSocket = clientSocket;
	}

	public synchronized void run() {
		try {
			os[GameGlobals.clientCount++] = new PrintStream (clientSocket.getOutputStream(), true);

			while(GameGlobals.isPlayable);

			for(int i = 0; i < GameGlobals.clientCount; i++) {
				os[i].close();
			}

			clientSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NoSuchElementException e) {
			System.out.println("Connection closed by client.");
		}
	}
}

class GameSetup {
	final int STEP = 3;
	final int STEP_FREQ = 30;
	final int SPAWN_TIME = 2000;
	final int SCREEN_W = 500;
	final int SCREEN_H = 700;
	final int S_SIZE_XY = 50;
	final int S_Y = SCREEN_H - S_SIZE_XY*2;
	Enemy hitZone = null;
	boolean left = false;

	class Enemy implements Runnable {
		int enemyY = 0;
		int enemyLeft, enemyCenter, enemyRight;
		Random r = new Random();

		Enemy() {
			/*
				Where:
				0 - Circle
				1 - Triangle
				2 - Square
				3 - Null
			*/

			enemyLeft = r.nextInt(4);
			enemyCenter = r.nextInt(4);
			enemyRight = r.nextInt(4);

			new Thread(this).start();
		}

		public void run() {
			while (GameGlobals.isPlayable && enemyY < SCREEN_H) {
				try {
					Thread.sleep(STEP_FREQ);
				} catch (InterruptedException e) {}

				enemyY += STEP;

				if (enemyY > (S_Y - S_SIZE_XY/2) && enemyY < (S_Y + S_SIZE_XY/2)) {
					hitZone = this;
					System.out.println("There's an enemy in the hit zone.");
				}
				else if (!left && enemyY > (S_Y + S_SIZE_XY/2)) {
					hitZone = null;
					left = true;
				}
			}
		}

		public String toString(){
			return enemyLeft + ":" + enemyCenter + ":" + enemyRight;
		}
	}
}