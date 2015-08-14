import java.io.*;
import java.net.*;
import java.awt.*;
import java.util.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.JOptionPane;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

class ServerGlobals{
	static boolean isPlayable = true;
	static boolean isSetting = true;
	static int portValue = 8080;
	static Server.Enemy hitZone = null;
}

class Server {

	public static void main (String[] args) {
		Server server = new Server();
		ServerSocket serverSocket = null;

		/*new ServerPreferences();
		while (ServerGlobals.isSetting)
			try { Thread.sleep(10); }
			catch (InterruptedException e) {}
*/
		try {
			serverSocket = new ServerSocket(ServerGlobals.portValue);
		} catch (IOException e) {
			System.out.println("Could not listen on port " + ServerGlobals.portValue + ", " + e);
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

		Serving serving1 = new Serving(clientSocket1, clientSocket2, 1);
		Serving serving2 = new Serving(clientSocket2, clientSocket1, 2);

		serving1.start();
		serving2.start();
		
		try { Thread.sleep(100);}
		catch (InterruptedException e) {}

		Server.EnemySequencer sequencer = server.new EnemySequencer(serving1.thisOS, serving2.thisOS);

		try { Thread.sleep(100);}
		catch (InterruptedException e) {}

		serving1.thisOS.println("PLAYERID 1");
		serving2.thisOS.println("PLAYERID 2");

		try { Thread.sleep(100);}
		catch (InterruptedException e) {}

		new Thread(sequencer).start();

		while (ServerGlobals.isPlayable);

		try { serverSocket.close(); }
		catch (IOException e) { e.printStackTrace(); }
	}

	class Enemy {
		int enemyY = 0;
		int enemy[] = new int[3];
		Random r = new Random();
		boolean left = false;
		int myIndex = -1;

		Enemy() {
			enemy[GameGlobals.LEFT] = r.nextInt(4);
			enemy[GameGlobals.CENTER] = r.nextInt(4);
			enemy[GameGlobals.RIGHT] = r.nextInt(4);
		}

		Enemy(StringBuilder seq) {
			enemy[GameGlobals.LEFT] = Character.getNumericValue(seq.charAt(GameGlobals.LEFT));
			enemy[GameGlobals.CENTER] = Character.getNumericValue(seq.charAt(GameGlobals.CENTER));
			enemy[GameGlobals.RIGHT] = Character.getNumericValue(seq.charAt(GameGlobals.RIGHT));
		}

		public String toString(){
			return enemy[GameGlobals.LEFT] + ":" + enemy[GameGlobals.CENTER] + ":" + enemy[GameGlobals.RIGHT];
		}
	}

	class EnemySequencer implements Runnable{
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
			ServerGlobals.hitZone = new Enemy();
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
			//FEED IT TO nextSubSequence() AND PUT RESULT INSIDE nextEnemy
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
						System.out.println("error maybe?");
					}
				}
				//SEND TO CLIENTS
				player1.println("ENEMY " + nextEnemy.charAt(GameGlobals.LEFT) + ":" + nextEnemy.charAt(GameGlobals.CENTER) + ":" + nextEnemy.charAt(GameGlobals.RIGHT));
				player2.println("ENEMY " + nextEnemy.charAt(GameGlobals.LEFT) + ":" + nextEnemy.charAt(GameGlobals.CENTER) + ":" + nextEnemy.charAt(GameGlobals.RIGHT));

				try{ Thread.sleep(GameGlobals.SPAWN_TIME); }
				catch (InterruptedException ie) {};
			}
		}

		public void run(){
			while (ServerGlobals.isPlayable)
				generate();
		}
	}
}

class Serving extends Thread {
	Socket thisSocket, otherSocket;
	PrintStream thisOS, otherOS;
	Scanner thisIS;
	int playerID;

	Serving (Socket thisSocket, Socket otherSocket, int id) {
		this.thisSocket = thisSocket;
		this.otherSocket = otherSocket;
		playerID = id;
	}

	public synchronized void run() {
		try {
			thisIS = new Scanner(thisSocket.getInputStream());
			thisOS = new PrintStream(thisSocket.getOutputStream(), true);
			otherOS = new PrintStream(otherSocket.getOutputStream(), true);
			String clientStr = "";
			Character keyPressed, newShape;
			Matcher m;

			while(ServerGlobals.isPlayable) {
				clientStr = thisIS.nextLine();
				if (clientStr.startsWith("HITZONE")){
					//HITZONE x:y:z:i >> x, y & z are the symbols, i is index
					m = Pattern.compile("\\w+\\s(\\d):(\\d):(\\d):(\\d+)").matcher(clientStr);
					m.find();
					ServerGlobals.hitZone.enemy[GameGlobals.LEFT] = Integer.parseInt(m.group(1));
					ServerGlobals.hitZone.enemy[GameGlobals.CENTER] = Integer.parseInt(m.group(2));
					ServerGlobals.hitZone.enemy[GameGlobals.RIGHT] = Integer.parseInt(m.group(3));
					ServerGlobals.hitZone.myIndex = Integer.parseInt(m.group(4));
				}
				else if (clientStr.startsWith("KEYPRESS ")){
					keyPressed = clientStr.charAt(9);
					newShape = clientStr.charAt(11);
					if (playerID == 1) {
						otherOS.println("FORM "+keyPressed+":"+newShape);
					}
					else{
						/*
							ID - 2
							thisOS - clientsocket2
							otherOS - clientsocket1

							string sent = SCORED x >> x is symbol hit
						*/
						if(ServerGlobals.hitZone != null) {
							switch (keyPressed) {
							case 'A':
								if (Character.getNumericValue(newShape) == ServerGlobals.hitZone.enemy[GameGlobals.LEFT]) {
									ServerGlobals.hitZone.enemy[GameGlobals.LEFT] = -1;
									thisOS.println("SCORED " + GameGlobals.LEFT);
									otherOS.println("SCORED " + GameGlobals.LEFT);
								}
								else{
									ServerGlobals.hitZone.enemy[GameGlobals.LEFT] = -1;
									thisOS.println("LOST " + GameGlobals.LEFT);
									otherOS.println("LOST " + GameGlobals.LEFT);	
								}
								break;
							case 'S':
								if (Character.getNumericValue(newShape) == ServerGlobals.hitZone.enemy[GameGlobals.CENTER]) {
									ServerGlobals.hitZone.enemy[GameGlobals.CENTER] = -1;
									thisOS.println("SCORED " + GameGlobals.CENTER);
									otherOS.println("SCORED " + GameGlobals.CENTER);
								}
								else{
									ServerGlobals.hitZone.enemy[GameGlobals.CENTER] = -1;
									thisOS.println("LOST " + GameGlobals.CENTER);
									otherOS.println("LOST " + GameGlobals.CENTER);	
								}
								break;
							case 'D':
								if (Character.getNumericValue(newShape) == ServerGlobals.hitZone.enemy[GameGlobals.RIGHT]) {
									ServerGlobals.hitZone.enemy[GameGlobals.RIGHT] = -1;
									thisOS.println("SCORED " + GameGlobals.RIGHT);
									otherOS.println("SCORED " + GameGlobals.RIGHT);
								}
								else{
									ServerGlobals.hitZone.enemy[GameGlobals.RIGHT] = -1;
									thisOS.println("LOST " + GameGlobals.RIGHT);
									otherOS.println("LOST " + GameGlobals.RIGHT);	
								}
								break;
							}
						}
					}
				}
			}

			thisIS.close();
			thisOS.close();
			otherOS.close();

			thisSocket.close();
			otherSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NoSuchElementException e) {
			System.out.println("Connection closed by client.");
		}
	}
}

class ServerPreferences extends JFrame implements ActionListener {
	JLabel text = new JLabel("Server Port:");
	JTextField portText = new JTextField(8);
	JButton setButton = new JButton("Set");

	ServerPreferences() {
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new FlowLayout());

		mainPanel.add(text);
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
				ServerGlobals.portValue = Integer.parseInt(portText.getText());
				ServerGlobals.isSetting = false;
				setVisible(false);
				System.out.println("Server listening to port: " + ServerGlobals.portValue);
			} catch (Exception ex) {
				System.out.println("Invalid Port, " + ex);
				System.exit(0);
			}
		}
	}
}