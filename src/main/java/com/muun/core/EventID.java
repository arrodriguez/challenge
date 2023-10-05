package com.muun.core;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EventID {
    private String eventId;
    private Boolean updated;

    public EventID(String eventId) {
        this.eventId = eventId;
        this.updated = Boolean.FALSE;
    }
    @JsonProperty
    public String getEventId() {
        return eventId;
    }
    @JsonProperty
    public Boolean updated() {
        return this.updated;
    }
    public void setUpdated(Boolean updated) {
        this.updated = updated;
    }
}
