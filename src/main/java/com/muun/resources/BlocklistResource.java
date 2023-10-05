package com.muun.resources;

import com.codahale.metrics.annotation.Timed;
import com.muun.api.BlockListStatus;
import com.muun.api.IPV4Address;
import com.muun.api.IPV4AddressBlockResult;
import com.muun.core.IPAddressListExtractor;
import com.muun.db.LockFreeBlackListDao;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.io.IOException;
import java.net.UnknownHostException;

@Path("/blocklist")
@Produces(MediaType.APPLICATION_JSON)
public class BlocklistResource {

    private final LockFreeBlackListDao lockFreeBlackListDao;
    private IPAddressListExtractor extractor;

    public BlocklistResource(LockFreeBlackListDao lockFreeBlackListDao, IPAddressListExtractor extractor){
       this.lockFreeBlackListDao = lockFreeBlackListDao;
       this.extractor = extractor;
    }
    @GET
    @Path("/ips/{ipv4_address_dot_decimal}")
    @Timed
    public IPV4AddressBlockResult blockList(@PathParam("ipv4_address_dot_decimal") String ipv4AddressStr) throws UnknownHostException {
        IPV4Address ipv4Address = new IPV4Address(ipv4AddressStr);
        IPV4AddressBlockResult result = new IPV4AddressBlockResult(ipv4Address);

        result.setBlockListStatus(this.lockFreeBlackListDao.test(ipv4Address) ?  BlockListStatus.ON_LIST : BlockListStatus.NOT_IN_LIST);

        return result;
    }

    // This method should not be public only internally accessible
    @PUT
    @Path("ips:reload")
    public void reloadBlockList() throws IOException {
        this.lockFreeBlackListDao.loadAndSwapKeys(this.extractor.extractIPAddresses());
    }
}
