package ttt_game_service.infrastructure;

import java.util.logging.Logger;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import sap.kafka.async.OutputEventChannel;
import ttt_game_service.domain.*;

public class KafkaGameObserver implements GameObserver {

	private OutputEventChannel evChannel;
	static Logger logger = Logger.getLogger("[KafkaGameEventObserver]");
	
	public KafkaGameObserver(Vertx vertx, String channelName, String channelAddress) {
		evChannel = new OutputEventChannel(vertx, channelName, channelAddress);
	}
	
	@Override
	public void notifyGameEvent(GameEvent event) {
		var jsonEv = toJson(event);
		evChannel
			.postEvent(jsonEv)
			.onSuccess(v -> {
				logger.info("game event notified " + jsonEv.encodePrettily());
			});
	}
	
	private JsonObject toJson(GameEvent ev) {
		JsonObject obj = new JsonObject();
		if (ev instanceof GameCreated) {
			obj.put("event", "game-created");
		} else if (ev instanceof PlayerJoined) {
			var evt = (PlayerJoined) ev;
			obj.put("event", "player-joined");
			obj.put("userId", evt.userId());
			obj.put("symbol", evt.symbol());
		} else if (ev instanceof GameStarted) {
			obj.put("event", "game-started");			
		} else if (ev instanceof GameEnded) {
			obj.put("event", "game-ended");
			var winner =  (((GameEnded) ev).winner());
			if (winner.isEmpty()) {
				obj.put("result", "tie");
			} else {
				obj.put("winner", winner.get());
			}
		} if (ev instanceof NewMove) {
			var evt = (NewMove) ev;
			obj.put("event", "new-move");
			obj.put("x", evt.x());
			obj.put("y", evt.y());
			obj.put("symbol", evt.symbol());			
		}
		return obj;
	}

}
