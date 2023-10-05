package com.muun.cli;

import com.muun.IPBlocklistConfiguration;
import com.muun.configuration.GithubEventApiConfiguration;
import com.muun.testutils.EventResponseTestUtil;
import io.dropwizard.core.setup.Bootstrap;
import net.sourceforge.argparse4j.inf.Namespace;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicStatusLine;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.CopyOption;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

public class BlockListRepoFetcherCommandTest {

    private BlockListRepoFetcherCommand command;
    private HttpClient mockHttpClient;
    private HttpResponse mockHttpResponse;
    private IPBlocklistConfiguration mockConfig;
    private Bootstrap<IPBlocklistConfiguration> mockBootstrap;
    private Namespace mockNamespace;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    @BeforeEach
    public void setUp() throws IOException {
        mockHttpClient = mock(HttpClient.class);
        mockHttpResponse = mock(HttpResponse.class);
        mockConfig = mock(IPBlocklistConfiguration.class);
        mockBootstrap = mock(Bootstrap.class);
        mockNamespace = mock(Namespace.class);

        command = new BlockListRepoFetcherCommand(mockHttpClient) {
            @Override
            protected void copyFile(InputStream content, Path target, CopyOption... options) throws IOException {
                // Do nothing, assume no throw
            }
        };

        GithubEventApiConfiguration mockGithubEventsApi = mock(GithubEventApiConfiguration.class);
        when(mockConfig.getGithubEventsApi()).thenReturn(mockGithubEventsApi);
        when(mockConfig.getGithubEventsApi().getGithubRepoOwner()).thenReturn("owner");
        when(mockConfig.getGithubEventsApi().getGithubRepoName()).thenReturn("repo");
        when(mockConfig.getGithubEventsApi().getGithubEventApiURL()).thenReturn("http://github.com/api");
        when(mockConfig.getGithubEventsApi().getGithubEventApiAcceptHeader()).thenReturn("header");
        when(mockConfig.getGithubEventsApi().getGithubEventsApiUserAgent()).thenReturn("agent");
        when(mockConfig.getBlockListDownloadURL()).thenReturn("http://example.com");
        when(mockConfig.getBlockListPath()).thenReturn("/path/to/file");

        when(mockHttpClient.execute(any())).thenReturn(mockHttpResponse);
        when(mockHttpResponse.getStatusLine()).thenReturn(new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), 200, "OK"));

        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    public void restoreStream() {
        System.setOut(originalOut);
    }

    @Test
    public void testRunWithoutEventsInRepo() throws Exception {
        Map<String, Object> namespaceMap = new HashMap<>();
        namespaceMap.put("version-id", "12345");
        namespaceMap.put("pretty-print", true);
        when(mockNamespace.getAttrs()).thenReturn(namespaceMap);

        when(mockHttpClient.execute(any())).thenReturn(mockHttpResponse);
        when(mockHttpResponse.getStatusLine()).thenReturn(new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), 200, "OK"));
        when(mockHttpResponse.getEntity()).thenReturn(new StringEntity("[]")); // Empty JSON array for simplicity

        Assertions.assertThrows(NoSuchFieldException.class,() -> command.run(mockBootstrap, mockNamespace, mockConfig));
        verify(mockHttpClient, times(1)).execute(any());
    }

    @Test
    public void testRunWithOutPreviousVersion() throws Exception {
        when(mockNamespace.getBoolean("pretty-print")).thenReturn(true);

        when(mockHttpResponse.getEntity()).thenReturn(new StringEntity(EventResponseTestUtil.mockEventResponse()));

        command.run(mockBootstrap, mockNamespace, mockConfig);

        String output = outContent.toString();

        Assertions.assertTrue(output.equals(EventResponseTestUtil.mockCommandOutput("32322495816", Boolean.TRUE, Boolean.TRUE)));
        verify(mockHttpClient, times(2)).execute(any());
    }


    @Test
    public void testRunWithAPreviousVersion() throws Exception {
        when(mockNamespace.getString("version-id")).thenReturn("32322495812");
        when(mockNamespace.getBoolean("pretty-print")).thenReturn(true);
        when(mockHttpResponse.getEntity()).thenReturn(new StringEntity(EventResponseTestUtil.mockEventResponse()));

        command.run(mockBootstrap, mockNamespace, mockConfig);

        String output = outContent.toString();

        Assertions.assertTrue(output.equals(EventResponseTestUtil.mockCommandOutput("32322495816", Boolean.TRUE, Boolean.TRUE)));
        verify(mockHttpClient, times(2)).execute(any());
    }
    @Test
    public void testRunWithAPreviousVersionDoesNotChanged() throws Exception {
        when(mockNamespace.getString("version-id")).thenReturn("32322495816");
        when(mockNamespace.getBoolean("pretty-print")).thenReturn(true);
        when(mockHttpResponse.getEntity()).thenReturn(new StringEntity(EventResponseTestUtil.mockEventResponse()));

        command.run(mockBootstrap, mockNamespace, mockConfig);

        String output = outContent.toString();

        Assertions.assertTrue(output.equals(EventResponseTestUtil.mockCommandOutput("32322495816", Boolean.FALSE, Boolean.TRUE)));
        verify(mockHttpClient, times(1)).execute(any());
    }

}

