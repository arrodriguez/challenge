package com.muun.core;

import com.fasterxml.jackson.annotation.JsonProperty;

public class VersionID {
    private String versionId;
    private Boolean updated;

    public VersionID(String versionId) {
        this.versionId = versionId;
        this.updated = Boolean.FALSE;
    }
    @JsonProperty
    public String getEventId() {
        return versionId;
    }
    @JsonProperty
    public Boolean updated() {
        return this.updated;
    }
    public void setUpdated(Boolean updated) {
        this.updated = updated;
    }
}
