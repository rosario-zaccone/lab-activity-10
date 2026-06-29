package ttt_game_service.application;

import common.ddd.Repository;
import common.exagonal.OutBoundPort;
import ttt_game_service.domain.Game;

/**
 * 
 * Games Repository
 * 
 */
@OutBoundPort
public interface GameRepository extends Repository {

	void addGame(Game game);

	void save(Game game);
	
	boolean isPresent(String gameId);
	
	Game getGame(String gameId);

	int getCurrentNumberOfGames();
}
