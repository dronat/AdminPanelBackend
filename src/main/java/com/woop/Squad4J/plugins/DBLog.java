package com.woop.Squad4J.plugins;

import com.ibasco.agql.protocols.valve.source.query.info.SourceServer;
import com.woop.Squad4J.connector.MySQLConnector;
import com.woop.Squad4J.event.a2s.A2SUpdatedEvent;
import com.woop.Squad4J.event.logparser.*;
import com.woop.Squad4J.listener.a2s.A2SUpdatedListener;
import com.woop.Squad4J.listener.logparser.*;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Map;

@NoArgsConstructor
public class DBLog implements A2SUpdatedListener, NewGameListener, PlayerDiedListener, PlayerRevivedListener,
        PlayerWoundedListener, RoundWinnerListener, ServerTickRateListener, SteamidConnectedListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(DBLog.class);
    //EntityManager entityManager = new EntityManager();

    @Override
    public void onA2SUpdated(A2SUpdatedEvent a2SUpdatedEvent) {
        //TODO: Remove try-catch after debugging
        try{
            /*A2SInfoResponse info = a2SUpdatedEvent.getResponse().getInfo();
            A2SRulesResponse rules = a2SUpdatedEvent.getResponse().getRules();*/
            SourceServer info = a2SUpdatedEvent.getResponse().getInfo().getResult();
            Map<String, String> rules = a2SUpdatedEvent.getResponse().getRules().getResult();

            String serverName = info.getName();
            Date time = a2SUpdatedEvent.getTime();

            Integer playerCount = Integer.valueOf(rules.get("PlayerCount_i"));
            Integer publicQueue = Integer.valueOf(rules.get("PublicQueue_i"));
            Integer reserveQueue = Integer.valueOf(rules.get("ReservedQueue_i"));
            Integer match = MySQLConnector.getCurrentMatchId();

            MySQLConnector.insertPlayerCount(time, playerCount, publicQueue, reserveQueue, match);
        }catch (Exception e){
            LOGGER.error("Exception in DBLog#onA2SUpdated. ", e);
        }
    }

    @Override
    public void onNewGame(NewGameEvent newGameEvent) {
        String dlc = newGameEvent.getDlc();
        String map = newGameEvent.getMapName();
        String layer = newGameEvent.getLayerName();
        Date startTime = newGameEvent.getTime();

        //TODO: Properly use map and layer classnames
        MySQLConnector.insertMatch(dlc, "null", "null",
                map, layer, startTime);

    }

    @Override
    public void onPlayerDied(PlayerDiedEvent playerDiedEvent) {
        //TODO: Implement
    }

    @Override
    public void onPlayerRevived(PlayerRevivedEvent playerRevivedEvent) {
        //TODO: Implement
    }

    @Override
    public void onPlayerWoundedEvent(PlayerWoundedEvent playerWoundedEvent) {
        //TODO: Implement
    }

    @Override
    public void onRoundWinner(RoundWinnerEvent roundWinnerEvent) {
        //TODO: Implement
    }

    @Override
    public void onServerTickRate(ServerTickRateEvent serverTickRateEvent) {
        //TODO: Remove try-catch after debugging
        try{
            Date time = serverTickRateEvent.getTime();
            Double tickRate = serverTickRateEvent.getTickRate();

            Integer match = MySQLConnector.getCurrentMatchId();

            MySQLConnector.insertTickRate(time, tickRate, match);
        }catch (Exception e){
            LOGGER.error("DBLog onServerTickRate exception", e);
        }
    }

    @Override
    public void onSteamIdConnected(SteamIdConnectedEvent steamidConnectedEvent) {
        //TODO: Implement
    }
}
