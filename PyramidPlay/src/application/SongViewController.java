package application;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ResourceBundle;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;


public class SongViewController implements Initializable{
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

	@FXML
	private ListView<String> AllSongsListView;
	
	@FXML
	private TextField AllSongsSearchBar;
	
	@FXML
	private ListView UserLibraryList;

	@FXML
	private Pane SearchBarPane;

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
	
	@FXML
	private TextField searchbar;

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
		
		// make search results invisible
		SearchBarPane.setVisible(false);
		SearchBarPane.setMouseTransparent(true);
	}

	@FXML
	public void OnNextClicked (MouseEvent event) {
		System.out.println("Clicked next button.");
		
		// make search results invisible
		SearchBarPane.setVisible(false);
		SearchBarPane.setMouseTransparent(true);
		this.resetSearchText();
	}

	@FXML
	public void OnPreviousClicked (MouseEvent event) {
		System.out.println("Clicked previous button.");
		
		// make search results invisible
		SearchBarPane.setVisible(false);
		SearchBarPane.setMouseTransparent(true);
		this.resetSearchText();
	}

	@FXML
	public void OnMySongsClicked (MouseEvent event) {
		//ensure mySongs button cannot be deselected
		mySongsButton.setSelected(true);
		
		// make search results invisible
		SearchBarPane.setVisible(false);
		SearchBarPane.setMouseTransparent(true);
		this.resetSearchText();
	}

	@FXML
	public void OnMyPlaylistsClicked (MouseEvent event) {
		//ensure myPlaylists button cannot be deselected
		myPlaylistsButton.setSelected(true);
				
		// make search results invisible
		SearchBarPane.setVisible(false);
		SearchBarPane.setMouseTransparent(true);
		this.resetSearchText();
	}

	@FXML
	public void OnSearchBarClicked (MouseEvent event) {
		// make search results invisible
		SearchBarPane.setVisible(true);
		SearchBarPane.setMouseTransparent(false);
	}

	@FXML
	public void OnSongViewClicked (MouseEvent event) {
		// make search results invisible
		SearchBarPane.setVisible(false);
		SearchBarPane.setMouseTransparent(true);
		this.resetSearchText();
	}
	
	@FXML
	public void OnLibraryListClicked(MouseEvent event) {
		// make search results invisible
		SearchBarPane.setVisible(false);
		SearchBarPane.setMouseTransparent(true);
		this.resetSearchText();
	}
	
	@FXML
	public void OnSliderClicked(MouseEvent event)
	{
		// make search results invisible
		SearchBarPane.setVisible(false);
		SearchBarPane.setMouseTransparent(true);
		this.resetSearchText();
	}
	
	@FXML
	public void search() {
		String query=searchbar.getText();
		/**
		 * Hypothetical json search
		 * package jsontoxml;
		 * import java.io.*;
		 * import org.json.simple.parser.JSONParser;
		 * import org.json.simple.*;
		 * import java.util.*;
		 * *****************************************
		 * JSONParser parser = new JSONParser();
		 * try{
		 * 	Object obj =parser.parse(new FileReader("text.json"));
		 * 	JSONObject jsonObject = (JSONObject) obj;
		 * 	JSONArray cells = (JSONArray) jsonObject.get("cells");
		 * 	Iterator<JSONObject> iterator = cells.iterator();
		 * 	while(iterator.hasNext()){
		 * 		//determines whether or not the song should be added to the list
		 * 		boolean addToResults = false;
		 *		JSONobject current=iterator.next();
		 *	
		 * 		//check if song has title, if yes, check if the query
		 *		//is in the title
		 *		if(current.get("title") != null){
		 *			if(query.indexOf(current.get("title")) != -1){
		 *				addToResults = true;
		 *			}
		 * 		}
		 * 
		 * 		//check if song has an artist, if yes, check if the query
		 * 		//is in the artist	
		 * 		if(current.get("artist") != null){
		 *			if(query.indexOf(current.get("artist")) != -1){
		 *				addToResults = true;
		 *			}
		 * 		}
		 * 
		 * 		//check if song has album, if yes, check if the query
		 * 		//is in the album
		 * 		if(current.get("album") != null){
		 *			if(query.indexOf(current.get("album")) != -1){
		 *				addToResults = true;
		 *			}
		 * 		}
		 * 		//add song to list if it is found in the query
		 * 		if(addToResults==true)
		 * 		{
		 * 			//add to list
		 * 		}
		 * } catch (Exception e) {
		 * 	e.printStackTrace();
		 * }
		 * 
		 
		 * 
		 */
		System.out.println(query);
		ObservableList<String> list =FXCollections.observableArrayList("Mark","Tom","John","Jack");
		//searchResults.setItems(list);
		UserLibraryList.getItems().addAll(query);
		
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

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		ToggleGroup menuToggleGroup = new ToggleGroup();

		mySongsButton.setToggleGroup(menuToggleGroup);
		myPlaylistsButton.setToggleGroup(menuToggleGroup);

		mySongsButton.setSelected(true);

		//test code for listview. TODO add song info to listview
		ArrayList<String> s = new ArrayList<String>(Arrays.asList("This", "is", "where", "our", "songs", "will", "go"));
		ObservableList<String> songs = FXCollections.observableArrayList(s);
		AllSongsListView.setItems(songs);

		//make listview automatically invisible until the search bar is selected
		SearchBarPane.setVisible(false);
		SearchBarPane.setMouseTransparent(true);
		this.resetSearchText();
	}
	
	public void resetSearchText()
	{
		// reset prompt text
		if(AllSongsSearchBar.getText().equals(""))
		{
			AllSongsSearchBar.setPromptText("search all songs");
		}
	}
}
