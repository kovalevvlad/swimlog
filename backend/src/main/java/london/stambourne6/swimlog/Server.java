package london.stambourne6.swimlog;

import london.stambourne6.swimlog.controller.ExceptionLogger;
import london.stambourne6.swimlog.controller.Util;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Server {

    private final Logger logger;
    private final URI baseUri;
    private final String databaseFileLocaiton;

    public Server(int port, String databaseFileLocaiton) {
        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new SimpleFormatter());
        handler.setLevel(Level.ALL);

        logger = Logger.getLogger(this.getClass().getName());
        logger.setLevel(Level.ALL);
        logger.addHandler(handler);

        baseUri = URI.create(String.format("http://localhost:%d/", port));
        this.databaseFileLocaiton = databaseFileLocaiton;
    }

    public static void main(String[] args) {
        new Server(9090, "/home/vladimir/code/swimlog/data.mv.db").run();
    }

    private void run() {
        try {
            final ResourceConfig resourceConfig = Util.resourceConfig(new File(databaseFileLocaiton));
            resourceConfig.register(new LoggingFeature(logger, LoggingFeature.Verbosity.PAYLOAD_ANY));
            resourceConfig.register(new ExceptionLogger());
            final HttpServer grizzlyServer = GrizzlyHttpServerFactory.createHttpServer(
                    baseUri,
                    resourceConfig,
                    true);

            grizzlyServer.start();

            // blocks until the process is terminated
            Thread.currentThread().join();
            grizzlyServer.shutdown();
        }
        catch (IOException | InterruptedException ex) {
            ex.printStackTrace();
        }
    }
}
