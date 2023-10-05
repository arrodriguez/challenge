package com.muun.api;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.UnknownHostException;

public class IPV4AddressTest {
    @BeforeEach
    public void setup(){
    }

    @Test public void shouldConvertfromIPV4DottedToNumerical() throws UnknownHostException {
        String dotDecimalIP = "103.233.155.93";
        IPV4Address address = new IPV4Address(dotDecimalIP);
        assertTrue(address.getNumericRepresentation() == 1743362909);
    }
    @Test public void shouldThrowForInvalidIPV4Dotted() throws UnknownHostException {
        String dotDecimalIP = "103.233.155.93/24";
        assertThrows(UnknownHostException.class, () -> new IPV4Address(dotDecimalIP));
    }
}
