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
    public String getVersionId() {
        return versionId;
    }
    @JsonProperty
    public Boolean getUpdated() {
        return updated;
    }
    public void setUpdated(Boolean updated) {
        this.updated = updated;
    }
}
