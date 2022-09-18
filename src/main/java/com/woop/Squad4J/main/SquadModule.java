package com.woop.Squad4J.main;

import com.woop.Squad4J.a2s.Query;
import com.woop.Squad4J.connector.MySQLConnector;
import com.woop.Squad4J.event.Event;
import com.woop.Squad4J.event.EventType;
import com.woop.Squad4J.event.logparser.ServerTickRateEvent;
import com.woop.Squad4J.server.EventEmitter;
import com.woop.Squad4J.server.SquadServer;
import com.woop.Squad4J.server.tailer.FtpBanService;
import com.woop.Squad4J.server.tailer.TailerService;
import com.woop.Squad4J.util.logger.LoggerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.woop.Squad4J.rcon.Rcon;

import java.util.Date;

/**
 *    _____                       _ _  _       _
 *   / ____|                     | | || |     | |
 *  | (___   __ _ _   _  __ _  __| | || |_    | |
 *   \___ \ / _` | | | |/ _` |/ _` |__   _|   | |
 *   ____) | (_| | |_| | (_| | (_| |  | || |__| |
 *  |_____/ \__, |\__,_|\__,_|\__,_|  |_| \____/
 *             | |
 *             |_|
 *
 * Main entry point for Squad4J. Initializes all services needed to run Squad4J.
 *
 * @author Robert Engle
 */

public class SquadModule {
    private static final Logger LOGGER = LoggerFactory.getLogger(SquadModule.class);

    public static void init() {
        //Initialize logger before pushing any output to console
        LoggerUtil.init();

        //Initailize query service
        Query.init();

        //Initialize connectors
        MySQLConnector.init();

        //Initialize services
        //Initialize RCON service
        Rcon.init();

        //Initialize log tailer service
        TailerService.init();

        new Thread(new FtpBanService()).start();

        //Initialize servers
        //Initialize squad server
        SquadServer.init();

        //Initialize event emitter last
        //Intialize event emitter service
        EventEmitter.init();

        //TODO: Remove me after debugging
        Event testEvent = new ServerTickRateEvent(new Date(), EventType.SERVER_TICK_RATE, 0, 35.5);

        LOGGER.debug("Emitting {}", testEvent);

        EventEmitter.emit(testEvent);

        printLogo();
    }

    private static void printLogo() {
        String color = "\u001B[46m \u001B[30m";
        LOGGER.info(color);
        LOGGER.info(color + "          //\\\\                 //\\\\              //   ||          //\\\\              //\\\\          ||===========");
        LOGGER.info(color + "         //  \\\\               //  \\\\            //               //  \\\\            //  \\\\         ||           ");
        LOGGER.info(color + "        //    \\\\             //    \\\\          //     ||        //    \\\\          //    \\\\        ||           ");
        LOGGER.info(color + "       //      \\\\           //      \\\\        //      ||       //      \\\\        //      \\\\       ||           ");
        LOGGER.info(color + "      //        \\\\         //        \\\\      //       ||      //        \\\\      //        \\\\      ||===========");
        LOGGER.info(color + "     //==========\\\\       //          \\\\    //        ||     //          \\\\    //          \\\\     ||           ");
        LOGGER.info(color + "    //            \\\\     //            \\\\  //         ||    //            \\\\  //            \\\\    ||           ");
        LOGGER.info(color + "   //              \\\\   //              \\\\//          ||   //              \\\\//              \\\\   ||===========");
        LOGGER.info(color + "");
        LOGGER.info(color + "");
        LOGGER.info(color + "");
        LOGGER.info(color + "     /=======\\          /=======\\       \\\\              //          //\\\\              //       /=======\\  ");
        LOGGER.info(color + "    //       \\\\        //       \\\\       \\\\            //          //  \\\\            //       //       \\\\ ");
        LOGGER.info(color + "   //                 //         \\\\       \\\\          //          //    \\\\          //       //         \\\\");
        LOGGER.info(color + "  ||                 ||           ||       \\\\        //          //      \\\\        //       ||           || ");
        LOGGER.info(color + "  ||      =====\\     ||           ||        \\\\      //          //        \\\\      //        ||           || ");
        LOGGER.info(color + "   \\\\         //      \\\\         //          \\\\    //          //          \\\\    //          \\\\         //  ");
        LOGGER.info(color + "    \\\\       //        \\\\       //            \\\\  //          //            \\\\  //            \\\\       //   ");
        LOGGER.info(color + "     \\======//          \\=======/              \\\\//          //              \\\\//              \\=======/    ");
        LOGGER.info(color);
        LOGGER.info("\u001B[0m");
        System.out.println();
    }
}
