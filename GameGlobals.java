import java.io.File;

public class GameGlobals{
	public static final int STEP = 3;
  public static final int STEP_FREQ = 30;
  public static final int SPAWN_TIME = 2000; 
  public static final int CIRCLE = 0;
  public static final int TRIANGLE = 1;
  public static final int SQUARE = 2;
  public static final int NULL = 3;
  public static final int LEFT = 0;
  public static final int CENTER = 1;
  public static final int RIGHT = 2;
  public static final int SCREEN_W = 500;
  public static final int SCREEN_H = 700;
  public static final int S_XL = 124;
  public static final int S_XC = 216;
  public static final int S_XR = 313;
  public static final int S_SIZE_XY = 65;
  public static final int S_Y = SCREEN_H - S_SIZE_XY*2;
  public static final int HITZONE_UPPER_BOUND = S_Y - S_SIZE_XY/2;
  public static final int HITZONE_LOWER_BOUND = S_Y + S_SIZE_XY/2;
  public static final int SCORE_STRING_SIZE = 4;
  public static final int SCORE_X = 409;
  public static final int SCORE_Y = 655;
  public static final int SCORE_RES_X = 20;
  public static final int SCORE_RES_Y = 30;
  
  public static final File BCIRC_IMG = new File("../images/bCirc.png");
  public static final File BTRI_IMG = new File("../images/bTri.png");
  public static final File BSQU_IMG = new File("../images/bQuad.png");
  public static final File BG_IMG = new File("../images/fundo.png");
  public static final File CIRC_IMG = new File("../images/circ.png");
  public static final File TRI_IMG = new File("../images/tri.png");
  public static final File SQU_IMG = new File("../images/quad.png");
}