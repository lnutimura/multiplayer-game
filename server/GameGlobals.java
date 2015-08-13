import java.io.File;

public class GameGlobals{
	static final int STEP = 3;
  static final int STEP_FREQ = 30;
  static final int SPAWN_TIME = 2000; 
  static final int CIRCLE = 0;
  static final int TRIANGLE = 1;
  static final int SQUARE = 2;
  static final int NULL = 3;
  static final int LEFT = 0;
  static final int CENTER = 1;
  static final int RIGHT = 2;
  static final int SCREEN_W = 500;
  static final int SCREEN_H = 700;
  static final int S_XL = 124;
  static final int S_XC = 216;
  static final int S_XR = 313;
  static final int S_SIZE_XY = 65;
  static final int S_Y = SCREEN_H - S_SIZE_XY*2;
  static final int HITZONE_UPPER_BOUND = S_Y - S_SIZE_XY/2;
  static final int HITZONE_LOWER_BOUND = S_Y + S_SIZE_XY/2;
  static final int SCORE_STRING_SIZE = 4;
  static final int SCORE_X = 409;
  static final int SCORE_Y = 655;
  static final int SCORE_RES_X = 20;
  static final int SCORE_RES_Y = 30;
  
  static final File BCIRC_IMG = new File("images/bCirc.png");
  static final File BTRI_IMG = new File("images/bTri.png");
  static final File BSQU_IMG = new File("images/bQuad.png");
  static final File BG_IMG = new File("images/fundo.png");
  static final File CIRC_IMG = new File("images/circ.png");
  static final File TRI_IMG = new File("images/tri.png");
  static final File SQU_IMG = new File("images/quad.png");
}