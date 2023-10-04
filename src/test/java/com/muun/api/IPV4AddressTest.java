package com.muun.api;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.UnknownHostException;

public class IPV4AddressTest {
    @BeforeEach
    public void setup(){
    }

    @Test public void fromIPV4DottedToNumerical() throws UnknownHostException {
        String dotDecimalIP = "103.233.155.93";
        IPV4Address address = new IPV4Address(dotDecimalIP);
        Assertions.assertThat(address.getNumericRepresentation() == 1743362909);
    }
}
