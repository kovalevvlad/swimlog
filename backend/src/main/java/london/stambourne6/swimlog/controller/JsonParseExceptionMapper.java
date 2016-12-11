package london.stambourne6.swimlog.controller;

import com.fasterxml.jackson.core.JsonParseException;

import javax.annotation.Priority;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

// nasty hack for a nasty issue:
// https://github.com/FasterXML/jackson-jaxrs-providers/issues/22
@Priority(4000)
@Provider
public class JsonParseExceptionMapper implements ExceptionMapper<JsonParseException> {
    @Override
    public Response toResponse(JsonParseException e) {
        return Util.responseWithStatusAndMessage(Response.Status.BAD_REQUEST, e.getMessage());
    }
}