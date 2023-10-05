package com.muun.cli;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.muun.IPBlocklistConfiguration;
import com.muun.core.EventID;
import io.dropwizard.core.cli.ConfiguredCommand;
import io.dropwizard.core.setup.Bootstrap;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BlockListMonitorChangesCommand extends ConfiguredCommand<IPBlocklistConfiguration> {
    private static final Logger logger = Logger.getLogger(BlockListMonitorChangesCommand.class.getName());
    private static final String GITHUB_EVENTS_API_ACCEPT_HEADER = "application/vnd.github.v3+json";
    private static final String GITHUB_EVENTS_API_USER_AGENT = "ForcePushMonitor";
    private static final String GITHUB_EVENTS_API_URL = "https://api.github.com/repos/%s/%s/events";

    public BlockListMonitorChangesCommand() {
        super("monitor", "Monitors a GitHub repo for force pushes.");
    }
    @Override
    public void configure(Subparser subparser) {
        super.configure(subparser);
        subparser.addArgument("-eid", "--event-id")
            .dest("event-id")
            .type(String.class)
            .required(false)
            .help("Version-id of the last ipsum dataset version that was retrieved from the repository");
        subparser.addArgument("-p", "--pretty-print")
            .dest("pretty-print")
            .type(Boolean.class)
            .required(false)
            .help("Allow the output of EventID to be pretty printed");
    }
    @Override
    protected void run(Bootstrap<IPBlocklistConfiguration> bootstrap, Namespace namespace, IPBlocklistConfiguration configuration) throws Exception {
        Boolean reloaded = Boolean.FALSE;
        String lastEventId = namespace.getString("event-id");
        Boolean prettyPrint = namespace.getBoolean("pretty-print") ;

        HttpClient client = HttpClients.createDefault();
        HttpGet request = new HttpGet(String.format(GITHUB_EVENTS_API_URL, configuration.getBlockListRepoOwner(),
            configuration.getBlockListRepoName()));

        request.addHeader("Accept", GITHUB_EVENTS_API_ACCEPT_HEADER);
        request.addHeader("User-Agent", GITHUB_EVENTS_API_USER_AGENT);
        HttpResponse response = client.execute(request);

        if(response.getStatusLine().getStatusCode() != 200) {
            String responseBody = EntityUtils.toString(response.getEntity());
            logger.log(Level.SEVERE, "Error while trying to request events from the repository. The status code of the request is: " + response.getStatusLine().getStatusCode()
                + ". Response body: " + responseBody);
            return;
        }
        String responseBody = EntityUtils.toString(response.getEntity());
        JSONArray events = new JSONArray(responseBody);

        if (events.length() == 0) {
            logger.log(Level.SEVERE, "There is no events in the configured repository to monitor.");
            return;
        }

        // The first object is the latest one.
        String currentEventId = events.getJSONObject(0).getString("id");
        for (int i = 0; (!currentEventId.equals(lastEventId)) && (i < events.length()); i++ ) {
            // If there were a push event after the one that we stored,  we need to reload our ip block list dataset. .
            if (events.getJSONObject(i).getString("type").equals("PushEvent")) {
                JSONArray commits = events.getJSONObject(i).getJSONObject("payload").getJSONArray("commits");
                // Looking at repository activity , automatic update occurs when there is only 1 commit in the push, and the message is Automatic update.
                // We also can look for distinct === true that appears to be a force update, but for the moment we skip this validation.
                if (commits.length() == 1 && commits.getJSONObject(0).getString("message").equals("Automatic update")) {
                    downloadFile(configuration.getBlockListDownloadURL(), configuration.getBlockListPath());
                    lastEventId = currentEventId;
                    reloaded = Boolean.TRUE;
                } else {
                    currentEventId = events.getJSONObject(i).getString("id");
                }
            }
        }

        EventID eventID = new EventID(lastEventId);
        if (reloaded) {
            eventID.setUpdated(Boolean.TRUE);
        } else {
            eventID.setUpdated(Boolean.FALSE);
        }

        ObjectMapper mapper = new ObjectMapper();

        if(Boolean.TRUE.equals(prettyPrint)) {
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
        }

        System.out.println(mapper.writeValueAsString(eventID));
    }

    private void downloadFile(String blockListFileURL, String blockListLocalPath) throws Exception {
        HttpClient client = HttpClients.createDefault();
        HttpGet request = new HttpGet(blockListFileURL);
        request.addHeader("Accept-Encoding", "gzip");

        try (CloseableHttpResponse response = (CloseableHttpResponse) client.execute(request)) {
            if (response.getStatusLine().getStatusCode() == 200) {
                InputStream content = response.getEntity().getContent();
                Files.copy(content, Paths.get(blockListLocalPath), StandardCopyOption.REPLACE_EXISTING);
            } else {
                logger.log(Level.SEVERE, "Failed to download the file. Status code: " + response.getStatusLine().getStatusCode());
            }
        }
    }

}
