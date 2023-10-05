package com.muun.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;

public class GithubEventApiConfiguration  {
    @NotEmpty
    private String githubEventApiURL;
    @NotEmpty
    private String githubEventApiAcceptHeader;
    @NotEmpty
    private String githubEventsApiUserAgent;
    @NotEmpty
    private String githubRepoOwner;
    @NotEmpty
    private String githubRepoName;
    public GithubEventApiConfiguration(){
    }

    @JsonProperty
    public String getGithubEventApiAcceptHeader() {
        return githubEventApiAcceptHeader;
    }

    @JsonProperty
    public String getGithubEventApiURL() {
        return githubEventApiURL;
    }

    @JsonProperty
    public String getGithubEventsApiUserAgent() {
        return githubEventsApiUserAgent;
    }

    @JsonProperty
    public String getGithubRepoName() {
        return githubRepoName;
    }

    @JsonProperty
    public String getGithubRepoOwner() {
        return githubRepoOwner;
    }
}
