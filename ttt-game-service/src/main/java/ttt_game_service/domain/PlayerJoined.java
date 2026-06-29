package ttt_game_service.domain;

/**
 * Domain event: player joined a game
 */
public record PlayerJoined(String gameId, String userId, String symbol) implements GameEvent {}
