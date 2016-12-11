package london.stambourne6.swimlog.controller;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LocalDateParam {
    private static final DateTimeFormatter ISO_BASIC = DateTimeFormatter.ISO_DATE;
    private final LocalDate date;

    public LocalDateParam(String date) throws WebApplicationException {
        try {
            this.date = LocalDate.parse(date, ISO_BASIC);
        } catch (IllegalArgumentException e) {
            throw new WebApplicationException(
                    Util.responseWithStatusAndMessage(Response.Status.BAD_REQUEST, String.format("Unable to parse %s", date))
            );
        }
    }

    public LocalDate date() {
        return date;
    }
}