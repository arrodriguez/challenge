package com.muun.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class IPV4AddressBlockResult {
    private IPV4Address ipv4Address;
    private BlockListStatus blockListStatus;
    public IPV4AddressBlockResult(IPV4Address ipv4Address) throws UnknownHostException {
        this.ipv4Address = ipv4Address;
    }

    public void setBlockListStatus(BlockListStatus blockListStatus) {
        this.blockListStatus = blockListStatus;
    }

    @JsonProperty
    public BlockListStatus getBlockListStatus() {
        return blockListStatus;
    }
}
