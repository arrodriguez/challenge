package com.muun.core;

import com.muun.api.IPV4Address;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class IPAddressExtractor {
    private Logger logger = Logger.getLogger(IPAddressExtractor.class.getName());
    private String ipAddressListFileName;

    public IPAddressExtractor(String ipAddressListFileName) {
        this.ipAddressListFileName = ipAddressListFileName;
    }

    public List<IPV4Address> extractIPAddresses() {
        List<IPV4Address> result = new LinkedList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(this.ipAddressListFileName))) {
            String line = null;
            while ((line = br.readLine()) != null) {
                if (!line.startsWith("#")) {
                    String[] parts = line.split("\\s+");
                    if (parts.length > 0 && parts[0].length() > 0) {
                        // Validate if is not a network or netmask
                        IPV4Address ipv4Address = new IPV4Address(parts[0]);
                        result.add(ipv4Address);
                    }
                }
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage());
        }
        return result;
    }
}
