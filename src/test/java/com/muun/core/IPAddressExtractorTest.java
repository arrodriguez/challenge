package com.muun.core;

import com.muun.api.IPV4Address;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class IPAddressExtractorTest {

    @Test
    public void testExtractIPAddresses() throws Exception {
        // Create a temporary file with IP addresses
        String content = "190.203.19.245  1\n121.227.120.22  1\n";
        Path tempFile = Files.createTempFile("temp", ".txt");
        Files.write(tempFile, content.getBytes());

        IPAddressExtractor extractor = new IPAddressExtractor(tempFile.toString());
        List<IPV4Address> ipAddressList = extractor.extractIPAddresses();


        List<IPV4Address> expectedOutput = Arrays.asList(new IPV4Address("190.203.19.245"),
            new IPV4Address("121.227.120.22"));
        assertEquals(expectedOutput.get(0).getNumericRepresentation(), ipAddressList.get(0).getNumericRepresentation());
        assertEquals(expectedOutput.get(1).getNumericRepresentation(), ipAddressList.get(1).getNumericRepresentation());
    }
}