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
}