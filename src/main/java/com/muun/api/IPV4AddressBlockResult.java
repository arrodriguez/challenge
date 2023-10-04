package com.muun.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class IPV4Address {
    private String dotDecimalRepresentation;
    private Integer numericRepresentation;
    public IPV4Address(String dotDecimalRepresentation) throws UnknownHostException {
        this.dotDecimalRepresentation = dotDecimalRepresentation;
        //Converts a String that represents an IP to an int.
        InetAddress i = InetAddress.getByName(this.dotDecimalRepresentation);
        this.numericRepresentation = ByteBuffer.wrap(i.getAddress()).getInt();
    }

    public String getDotDecimalRepresentation() {
        return dotDecimalRepresentation;
    }

    public Integer getNumericRepresentation() {
        return numericRepresentation;
    }
}
