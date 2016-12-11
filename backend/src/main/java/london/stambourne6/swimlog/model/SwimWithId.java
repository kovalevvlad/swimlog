package london.stambourne6.swimlog.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sun.istack.internal.NotNull;

import java.time.LocalDate;

public class SwimWithId extends Swim {
    private final int id;

    @JsonCreator
    public SwimWithId(
            @JsonProperty("id") int id,
            @JsonProperty("date") @NotNull LocalDate date,
            @JsonProperty("distanceKm") double distanceKm,
            @JsonProperty("durationSeconds") double durationSeconds,
            @JsonProperty("userId") int userId) {
        super(date, distanceKm, userId, durationSeconds);
        this.id = id;
    }

    public static SwimWithId fromSwimState(int id, @NotNull Swim state) {
        return new SwimWithId(id, state.getDate(), state.getDistanceKm(), state.getDurationSeconds(), state.getUserId());
    }

    public int getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SwimWithId)) return false;

        SwimWithId swim = (SwimWithId) o;

        return this.id == swim.id &&
               this.getDate().equals(swim.getDate()) &&
               this.getUserId() == swim.getUserId() &&
               Math.abs(this.getDurationSeconds() - swim.getDurationSeconds()) < 0.0001 &&
               Math.abs(this.getDistanceKm() - swim.getDistanceKm()) < 0.0001;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
