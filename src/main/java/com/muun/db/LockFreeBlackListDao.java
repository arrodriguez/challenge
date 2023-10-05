package com.muun.db;

import com.muun.api.IPV4Address;
import it.unimi.dsi.bits.TransformationStrategies;
import it.unimi.dsi.sux4j.mph.GOVMinimalPerfectHashFunction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

public class LockFreeBlackListDao {
    private AtomicReference<GOVMinimalPerfectHashFunction<Long>> perfectHashFunctionRef = new AtomicReference<>();
    public LockFreeBlackListDao() throws IOException {
        // empty initialization
        loadAndSwapKeys(new ArrayList<>(){});
    }
    public void loadAndSwapKeys(List<IPV4Address> keys) throws IOException {
        LongStream longKeys = keys.stream().mapToLong(address -> address.getNumericRepresentation());
        GOVMinimalPerfectHashFunction<Long> perfectHashFunction =  new GOVMinimalPerfectHashFunction.Builder<Long>().keys(longKeys.boxed()
            .collect(Collectors.toList())).transform(TransformationStrategies.fixedLong()).signed(Long.SIZE).build();
        this.perfectHashFunctionRef.set(perfectHashFunction);
    }

    public Boolean test(IPV4Address ipv4Address) {
       Long result = this.perfectHashFunctionRef.get().getLong(ipv4Address.getNumericRepresentation());
       return (result > -1);
    }

}
