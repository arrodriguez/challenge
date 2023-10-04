package com.muun.api;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class IPV4Address {
    private String dotDecimalRepresentation;
    private Long numericRepresentation;
    public IPV4Address(String dotDecimalRepresentation) throws UnknownHostException {
        this.dotDecimalRepresentation = dotDecimalRepresentation;
        //Converts a String that represents an IP to an int.
        InetAddress i = InetAddress.getByName(this.dotDecimalRepresentation);
        Integer numericSignedIP = ByteBuffer.wrap(i.getAddress()).getInt();
        this.numericRepresentation = Integer.toUnsignedLong(numericSignedIP);
    }

    public String getDotDecimalRepresentation() {
        return dotDecimalRepresentation;
    }

    public Long getNumericRepresentation() {
        return numericRepresentation;
    }
}
