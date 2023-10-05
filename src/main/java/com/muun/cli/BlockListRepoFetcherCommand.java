package com.muun.cli;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.muun.IPBlocklistConfiguration;
import com.muun.core.VersionID;
import io.dropwizard.core.cli.ConfiguredCommand;
import io.dropwizard.core.setup.Bootstrap;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BlockListRepoFetcherCommand extends ConfiguredCommand<IPBlocklistConfiguration> {
    private static final Logger logger = Logger.getLogger(BlockListRepoFetcherCommand.class.getName());
    // All constants are tied to this class implementation
    private static final String VERSION_ID_PARAMETER = "version-id";
    private static final String PRETTY_PRINT_PARAMETER = "pretty-print";
    private static final String UPDATE_MEESAGE_TRIGGER = "Automatic update";
    private HttpClient client;

    public BlockListRepoFetcherCommand(HttpClient client) {
        super("monitor", "Retrieves the latest blocklist from a GitHub repository, checking against a provided version to ensure updates are captured.");
        this.client = client;
    }

    @Override
    public void configure(Subparser subparser) {
        super.configure(subparser);
        subparser.addArgument("-vid", "--version-id")
            .dest(VERSION_ID_PARAMETER)
            .type(String.class)
            .required(false)
            .help("Version-id of the last ipsum dataset version that was retrieved from the repository");
        subparser.addArgument("-p", "--pretty-print")
            .dest(PRETTY_PRINT_PARAMETER)
            .type(Boolean.class)
            .required(false)
            .help("Allow the output of VersionID to be pretty printed");
    }

    @Override
    protected void run(Bootstrap<IPBlocklistConfiguration> bootstrap, Namespace namespace, IPBlocklistConfiguration configuration) throws NoSuchFieldException, IOException {
        Boolean downloaded = Boolean.FALSE;
        // Can be null because is optional.
        String lastVersionId = namespace.getString(VERSION_ID_PARAMETER);
        Boolean prettyPrint = namespace.getBoolean(PRETTY_PRINT_PARAMETER);

        HttpGet request = new HttpGet(String.format(configuration.getGithubEventsApi().getGithubEventApiURL(), configuration.getGithubEventsApi().getGithubRepoOwner(),
            configuration.getGithubEventsApi().getGithubRepoName()));

        request.addHeader("Accept", configuration.getGithubEventsApi().getGithubEventApiAcceptHeader());
        request.addHeader("User-Agent", configuration.getGithubEventsApi().getGithubEventsApiUserAgent());
        HttpResponse response = this.client.execute(request);

        if (response.getStatusLine().getStatusCode() != 200) {
            String responseBody = EntityUtils.toString(response.getEntity());
            logger.log(Level.SEVERE, "Error while trying to request events from the repository. The status code of the request is: " + response.getStatusLine().getStatusCode()
                + ". Response body: " + responseBody);
            return;
        }
        String responseBody = EntityUtils.toString(response.getEntity());
        JSONArray events = new JSONArray(responseBody);

        if (events.length() == 0) {
            throw new NoSuchFieldException("There is no events in the configured repository to monitor.");
        }

        // The first object is the latest one.
        String currentVersionId = events.getJSONObject(0).getString("id");
        for (int i = 0; (!currentVersionId.equals(lastVersionId)) && (i < events.length()); i++) {
            // If the caller send a versionId, iterate over to find a new one if exists.
            // If the caller just want to download latest version, only the first PushEvent appearing will trigger a download.
            if (events.getJSONObject(i).getString("type").equals("PushEvent")) {
                JSONArray commits = events.getJSONObject(i).getJSONObject("payload").getJSONArray("commits");
                // Looking at repository activity , automatic update occurs when there is only 1 commit in the push, and the message: 'Automatic update'.
                // We also can look for distinct === true that appears to be a force update, but for the moment we skip this validation.
                if (commits.length() == 1 && commits.getJSONObject(0).getString("message").equals(UPDATE_MEESAGE_TRIGGER)) {
                    downloadFile(configuration.getBlockListDownloadURL(), configuration.getBlockListPath());
                    lastVersionId = currentVersionId;
                    downloaded = Boolean.TRUE;
                } else {
                    currentVersionId = events.getJSONObject(i).getString("id");
                }
            }
        }

        VersionID versionID = new VersionID(lastVersionId);

        if (downloaded) {
            versionID.setUpdated(Boolean.TRUE);
        } else {
            versionID.setUpdated(Boolean.FALSE);
        }

        ObjectMapper mapper = new ObjectMapper();

        if (Boolean.TRUE.equals(prettyPrint)) {
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
        }

        System.out.print(mapper.writeValueAsString(versionID));
    }

    private void downloadFile(String blockListFileURL, String blockListLocalPath) throws IOException {
        HttpGet request = new HttpGet(blockListFileURL);
        request.addHeader("Accept-Encoding", "gzip");

        HttpResponse response = this.client.execute(request);

        if (response.getStatusLine().getStatusCode() == 200) {
            this.copyFile(response.getEntity().getContent(), Paths.get(blockListLocalPath), StandardCopyOption.REPLACE_EXISTING);
        } else {
            logger.log(Level.SEVERE, "Failed to download the file. Status code: " + response.getStatusLine().getStatusCode());
        }
    }

    protected void copyFile(InputStream content, Path target, CopyOption... options) throws IOException {
        Files.copy(content, target, options);
    }

}
