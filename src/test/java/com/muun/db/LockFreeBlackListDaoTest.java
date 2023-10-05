package com.muun.db;

import com.muun.api.IPV4Address;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class LockFreeBlackListDaoTest {

    private LockFreeBlackListDao dao;

    @BeforeEach
    public void setUp() throws IOException {
        dao = new LockFreeBlackListDao();
    }

    @Test
    public void testLoadAndSwapKeys() throws IOException {
        IPV4Address address1 = new IPV4Address("192.168.1.1");
        IPV4Address address2 = new IPV4Address("10.0.0.1");
        List<IPV4Address> keys = Arrays.asList(address1, address2);

        assertDoesNotThrow(() -> dao.loadAndSwapKeys(keys));
    }

    @Test
    public void testTestWithKnownAddress() throws IOException {
        IPV4Address address = new IPV4Address("192.168.1.1");
        dao.loadAndSwapKeys(Arrays.asList(address));

        Boolean result = dao.test(address);

        assertTrue(result);
    }

    @Test
    public void testTestWithUnknownAddress() throws UnknownHostException {
        IPV4Address address = new IPV4Address("192.168.1.2");

        Boolean result = dao.test(address);

        assertFalse(result);
    }
}
