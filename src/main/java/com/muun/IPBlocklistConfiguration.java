package com.muun;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import io.dropwizard.core.Configuration;

public class IPBlocklistConfiguration extends Configuration {
    @NotEmpty
    private String blockListPath;
    @NotEmpty
    private String blockListHostName;
    @NotEmpty
    private String blockListRepoOwner;
    @NotEmpty
    private String blockListRepoName;
    @NotEmpty
    private String blockListDownloadURL;

    @JsonProperty
    public String getBlockListPath() {
        return blockListPath;
    }

    @JsonProperty
    public String getBlockListHostName() {
        return blockListHostName;
    }
    @JsonProperty
    public void setBlockListPath(String blockListPath) {
        this.blockListPath = blockListPath;
    }

    @JsonProperty
    public String getBlockListRepoOwner() {
        return this.blockListRepoOwner;
    }

    @JsonProperty
    public String getBlockListRepoName() {
        return this.blockListRepoName;
    }

    @JsonProperty
    public String getBlockListDownloadURL() {
        return this.blockListDownloadURL;
    }
}