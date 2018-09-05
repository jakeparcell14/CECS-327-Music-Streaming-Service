package application;

import java.io.File;
import java.io.IOException;
import java.time.Duration;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseEvent;

public class SongViewController {
	@FXML
	private Button _playButton;

	@FXML
	private Slider _slider;
	
	@FXML
	private Label currentTime;

	@FXML
	private Label totalTime;
	
	@FXML
	private ToggleButton mySongsButton;
	
	@FXML
	private ToggleButton myPlaylistsButton;
	
	/**
	 * Current song.
	 */
	private Clip _currentSong;
	
	/**
	 * Current time of the song. Used for pausing purposes.
	 */
	private long _currentTime;
	
	/**
	 * Current slider position.
	 */
	public double _sliderPosition;
	
	/**
	 * Terrible name, but thread that watches the current time of the song.
	 */
	public Thread _thread;
	
	Runnable theTask = () -> {
		while (_currentSong.isActive()) {
			long s = _currentSong.getMicrosecondPosition();	
			
			/**
			 * This places the code within it into the UI thread's execution queue.
			 * Allows us to access UI thread elements from a different thread.
			 * We cannot place any of the while loop in the UI thread because it locks 
			 * the thread up.
			 */
			Platform.runLater(() -> {
				_slider.setValue(s);
				currentTime.setText(getTime(s));
			});
			
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	};
	

	// Event Listener on Button[#_playButton].onMouseClicked
	@FXML
	public void OnPlayPauseClicked(MouseEvent event) {
		if(_playButton.getText().equals("Play")) {
			_playButton.setText("Pause");
			playSong(_currentTime);
		} else {
			_playButton.setText("Play");
			_currentTime = _currentSong.getMicrosecondPosition();
			_currentSong.stop();
		}
			
	}
	
	@FXML
	public void OnNextClicked (MouseEvent event) {
		System.out.println("Clicked next button.");
	}
	
	@FXML
	public void OnPreviousClicked (MouseEvent event) {
		System.out.println("Clicked previous button.");
	}
	
	/**
	 * Plays the currently hardcoded song.
	 * @param time Time in microseconds at which the song should start
	 */
	public void playSong(long time) {
		try {
			//initialize clip
			_currentSong = AudioSystem.getClip();
			//open file and stream
			//this is currently hardcoded in at the moment, only wanted to get this working
			File f = new File("RickAstley.wav");
			AudioInputStream inputStream = AudioSystem.getAudioInputStream(f.toURI().toURL());
			
			_currentSong.open(inputStream);
			_currentSong.setMicrosecondPosition(time); //sets time song will start at
			_currentSong.start();
			
			//start background thread to update UI with song
			_thread = new Thread(theTask);
			_thread.setDaemon(true); //allows thread to end on exit
			_thread.start();
			
			//set slider details for this song
			_slider.setMin(0);
			_slider.setMax(_currentSong.getMicrosecondLength());
			totalTime.setText(getTime(_currentSong.getMicrosecondLength()));
			currentTime.setText(getTime(time));
		} catch (LineUnavailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedAudioFileException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/***
	 * Formats microseconds into a string that is in HH:MM:SS format.
	 * @param microseconds
	 * @return Returns properly formatted string.
	 */
	public String getTime(long microseconds) {
		//convert to seconds
		long s = microseconds/1000000;
		return String.format("%d:%02d:%02d", s/3600, (s%3600)/60, (s%60));
	}
}
