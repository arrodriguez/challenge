package com.muun.core;

import com.muun.api.IPV4Address;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Gauge;
import jakarta.xml.bind.DatatypeConverter;

public class IPAddressListExtractor {
    private Logger logger = Logger.getLogger(IPAddressListExtractor.class.getName());
    private String ipAddressListFileName;
    private AtomicReference<String> sha256Checksum = new AtomicReference<>();
    private AtomicReference<Long> lastUpdate = new AtomicReference<>();
    private final MetricRegistry metrics;

    public IPAddressListExtractor(String ipAddressListFileName, MetricRegistry metrics) {
        this.ipAddressListFileName = ipAddressListFileName;
        this.metrics = metrics;
        registerMetrics();
    }

    private void registerMetrics() {
        if (metrics.getMetrics().get("blocklistVersion.sha256") == null) {
            metrics.register("blockListVersion.sha256", (Gauge<String>) () -> sha256Checksum.get());
        }
        if (metrics.getMetrics().get("blockListVersion.lastUpdate") == null) {
            metrics.register("blockListVersion.lastUpdate", (Gauge<Long>) () -> lastUpdate.get());
        }
    }
    public List<IPV4Address> extractIPAddresses() throws IOException {
        List<IPV4Address> result = new LinkedList<>();
        BufferedReader br = new BufferedReader(new FileReader(this.ipAddressListFileName));
        String line = null;
        while ((line = br.readLine()) != null) {
            if (!line.startsWith("#")) {
                String[] parts = line.split("\\s+");
                if (parts.length > 0 && parts[0].length() > 0) {
                    IPV4Address ipv4Address = new IPV4Address(parts[0]);
                    result.add(ipv4Address);
                } else {
                    logger.log(Level.WARNING, "The line: " + line + " has an incorrect format. Expecting: [DotDecimalStringAddress] [numberOfBlockList]\\n");
                }
            }
        }
        reportFileChecksumToMetric();
        return result;
    }

    private void reportFileChecksumToMetric() {
        try {
            sha256Checksum.set(computeSHA256(Paths.get(this.ipAddressListFileName)));
            lastUpdate.set(System.currentTimeMillis());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to compute SHA-256 checksum for the file.", e);
        }
    }

    private String computeSHA256(java.nio.file.Path filePath) throws Exception {
        MessageDigest sha256Digest = MessageDigest.getInstance("SHA-256");
        byte[] fileBytes = Files.readAllBytes(filePath);
        byte[] hashedBytes = sha256Digest.digest(fileBytes);
        return DatatypeConverter.printHexBinary(hashedBytes).toLowerCase();
    }
}
