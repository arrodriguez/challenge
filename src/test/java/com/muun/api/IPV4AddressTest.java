package com.muun.api;

import org.assertj.core.api.Assertions;
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
        Assertions.assertThat(address.getNumericRepresentation() == 1743362909);
    }
    @Test public void shouldThrowForInvalidIPV4Dotted() {
        String dotDecimalIP = "103.233.155.93/23";
        Assertions.assertThatThrownBy(() -> new IPV4Address(dotDecimalIP)).isInstanceOf(UnknownHostException.class);
    }
}
