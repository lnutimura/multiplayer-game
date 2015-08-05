//JAVA
/*import java.io.*;
import sun.audio.*;
import javax.sound.sampled.*;*/

//JAVAFX
import java.net.URL;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.embed.swing.JFXPanel;

public class BGM {
	JFXPanel p = new JFXPanel();
	static URL path = BGM.class.getProtectionDomain().getCodeSource().getLocation();
	static Media bgMusic, score, miss;
	static MediaPlayer bgPlayer, scoreSE, missSE;
	
	public BGM(){
		try{
			bgMusic = new Media("file://"+path.getFile()+"audio/easy-electro.wav");
			bgPlayer = new MediaPlayer(bgMusic);
			bgPlayer.setCycleCount(MediaPlayer.INDEFINITE);
			score = new Media("file://"+path.getFile()+"audio/score.wav");
			scoreSE = new MediaPlayer(score);
			miss = new Media("file://"+path.getFile()+"audio/miss.wav");
			missSE = new MediaPlayer(miss);
		} catch(Exception e){
			e.printStackTrace();
		}
	}

	public static synchronized void playBackgroundMusic(){
		bgPlayer.play();
	}
	public static void stopBackgroundMusic(){
		bgPlayer.stop();
	}

	public static synchronized void playScoreSE(){
		scoreSE.play();
	}
	public static synchronized void playMissSE(){
		missSE.play();
	}


}