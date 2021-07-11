package fr.lernejo.navy_battle;

import java.io.IOException;
import java.util.UUID;

public class Launcher {

    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length != 0) {
            int serverPort = Integer.parseInt(args[0]);
            Server server = new Server(UUID.randomUUID().toString(),"localhost:" + serverPort, "Coucou" );
            server.startServer(serverPort);
            if (args.length == 2)
                server.sendPostRequest(args[1]);
        }

    }
}
