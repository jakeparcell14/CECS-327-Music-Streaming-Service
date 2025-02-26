package application;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.UnknownHostException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.util.Duration;

import com.google.gson.Gson;


/**
 * This class handles the Song View user interface.
 * 
 * We are aware about how absurdly horrific this code is.
 */
public class SongViewController implements Initializable{
	static Log server_log;
	/**
	 * the socket used to make a connection to the server
	 */
	DatagramSocket socket;
	/**
	 * gson variable
	 */
	Gson gson;
	/**
	 * Id used for request method needed
	 */
	public static int requestID;

	@FXML
	/**
	 * This button makes the song start or stop playing
	 */
	private Button _playButton;

	@FXML
	/**
	 * This button shows what part of the song is playing and allows the user to manually choose a part of the song to start playing
	 */
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
	/**
	 * Name of current song selected/playing
	 */
	private Label currentSongName;

	@FXML
	/**
	 * This label displays the artist of the current song
	 */
	private Label currentSongArtist;

	@FXML
	/**
	 * This label displays the album of the current song
	 */
	private Label currentSongAlbum;

	@FXML
	/**
	 * This label displays the current time of the song playing
	 */
	private Label currentTime;

	@FXML
	/**
	 * This label displays the length of the song in minutes and seconds
	 */
	private Label totalTime;

	@FXML
	/**
	 * This button allows the user to display their saved songs
	 */
	private ToggleButton mySongsButton;

	@FXML
	/**
	 * This button allows the user to display their playlists
	 */
	private ToggleButton myPlaylistsButton;

	@FXML
	/**
	 * This button allows the user to display the songs in the current playlist
	 */
	private ToggleButton currentPlaylistButton;

	@FXML
	/**
	 * This displays the information for the songs in the search results
	 */
	private TableView AllSongsList;

	@FXML
	/**
	 * This textfield allows the user to type information for the song they want to search
	 */
	private TextField AllSongsSearchBar;

	@FXML
	/**
	 * This textfield allows the user to view their songs and playlists
	 */
	private TableView UserLibraryList;

	@FXML
	/**
	 * This pane holds the All Songs search bar
	 */
	private Pane SearchResultsPane;

	@FXML
	/**
	 * This Pane is the main window
	 */
	private Pane SongViewPane;

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
	 * Current time of the song. Used for pausing purposes.
	 */
	private Duration _currentTime;

	/**
	 * Current slider position.
	 */
	public double _sliderPosition;
	/**
	 * search bar
	 */
	@FXML
	private TextField searchbar;


	/**
	 * This textfield allows the user to search through songs and playlists
	 */
	@FXML
	private TextField userLibrarySearchBar;

	/**
	 * thread that watches the current time of the song.
	 */
	public Thread _thread;

	/**
	 * this is the group of buttons that allows the user to choose what to display in the User Library List
	 */
	private ToggleGroup menuToggleGroup;

	/**
	 * sections of the TableViews that show the song title
	 */
	private TableColumn titleColumn, allSongsTitleColumn;

	/**
	 * sections of the TableViews that show the artist of the song
	 */
	private TableColumn artistColumn, allSongsArtistColumn;

	/**
	 * sections of the TableViews that show the album name
	 */
	private TableColumn albumColumn, allSongsAlbumColumn;

	/**
	 * section of the TableView that shows the playlist names
	 */
	private TableColumn playlistNameColumn;

	/**
	 * section of the TableView that shows the date the playlist was made
	 */
	private TableColumn dateCreatedColumn;

	/**
	 * Current song object.
	 */
	private Media Song;

	/**
	 * Media player to play song.
	 */
	private MediaPlayer player;

	/**
	 * Cached song file.
	 */
	private File cachedSong;
	
	/**
	 * Timeout in ms.
	 */
	private final int TIMEOUT = 500;
	
	/**
	 * Tries before client gives up trying to contact server.
	 */
	private final int TRIES = 5;

	/**
	 * Runnable type that runs in the thread. Updates UI as the song plays.
	 */
	Runnable UIUpdateThread = () -> {
		//busy wait
		while(!player.getStatus().equals(Status.PLAYING));
		while (player.getStatus().equals(Status.PLAYING)) {
			Duration s = player.getCurrentTime();	

			/**
			 * This places the code within it into the UI thread's execution queue.
			 * Allows us to access UI thread elements from a different thread.
			 * We cannot place any of the while loop in the UI thread because it locks 
			 * the thread up.
			 */
			Platform.runLater(() -> {
				_slider.setValue(s.toMillis());
				currentTime.setText(getTime(s));
			});

			/*if we are within a second of the end of the song, move to the next one 
			 * by simulating a "next song" click.
			 */
			if (s.toMillis() > player.getTotalDuration().toMillis() - 1000) {
				Platform.runLater(() -> {
					OnNextClicked(null);
				});
			}

			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
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

		//display my songs and set it as the current playlist
		mySongs= user.getSavedSongs();
		if(mySongs != null)
		{
			displaySongs(mySongs);
		}
		currentPlaylist = user.getSavedSongs();

	}

	/**
	 * displays songs from a given playlist on the main search bar or on the user library
	 * @param pl	playlist that contains the songs to be displayed
	 */
	/**
	 * displays songs from a given playlist on the main search bar or on the user library
	 * @param pl	playlist that contains the songs to be displayed
	 */
	public void displaySongs(Playlist pl) {

		if(AllSongsSearchBar.isFocused())
		{
			// display songs on all songs list
			AllSongsList.getItems().clear();

			SearchResultsPane.setVisible(true);
			SearchResultsPane.setMouseTransparent(false);


			if(pl.getSongs().size() > 20)
			{
				AllSongsList.getItems().addAll(pl.getSongs().subList(0, 20));
			}
			else
			{
				AllSongsList.getItems().addAll(pl.getSongs());
			}
		}
		else
		{
			if(UserLibraryList.getColumns().get(0).equals(playlistNameColumn))
			{
				//table is currently displaying playlists
				UserLibraryList.getColumns().clear();
				UserLibraryList.getColumns().addAll(titleColumn, artistColumn, albumColumn);
			}

			//display songs on user library list
			UserLibraryList.getItems().clear();
			UserLibraryList.getItems().addAll(pl.getSongs());
		}
	}


	/**
	 * displays all playlists and the dates they were created on the User Library
	 * @param playlists		all of the users playlists
	 */
	public void displayPlaylists(ArrayList<Playlist> playlists) {

		if(UserLibraryList.getColumns().get(0).equals(titleColumn))
		{
			//table is currently displaying songs
			UserLibraryList.getColumns().clear();
			UserLibraryList.getColumns().addAll(playlistNameColumn, dateCreatedColumn);
		}

		UserLibraryList.getItems().clear();
		UserLibraryList.getItems().addAll(playlists);
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

	@FXML
	/**
	 * Event Listener for the play button. This listener will stop the song if it is currently playing or start the song if it is not playing
	 * @param event		the play/pause button has been clicked
	 */
	public void OnPlayPauseClicked(MouseEvent event) {
		if(_playButton.getText().equals("Play")) {
			_playButton.setText("Pause");
			try {
				playSong(_currentTime);
			} catch (NullPointerException e) {
				_playButton.setText("Play");
				DisplayAlert("Error!", "Error reading cache.", "May be due to a server error.");
			}
			
		} else {
			_playButton.setText("Play");
			_currentTime = player.getCurrentTime();
			player.stop();
		}

		// make search results invisible
		SearchResultsPane.setVisible(false);
		SearchResultsPane.setMouseTransparent(true);
	}

	@FXML
	/**
	 * Stops the current song and plays the next song in the playlist
	 * @param event  the previous button is clicked
	 */
	public void OnNextClicked (MouseEvent event) {
		//updates current song index then plays
		playlistNum++;
		if(playlistNum == currentPlaylist.getLength()) {
			playlistNum = 0;
		}

		playSelectedSong();
	}

	/**
	 * Method to play the current song from the beginning.
	 */
	public void playSelectedSong () {



		if(player != null && player.getStatus().equals(Status.PLAYING)) {
			player.stop();
		}

		DatagramSocket socket = null;
		for (int i = 0; i< TRIES; i++) {
			System.out.println("Awaiting response from server...");
			try {
				socket = new DatagramSocket();
				socket.setSoTimeout(TIMEOUT);
				cachedSong = SongDownloader.DownloadSong(socket, (currentPlaylist.getSongs()).get(playlistNum), requestID++);
				_currentTime = Duration.ZERO;
				currentTime.setText((getTime(_currentTime)));
				System.out.println("Downloaded song.");
				
				if(_playButton.getText().equals("Play")) {
					_playButton.setText("Pause");
				}
				playSong(_currentTime);
				break;
			} catch (SocketTimeoutException e) {
				if (i == TRIES - 1) {
					DisplayAlert("Server connection error", "Client was unable to connect to server", "Please try again later");
					return;
				}
				
				System.out.println("No response from server, sending request again.");
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				if (socket != null) 
					socket.close();
			}
		}
		




		// make search results invisible
		SearchResultsPane.setVisible(false);
		SearchResultsPane.setMouseTransparent(true);
		this.resetSearchText();

		

	}

	@FXML
	/**
	 * Stops the current song and plays the previous song in the playlist
	 * @param event  the previous button is clicked
	 */
	public void OnPreviousClicked (MouseEvent event) {


		//updates current song index then plays
		if(playlistNum > 0) {
			playlistNum--;
			playSelectedSong();
		}


	}

	@FXML
	/**
	 * This is a listener for the my songs button. If the user library is not already displaying the users saved songs, it will display the saved songs
	 * @param event		The my songs button has been clicked
	 */
	public void OnMySongsClicked (MouseEvent event) {

		// display the table view with all saved songs of the user
		if (mySongs != null) {
			displaySongs(mySongs);
		}

		//ensure mySongs button cannot be deselected
		mySongsButton.setSelected(true);

		// make search results invisible
		SearchResultsPane.setVisible(false);
		SearchResultsPane.setMouseTransparent(true);
		this.resetSearchText();
		userLibrarySearchBar.setText("");
		userLibrarySearchBar.setPromptText("search my songs");
	}

	@FXML
	/**
	 * This is a listener for the my playlists button. If the user library is not already displaying the users playlists, it will display the users playlists
	 * @param event		the my playlists button has been clicked
	 */
	public void OnMyPlaylistsClicked (MouseEvent event) {
		// initalized table view with all the playlists the user has
		if (user.getPlaylists() != null) {
			displayPlaylists(user.getPlaylists());
		}

		//ensure myPlaylists button cannot be deselected
		myPlaylistsButton.setSelected(true);

		// make search results invisible
		SearchResultsPane.setVisible(false);
		SearchResultsPane.setMouseTransparent(true);
		this.resetSearchText();
		userLibrarySearchBar.setText("");
		userLibrarySearchBar.setPromptText("search my playlists");
	}

	@FXML 
	/**
	 * This is a listener for the current playlist button. If the user library is not already displaying the current playlist, it will display the current playlist
	 * @param event		the current playlist button has been clicked
	 */
	public void OnCurrentPlaylistClicked(MouseEvent event) {
		currentPlaylist=user.getPlaylist(currentPlaylist.getPlaylistName());
		if(!currentPlaylist.getSongs().isEmpty())
		{
			//display songs from current playlist
			displaySongs(currentPlaylist);
		}

		//ensure myPlaylists button cannot be deselected
		currentPlaylistButton.setSelected(true);

		// make search results invisible
		SearchResultsPane.setVisible(false);
		SearchResultsPane.setMouseTransparent(true);
		userLibrarySearchBar.setText("");
		userLibrarySearchBar.setPromptText("search " + currentPlaylist.getPlaylistName());
	}

	@FXML
	/**
	 * This is a listener for the empty space in the window. If it is clicked, the search bar results will be hidden
	 * @param event		the empty space in the song view has been clicked
	 */
	public void OnSongViewClicked (MouseEvent event) {
		// make search results invisible
		SearchResultsPane.setVisible(false);
		SearchResultsPane.setMouseTransparent(true);

		//removes focus from searchbar 
		SongViewPane.requestFocus();

		AllSongsList.getItems().clear();
	}


	@FXML
	/**
	 * This is a listener for the all songs results. If a song is left-clicked, the song will play. If a song is right-clicked, another menu appears to add the song to a playlist or saved songs
	 * @param event		the table showing search results has been clicked
	 */
	public void OnAllSongsListClicked(MouseEvent event)
	{
		//user left clicks search result
		try {
			Song sel = (Song) AllSongsList.getSelectionModel().getSelectedItem();
			//user left clicks on library list
			if(event.getButton() == MouseButton.PRIMARY) {
				Playlist allSongsPlaylist = new Playlist("all songs", allSongs);
				for(int i=0; i<allSongs.size();i++) {
					if(allSongs.get(i).getTitle()!=null) {
						//check if the selected list item is equal to the current songs title
						if(allSongs.get(i).equals(sel)) {
							currentPlaylist=allSongsPlaylist;
							playlistNum=i;
							playSelectedSong();
							break;
						}
					}
				}

				// make search results invisible
				this.resetSearchText();
			}
			//user right clicks search result
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
											//check if the selected list item is equal to the current songs title TODO make it work
											if(allSongs.get(j).equals(sel)) {
												Playlist tp = playlists.get(k);
												ArrayList<Playlist> updatedPlaylist;
												try {
													updatedPlaylist = addSongToServer(allSongs.get(j), tp);
													user.setPlaylists(updatedPlaylist);
													if(((ToggleButton)menuToggleGroup.getSelectedToggle()).equals(currentPlaylistButton)) {
														OnCurrentPlaylistClicked(null);
													}
													else if(((ToggleButton)menuToggleGroup.getSelectedToggle()).equals(myPlaylistsButton)) {
														OnMyPlaylistsClicked(null);
													}
													break;
												} catch (SocketException e) {
													// TODO Auto-generated catch block
													e.printStackTrace();
												}
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
								if(allSongs.get(j).equals(sel)) {
									ArrayList<Song> myActualSongs=mySongs.getSongs();
									int sameTitle=0;
									for(int z=0;z<myActualSongs.size();z++) {
										if(myActualSongs.get(z).equals(sel))
										{
											sameTitle++;
										}
									}
									if(sameTitle==0) {
										Playlist temp = new Playlist("saved");
										temp.addSong(allSongs.get(j));
										ArrayList<Playlist> updatedSavedSongs;
										try {
											updatedSavedSongs = addSongToServer(allSongs.get(j), temp);
											mySongs.setSongs(updatedSavedSongs.get(0).getSongs());
											user.setSavedSongs(mySongs);
											mySongs = user.getSavedSongs();

											if(((ToggleButton)menuToggleGroup.getSelectedToggle()).equals(currentPlaylistButton)) {
												OnCurrentPlaylistClicked(null);
											}
											else if(((ToggleButton)menuToggleGroup.getSelectedToggle()).equals(mySongsButton)) {
												OnMySongsClicked(null);
											}
										} catch (SocketException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}


									}
									else {
										DisplayAlert("Save Song Error", "Song Already in Saved Songs", "Please try a different option");
	
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


	/**
	 * action when the user left clicks on the UserLibraryList while My Songs is selected. Only action is
	 * playing the song that is selected
	 * @param event - the left mouse button is clicked
	 */
	public void ltMouseClickMySongs(MouseEvent event){

		//TableView experiment
		Song selectedSong = (Song) UserLibraryList.getSelectionModel().getSelectedItem();
		Playlist mySongs=user.getSavedSongs();
		ArrayList<Song> savedSongs=mySongs.getSongs();

		for(int i=0; i<savedSongs.size();i++) {
			if(savedSongs.get(i).getTitle()!=null) {
				//check if the selected list item is equal to the current songs title
				if(savedSongs.get(i).equals(selectedSong)) {
					currentPlaylist=mySongs;
					playlistNum=i;
					playSelectedSong();
					break;
				}
			}
		}
	}

	/**
	 * action when the left mouse button is clicked while my playlists is selected. Either play music from the playlist,
	 * or do nothing if the playlist doesnt have any songs
	 * @param event - the left mouse is clicked
	 */
	public void ltMouseClickMyPlaylists(MouseEvent event) {
		//TableView experiment
		Playlist selectedPlaylist = (Playlist) UserLibraryList.getSelectionModel().getSelectedItem();
		ArrayList<Playlist> playlists=user.getPlaylists();
		for(int i=0; i<playlists.size();i++) {
			//make sure playlist is not null and it has a song in it
			if(playlists.get(i).getPlaylistName()!=null && playlists.get(i).getSongs().size() > 0) {
				//check if the selected list item is equal to the current playlist
				if(playlists.get(i).equals(selectedPlaylist)) {
					currentPlaylist=selectedPlaylist;
					playlistNum=0;
					currentPlaylistButton.setSelected(true);
					OnCurrentPlaylistClicked(null);
					break;
				}
			}
		}
	}

	/**
	 * action that occurs after the left mouse is clicked on the UserLibraryList while the current playlist is selected. Only action
	 * is to play songs
	 * @param event - the left mouse is clicked
	 */
	public void ltMouseClickCurrentPlayList(MouseEvent event) {
		Song sel = (Song) UserLibraryList.getSelectionModel().getSelectedItem();
		ArrayList<Song> songs = currentPlaylist.getSongs();
		for (int i = 0; i< songs.size(); i++) {
			if (songs.get(i).equals(sel)) {
				playlistNum = i;
				playSelectedSong();
				break;
			}
		}
	}

	/**
	 * action that occurs when the right mouse is clicked on user library list while my songs is selected.
	 * menu pops up to either add the song to a playlist or remove the song from saved list
	 * @param event - right mouse button is clicked
	 */
	public void rtMouseClickMySongs(MouseEvent event) {
		//the selected item
		Song sel = (Song) UserLibraryList.getSelectionModel().getSelectedItem();
		if(!sel.getTitle().equals(null)) {
			//popup menu to appear on right click
			ContextMenu cm = new ContextMenu();
			//menu with the names of all the playlists in it
			Menu parentMenu = new Menu("Add To Playlist");
			ArrayList<Playlist> playlists=user.getPlaylists();
			// creates selections for all the playlists
			for (int i = 0; i<playlists.size(); i++) {
				MenuItem temp = new MenuItem(playlists.get(i).getPlaylistName());
				// when the playlist name is selected
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
										if(savedSongs.get(j).equals(sel)) {
											// add song to playlist											
											Playlist tp = playlists.get(k);
											try {
												ArrayList<Playlist> updatedPlaylist = addSongToServer(sel, tp);												
												user.setPlaylists(updatedPlaylist);
											} catch (SocketException e) {

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
			//option to remove song
			MenuItem removeSavedSong = new MenuItem("Remove Saved Song");
			removeSavedSong.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					Playlist temp=user.getSavedSongs();
					//cant remove a song from saved songs if there is only one song
					if(temp.getSongs().size()==1) {
						DisplayAlert("Remove Saved Song Error","Saved Songs Must Have At Least One Song", "Please try a different option"); 
					}
					else {
						Playlist tp = new Playlist("saved");
						tp.setSongs(user.getSavedSongs().getSongs());
						//remove song from saved songs
						ArrayList<Playlist> updatedPlaylist = removeSongFromServer(sel, tp);
						//TODO
						user.setSavedSongs(updatedPlaylist.get(0));
						currentPlaylist=user.getSavedSongs();
						mySongs = user.getSavedSongs();
						OnMySongsClicked(null);
					}
				}
			});
			//add options to list and show
			cm.getItems().add(parentMenu);
			cm.getItems().add(removeSavedSong);
			cm.show(UserLibraryList.getScene().getWindow(), event.getScreenX(), event.getScreenY());
		}
	}

	/**
	 * action that occurs when the right mouse is clicked on the UserLibraryList while current playlist is selected.
	 * Only functionality is to remove songs
	 * @param event - user right mouse clicks
	 */
	public void rtMouseClickCurrentPlaylist(MouseEvent event) {
		//selected object
		Song sel = (Song) UserLibraryList.getSelectionModel().getSelectedItem();
		ContextMenu cm = new ContextMenu();
		//option to remove song from current playlist
		MenuItem remove = new MenuItem("Remove Song");
		remove.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				try {
					ArrayList<Playlist> playlists=user.getPlaylists();
					if(currentPlaylist.getPlaylistName().equals("saved"))
					{
						Playlist temp=user.getSavedSongs();
						//cant delete song if there is only one song in the playlist
						if(temp.getSongs().size()==1) {
							DisplayAlert("Remove Saved Song Error", "Saved Songs Must Have At Least One Song", "Please try a different option");
						}
						else {
							//remove song from saved songs
							temp.removeSong(sel);
							user.setSavedSongs(temp);
							UserRepository.UpdateUser(user);
							OnCurrentPlaylistClicked(null);
						}
					}
					else {//current playlist is a playlist, not saved songs
						for(int n=0;n<playlists.size();n++) {
							if(currentPlaylist.getPlaylistName().equals(playlists.get(n).getPlaylistName())) {
								Playlist tempo=playlists.get(n);
								//cant remove last song from playlist
								if(tempo.getSongs().size()==1) {
									DisplayAlert("Remove Playlist Song Error", "Playlist Must Have At Least One Song", "Please try a different option");
								}
								else {
									// remove song from current playlist
									tempo.removeSong(sel);
									playlists.set(n, tempo);
									currentPlaylist=playlists.get(n);
									UserRepository.UpdateUser(user);
									OnCurrentPlaylistClicked(null);
									break;
								}
							}
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		cm.getItems().add(remove);
		cm.show(UserLibraryList.getScene().getWindow(), event.getScreenX(), event.getScreenY());
	}

	/**
	 * action that occurs after the user right clicks on the userlibrarylist while my playlists are selected
	 * @param event
	 */
	public void rtMouseClickMyPlaylists(MouseEvent event) {
		Playlist sel = (Playlist) UserLibraryList.getSelectionModel().getSelectedItem();
		ContextMenu cm = new ContextMenu();
		//option to create a new playlist
		MenuItem createP = new MenuItem("Create New Playlist");
		//option to remove selected playlist
		MenuItem removeP = new MenuItem("Remove Playlist");

		removeP.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				ArrayList<Playlist> playlists=user.getPlaylists();
				if(!sel.getPlaylistName().equals("My Playlist")){
					playlists = removePlaylist(sel);
					//set current playlist to saved 
					if (sel.getPlaylistName().equals(currentPlaylist.getPlaylistName())) {
						currentPlaylist = user.getSavedSongs();
					}

					ArrayList<Playlist> updatedPlaylist = removePlaylist(sel);												
					user.setPlaylists(updatedPlaylist);
					OnMyPlaylistsClicked(null);

				}
				else {//My playlist has to exist					
					DisplayAlert("Error", "Cannot delete My Playlist", "Please try a different option"); 
				}
			}
		});

		createP.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				//user enters playlist name
				TextInputDialog dialog = new TextInputDialog("");
				dialog.setTitle("New Playlist Dialog");
				dialog.setHeaderText("New Playlist");
				dialog.setContentText("Enter Playlist Name:");

				try {
					ArrayList<Playlist> playlists=user.getPlaylists();
					//keep on prompting user for name until it is unique or has non whitespace characters
					while(true) {
						Optional<String> result = dialog.showAndWait();
						int count=0;
						if(result.isPresent()) {
							for(int i=0;i<playlists.size();i++) {
								if(playlists.get(i).getPlaylistName().equals(result.get())) {
									count++;
								}
							}
							if(count!=0) {// input isnt unique								
								DisplayAlert("Error adding playlist", "There is already a playlist with that name", "Please try a different name");
							}
							else if(result.get().trim().length() == 0) { //only whitespace
								DisplayAlert("Error adding playlist", "Enter Characters that are not blank space", "Please try a different name");
							}
							else{//add playlist
								Playlist p = new Playlist(result.get());
								p.setCurrentDate();
								playlists.add(p);
								user.setPlaylists(playlists);
								UserRepository.UpdateUser(user);
								OnMyPlaylistsClicked(null);
								break;
							}
						}
						else {
							break;
						}

					}
				} catch (Exception e) {

				}
			}
		});
		cm.getItems().add(removeP);
		cm.getItems().add(createP);
		cm.show(UserLibraryList.getScene().getWindow(), event.getScreenX(), event.getScreenY());
	}

	@FXML
	/**
	 * decides what to do based on what is selected when a user clicks the mouse on the userlibrarylist
	 * @param event-user clicks mouse
	 */
	public void OnLibraryListClicked(MouseEvent event) {
		// make search results for main search bar invisible
		SearchResultsPane.setVisible(false);
		SearchResultsPane.setMouseTransparent(true);
		this.resetSearchText();
		//item on the list view that the user selects

		try {
			//user left clicks on library list
			if(event.getButton() == MouseButton.PRIMARY) {
				//if the saved songs button is selected, find the selected song to play
				if(((ToggleButton)menuToggleGroup.getSelectedToggle()).equals(mySongsButton)) {
					ltMouseClickMySongs(event);
				}
				//my playlists are selected
				else if(((ToggleButton)menuToggleGroup.getSelectedToggle()).equals(myPlaylistsButton)){
					ltMouseClickMyPlaylists(event);
				}
				else if (((ToggleButton)menuToggleGroup.getSelectedToggle()).equals(currentPlaylistButton)) {
					ltMouseClickCurrentPlayList(event);
				}
			}
			//user right clicks library list
			else if(event.getButton() == MouseButton.SECONDARY) {
				if(((ToggleButton)menuToggleGroup.getSelectedToggle()).equals(mySongsButton)) {
					rtMouseClickMySongs(event);
				}
				else if(((ToggleButton)menuToggleGroup.getSelectedToggle()).equals(currentPlaylistButton)) {
					rtMouseClickCurrentPlaylist(event);
				}
				else if(((ToggleButton)menuToggleGroup.getSelectedToggle()).equals(myPlaylistsButton)) {
					rtMouseClickMyPlaylists(event);
				}
			}
		} catch (Exception e) 
		{
			//listview item that was clicked is	 blank
		}
	}

	@FXML
	/**
	 * This is a listener for the song slider. When dragged, the slider updates the position in the song to the new location of the slider
	 * @param event		the song slider has been clicked
	 */
	public void OnSliderClicked(MouseEvent event)
	{
		System.out.println("Click detected.");
		/* if it was not dragged but simple clicked to a new position,
		 * make sure to stop the song.
		 */
		if (player !=null && player.getStatus().equals(Status.PLAYING)) {
			player.stop();
		}
		_currentTime = Duration.millis(_slider.getValue());

		//play the song from this new position
		if (_playButton.getText().equals("Pause")) {
			playSong(_currentTime);
		} else {
			currentTime.setText((getTime(_currentTime)));
		}

	}

	@FXML
	/**
	 * Event handler when a drag is detected on the slider
	 * @param event
	 */
	public void OnSliderDragDetected(MouseEvent event) {

		System.out.println("Drag detected.");
		//if a drag is detected, stop the song.
		if (player !=null && player.getStatus().equals(Status.PLAYING)) {
			player.stop();
		}

		/* the dropping of a drag will result in a click on the slider, 
		 * so that event handler will deal with playing the song from 
		 * the new position.
		 * 
		 * Total hack, I know.
		 */
	}


	@FXML
	/**
	 * This displays search results in the all songs table when there is text in the all songs search bar
	 */
	public void searchAllSongs() throws IOException
	{
		Playlist validSongs = new Playlist("valid");

		String query = AllSongsSearchBar.getText();
		DatagramSocket socket;
		try {
			socket = new DatagramSocket();
			socket.setSoTimeout(TIMEOUT);
			String[] arr= {user.getUsername(),query};
			Message searchMessage;
			OpID opID=OpID.SEARCHALLSONGS;
			try {
				searchMessage = new Message(1, requestID++, opID, arr, InetAddress.getLocalHost(),1);
				String json = gson.toJson(searchMessage);
				byte[] msg = gson.toJson(searchMessage).getBytes();
				DatagramPacket request = new DatagramPacket(msg, msg.length, searchMessage.getAddress() , 1234);
				try {
					socket.send(request);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("request port: " + request.getPort());
				byte[] buffer = new byte[5000];
				DatagramPacket reply = new DatagramPacket(buffer, buffer.length);

				//keep sending request until server responds
				for (int i = 0; i < TRIES; i++) {
					System.out.println("Awaiting response from server...");
					try {
						socket.receive(reply);		
						System.out.println("Response received from port " + reply.getPort() + "!");
						break;
					} catch (SocketTimeoutException e) {
						if (i == TRIES - 1) {
							DisplayAlert("Server connection error", "Client was unable to connect to server", "Please try again later");
							return;
						}
						System.out.println("No response from server, sending request again.");
						socket.send(request);
					}
				}
				System.out.println("Response received!");

				//System.out.println(gson.fromJson(new String(buffer).trim(), String.class));
				Song[] temp=gson.fromJson(new String(buffer).trim(), Song[].class);
				if(temp.length>0) {
					for(int i=0;i<temp.length;i++) {
						System.out.println(temp[i]);
						validSongs.addSong(temp[i]);
					}
					displaySongs(validSongs);
				}

			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}

	/**
	 * searches mysongs and displays on userlibrarylist
	 * @param query - user inputed query
	 */
	public void searchMySongs(String query,OpID opID,DatagramSocket socket) throws IOException {
		String[] arr= {user.getUsername(),query};
		Message searchMessage;
		try {
			searchMessage = new Message(1, requestID++, opID, arr, InetAddress.getLocalHost(),1);
			String json = gson.toJson(searchMessage);
			byte[] msg = gson.toJson(searchMessage).getBytes();
			DatagramPacket request = new DatagramPacket(msg, msg.length, searchMessage.getAddress() , 1234);
			try {
				socket.send(request);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("request port: " + request.getPort());
			byte[] buffer = new byte[1000];
			DatagramPacket reply = new DatagramPacket(buffer, buffer.length);

			//keep sending request until server responds
			for (int i = 0; i < TRIES; i++) {
				System.out.println("Awaiting response from server...");
				try {
					socket.receive(reply);		
					System.out.println("Response received from port " + reply.getPort() + "!");
					break;
				} catch (SocketTimeoutException e) {
					if (i == TRIES - 1) {
						DisplayAlert("Server connection error", "Client was unable to connect to server", "Please try again later");
						return;
					}
					System.out.println("No response from server, sending request again.");
					socket.send(request);
				}
			}
			System.out.println("Response received!");

			//System.out.println(gson.fromJson(new String(buffer).trim(), String.class));
			Song[] temp=gson.fromJson(new String(buffer).trim(), Song[].class);
			if(temp.length>0) {
				for(int i=0;i<temp.length;i++) {
					UserLibraryList.getItems().addAll(temp[i]);
				}
			}
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * searches and displays user myplaylists on userlibrarylist
	 * @param query-user inputted query
	 */
	public void searchMyPlaylists(String query,OpID opID, DatagramSocket socket) throws IOException {
		if(query=="") {
			query=" ";
		}
		String[] arr= {user.getUsername(),query,currentPlaylist.getPlaylistName()};
		Message searchMessage;
		try {
			searchMessage = new Message(1, requestID++, opID, arr, InetAddress.getLocalHost(),1);
			String json = gson.toJson(searchMessage);
			byte[] msg = gson.toJson(searchMessage).getBytes();
			DatagramPacket request = new DatagramPacket(msg, msg.length, searchMessage.getAddress() , 1234);
			try {
				socket.send(request);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("request port: " + request.getPort());

			byte[] buffer = new byte[5000];
			DatagramPacket reply = new DatagramPacket(buffer, buffer.length);

			//keep sending request until server responds
			for (int i = 0; i < TRIES; i++) {
				System.out.println("Awaiting response from server...");
				try {
					socket.receive(reply);		
					System.out.println("Response received from port " + reply.getPort() + "!");
					break;
				} catch (SocketTimeoutException e) {
					if (i == TRIES - 1) {
						DisplayAlert("Server connection error", "Client was unable to connect to server", "Please try again later");
						return;
					}
					System.out.println("No response from server, sending request again.");
					socket.send(request);
				}
			}
			System.out.println("Response received!");

			Playlist[] temp=gson.fromJson(new String(buffer).trim(), Playlist[].class);
			if(temp.length>0) {
				for(int i=0;i<temp.length;i++) {
					ArrayList<Playlist> arrayList = new ArrayList<Playlist>(Arrays.asList(temp));
					displayPlaylists(arrayList);
				}
			}
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * searches current playlists and displays on userlibrarylist
	 * @param query
	 */
	public void searchCurrentPlaylist(String query,OpID opID,DatagramSocket socket) throws IOException {
		String[] arr= {user.getUsername(),query,currentPlaylist.getPlaylistName()};
		Message searchMessage;
		try {
			searchMessage = new Message(1, requestID++, opID, arr, InetAddress.getLocalHost(),1);
			String json = gson.toJson(searchMessage);
			byte[] msg = gson.toJson(searchMessage).getBytes();
			DatagramPacket request = new DatagramPacket(msg, msg.length, searchMessage.getAddress() , 1234);
			try {
				socket.send(request);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("request port: " + request.getPort());
			//InetAddress host = InetAddress.getLocalHost();

			//int serverPort = 6789;
			//DatagramPacket request = new DatagramPacket(m, m.length, host, serverPort);
			//socket.send(request);
			//System.out.println("Request: " + new String(request.getData()));
			byte[] buffer = new byte[1000];
			DatagramPacket reply = new DatagramPacket(buffer, buffer.length);

			//keep sending request until server responds
			for (int i = 0; i < TRIES; i++) {
				System.out.println("Awaiting response from server...");
				try {
					socket.receive(reply);		
					System.out.println("Response received from port " + reply.getPort() + "!");
					break;
				} catch (SocketTimeoutException e) {
					if (i == TRIES - 1) {
						DisplayAlert("Server connection error", "Client was unable to connect to server", "Please try again later");
						return;
					}
					System.out.println("No response from server, sending request again.");
					socket.send(request);
				}
			}
			System.out.println("Response received!");

			//System.out.println(gson.fromJson(new String(buffer).trim(), String.class));
			Song[] temp=gson.fromJson(new String(buffer).trim(), Song[].class);
			if(temp.length>0) {
				for(int i=0;i<temp.length;i++) {
					UserLibraryList.getItems().addAll(temp[i]);
				}
			}
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@FXML
	/**
	 * search for user library. decides what to display on userlibrarylist
	 */
	public void search() {
		try {			
			UserLibraryList.getItems().clear();
			//user inputted text
			String query=userLibrarySearchBar.getText();

			DatagramSocket socket = null;
			try {
				if(query.equals("")) {
					query=" ";
				}
				socket = new DatagramSocket();
				socket.setSoTimeout(TIMEOUT);
				//String user="amyer";
				OpID opID=OpID.SEARCHMYSONGS;
				//my songs are selected
				String req="";
				if(((ToggleButton)menuToggleGroup.getSelectedToggle()).equals(mySongsButton)){
					opID=OpID.SEARCHMYSONGS;
					searchMySongs(query,opID, socket);

				}
				//my playlists are selected
				else if(((ToggleButton)menuToggleGroup.getSelectedToggle()).equals(myPlaylistsButton)) {
					opID=OpID.SEARCHMYPLAYLISTS;
					searchMyPlaylists(query,opID, socket);

				}
				//current playlists are selected
				else if (((ToggleButton)menuToggleGroup.getSelectedToggle()).equals(currentPlaylistButton)) {
					opID=OpID.SEARCHCURRENTPLAYLIST;
					searchCurrentPlaylist(query,opID, socket);
				}


			} catch (SocketException e) {
				System.out.println("Socket: " + e.getMessage());
			} catch (IOException e) {
				System.out.println("IO: " + e.getMessage());
			}finally {
				if(socket!=null)
					socket.close();
			}

		}catch (Exception e) {
			e.printStackTrace();
		}		
	}


	/**
	 * Plays the current song at the given time.
	 * @param time Time in microseconds at which the song should start
	 */
	public void playSong(Duration time) {
		updateSongLabels(currentPlaylist.getSongs().get(playlistNum));

		try {
			DatagramSocket socket = new DatagramSocket();

			Song = new Media(cachedSong.toURI().toString());
			player = new MediaPlayer(Song);

			player.setOnReady(new Runnable() {

				@Override
				public void run() {

					//seek to this time in song
					player.seek(time);

					//play song
					player.play();

					//start background thread to update UI with song
					_thread = new Thread(UIUpdateThread);
					_thread.setDaemon(true); //allows thread to end on exit
					_thread.start();

					//update song UI
					totalTime.setText(getTime(player.getTotalDuration()));
					currentTime.setText(getTime(time));
					_slider.setMin(0);
					_slider.setMax(player.getTotalDuration().toMillis());
				}
			});

			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}

	/***
	 * Formats microseconds into a string that is in HH:MM:SS format.
	 * @param duration	amount of microseconds
	 * @return Returns properly formatted string.
	 */
	public String getTime(Duration duration) {
		//convert to seconds
		double s = duration.toMillis()/1000;
		return String.format("%d:%02d:%02d", (int)(s/3600), (int)((s%3600)/60), (int)(s%60));
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
	/**
	 * This initializes columns and toggle button groups. It also adds all songs the the allSongs list
	 * @param arg0 unused but needed to override the initialize function
	 * @param arg1 unused but needed to override the initialize function
	 */
	public void initialize(URL arg0, ResourceBundle arg1) {
		gson = new Gson();	
		menuToggleGroup = new ToggleGroup();

		//buttons to choose what is displayed in the user library
		mySongsButton.setToggleGroup(menuToggleGroup);
		myPlaylistsButton.setToggleGroup(menuToggleGroup);
		currentPlaylistButton.setToggleGroup(menuToggleGroup);

		mySongsButton.setSelected(true);
		userLibrarySearchBar.setPromptText("search my songs");

		// makes sure the columns take up the entire width of the table
		UserLibraryList.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		AllSongsList.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

		// title column for song view
		titleColumn = new TableColumn("Title");
		titleColumn.setCellValueFactory(new PropertyValueFactory<Song, String>("title"));

		// artist column for song view
		artistColumn = new TableColumn("Artist");
		artistColumn.setCellValueFactory(new PropertyValueFactory<Song, String>("artist"));

		// album column for song view
		albumColumn = new TableColumn("Album");
		albumColumn.setCellValueFactory(new PropertyValueFactory<Song, String>("album"));

		// name table column for playlist view
		playlistNameColumn = new TableColumn("Playlist Name");
		playlistNameColumn.setCellValueFactory(new PropertyValueFactory<Playlist, String>("playlistName"));

		// date created table column for playlist view
		dateCreatedColumn = new TableColumn("Date Created");
		dateCreatedColumn.setCellValueFactory(new PropertyValueFactory<Playlist, String>("dateCreated"));


		// title column for all songs view
		allSongsTitleColumn = new TableColumn("Title");
		allSongsTitleColumn.setCellValueFactory(new PropertyValueFactory<Song, String>("title"));

		// artist column for all songs view
		allSongsArtistColumn = new TableColumn("Artist");
		allSongsArtistColumn.setCellValueFactory(new PropertyValueFactory<Song, String>("artist"));

		// album column for all songs view
		allSongsAlbumColumn = new TableColumn("Album");
		allSongsAlbumColumn.setCellValueFactory(new PropertyValueFactory<Song, String>("album"));

		UserLibraryList.getColumns().addAll(titleColumn, artistColumn, albumColumn);
		AllSongsList.getColumns().addAll(allSongsTitleColumn, allSongsArtistColumn, allSongsAlbumColumn);

		try 
		{
			allSongs = UserRepository.getAllSongs();
			allSongs.sort(null);
		} 
		catch (IOException e) {
			e.printStackTrace();
		}

		//make listview automatically invisible until the search bar is selected
		SearchResultsPane.setVisible(false);
		SearchResultsPane.setMouseTransparent(true);
		this.resetSearchText();
	}

	/**
	 * This clears the text in the all songs search bar and resets the prompt text
	 */
	public void resetSearchText()
	{
		// reset prompt text
		if(AllSongsSearchBar.getText().equals(""))
		{
			AllSongsSearchBar.setPromptText("search all songs");
		}
	}
	
	/**
	 * Adds a song to a given playlist using a server
	 * @param songToAdd			song to be added
	 * @param playlistToUpdate  playlist to add the song to
	 * @return					updated playlist that includes the given song
	 * @throws SocketException	the socket is invalid
	 */
	public ArrayList<Playlist> addSongToServer(Song songToAdd, Playlist playlistToUpdate) throws SocketException 
	{		
		DatagramSocket socket = new DatagramSocket();
		socket.setSoTimeout(TIMEOUT);
		//initialize buffer
		byte[] buffer = new byte[5000];
		try {
			String songJSON = gson.toJson(songToAdd);
			String playlistJSON = gson.toJson(playlistToUpdate);

			String[] arr = {user.getUsername(), songJSON, playlistJSON};

			Message addSongMessage = new Message(1, requestID++, OpID.ADDSONGTOPLAYLIST, arr, InetAddress.getLocalHost(), 1);

			//convert to json
			String json = gson.toJson(addSongMessage);

			//we can only send bytes, so flatten the string to a byte array
			byte[] msg = gson.toJson(addSongMessage).getBytes();				

			System.out.println("Sending request.");
			//initialize and send request packet using port 1234, the port the server is listening on
			DatagramPacket request = new DatagramPacket(msg, msg.length, addSongMessage.getAddress() , 1234);
			System.out.println("request port: " + request.getPort());
			socket.send(request);
			//initialize reply from server and receive it

			/* without specifying a port in this datagram packet, the OS will
			 * randomly assign a port to the reply for the program to listen on
			 */
			DatagramPacket reply = new DatagramPacket(buffer, buffer.length);

			//keep sending request until server responds
			for (int i = 0; i < TRIES; i++) {
				System.out.println("Awaiting response from server...");
				try {
					socket.receive(reply);		
					System.out.println("Response received from port " + reply.getPort() + "!");
					break;
				} catch (SocketTimeoutException e) {
					if (i == TRIES - 1) {
						
						DisplayAlert("Server connection error", "Client was unable to connect to server", "Please try again later");
						
						//return unupdated list of playlists
						return user.getPlaylists();
					}
					System.out.println("No response from server, sending request again.");
					socket.send(request);
				}
			}		
			System.out.println(new String(buffer));
			Playlist[] updatedPlaylists = gson.fromJson(new String(buffer).trim(), Playlist[].class);

			socket.close();
			//return updated set of playlists
			return new ArrayList<Playlist>(Arrays.asList(updatedPlaylists));
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		socket.close();
		return null;
	}
	
	/**
	 * adds a playlist to the users list of playlists using a server
	 * @param playlist		playlist to be added
	 * @return				updated list of playlists including the given playlist
	 */
	public ArrayList<Playlist> addPlaylist(Playlist playlist){

		try {
			DatagramSocket socket;
			socket = new DatagramSocket();
			socket.setSoTimeout(TIMEOUT);
			byte[] buffer = new byte[5000];
			String playlistJSON = gson.toJson(playlist);
			String[] arr = {user.getUsername(), playlistJSON};
			Message addSongMessage = new Message(1, requestID++, OpID.ADDPLAYLIST, arr, InetAddress.getLocalHost(), 1);
			//convert to json
			String json = gson.toJson(addSongMessage);
			//we can only send bytes, so flatten the string to a byte array
			byte[] msg = gson.toJson(addSongMessage).getBytes();				
			System.out.println("Sending request.");
			DatagramPacket request = new DatagramPacket(msg, msg.length, addSongMessage.getAddress() , 1234);
			System.out.println(socket == null);
			socket.send(request);
			System.out.println("request port: " + request.getPort());

			//initialize reply from server and receive it

			/* without specifying a port in this datagram packet, the OS will
			 * randomly assign a port to the reply for the program to listen on
			 */
			DatagramPacket reply = new DatagramPacket(buffer, buffer.length);

			//keep sending request until server responds
			for (int i = 0; i < TRIES; i++) {
				System.out.println("Awaiting response from server...");
				try {
					socket.receive(reply);		
					System.out.println("Response received from port " + reply.getPort() + "!");
					break;
				} catch (SocketTimeoutException e) {
					if (i == TRIES - 1) {
						DisplayAlert("Server connection error", "Client was unable to connect to server", "Please try again later");
						
						//return unupdated list of playlists
						return user.getPlaylists();
					}
					System.out.println("No response from server, sending request again.");
					socket.send(request);
				}
			}
			Playlist[] updatedPlaylists = gson.fromJson(new String(buffer).trim(), Playlist[].class);
			socket.close();
			return new ArrayList<Playlist>(Arrays.asList(updatedPlaylists));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Communicates with a server to remove a playlist from a users list of playlists
	 * @param playlist		playlist to be removed
	 * @return				updated list of playlists that no longer contains the given playlist
	 */
	public ArrayList<Playlist> removePlaylist(Playlist playlist) {
		try {
			DatagramSocket socket;
			socket = new DatagramSocket();
			socket.setSoTimeout(TIMEOUT);
			byte[] buffer = new byte[5000];
			String playlistJSON = gson.toJson(playlist);
			String[] arr = {user.getUsername(), playlistJSON};
			Message removePlaylistMessage = new Message(1, requestID++, OpID.DELETEPLAYLIST, arr, InetAddress.getLocalHost(), 1);

			//convert to json
			String json = gson.toJson(removePlaylistMessage);

			//we can only send bytes, so flatten the string to a byte array
			byte[] msg = gson.toJson(removePlaylistMessage).getBytes();				

			System.out.println("Sending request.");
			DatagramPacket request = new DatagramPacket(msg, msg.length, removePlaylistMessage.getAddress() , 1234);
			System.out.println(socket == null);
			socket.send(request);
			System.out.println("request port: " + request.getPort());
			//initialize and send request packet using port 1234, the port the server is listening on
			DatagramPacket reply = new DatagramPacket(buffer, buffer.length);

			//keep sending request until server responds
			for (int i = 0; i < TRIES; i++) {
				System.out.println("Awaiting response from server...");
				try {
					socket.receive(reply);		
					System.out.println("Response received from port " + reply.getPort() + "!");
					break;
				} catch (SocketTimeoutException e) {
					if (i == TRIES - 1) {
						DisplayAlert("Server connection error", "Client was unable to connect to server", "Please try again later");
						
						//return unupdated list of playlists
						return user.getPlaylists();
					}
					System.out.println("No response from server, sending request again.");
					socket.send(request);
				}
			}	
			System.out.println(new String(buffer));
			Playlist[] updatedPlaylists = gson.fromJson(new String(buffer).trim(), Playlist[].class);

			socket.close();
			//return updated set of playlists
			return new ArrayList<Playlist>(Arrays.asList(updatedPlaylists));
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * using client-server protocol, this method removes a song from a playlist using a server
	 * @param songToRemove		song to be removed
	 * @param playlistToUpdate	playlist holding the song that will be removed
	 * @return					arraylist of playlists where the first index is the updated playlist
	 */
	public ArrayList<Playlist> removeSongFromServer(Song songToRemove, Playlist playlistToUpdate)
	{		
		//initialize buffer
		DatagramSocket socket;
		try {
			socket = new DatagramSocket();
			socket.setSoTimeout(TIMEOUT);
			byte[] buffer = new byte[5000];
			try {
				String songJSON = gson.toJson(songToRemove);
				String playlistJSON = gson.toJson(playlistToUpdate);

				String[] arr = {user.getUsername(), songJSON, playlistJSON};

				Message removeSongMessage = new Message(1, requestID++, OpID.DELETESONGFROMPLAYLIST, arr, InetAddress.getLocalHost(), 1);

				//convert to json
				String json = gson.toJson(removeSongMessage);

				//we can only send bytes, so flatten the string to a byte array
				byte[] msg = gson.toJson(removeSongMessage).getBytes();				

				System.out.println("Sending request.");
				//initialize and send request packet using port 1234, the port the server is listening on
				DatagramPacket request = new DatagramPacket(msg, msg.length, removeSongMessage.getAddress() , 1234);
				System.out.println(socket == null);
				socket.send(request);
				System.out.println("request port: " + request.getPort());

				//initialize reply from server and receive it

				/* without specifying a port in this datagram packet, the OS will
				 * randomly assign a port to the reply for the program to listen on
				 */
				DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
				//keep sending reply until server responds
				for (int i = 0; i < TRIES; i++) {
					System.out.println("Awaiting response from server...");
					try {
						socket.receive(reply);		
						System.out.println("Response received from port " + reply.getPort() + "!");
						break;
					} catch (SocketTimeoutException e) {

						if (i == TRIES - 1) {						
							DisplayAlert("Server connection error", "Client was unable to connect to server","Please try again later");
							//return unupdated list of playlists
							return user.getPlaylists();
						}
						System.out.println("No response from server, sending request again.");
						socket.send(request);
					}
				}
				System.out.println(new String(buffer));
				Playlist[] updatedPlaylists = gson.fromJson(new String(buffer).trim(), Playlist[].class);

				socket.close();
				//return updated set of playlists
				return new ArrayList<Playlist>(Arrays.asList(updatedPlaylists));
			} catch (UnknownHostException e1) {
				e1.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			socket.close();
			return null;
		} catch (SocketException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Displays an alert to the user.
	 * 
	 * @param title Alert title.
	 * @param header Alert header.
	 * @param content Alert content.
	 * 
	 */
	public static void DisplayAlert(String title, String header, String content) {
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.setContentText(content);
		alert.showAndWait();
	}
}
