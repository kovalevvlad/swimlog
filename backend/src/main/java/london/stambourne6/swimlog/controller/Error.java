package london.stambourne6.swimlog.controller;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Error {
    private final String error;

    @JsonCreator
    public Error(@JsonProperty("error") String error) {
        this.error = error;
    }

    public String getError() {
        return error;
    }
}
