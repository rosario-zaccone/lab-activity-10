package ttt_game_service.infrastructure;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

import common.exagonal.Adapter;
import ttt_game_service.application.GameRepository;
import ttt_game_service.domain.Game;
import ttt_game_service.domain.GameEvent;

/**
 * 
 * Games Repository
 * 
 */
@Adapter
public class InMemoryGameRepository implements GameRepository {

	private HashMap<String, Game> games;
	private HashMap<String, List<GameEvent>> eventStreams;

	public InMemoryGameRepository() {
		games = new HashMap<>();
		eventStreams = new HashMap<>();
	}
	
	public void addGame(Game game) {
		if (!eventStreams.containsKey(game.getId())) {
			eventStreams.put(game.getId(), new ArrayList<>());
		}
		save(game);
	}

	@Override
	public void save(Game game) {
		var stream = eventStreams.computeIfAbsent(game.getId(), id -> new ArrayList<>());
		stream.addAll(game.getUncommittedEvents());
		game.clearUncommittedEvents();
		games.put(game.getId(), game);
	}
	
	public boolean isPresent(String gameId) {
		return eventStreams.containsKey(gameId);
	}
	
	public Game getGame(String gameId) {
		var stream = eventStreams.get(gameId);
		if (stream == null) {
			return null;
		}
		return Game.rehydrate(gameId, stream);
	}

	@Override
	public int getCurrentNumberOfGames() {
		return games.size();
	}


}
