import java.net.*;
import java.io.*;
import java.util.*;

class ServerGlobals{
	static boolean isPlayable = true;
}

class Serving extends Thread {
	Socket clientSocket;
	PrintStream os;

	Serving (Socket clientSocket) {
		this.clientSocket = clientSocket;
	}

	public synchronized void run() {
		try {
			os = new PrintStream(clientSocket.getOutputStream(), true);

			while(ServerGlobals.isPlayable);
			
			os.close();

			clientSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NoSuchElementException e) {
			System.out.println("Connection closed by client.");
		}
	}
}

class Server {
	final int STEP = 3;
	final int STEP_FREQ = 30;
	final int SPAWN_TIME = 2000;
	final int SCREEN_W = 500;
	final int SCREEN_H = 700;
	final int S_SIZE_XY = 65;
	final int S_Y = SCREEN_H - S_SIZE_XY*2;
	final int LEFT = 0;
	final int CENTER = 1;
	final int RIGHT = 2;
	Enemy hitZone = null;
	boolean left = false; //left de saiu

	class Enemy implements Runnable {
		int enemyY = 0;
		int enemy[] = new int[3];
		Random r = new Random();

		Enemy() {
			/*
				Where:
				0 - Circle
				1 - Triangle
				2 - Square
				3 - Null
			*/

			enemy[LEFT] = r.nextInt(4);
			enemy[CENTER] = r.nextInt(4);
			enemy[RIGHT] = r.nextInt(4);
		}

		Enemy(StringBuilder seq) {
			/*
				Where:
				0 - Circle
				1 - Triangle
				2 - Square
				3 - Null
			*/
			enemy[LEFT] = seq.charAt(LEFT);
			enemy[CENTER] = seq.charAt(CENTER);
			enemy[RIGHT] = seq.charAt(RIGHT);
		}

		public synchronized void run() {
			while (enemyY < SCREEN_H) {
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
			return enemy[LEFT] + ":" + enemy[CENTER] + ":" + enemy[RIGHT];
		}
	}

	class EnemySequencer implements Runnable{ //WORKING
		PrintStream player1, player2;
		Random r = new Random();
		String[] sequences = {
			"001:010:100:010:001:010",
			"001:010:100:001:010:100",
			"100:100:010:010:001:001",
			"101:010:111:010:101:010",
			"001:101:010:101:100:101"
		};
		int subSeqIndex = 0;

		EnemySequencer(PrintStream player1, PrintStream player2){
			this.player1 = player1;
			this.player2 = player2;
		}

		public StringBuilder nextSubSequence(String seq){
			StringBuilder part = new StringBuilder("");
			if (subSeqIndex >= seq.length()){
				subSeqIndex = 0;
				return null;
			}

			for (int i = 0; i < 3; i++) {
				part.append(seq.charAt(subSeqIndex++));
			}
			subSeqIndex++; //THROUGH ':'

			return part;
		}

		public void generate(){
			String seq;
			StringBuilder nextEnemy;
			int symbols[] = new int[3];

			//GENERATE COMBINATION OF SYMBOLS
			for (int i = 0; i < 3; i++) {
				symbols[i] = r.nextInt(3); //0, 1 OR 2 BECAUSE COMBINATION CANNOT CONTAIN NULL(3)
			}

			//CHOOSE A SEQUENCE FROM sequences
			seq = sequences[r.nextInt(sequences.length)];

			// \/\/ LOOP UNTIL SEQUENCE ENDS \/\/
			//FEED IT TO subSequence() AND PUT RESULT INSIDE nextEnemy
			while ((nextEnemy = nextSubSequence(seq)) != null){

				//CHANGE nextEnemy SYMBOLS WHEN IT'S 1
				for (int i = 0; i < 3; i++) {
					switch(nextEnemy.charAt(i)){
					case '0':
						nextEnemy.setCharAt(i,'3');
						break;
					case '1':
						nextEnemy.setCharAt(i,(char)(symbols[i] + 48));
						break;
					default:
						System.out.println("error?");
						//ServerGlobals.isPlayable = false;
						//System.exit(0);
					}
				}

				//INSTANCIATE ENEMY
				new Thread(new Enemy(nextEnemy)).start();

				//SEND TO CLIENTS
				player1.println("ENEMY " + nextEnemy.charAt(LEFT) + ":" + nextEnemy.charAt(CENTER) + ":" + nextEnemy.charAt(RIGHT));
				player2.println("ENEMY " + nextEnemy.charAt(LEFT) + ":" + nextEnemy.charAt(CENTER) + ":" + nextEnemy.charAt(RIGHT));
			}
		}

		public void run(){
			while (ServerGlobals.isPlayable){
				generate();
				try{ Thread.sleep(SPAWN_TIME); }
				catch (InterruptedException ie) {};
			}
		}
	}

	public static void main (String[] args) {
		Server server = new Server();
		ServerSocket serverSocket = null;

		try {
			serverSocket = new ServerSocket(8080);
		} catch (IOException e) {
			System.out.println("Could not listen on port 8080: " + e);
			System.exit(0);
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

		serving1.start();
		serving2.start();
		
		try { Thread.sleep(100);}
		catch (InterruptedException e) {}

		Server.EnemySequencer sequencer = server.new EnemySequencer(serving1.os, serving2.os);

		try { Thread.sleep(100);}
		catch (InterruptedException e) {}

		serving1.os.println("PLAYERID 1");
		serving2.os.println("PLAYERID 2");

		try { Thread.sleep(100);}
		catch (InterruptedException e) {}

		new Thread(sequencer).start();

		while (ServerGlobals.isPlayable);

		try { serverSocket.close(); }
		catch (IOException e) { e.printStackTrace(); }
	}

}