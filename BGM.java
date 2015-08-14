//JAVAFX
import java.net.URL;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.embed.swing.JFXPanel;

public class BGM {
	JFXPanel p = new JFXPanel();
	static URL path = BGM.class.getProtectionDomain().getCodeSource().getLocation();
	static Media titleMusic, inGame, score, miss;
	static MediaPlayer titlePlayer, inPlayer, scoreSE, missSE;
	
	public BGM(){
		try{
			titleMusic = new Media("file://"+path.getFile()+"audio/music1.mp3");
			titlePlayer = new MediaPlayer(titleMusic);
			titlePlayer.setCycleCount(MediaPlayer.INDEFINITE);
			inGame = new Media("file://"+path.getFile()+"audio/music2.mp3");
			inPlayer = new MediaPlayer(inGame);
			inPlayer.setCycleCount(MediaPlayer.INDEFINITE);
			score = new Media("file://"+path.getFile()+"audio/score.wav");
			scoreSE = new MediaPlayer(score);
			miss = new Media("file://"+path.getFile()+"audio/miss.wav");
			missSE = new MediaPlayer(miss);
		} catch(Exception e){
			e.printStackTrace();
		}
	}

	public static synchronized void playTitleMusic(){
		titlePlayer.play();
	}
	public static void stopTitleMusic(){
		titlePlayer.stop();
	}

	public static synchronized void playScoreSE(){
		scoreSE.play();
	}
	public static synchronized void playMissSE(){
		missSE.play();
	}


}