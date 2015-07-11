import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;
import java.io.*;
import javax.imageio.*;

class Game extends JFrame {
	static Vector<Imagens> vetImgs = new Vector<Imagens>();
    final int PASSO = 3; /*em pixels*/
    final int PASSO_FREQ = 30; /*em milissegundos*/
    final int SPAWN_TIME = 1000; /*em milissegundos*/
    final int CIRC = 0;
    final int TRI = 1;
    final int QUAD = 2;
    final int NULO = 3;
    final int ESQ = 0;
    final int CEN = 1;
    final int DIR = 2;
    final int TELA_W = 500;
    final int TELA_H = 700;
    final int J_XE = 143;
    final int J_XC = 223;
    final int J_XD = 303;
    final int J_TAM_XY = 50;
    final int J_Y = TELA_H - J_TAM_XY*2;
    int formaAtual[] = new int[3];
    Image jogador[][] = new Image[3][3];
    Inimigo zonaDeAcerto = null;
    Cena cn = new Cena();
    Object sincronia = new Object();

    class Imagens {
    	public Image grafico;
    	public int posX, posY, tamX, tamY;

    	Imagens (Image img, int pX, int pY, int tX, int tY) {
    		grafico = img;
    		posX = pX;
    		posY = pY;
    		tamX = tX;
    		tamY = tY;
    	}	
    }

	class Cena extends JPanel {
		Image fundo;

		Cena () {
			try {
				fundo = ImageIO.read(new File("fundo.png"));
				jogador[ESQ][CIRC] = ImageIO.read(new File("bCirc.png"));
                jogador[ESQ][TRI] = ImageIO.read(new File("bTri.png"));
                jogador[ESQ][QUAD] = ImageIO.read(new File("bQuad.png"));
                jogador[CEN][CIRC] = ImageIO.read(new File("bCirc.png"));
                jogador[CEN][TRI] = ImageIO.read(new File("bTri.png"));
                jogador[CEN][QUAD] = ImageIO.read(new File("bQuad.png"));
                jogador[DIR][CIRC] = ImageIO.read(new File("bCirc.png"));
                jogador[DIR][TRI] = ImageIO.read(new File("bTri.png"));
                jogador[DIR][QUAD] = ImageIO.read(new File("bQuad.png"));
			} catch (IOException e) {
				System.out.println("Não foi possivel carregar algum arquivo.");
			}

			formaAtual[ESQ] = TRI;
			formaAtual[CEN] = CIRC;
			formaAtual[DIR] = QUAD;
			vetImgs.add(new Imagens(fundo, 0, 0, TELA_W, TELA_H));
			vetImgs.add(new Imagens(jogador[ESQ][formaAtual[ESQ]], J_XE, J_Y, J_TAM_XY, J_TAM_XY));
			vetImgs.add(new Imagens(jogador[CEN][formaAtual[CEN]], J_XC, J_Y, J_TAM_XY, J_TAM_XY));
			vetImgs.add(new Imagens(jogador[DIR][formaAtual[DIR]], J_XD, J_Y, J_TAM_XY, J_TAM_XY));
		}

		public void paint (Graphics g) {
			super.paint(g);
			synchronized (sincronia) {
				for (Imagens m : vetImgs) {
					g.drawImage(m.grafico, m.posX, m.posY, m.tamX, m.tamY, this);
				}
			}
		}

		public Dimension getPreferredSize () {
			return new Dimension (TELA_W, TELA_H);
		}
	}

	class Inimigo implements Runnable {
		Image inimigo[] = new Image[3];
		int iPosY = 0;
		int iForma[] = new int[3];
		int meuIndice = 0;
		boolean saiu = false;
		Random r = new Random();

		Inimigo () {
			try {
				do {
					iForma[0] = r.nextInt(2);
					switch(iForma[0]) {
						case 0: inimigo[ESQ] = ImageIO.read(new File("tri.png")); break;
						case 1: inimigo[ESQ] = null;

					}
					iForma[1] = r.nextInt(2);
					switch(iForma[1]) {
						case 0: inimigo[CEN] = ImageIO.read(new File("circ.png")); break;
						case 1: inimigo[CEN] = null;
					}
					iForma[2] = r.nextInt(2);
					switch(iForma[2]) {
						case 0: inimigo[DIR] = ImageIO.read(new File("quad.png")); break;
						case 1: inimigo[DIR] = null;
					}
				} while (inimigo[ESQ] == null && inimigo[CEN] == null && inimigo[DIR] == null);
			} catch (IOException e) {
				System.out.println("Não foi possivel carregar alguma imagem.");
			}

			synchronized (sincronia) {
				meuIndice = vetImgs.size();
				vetImgs.add(new Imagens(inimigo[ESQ], J_XE, iPosY, J_TAM_XY, J_TAM_XY));
				vetImgs.add(new Imagens(inimigo[CEN], J_XC, iPosY, J_TAM_XY, J_TAM_XY));
				vetImgs.add(new Imagens(inimigo[DIR], J_XD, iPosY, J_TAM_XY, J_TAM_XY));
			}

			new Thread(this).start();
		}

		public void run () {
			while (iPosY < TELA_H) {
				try {
					Thread.sleep(PASSO_FREQ);
				} catch (InterruptedException ex) {}
				iPosY += PASSO;
				vetImgs.set(meuIndice, new Imagens(inimigo[ESQ], J_XE, iPosY, J_TAM_XY, J_TAM_XY));
				vetImgs.set(meuIndice+1, new Imagens(inimigo[CEN], J_XC, iPosY, J_TAM_XY, J_TAM_XY));
				vetImgs.set(meuIndice+2, new Imagens(inimigo[DIR], J_XD, iPosY, J_TAM_XY, J_TAM_XY));
				if (iPosY > (J_Y - J_TAM_XY/2) && iPosY < (J_Y + J_TAM_XY/2)) {
					zonaDeAcerto = this;
					System.out.println("ESTA NA ZONA DE ACERTO!");
				} else if (!saiu && iPosY >= (J_Y + J_TAM_XY/2)) {
					saiu = true;
					zonaDeAcerto = null;
				}
				cn.repaint();
			}
		}
	}

	class GeraInimigos extends Thread {
		public void run () {
			while (true) {
				new Inimigo();
				try {
					sleep(SPAWN_TIME);
				} catch (InterruptedException ex) {}
			}
		}
	}

	Game () {
		super("Untitled Game");
		setLocation(400,00);
		add(cn);
		addKeyListener(new KeyAdapter() {
			public void keyPressed (KeyEvent e) {
				switch(e.getKeyCode()) {
					case KeyEvent.VK_A:
					if (zonaDeAcerto != null && zonaDeAcerto.iForma[0] == 0) {
						System.out.println("Acertou na esquerda!");
						zonaDeAcerto.iForma[0] = 1;
						zonaDeAcerto.inimigo[ESQ] = null;
					}
						break;
					case KeyEvent.VK_S:
					if (zonaDeAcerto != null && zonaDeAcerto.iForma[1] == 0) {
						System.out.println("Acertou no meio!");
						zonaDeAcerto.iForma[1] = 1;
						zonaDeAcerto.inimigo[CEN] = null;
					}
						break;
					case KeyEvent.VK_D:
					if (zonaDeAcerto != null && zonaDeAcerto.iForma[2] == 0) {
						System.out.println("Acertou na direita!");
						zonaDeAcerto.iForma[2] = 1;
						zonaDeAcerto.inimigo[DIR] = null;
					}
						break;
				}
				cn.repaint();
			}
		});
		new GeraInimigos().start();
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		pack();
		setVisible(true);
	}

	public static void main (String[] args) {
		new Game();
	}
}