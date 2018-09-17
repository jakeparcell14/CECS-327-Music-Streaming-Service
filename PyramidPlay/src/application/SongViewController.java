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
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;


public class SongViewController implements Initializable{
	@FXML
	private Button _playButton;

	@FXML
	private Slider _slider;
	
	/**
	 * The current user logged into the application.
	 */
	private User user;
	
	/**
	 * The user's personal library of songs.
	 */
	private Playlist mySongs;

	@FXML
	private Label currentTime;

	@FXML
	private Label totalTime;

	@FXML
	private ToggleButton mySongsButton;

	@FXML
	private ToggleButton myPlaylistsButton;
	
	@FXML
	private ToggleButton currentPlaylistButton;

	@FXML
	private ListView<String> AllSongsListView;
	
	@FXML
	private TextField AllSongsSearchBar;
	
	@FXML
	private ListView UserLibraryList;

	@FXML
	private Pane SearchBarPane;
	
	/**
	 * Current playlist selected.
	 */
	private Playlist currentPlaylist;
	
	/**
	 * Index of current song selected in the playlist.
	 */
	private int playlistNum;

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
	ToggleGroup menuToggleGroup;

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
			
			/*if we are within a second of the end of the song, move to the next one 
			 * by simulating a "next song" click.
			 */
			if (s > _currentSong.getMicrosecondLength() - 1000000) {
				Platform.runLater(() -> {
					OnNextClicked(null);
					
				});
			}
			
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	};
	
	/**
	 * A method to be called by LoginController to pass user information from
	 * the login screen to song viewer
	 * @param user - user that is grabbed from the login screen controller
	 */
	public void initUser(User user) {
		this.user = user;
		mySongs= user.getSavedSongs();
		if (mySongs != null) {
			displaySongs(mySongs);
		}
	}
	
	public void displaySongs(Playlist pl) {
		ArrayList<Song> savedSongs=pl.getSongs();
		
		for(int i=0; i<savedSongs.size();i++) {
			if(savedSongs.get(i).getTitle()!=null) {
				UserLibraryList.getItems().addAll(savedSongs.get(i).getTitle());
			}
		}
	}
	
	public void displayPlaylists(ArrayList<Playlist> playlists) {
		for (int i = 0; i<playlists.size(); i++) {
			if(playlists.get(i).getPlaylistName()!=null) {
				UserLibraryList.getItems().addAll(playlists.get(i).getPlaylistName());
					break;
			}
		}
	}
	
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
			_currentSong.close();
		}
		
		// make search results invisible
		SearchBarPane.setVisible(false);
		SearchBarPane.setMouseTransparent(true);
	}

	@FXML
	/**
	 * Stops the current song and plays the next song in the playlist
	 * @param event - the previous button is clicked
	 */
	public void OnNextClicked (MouseEvent event) {

		_currentTime = 0;
		currentTime.setText((getTime(_currentTime)));
		if(_currentSong!=null) {
			_currentSong.stop();
			_currentSong.close();
		}
		
		//updates current song index then plays
		playlistNum++;
		if(playlistNum == currentPlaylist.getLength()) {
			playlistNum = 0;
		}
		
		if(_playButton.getText().equals("Pause")) {
			playSong(_currentTime);
		}
	
		// make search results invisible
		SearchBarPane.setVisible(false);
		SearchBarPane.setMouseTransparent(true);
		this.resetSearchText();
	}
	
	public void playSelectedSong () {
		_currentTime = 0;
		currentTime.setText((getTime(_currentTime)));
		if(_currentSong!=null) {
			_currentSong.stop();
			_currentSong.close();
		}
		
		
		//updates current song index then plays
		playlistNum++;
		if(playlistNum == currentPlaylist.getLength()) {
			playlistNum = 0;
		}
		
		playSong(_currentTime);
		if(_playButton.getText().equals("Play")) {
			_playButton.setText("Pause");
		}
		
		
	
		// make search results invisible
		SearchBarPane.setVisible(false);
		SearchBarPane.setMouseTransparent(true);
		this.resetSearchText();
	}

	@FXML
	/**
	 * Stops the current song and plays the previous song in the playlist
	 * @param event - the previous button is clicked
	 */
	public void OnPreviousClicked (MouseEvent event) {
		_currentSong.stop();
		_currentSong.close();
		
		//resets time and updates slider info
		_currentTime = 0;
		currentTime.setText((getTime(_currentTime)));
		
		//updates current song index then plays
		if(playlistNum > 0) {
			playlistNum--;
		}
		
		if(_playButton.getText().equals("Pause")) {
			playSong(_currentTime);
		}
		
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
		searchbar.setText("");
		searchbar.setPromptText("search my songs");
		UserLibraryList.getItems().clear();
		// display initialize the listview with all the my songs of the user
		if (mySongs != null) {
			displaySongs(mySongs);
		}
		
	}

	@FXML
	public void OnMyPlaylistsClicked (MouseEvent event) {
		//ensure myPlaylists button cannot be deselected
		myPlaylistsButton.setSelected(true);
				
		// make search results invisible
		SearchBarPane.setVisible(false);
		SearchBarPane.setMouseTransparent(true);
		this.resetSearchText();
		searchbar.setText("");
		searchbar.setPromptText("search my playlists");
		UserLibraryList.getItems().clear();
		// initalized listview with all the playlists the user has
		if (user.getPlaylists() != null) {
			displayPlaylists(user.getPlaylists());
		}
	}
	
	@FXML 
	public void OnCurrentPlaylistClicked(MouseEvent event) {
		ArrayList<Song> songs = currentPlaylist.getSongs();
		UserLibraryList.getItems().clear();
		UserLibraryList.getItems().addAll(songs);
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
		//item on the list view that the user selects
		String sel = UserLibraryList.getSelectionModel().getSelectedItem().toString();
		//user left clicks on library list
		if(event.getButton() == MouseButton.PRIMARY) {
			//if the saved songs button is selected, find the selected song to play
			if(((ToggleButton)menuToggleGroup.getSelectedToggle()).equals(mySongsButton)) {
				Playlist mySongs=user.getSavedSongs();
				ArrayList<Song> savedSongs=mySongs.getSongs();
				for(int i=0; i<savedSongs.size();i++) {
					if(savedSongs.get(i).getTitle()!=null) {
						//check if the selected list item is equal to the current songs title
						if(savedSongs.get(i).getTitle().toLowerCase().contains(sel.toLowerCase())) {
							currentPlaylist=mySongs;
							playlistNum=i-1;
							playSelectedSong();
							break;
						}
					}
					/**
					else if(savedSongs.get(i).getAlbum()!=null) {
						if(savedSongs.get(i).getAlbum().toLowerCase().contains(sel.toLowerCase())) {
							currentPlaylist=mySongs;
							playlistNum=i-1;
							playSelectedSong();
							break;
						}
					}
					else if(savedSongs.get(i).getArtist()!=null) {
						if(savedSongs.get(i).getArtist().toLowerCase().contains(sel.toLowerCase())) {
							currentPlaylist=mySongs;
							playlistNum=i-1;
							playSelectedSong();
							break;
						}
						
					}
					**/
				}
				
			}
			//my playlists are selected
			else if(((ToggleButton)menuToggleGroup.getSelectedToggle()).equals(myPlaylistsButton)){
				ArrayList<Playlist> playlists=user.getPlaylists();
				for (int i = 0; i<playlists.size(); i++) {
					if(playlists.get(i).getPlaylistName()!=null) {
						//check to see if the selected item matches the playlist title
						if(playlists.get(i).getPlaylistName().toLowerCase().equals(sel.toLowerCase())) {
							currentPlaylist=playlists.get(i);
							playlistNum=-1;
							playSelectedSong();
							currentPlaylistButton.setSelected(true);
							OnCurrentPlaylistClicked(null);
							break;
						}
					}
				}
			}
		}
		//user right clicks library list
		else if(event.getButton() == MouseButton.SECONDARY) {
			if(((ToggleButton)menuToggleGroup.getSelectedToggle()).equals(mySongsButton)) {
				ContextMenu cm = new ContextMenu();
				Menu parentMenu = new Menu("Add To Playlist");
				
				User user;
				try {
					//user hard coded
					user = UserRepository.getUser("amyer");
					ArrayList<Playlist> playlists=user.getPlaylists();
					ArrayList<MenuItem> childMenu = new ArrayList<MenuItem>();
					for (int i = 0; i<playlists.size(); i++) {
						MenuItem temp = new MenuItem(playlists.get(i).getPlaylistName());
						temp.setOnAction(new EventHandler<ActionEvent>() {
							 
				            @Override
				            public void handle(ActionEvent event) {
				            	String playlistName=temp.getText();
				            	Playlist mySongs=user.getSavedSongs();
								ArrayList<Song> savedSongs=mySongs.getSongs();
								for(int k=0;k<playlists.size();k++)
								{
									if(playlists.get(k).getPlaylistName().equals(playlistName)) {
										for(int j=0; j<savedSongs.size();j++) {
											if(savedSongs.get(j).getTitle()!=null) {
												//check if the selected list item is equal to the current songs title
												if(savedSongs.get(j).getTitle().toLowerCase().equals(sel.toLowerCase())) {
													Playlist tp = playlists.get(k);
													tp.addSong(savedSongs.get(j));
													playlists.set(k, tp);
													ArrayList<Song> random = playlists.get(k).getSongs();
													try {
														UserRepository.UpdateUser(user);
														currentPlaylist=playlists.get(k);
													} catch (IOException e) {
														// TODO Auto-generated catch block
														e.printStackTrace();
													}
													for(int f=0;f<random.size();f++) {
														System.out.println(random.get(f).getTitle());
													}
													break;
												}
											}
										}
									}
								}
				            }
				        });
						//childMenu.add(temp);
						
						parentMenu.getItems().add(temp);
					}
					cm.getItems().add(parentMenu);
					cm.show(UserLibraryList.getScene().getWindow(), event.getScreenX(), event.getScreenY());
				}catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else if(((ToggleButton)menuToggleGroup.getSelectedToggle()).equals(currentPlaylistButton)) {
				ContextMenu cm = new ContextMenu();
				MenuItem remove = new MenuItem("Remove Song");
				remove.setOnAction(new EventHandler<ActionEvent>() {
					 
		            @Override
		            public void handle(ActionEvent event) {
		            	User user;
						try {
							user = UserRepository.getUser("amyer");
							ArrayList<Playlist> playlists=user.getPlaylists();
							for(int n=0;n<playlists.size();n++) {
								if(currentPlaylist.getPlaylistName().equals(playlists.get(n).getPlaylistName())) {
									Playlist tempo=playlists.get(n);
									tempo.removeSong(sel);
									playlists.set(n, tempo);
									currentPlaylist=playlists.get(n);
									UserRepository.UpdateUser(user);
					            	OnCurrentPlaylistClicked(null);
					            	break;
								}
								
							}
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
		            }
				});
				cm.getItems().add(remove);
				cm.show(UserLibraryList.getScene().getWindow(), event.getScreenX(), event.getScreenY());
			}
			else if(((ToggleButton)menuToggleGroup.getSelectedToggle()).equals(myPlaylistsButton)) {
				ContextMenu cm = new ContextMenu();
				MenuItem createP = new MenuItem("Create New Playlist");
				MenuItem removeP = new MenuItem("Remove Playlist");
				removeP.setOnAction(new EventHandler<ActionEvent>() {
					 
		            @Override
		            public void handle(ActionEvent event) {
		            	User user;
						try {
							user = UserRepository.getUser("amyer");
							ArrayList<Playlist> playlists=user.getPlaylists();
			            	for(int n=0;n<playlists.size();n++) {
								if(sel.equals(playlists.get(n).getPlaylistName())) {
									System.out.println("it Works!");
								}
							}
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
		            }
	            });
				cm.getItems().add(removeP);
				cm.show(UserLibraryList.getScene().getWindow(), event.getScreenX(), event.getScreenY());
			}
		}
	}
	
	@FXML
	public void OnSliderClicked(MouseEvent event)
	{
		/* if it was not dragged but simple clicked to a new position,
		 * make sure to stop the song.
		 */
		if (_currentSong !=null &&_currentSong.isActive()) {
			_currentSong.stop();	
			_currentSong.close();
			
		}
		_currentTime = (long)_slider.getValue();

		//play the song from this new position
		if (_playButton.getText().equals("Pause")) {
			playSong(_currentTime);
		} else {
			currentTime.setText((getTime(_currentTime)));
		}
		
	}
	
	@FXML
	public void OnSliderDragDetected(MouseEvent event) {
		//if a drag is detected, stop the song.
		if (_currentSong !=null &&_currentSong.isActive()) {
			_currentSong.stop();		
			_currentSong.close();
		}
		
		/* the dropping of a drag will result in a click on the slider, 
		 * so that event handler will deal with playing the song from 
		 * the new position.
		 * 
		 * Total hack, I know.
		 */
	}
	
	@FXML 
	public void OnSliderDragDropped(MouseEvent event) {
		System.out.println("dropped");
	}
	
	@FXML
	public void search() {
		try {
			UserLibraryList.getItems().clear();
			//user inputted text
			String query=searchbar.getText();
			User user=UserRepository.getUser("amyer");
			//my songs are selected
			if(((ToggleButton)menuToggleGroup.getSelectedToggle()).equals(mySongsButton))
			{
				Playlist savedSongsPlaylist=user.getSavedSongs();
				ArrayList<Song> savedSongs = savedSongsPlaylist.getSongs();
				for(int i=0; i<savedSongs.size();i++) {
					//checks if query matches the title of the current song
					if(savedSongs.get(i).getTitle()!=null) {
						if(savedSongs.get(i).getTitle().toLowerCase().contains(query.toLowerCase())) {
							UserLibraryList.getItems().addAll(savedSongs.get(i).getTitle());
						}
					}
					//checks if query matches the album of the current song
					else if(savedSongs.get(i).getAlbum()!=null) {
						if(savedSongs.get(i).getAlbum().toLowerCase().contains(query.toLowerCase())) {
							UserLibraryList.getItems().addAll(savedSongs.get(i).getTitle());
						}
					}
					//checks if query matches the artist of the current song
					else if(savedSongs.get(i).getArtist()!=null) {
						if(savedSongs.get(i).getArtist().toLowerCase().contains(query.toLowerCase())) {
							UserLibraryList.getItems().addAll(savedSongs.get(i).getTitle());
						}
					}
				}
			}
			//my playlists are selected
			else if(((ToggleButton)menuToggleGroup.getSelectedToggle()).equals(myPlaylistsButton)) {
				//System.out.println(menuToggleGroup.getSelectedToggle());
				ArrayList<Playlist> playlists=user.getPlaylists();
				for (int i = 0; i<playlists.size(); i++) {
					if(playlists.get(i).getPlaylistName()!=null) {
						//check if query matches the playlist title
						if(playlists.get(i).getPlaylistName().toLowerCase().contains(query.toLowerCase())) {
							UserLibraryList.getItems().addAll(playlists.get(i).getPlaylistName());
						}
					}
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
			}		
	}


	/**
	 * Plays the currently hardcoded song.
	 * @param time Time in microseconds at which the song should start
	 */
	public void playSong(long time) {
		File f;
		try {
			//initialize clip
			_currentSong = AudioSystem.getClip();
			//open file and stream
			//grabs song from current playlist
			f = new File((currentPlaylist.getSongs()).get(playlistNum).getFileSource());
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
		menuToggleGroup = new ToggleGroup();

		mySongsButton.setToggleGroup(menuToggleGroup);
		myPlaylistsButton.setToggleGroup(menuToggleGroup);
		currentPlaylistButton.setToggleGroup(menuToggleGroup);

		mySongsButton.setSelected(true);
		searchbar.setPromptText("search my songs");
		
		
		//hardcoding a playlist for testing
		playlistNum = 0;
		Song rickroll = new Song("Never Gonna Give You Up", "RickAstley.wav");
		Song september = new Song("September", "September_EarthWindFire.wav");
		currentPlaylist = new Playlist("Saved Songs"); //let's assume the song player starts with the user's saved songs library
		currentPlaylist.addSong(rickroll);
		currentPlaylist.addSong(september);

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
