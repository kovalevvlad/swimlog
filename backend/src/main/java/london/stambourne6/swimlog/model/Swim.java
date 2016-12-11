package london.stambourne6.swimlog.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;

import java.time.LocalDate;

public class Swim {
    private final LocalDate date;
    private final double distanceKm;
    private final double durationSeconds;
    private final int userId;

    @JsonCreator
    public Swim(
            @JsonProperty("date") LocalDate date,
            @JsonProperty("distanceKm") double distanceKm,
            @JsonProperty("userId") int userId,
            @JsonProperty("durationSeconds") double durationSeconds) {
        this.date = date;
        this.distanceKm = distanceKm;
        this.userId = userId;
        this.durationSeconds = durationSeconds;
    }

    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    public LocalDate getDate() {
        return date;
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    public double getDurationSeconds() {
        return durationSeconds;
    }

    public int getUserId() {
        return userId;
    }
}
