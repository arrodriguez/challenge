package com.muun.resources;

import com.muun.api.BlockListStatus;
import com.muun.api.IPV4AddressBlockResult;
import com.muun.core.IPAddressListExtractor;
import com.muun.db.LockFreeBlackListDao;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.UnknownHostException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class BlockListResourceTest {
    private LockFreeBlackListDao mockDao;
    private IPAddressListExtractor extractor;
    private BlocklistResource resource;

    @BeforeEach
    public void setUp() {
        mockDao = mock(LockFreeBlackListDao.class);
        extractor = mock(IPAddressListExtractor.class);
        resource = new BlocklistResource(mockDao,extractor);
    }

    @Test
    public void testBlockListOnList() throws UnknownHostException {
        when(mockDao.test(any())).thenReturn(true);
        IPV4AddressBlockResult result = resource.blockList("192.168.1.1");
        assertEquals(BlockListStatus.ON_LIST, result.getBlockListStatus());
    }

    @Test
    public void testBlockListNotInList() throws UnknownHostException {
        when(mockDao.test(any())).thenReturn(false);
        IPV4AddressBlockResult result = resource.blockList("192.168.1.1");
        assertEquals(BlockListStatus.NOT_IN_LIST, result.getBlockListStatus());
    }

    @Test
    public void testBlockListInvalidIPAddressFormat() throws UnknownHostException {
        when(mockDao.test(any())).thenReturn(true);
        Assertions.assertThatThrownBy(() -> resource.blockList("192.168.1.1/24")).isInstanceOf(UnknownHostException.class);
    }

    @Test
    public void testReloadBlockList() throws Exception {
        // Assuming the extractor and DAO work correctly, this test ensures the method is called.
        when(extractor.extractIPAddresses()).thenReturn(new ArrayList<>());
        resource.reloadBlockList();
        verify(mockDao, times(1)).loadAndSwapKeys(any());
    }
}
