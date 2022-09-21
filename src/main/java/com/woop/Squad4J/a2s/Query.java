package com.woop.Squad4J.a2s;

import com.ibasco.agql.protocols.valve.source.query.info.SourceQueryInfoResponse;
import com.ibasco.agql.protocols.valve.source.query.rules.SourceQueryRulesResponse;
import com.woop.Squad4J.a2s.response.A2SCombinedResponse;
import com.woop.Squad4J.util.ConfigLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Robert Engle
 *
 * Singleton wrapper for QueryImpl
 */
public class Query {
    private static final Logger LOGGER = LoggerFactory.getLogger(Query.class);

    //private static QueryImpl queryImpl;
    private static NewQueryImpl newQueryImpl;
    private static boolean initialized = false;

    private Query(){
        throw new UnsupportedOperationException("You cannot instantiate this class.");
    }
    
    /*public static void init(){
        //Dont allow re-initialization
        if(initialized)
            throw new IllegalStateException(Query.class.getSimpleName() + " has already been initialized.");

        LOGGER.info("Starting query service.");
        String host = ConfigLoader.get("$.server.host", String.class);
        Integer port = ConfigLoader.get("$.server.queryPort", Integer.class);
        try {
            queryImpl = new QueryImpl(host, port);
        } catch (IOException e) {
            LOGGER.error("Error opening UDP socket.");
            LOGGER.error(e.getMessage());
        }
        initialized = true;
    }*/

    public static void init(){
        //Dont allow re-initialization
        if(initialized)
            throw new IllegalStateException(Query.class.getSimpleName() + " has already been initialized.");

        LOGGER.info("Starting query service.");
        String host = ConfigLoader.get("$.server.host", String.class);
        Integer port = ConfigLoader.get("$.server.queryPort", Integer.class);
        newQueryImpl = new NewQueryImpl(host, port, 3000).init();
        initialized = true;
    }

    /*public static A2SCombinedResponse queryBoth(){
        A2SInfoResponse info = queryInfo();
        A2SRulesResponse rules = queryRules();
        LOGGER.info("Query updated");
        return new A2SCombinedResponse(info, rules);
    }*/

    public static A2SCombinedResponse queryBoth(){
        SourceQueryInfoResponse info = newQueryImpl.getQueryInfo();
        SourceQueryRulesResponse rules = newQueryImpl.getQueryRules();
        LOGGER.info("Query updated");
        return new A2SCombinedResponse(info, rules);
    }

    /*public static A2SInfoResponse queryInfo(){
        if(!initialized)
            throw new IllegalStateException("Query must be initialized first.");
        try {
            return queryImpl.queryInfo();
        } catch (IOException e) {
            LOGGER.error("Error querying server info.");
            LOGGER.error(e.getMessage());
        }
        return null;
    }

    public static A2SRulesResponse queryRules(){
        if(!initialized)
            throw new IllegalStateException("Query must be initialized first.");
        try {
            return queryImpl.queryRules();
        } catch (IOException e) {
            LOGGER.error("Error querying server rules.");
            LOGGER.error(e.getMessage());
        }
        return null;
    }*/
}
