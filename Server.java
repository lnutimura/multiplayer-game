import java.net.*;
import java.io.*;
import java.util.*;

class GameGlobals{
	static boolean isPlayable = true;
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
		
		serving1.currEnemy = game.new Enemy();
		serving1.start();
		serving2.start();

		boolean isPlayable = true;

		while (GameGlobals.isPlayable) {
			try {
				serving1.currEnemy = game.new Enemy();				
				System.out.println("<<<<< " + serving1.currEnemy);
				Thread.sleep(game.SPAWN_TIME);
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
	static int count = 0;
	static boolean isPlayable = true;
	static PrintStream os[] = new PrintStream[2];
	static SendToClient send = new SendToClient();

	static GameSetup.Enemy currEnemy = null;


	Serving (Socket clientSocket) {
		this.clientSocket = clientSocket;
	}

	public synchronized void run() {
		try {
			os[count++] = new PrintStream (clientSocket.getOutputStream(), true);

			while(GameGlobals.isPlayable) {
				if(!send.isAlive())
					send.start();
			}

			for(int i = 0; i < count; i++) {
				os[i].close();
			}

			clientSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NoSuchElementException e) {
			System.out.println("Connection closed by client.");
		}
	}

	static class SendToClient extends Thread {
		public synchronized void run() {
			while(true) {
				for(int i = 0; i < count; i++) {
					System.out.println("ENEMY " + currEnemy);
					os[i].println("ENEMY " + currEnemy);
				}
				System.out.println("#########");
				try {
					sleep(2000);
				} catch (InterruptedException e) {}
			}
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