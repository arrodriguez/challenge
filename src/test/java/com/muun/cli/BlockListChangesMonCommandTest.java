package com.muun.cli;

import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import net.sourceforge.argparse4j.inf.Namespace;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HTTP;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;
@ExtendWith(DropwizardExtensionsSupport.class)
public class BlockListChangesMonCommandTest {
    @Mock
    private Bootstrap<?> bootstrap;

    @Mock
    private Namespace namespace;
    @Mock
    private HttpClient httpClient;
    @Mock
    private HttpResponse httpResponse;
    private BlockListChangesMonCommand command;
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        command = new BlockListChangesMonCommand();
        // Mock the static HttpClients.createDefault() method to return our mock httpClient
        mockStatic(HttpClients.class, invocation -> httpClient);
    }


    @Disabled
    @Test
    public void testForcePushDetected() throws Exception {
        // Mock GitHub API response to simulate a force push
        String mockResponse = "[{ \"type\": \"PushEvent\", \"id\": \"12345\", \"payload\": { \"forced\": true }, \"actor\": { \"login\": \"user\" } }]";
        when(httpClient.execute(any())).thenReturn(httpResponse);
        when(httpResponse.getEntity()).thenReturn(new StringEntity(mockResponse, HTTP.UTF_8));

        command.run(bootstrap, namespace);

        // Verify that the PUT request to /blocklist/ips:reload was made
        verify(httpClient).execute(argThat(request -> request.getURI().toString().equals("http://localhost:8080/blocklist/ips:reload")));
    }

}