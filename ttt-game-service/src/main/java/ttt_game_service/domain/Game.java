package ttt_game_service.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import common.ddd.Aggregate;


/**
 * 
 * Modelling a running game.
 * 
 */
public class Game implements Aggregate<String>{
	static Logger logger = Logger.getLogger("[Game]");

	private String id;
	private GameBoard board;

	public enum GameState { WAITING_FOR_PLAYERS, STARTED, FINISHED }
	private GameState state;
	
	private Optional<UserId> playerCross;  /* joined player with cross */
	private Optional<UserId> playerCircle; /* joined player with circle */
	private Optional<UserId> winner;		
	private Optional<UserId> currentTurn;
	
	private List<GameObserver> observers; /* observers of game events */
	private List<GameEvent> uncommittedEvents;
	
	public Game(String id) {
		this(id, true);
	}	
	
	private Game(String id, boolean recordCreation) {
		this.id = id;
		board = new GameBoard(id+"-board");

		playerCross = Optional.empty();
		playerCircle = Optional.empty();
		currentTurn = Optional.empty();		
		winner = Optional.empty();
		state = GameState.WAITING_FOR_PLAYERS;
		observers = new ArrayList<>();
		uncommittedEvents = new ArrayList<>();
		if (recordCreation) {
			recordThat(new GameCreated(id));
		}
	}

	public static Game rehydrate(String id, List<GameEvent> events) {
		var game = new Game(id, false);
		for (var event: events) {
			game.apply(event);
		}
		game.clearUncommittedEvents();
		return game;
	}
	
	public String getId() {
		return id;
	}
		
	/**
	 * 
	 * A player joins a game
	 * 
	 * @param user
	 * @param symbol
	 * @throws InvalidJoinException
	 */
	public void joinGame(UserId userId, TTTSymbol symbol) throws InvalidJoinException {
		if (!state.equals(GameState.WAITING_FOR_PLAYERS) || 
		    (symbol.equals(TTTSymbol.X) && playerCross.isPresent()) ||
			(symbol.equals(TTTSymbol.O) && playerCircle.isPresent())) {
			throw new InvalidJoinException();
		} 
		
		recordThat(new PlayerJoined(id, userId.id(), symbol.toString()));
	}
	
	
	/**
	 * 
	 * Starts the game.
	 * 
	 * The game is started after that both players joined the game.
	 * 
	 */
	public void startGame() {
		logger.info("start game");
		recordThat(new GameStarted(id));
	}
	
	/**
	 * Get the board state
	 * 
	 * @return
	 */
	public List<String> getBoardState(){
		return this.board.getState();
	}
	
	/**
	 * 
	 * Get current turn
	 * 
	 * @return
	 */
	public String getCurrentTurn() {
		if (currentTurn.isPresent() && playerCross.isPresent() &&
				currentTurn.get().id().equals(playerCross.get().id())) {
			return "X";
		} else {
			return "O";
		}
	}

	/**
	 * 
	 * Check if the game has started
	 * 
	 * @return
	 */
	public boolean isStarted() {
		return state.equals(GameState.STARTED);
	}

	/**
	 * 
	 * Check if the game is ended
	 * 
	 * @return
	 */
	public boolean isFinished() {
		return state.equals(GameState.FINISHED);
	}
	
	/**
	 * Get the game state
	 * 
	 * @return
	 */
	public String getGameState() {
		if (state.equals(GameState.WAITING_FOR_PLAYERS)) {
			return "waiting-for-players";
		} else if (state.equals(GameState.STARTED)) {
			return "started";
		} else if (state.equals(GameState.FINISHED)) {
			return "finished";
		} else {
			return "unknown";
		}
	}
	
	/**
	 * 
	 * Checks if the game can start.
	 * 
	 * @return
	 */
	public boolean isReadyToStart() {
		return (playerCross.isPresent() && playerCircle.isPresent());
	}
	
	
	/**
	 * 
	 * A player makes a move
	 * 
	 * @param UserId
	 * @param symbol
	 * @param x
	 * @param y
	 * @throws InvalidMoveException
	 */
	public void makeAmove(UserId userId, int x, int y) throws InvalidMoveException {
		logger.log(Level.INFO, "new move by " + userId.id() + " in (" + x + ", " + y + ")");
		UserId p = currentTurn.get();
		if (userId.id().equals(p.id())) {
			
			var gridSymbol = userId.id().equals(playerCross.get().id()) ?
						TTTSymbol.X : TTTSymbol.O;
			
			recordThat(new NewMove(id, gridSymbol.toString(), x, y));				

			/* check state */ 
			
			var optWin = board.checkWinner();
			if (optWin.isPresent()) {
				var winnerUser = getPlayerUsingSymbol(optWin.get());
				recordThat(new GameEnded(id, Optional.of(winnerUser.id())));				
			} else if (board.isTie()) {
				recordThat(new GameEnded(id, Optional.empty()));				
			}				
		} else {
			throw new InvalidMoveException();			
		}
	}
	
	/**
	 * 
	 * Adding an observer to notify game events
	 * 
	 * @param observer
	 */
	public void addGameObserver(GameObserver observer) {
		observers.add(observer);
	}

	public List<GameEvent> getUncommittedEvents() {
		return List.copyOf(uncommittedEvents);
	}

	public void clearUncommittedEvents() {
		uncommittedEvents.clear();
	}
	
	private void recordThat(GameEvent event) {
		apply(event);
		uncommittedEvents.add(event);
		notifyGameEvent(event);
	}

	private void apply(GameEvent event) {
		if (event instanceof GameCreated) {
			/* Initial state is already set by the constructor. */
		} else if (event instanceof PlayerJoined) {
			apply((PlayerJoined) event);
		} else if (event instanceof GameStarted) {
			apply((GameStarted) event);
		} else if (event instanceof NewMove) {
			apply((NewMove) event);
		} else if (event instanceof GameEnded) {
			apply((GameEnded) event);
		}
	}

	private void apply(PlayerJoined event) {
		var userId = new UserId(event.userId());
		if (event.symbol().equals(TTTSymbol.X.toString())) {
			playerCross = Optional.of(userId);
		} else {
			playerCircle = Optional.of(userId);
		}
	}

	private void apply(GameStarted event) {
		state = GameState.STARTED;
		currentTurn = playerCross;
	}

	private void apply(NewMove event) {
		try {
			var symbol = event.symbol().equals(TTTSymbol.X.toString()) ? TTTSymbol.X : TTTSymbol.O;
			board.newMove(symbol, event.x(), event.y());
			currentTurn = (symbol.equals(TTTSymbol.X)) ? playerCircle : playerCross;
		} catch (InvalidMoveException ex) {
			throw new IllegalStateException("Invalid event stream for game " + id, ex);
		}
	}

	private void apply(GameEnded event) {
		state = GameState.FINISHED;
		if (event.winner().isPresent()) {
			winner = Optional.of(new UserId(event.winner().get()));
		} else {
			winner = Optional.empty();
		}
	}

	private void notifyGameEvent(GameEvent ev) {
		for (var o: observers) {
			logger.info("notify game event " + ev + " to " + o + "...");
			o.notifyGameEvent(ev);				
		}
	}
		
	private UserId getPlayerUsingSymbol(TTTSymbol symbol) {
		if (symbol.equals(TTTSymbol.X)) {
			return playerCross.get();
		} else {
			return playerCircle.get();
		}
	}
	
}
