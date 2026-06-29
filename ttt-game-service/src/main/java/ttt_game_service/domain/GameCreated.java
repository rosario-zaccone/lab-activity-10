package ttt_game_service.domain;

/**
 * Domain event: game created
 */
public record GameCreated(String gameId) implements GameEvent {}
