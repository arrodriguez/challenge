package com.muun.core;

import com.muun.api.IPV4Address;
import it.unimi.dsi.bits.TransformationStrategy;
import it.unimi.dsi.sux4j.io.BucketedHashStore;
import it.unimi.dsi.sux4j.mph.GOVMinimalPerfectHashFunction;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class IPV4AddressListBuilder {
        protected Iterable<IPV4Address> keys;
        protected TransformationStrategy<IPV4Address> transform;
        protected File ipAddressListFile;

        public IPV4AddressListBuilder(String ipAddressListPath) {
            this.ipAddressListFile = new File(ipAddressListPath);
        }

        public IPV4AddressListBuilder keys(Iterable<IPV4Address> keys) {
            this.keys = keys;
            return this;
        }

        public IPV4AddressListBuilder tempDir(File ipAddressListFile) {
            this.ipAddressListFile = ipAddressListFile;
            return this;
        }
        public List<IPV4Address> build() throws IOException {


            return null;
        }
}
