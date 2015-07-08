import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;
import java.io.*;
import javax.imageio.*;

class Game extends JFrame{
    static Vector<Imagens> vetImgs = new Vector<Imagens>();
    final int PASSO = 3; /*em pixels*/
    final int PASSO_FREQ = 10; /*em milissegundos*/
    final int SPAWN_TIME = 2000; /*em milissegundos*/
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

    class Imagens {
        public Image grafico;
        public int posX, posY, tamX, tamY;
        Imagens(Image g, int pX, int pY, int tX, int tY){
            grafico = g;
            posX = pX;
            posY = pY;
            tamX = tX;
            tamY = tY;
        }
    };

    class Cena extends JPanel{
        Image fundo;
        Cena(){
            try{
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
            }
            catch (IOException e){
                System.out.println("Não foi possivel carregar alguma das imagens.");
            };
            formaAtual[ESQ] = TRI;
            formaAtual[CEN] = CIRC;
            formaAtual[DIR] = QUAD;
            vetImgs.add(new Imagens(fundo,0,0,TELA_W,TELA_H)); //fundo >> item 0
            vetImgs.add(new Imagens(jogador[ESQ][formaAtual[ESQ]],J_XE,J_Y,J_TAM_XY,J_TAM_XY)); //esq > item 1
            vetImgs.add(new Imagens(jogador[CEN][formaAtual[CEN]],J_XC,J_Y,J_TAM_XY,J_TAM_XY)); //cen > item 2
            vetImgs.add(new Imagens(jogador[DIR][formaAtual[DIR]],J_XD,J_Y,J_TAM_XY,J_TAM_XY)); //dir > item 3
        }

        public void paint(Graphics g){
            super.paint(g);
            for (Imagens m : vetImgs){
                g.drawImage(m.grafico,m.posX,m.posY,m.tamX,m.tamY,this);
            }
        }

        public Dimension getPreferredSize(){
            return new Dimension(TELA_W,TELA_H);
        }
    };


    class Inimigo implements Runnable{
        Image inimigo[] = new Image[3];
        int iPosY = 0;
        int iForma[] = new int[3];
        int meuIndice = 0;
        boolean saiu = false;
        Random r = new Random();

        Inimigo(){
            try{
                iForma[ESQ] = r.nextInt(4);
                switch(iForma[ESQ]){
                case TRI: inimigo[ESQ] = ImageIO.read(new File("tri.png")); break;
                case CIRC: inimigo[ESQ] = ImageIO.read(new File("circ.png")); break;
                case QUAD: inimigo[ESQ] = ImageIO.read(new File("quad.png")); break;
                default: inimigo[ESQ] = null;
                }
                iForma[CEN] = r.nextInt(4);
                switch(iForma[CEN]){
                case TRI: inimigo[CEN] = ImageIO.read(new File("tri.png")); break;
                case CIRC: inimigo[CEN] = ImageIO.read(new File("circ.png")); break;
                case QUAD: inimigo[CEN] = ImageIO.read(new File("quad.png")); break;
                default: inimigo[CEN] = null;
                }
                iForma[DIR] = r.nextInt(4);
                switch(iForma[DIR]){
                case TRI: inimigo[DIR] = ImageIO.read(new File("tri.png")); break;
                case CIRC: inimigo[DIR] = ImageIO.read(new File("circ.png")); break;
                case QUAD: inimigo[DIR] = ImageIO.read(new File("quad.png")); break;
                default: inimigo[DIR] = null;
                }
            }
            catch (IOException e){
                System.out.println("Não foi possivel carregar alguma das imagens.");
            };
            meuIndice = vetImgs.size();
            vetImgs.add(new Imagens(inimigo[ESQ],J_XE,iPosY,J_TAM_XY,J_TAM_XY));
            vetImgs.add(new Imagens(inimigo[CEN],J_XC,iPosY,J_TAM_XY,J_TAM_XY));
            vetImgs.add(new Imagens(inimigo[DIR],J_XD,iPosY,J_TAM_XY,J_TAM_XY));
            //cn.repaint();
            new Thread(this).start();
        }

        public void run(){
            while(iPosY < TELA_H){
                try{Thread.sleep(PASSO_FREQ);}
                catch(InterruptedException exc){};
                iPosY += PASSO;
                vetImgs.set(meuIndice,new Imagens(inimigo[ESQ],J_XE,iPosY,J_TAM_XY,J_TAM_XY));
                vetImgs.set(meuIndice+1,new Imagens(inimigo[CEN],J_XC,iPosY,J_TAM_XY,J_TAM_XY));
                vetImgs.set(meuIndice+2,new Imagens(inimigo[DIR],J_XD,iPosY,J_TAM_XY,J_TAM_XY));
                if (iPosY > (J_Y - J_TAM_XY/2) && iPosY < (J_Y + J_TAM_XY/2)){ /**INTERVALO DA ZONA DE ACERTO**/
                    zonaDeAcerto = this;
                    System.out.println("ESTÁ NA ZONA DE ACERTO!");
                }
                else if(!saiu && iPosY >= (J_Y + J_TAM_XY/2)){
                    saiu = true;
                    zonaDeAcerto = null;
                }
                cn.repaint();
            }
        }
    };

    class GeraInimigos extends Thread{
        public void run(){
            while(true){
                new Inimigo();
                try{Thread.sleep(SPAWN_TIME);}
                catch(InterruptedException exc){};
            }
        }
    };

    Game(){
        super("Joguito du Lorde");
        add(cn);
        addKeyListener(new KeyAdapter(){
            public void keyPressed(KeyEvent e){
                switch(e.getKeyCode()){
                case KeyEvent.VK_A:
                    switch(formaAtual[ESQ]){
                    case CIRC:
                        formaAtual[ESQ] = TRI;
                        vetImgs.set(1,new Imagens(jogador[ESQ][formaAtual[ESQ]],J_XE,J_Y,J_TAM_XY,J_TAM_XY));
                        break;
                    case TRI:
                        formaAtual[ESQ] = QUAD;
                        vetImgs.set(1,new Imagens(jogador[ESQ][formaAtual[ESQ]],J_XE,J_Y,J_TAM_XY,J_TAM_XY));
                        break;
                    case QUAD:
                        formaAtual[ESQ] = CIRC;
                        vetImgs.set(1,new Imagens(jogador[ESQ][formaAtual[ESQ]],J_XE,J_Y,J_TAM_XY,J_TAM_XY));
                        break;
                    }
                    break;
                case KeyEvent.VK_S:
                    switch(formaAtual[CEN]){
                    case CIRC:
                        formaAtual[CEN] = TRI;
                        vetImgs.set(2,new Imagens(jogador[CEN][formaAtual[CEN]],J_XC,J_Y,J_TAM_XY,J_TAM_XY));
                        break;
                    case TRI:
                        formaAtual[CEN] = QUAD;
                        vetImgs.set(2,new Imagens(jogador[CEN][formaAtual[CEN]],J_XC,J_Y,J_TAM_XY,J_TAM_XY));
                        break;
                    case QUAD:
                        formaAtual[CEN] = CIRC;
                        vetImgs.set(2,new Imagens(jogador[CEN][formaAtual[CEN]],J_XC,J_Y,J_TAM_XY,J_TAM_XY));
                        break;
                    }
                    break;
                case KeyEvent.VK_D:
                    switch(formaAtual[DIR]){
                    case CIRC:
                        formaAtual[DIR] = TRI;
                        vetImgs.set(3,new Imagens(jogador[DIR][formaAtual[DIR]],J_XD,J_Y,J_TAM_XY,J_TAM_XY));
                        break;
                    case TRI:
                        formaAtual[DIR] = QUAD;
                        vetImgs.set(3,new Imagens(jogador[DIR][formaAtual[DIR]],J_XD,J_Y,J_TAM_XY,J_TAM_XY));
                        break;
                    case QUAD:
                        formaAtual[DIR] = CIRC;
                        vetImgs.set(3,new Imagens(jogador[DIR][formaAtual[DIR]],J_XD,J_Y,J_TAM_XY,J_TAM_XY));
                        break;
                    }
                    break;
                }
                System.out.println("####");
                cn.repaint();
            }
        });
        new GeraInimigos().start();
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        pack();
        setVisible(true);
    }

    public static void main(String[] args){
        new Game();
    }
}
