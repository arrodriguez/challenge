package com.muun;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.muun.configuration.GithubEventApiConfiguration;
import jakarta.validation.constraints.NotEmpty;
import io.dropwizard.core.Configuration;

public class IPBlocklistConfiguration extends Configuration {
    @NotEmpty
    private String blockListPath;
    @NotEmpty
    private String blockListDownloadURL;
    private GithubEventApiConfiguration githubEventsApi;

    @JsonProperty
    public String getBlockListPath() {
        return blockListPath;
    }

    @JsonProperty
    public String getBlockListDownloadURL() {
        return this.blockListDownloadURL;
    }
    @JsonProperty("githubEventsApi")
    public GithubEventApiConfiguration getGithubEventsApi() {
        return githubEventsApi;
    }

    @JsonProperty
    public void setGithubEventsApi(GithubEventApiConfiguration githubEventsApi) {
        this.githubEventsApi = githubEventsApi;
    }
}