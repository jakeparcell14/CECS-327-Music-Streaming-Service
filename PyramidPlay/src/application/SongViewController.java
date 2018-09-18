package application;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
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
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
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

	/*
	 * Name of current song selected/playing
	 */
	@FXML
	private Label currentSongName;

	@FXML
	private Label currentSongArtist;

	@FXML
	private Label currentSongAlbum;

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
	private ListView AllSongsListView;

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
	 * List of all songs in program
	 */
	private ArrayList<Song> allSongs;

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
		currentPlaylist = user.getSavedSongs();
	}

	public void displaySongs(Playlist pl) {
		ArrayList<Song> savedSongs=pl.getSongs();
		savedSongs.sort(null);

		for(int i=0; i<savedSongs.size();i++) {
			if(savedSongs.get(i).getTitle()!=null) {
				UserLibraryList.getItems().addAll(savedSongs.get(i).getTitle());
			}
		}
	}

	public void displayPlaylists(ArrayList<Playlist> playlists) {
		UserLibraryList.getItems().clear();
		playlists.sort(null);
		for (int i = 0; i<playlists.size(); i++) {
			if(playlists.get(i).getPlaylistName()!=null) {
				UserLibraryList.getItems().addAll(playlists.get(i).getPlaylistName());
			}
		}
	}

	/**
	 * updates the labels of the song viewer
	 * @param song - song currently playing
	 */
	public void updateSongLabels(Song song) {
		currentSongName.setText(song.getTitle());
		currentSongArtist.setText(song.getArtist());
		currentSongAlbum.setText(song.getAlbum());
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

		updateSongLabels(currentPlaylist.getSongs().get(playlistNum));
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

		updateSongLabels(currentPlaylist.getSongs().get(playlistNum));
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

		updateSongLabels(currentPlaylist.getSongs().get(playlistNum));
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
		ArrayList<Song> songs = new ArrayList<Song>();

		if(!currentPlaylist.getSongs().isEmpty())
		{
			songs = currentPlaylist.getSongs();
			songs.sort(null);
		}

		UserLibraryList.getItems().clear();
		UserLibraryList.getItems().addAll(songs);
		searchbar.setText("");
		searchbar.setPromptText("search " + currentPlaylist.getPlaylistName());
	}

	@FXML
	public void OnSearchBarClicked (MouseEvent event) {
		// make search results invisible
		SearchBarPane.setVisible(true);
		SearchBarPane.setMouseTransparent(false);
		
		AllSongsListView.getItems().clear();
		
		for(int i=0; i<allSongs.size();i++) {
			AllSongsListView.getItems().addAll(allSongs.get(i).getTitle());
		}
	}

	@FXML
	public void OnSongViewClicked (MouseEvent event) {
		// make search results invisible
		SearchBarPane.setVisible(false);
		SearchBarPane.setMouseTransparent(true);

		AllSongsListView.getItems().clear();

		for(int i=0; i<allSongs.size();i++) {
			AllSongsListView.getItems().addAll(allSongs.get(i).getTitle());
		}

	}

	@FXML
	public void OnAllSongsListClicked(MouseEvent event)
	{
		//item on the list view that the user selects
 		try {
			String sel = AllSongsListView.getSelectionModel().getSelectedItem().toString();
			//user left clicks on library list
			if(event.getButton() == MouseButton.PRIMARY) {
 				Playlist allSongsPlaylist = new Playlist("all songs", allSongs);
				for(int i=0; i<allSongs.size();i++) {
					if(allSongs.get(i).getTitle()!=null) {
						//check if the selected list item is equal to the current songs title
						if(allSongs.get(i).getTitle().toLowerCase().contains(sel.toLowerCase())) {
							currentPlaylist=allSongsPlaylist;
							playlistNum=i-1;
							playSelectedSong();
							break;
						}
					}
				}
				
				// make search results invisible
				this.resetSearchText();
			}
			//user right clicks library list
			else if(event.getButton() == MouseButton.SECONDARY) {
				ContextMenu cm = new ContextMenu();
				Menu parentMenu = new Menu("Add To Playlist");
 				//user hard coded
 				ArrayList<Playlist> playlists=user.getPlaylists();
				ArrayList<MenuItem> childMenu = new ArrayList<MenuItem>();
				for (int i = 0; i<playlists.size(); i++) {
					MenuItem temp = new MenuItem(playlists.get(i).getPlaylistName());
					temp.setOnAction(new EventHandler<ActionEvent>() {
 						@Override
						public void handle(ActionEvent event) {
							String playlistName=temp.getText();
							for(int k=0;k<playlists.size();k++)
							{
								if(playlists.get(k).getPlaylistName().equals(playlistName)) {
									for(int j=0; j<allSongs.size();j++) {
										if(allSongs.get(j).getTitle()!=null) {
											//check if the selected list item is equal to the current songs title
											if(allSongs.get(j).getTitle().toLowerCase().equals(sel.toLowerCase())) {
												Playlist tp = playlists.get(k);
												tp.addSong(allSongs.get(j));
												playlists.set(k, tp);
												try {
													user.setPlaylists(playlists);
													UserRepository.UpdateUser(user);
													//currentPlaylist=playlists.get(k);
													
													//update song list
													//ArrayList<Song> songs = currentPlaylist.getSongs();
													//UserLibraryList.getItems().clear();
													//UserLibraryList.getItems().addAll(songs);
													
													// make search results invisible
													SearchBarPane.setVisible(false);
													SearchBarPane.setMouseTransparent(true);
													resetSearchText();
												} catch (IOException e) {
													// TODO Auto-generated catch block
													e.printStackTrace();
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
				MenuItem addToSaved = new MenuItem("Add To Saved Songs");
				addToSaved.setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent event) {
						Playlist mySongs=user.getSavedSongs();
						for(int j=0; j<allSongs.size();j++) {
							if(allSongs.get(j).getTitle()!=null) {
								if(allSongs.get(j).getTitle().toLowerCase().equals(sel.toLowerCase())) {
									ArrayList<Song> myActualSongs=mySongs.getSongs();
									int sameTitle=0;
									for(int z=0;z<myActualSongs.size();z++) {
										if(myActualSongs.get(z).getTitle().equals(sel))
										{
											sameTitle++;
										}
									}
									if(sameTitle==0) {
										mySongs.addSong(allSongs.get(j));
										user.setSavedSongs(mySongs);
										try {
											UserRepository.UpdateUser(user);
										} catch (IOException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
									}
									else {
										Alert alert = new Alert(AlertType.ERROR);
										alert.setTitle("Save Song Error");
										alert.setHeaderText("Song Already in Saved Songs");
										alert.setContentText("Please try a different option");
										alert.showAndWait();
									}
								}
							}
						}
					}
				});
				cm.getItems().add(parentMenu);
				cm.getItems().add(addToSaved);
				cm.show(UserLibraryList.getScene().getWindow(), event.getScreenX(), event.getScreenY());
 			}
		} catch (Exception e) 
		{
			//listview item that was clicked is	 blank
		}
	}
	
	@FXML
	public void OnLibraryListClicked(MouseEvent event) {
		// make search results for main search bar invisible
		SearchBarPane.setVisible(false);
		SearchBarPane.setMouseTransparent(true);
		this.resetSearchText();
		//item on the list view that the user selects

		try {
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
					}

				}
				//my playlists are selected
				else if(((ToggleButton)menuToggleGroup.getSelectedToggle()).equals(myPlaylistsButton)){
					ArrayList<Playlist> playlists = user.getPlaylists();
					for (int i = 0; i<playlists.size(); i++) {
						if(playlists.get(i).getPlaylistName()!=null) {
							//check to see if the selected item matches the playlist title
							if(playlists.get(i).getPlaylistName().toLowerCase().equals(sel.toLowerCase())) {
								currentPlaylist=playlists.get(i);
								playlistNum=0;
								playSelectedSong();
								currentPlaylistButton.setSelected(true);
								OnCurrentPlaylistClicked(null);
								break;
							}
						}
					}
				}
				else if (((ToggleButton)menuToggleGroup.getSelectedToggle()).equals(currentPlaylistButton)) {
					ArrayList<Song> songs = currentPlaylist.getSongs();
					for (int i = 0; i< songs.size(); i++) {
						if (songs.get(i).getTitle().equals(sel)) {
							playlistNum = i -1;
							playSelectedSong();
							break;
						}
					}
					
				}
			}
			//user right clicks library list
			else if(event.getButton() == MouseButton.SECONDARY) {
				if(((ToggleButton)menuToggleGroup.getSelectedToggle()).equals(mySongsButton)) {
					ContextMenu cm = new ContextMenu();
					Menu parentMenu = new Menu("Add To Playlist");
					ArrayList<Playlist> playlists=user.getPlaylists();
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
													try {
														user.setPlaylists(playlists);
														UserRepository.UpdateUser(user);
													} catch (IOException e) {
														// TODO Auto-generated catch block
														e.printStackTrace();
													}
													break;
												}
											}
											
										}
									}
								}
							}
						});
						parentMenu.getItems().add(temp);
					}
					MenuItem removeSavedSong = new MenuItem("Remove Saved Song");
					removeSavedSong.setOnAction(new EventHandler<ActionEvent>() {
						@Override
						public void handle(ActionEvent event) {
							Playlist temp=user.getSavedSongs();
							temp.removeSong(sel);
							user.setSavedSongs(temp);
							try {
								UserRepository.UpdateUser(user);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							OnMySongsClicked(null);
						}
					});
					cm.getItems().add(parentMenu);
					cm.getItems().add(removeSavedSong);
					cm.show(UserLibraryList.getScene().getWindow(), event.getScreenX(), event.getScreenY());
				}
				else if(((ToggleButton)menuToggleGroup.getSelectedToggle()).equals(currentPlaylistButton)) {
					ContextMenu cm = new ContextMenu();
					MenuItem remove = new MenuItem("Remove Song");
					remove.setOnAction(new EventHandler<ActionEvent>() {

						@Override
						public void handle(ActionEvent event) {
							try {
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
							try {
								ArrayList<Playlist> playlists=user.getPlaylists();
								if(!sel.equals("My Playlist")){
									for(int n=0;n<playlists.size();n++) {
										if(sel.equals(playlists.get(n).getPlaylistName())) {
											if(playlists.get(n).equals(currentPlaylist)) {
												currentPlaylist=user.getSavedSongs();
											}
											playlists.remove(n);
											user.setPlaylists(playlists);
											UserRepository.UpdateUser(user);
											OnMyPlaylistsClicked(null);
										}
									}
								}
								else {
									Alert alert = new Alert(AlertType.ERROR);
									alert.setTitle("Error");
									alert.setHeaderText("Cannot delete My Playlist");
									alert.setContentText("Please try a different option");
									alert.showAndWait();
								}
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					});
					createP.setOnAction(new EventHandler<ActionEvent>() {
						@Override
						public void handle(ActionEvent event) {
							TextInputDialog dialog = new TextInputDialog("");
							dialog.setTitle("New Playlist Dialog");
							dialog.setHeaderText("New Playlist");
							dialog.setContentText("Enter Playlist Name:");

							try {
								ArrayList<Playlist> playlists=user.getPlaylists();
								while(true) {
									Optional<String> result = dialog.showAndWait();
									int count=0;
									if(result.isPresent()) {
										for(int i=0;i<playlists.size();i++) {
											if(playlists.get(i).getPlaylistName().equals(result.get())) {
												count++;
											}
										}
										if(count!=0) {
											Alert alert = new Alert(AlertType.ERROR);
											alert.setTitle("Error adding playlist");
											alert.setHeaderText("There is already a playlist with that name");
											alert.setContentText("Please try a different name");
											alert.showAndWait();
										}
										else if(result.get().trim().length() == 0) {
											Alert alert = new Alert(AlertType.ERROR);
											alert.setTitle("Error adding playlist");
											alert.setHeaderText("Enter Characters that are not blank space");
											alert.setContentText("Please try a different name");
											alert.showAndWait();
										}
										else{
											playlists.add(new Playlist(result.get()));
											user.setPlaylists(playlists);
											UserRepository.UpdateUser(user);
											OnMyPlaylistsClicked(null);
											break;
										}
									}
									else
									{
										break;
									}
								}
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

						}

					});
					cm.getItems().add(removeP);
					cm.getItems().add(createP);
					cm.show(UserLibraryList.getScene().getWindow(), event.getScreenX(), event.getScreenY());
				}
			}
		} catch (Exception e) 
		{
			//listview item that was clicked is	 blank
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
	public void searchAllSongs()
	{
		try
		{
			AllSongsListView.getItems().clear();

			//user inputed text
			String query = AllSongsSearchBar.getText();

			for(int i=0; i<allSongs.size();i++) {
				//checks if query matches the title of the current song				
				if(allSongs.get(i).getTitle()!=null && allSongs.get(i).getTitle().toLowerCase().contains(query.toLowerCase())) {
					AllSongsListView.getItems().addAll(allSongs.get(i).getTitle());
				}
				//checks if query matches the album of the current song
				else if(allSongs.get(i).getAlbum()!=null && allSongs.get(i).getAlbum().toLowerCase().contains(query.toLowerCase())) {
					AllSongsListView.getItems().addAll(allSongs.get(i).getTitle());
				}
				//checks if query matches the artist of the current song
				else if(allSongs.get(i).getArtist()!=null && allSongs.get(i).getArtist().toLowerCase().contains(query.toLowerCase())) {
					AllSongsListView.getItems().addAll(allSongs.get(i).getTitle());
				}
			}

		}
		catch(Exception e)
		{

		}
	}

	@FXML
	public void search() {
		try {
			UserLibraryList.getItems().clear();
			//user inputted text
			String query=searchbar.getText();

			//my songs are selected
			if(((ToggleButton)menuToggleGroup.getSelectedToggle()).equals(mySongsButton))
			{
				Playlist savedSongsPlaylist=user.getSavedSongs();
				ArrayList<Song> savedSongs = savedSongsPlaylist.getSongs();
				for(int i=0; i<savedSongs.size();i++) {
					//checks if query matches the title of the current song
					if(savedSongs.get(i).getTitle()!=null && savedSongs.get(i).getTitle().toLowerCase().contains(query.toLowerCase())) {
						UserLibraryList.getItems().addAll(savedSongs.get(i).getTitle());
					}
					//checks if query matches the album of the current song
					else if(savedSongs.get(i).getAlbum()!=null && savedSongs.get(i).getAlbum().toLowerCase().contains(query.toLowerCase())) {
						UserLibraryList.getItems().addAll(savedSongs.get(i).getTitle());
					}
					//checks if query matches the artist of the current song
					else if(savedSongs.get(i).getArtist()!=null && savedSongs.get(i).getArtist().toLowerCase().contains(query.toLowerCase())) {
						UserLibraryList.getItems().addAll(savedSongs.get(i).getTitle());
					}
				}
			}
			//my playlists are selected
			else if(((ToggleButton)menuToggleGroup.getSelectedToggle()).equals(myPlaylistsButton)) {
				ArrayList<Playlist> playlists=user.getPlaylists();
				for (int i = 0; i<playlists.size(); i++) {
					if(playlists.get(i).getPlaylistName()!=null) {
						//check if query matches the playlist title
						if(playlists.get(i).getPlaylistName().toLowerCase().contains(query.toLowerCase())) {
							UserLibraryList.getItems().addAll(playlists.get(i).getPlaylistName());
						}
					}
				}
			} else if (((ToggleButton)menuToggleGroup.getSelectedToggle()).equals(currentPlaylistButton)) {
				ArrayList<Song> songs = currentPlaylist.getSongs();
				for(int i=0; i<songs.size();i++) {
					//checks if query matches the title of the current song
					if(songs.get(i).getTitle()!=null && songs.get(i).getTitle().toLowerCase().contains(query.toLowerCase())) {
						UserLibraryList.getItems().addAll(songs.get(i).getTitle());
					}
					//checks if query matches the album of the current song
					else if(songs.get(i).getAlbum()!=null && songs.get(i).getAlbum().toLowerCase().contains(query.toLowerCase())) {
						UserLibraryList.getItems().addAll(songs.get(i).getTitle());
					}
					//checks if query matches the artist of the current song
					else if(songs.get(i).getArtist()!=null && songs.get(i).getArtist().toLowerCase().contains(query.toLowerCase())) {
						UserLibraryList.getItems().addAll(songs.get(i).getTitle());
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
		updateSongLabels(currentPlaylist.getSongs().get(playlistNum));
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

		try 
		{
			allSongs = UserRepository.getAllSongs();
			allSongs.sort(null);
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
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
