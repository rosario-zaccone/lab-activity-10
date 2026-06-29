package ttt_game_service.application;

import java.util.logging.Logger;

import ttt_game_service.domain.*;
/**
 * 
 * Representing a player session.
 * 
 * - Created when a logged user joins a game.
 * - It includes the operations that a player can do.
 * - It acts as observer of events generated in the game. 
 * 
 */
public class PlayerSession /* implements GameObserver */ {

	static Logger logger = Logger.getLogger("[Player Session]");
	private UserId userId;
	private String gameId;
	private GameRepository gameRepository;
	private GameObserverFactory observerFactory;
	private TTTSymbol symbol;
	private String playerSessionId;
	
	public PlayerSession(String playerSessionId, UserId userId, String gameId, TTTSymbol symbol,
			GameRepository gameRepository, GameObserverFactory observerFactory) {
		this.userId = userId;
		this.gameId = gameId;
		this.symbol = symbol;
		this.playerSessionId = playerSessionId;
		this.gameRepository = gameRepository;
		this.observerFactory = observerFactory;
	}
		
	public void makeMove(int x, int y) throws InvalidMoveException {
		var game = gameRepository.getGame(gameId);
		game.addGameObserver(observerFactory.makeNewGameObserver(gameId));
		game.makeAmove(userId, x, y);
		gameRepository.save(game);
	}
	
	public TTTSymbol getSymbol() {
		return symbol;
	}
	
	public String getGameId() {
		return gameId;
	}
	
	public String getId() {
		return playerSessionId;
	}

	private void log(String msg) {
		System.out.println("[ player " + userId.id() + " in game " + gameId + " ] " + msg);
	}
}
