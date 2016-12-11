package london.stambourne6.swimlog.controller;

import org.glassfish.jersey.server.monitoring.ApplicationEvent;
import org.glassfish.jersey.server.monitoring.ApplicationEventListener;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.monitoring.RequestEventListener;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ExceptionLogger implements ApplicationEventListener, RequestEventListener {

    private static final Logger log = Logger.getLogger(ExceptionLogger.class.getName());

    @Override
    public void onEvent(final ApplicationEvent applicationEvent) {
    }

    @Override
    public RequestEventListener onRequest(final RequestEvent requestEvent) {
        return this;
    }

    @Override
    public void onEvent(RequestEvent paramRequestEvent) {
        if(paramRequestEvent.getType() == RequestEvent.Type.ON_EXCEPTION) {
            log.log(Level.SEVERE, "unhandled exception", paramRequestEvent.getException());
        }
    }
}
