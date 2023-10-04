package com.muun.core;

import com.muun.api.IPV4Address;
import it.unimi.dsi.bits.TransformationStrategy;
import it.unimi.dsi.sux4j.io.BucketedHashStore;
import it.unimi.dsi.sux4j.mph.GOVMinimalPerfectHashFunction;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class LockFreeBlackListBuilder {
        protected Iterable<Long> keys;
        protected TransformationStrategy<Long> transform;
        protected File tempDir;

        public LockFreeBlackListBuilder () {
        }

        public LockFreeBlackListBuilder<Long> keys(Iterable<? extends T> keys) {
            this.keys = keys;
            return this;
        }

        public GOVMinimalPerfectHashFunction.Builder<T> transform(TransformationStrategy<? super T> transform) {
            this.transform = transform;
            return this;
        }

        public GOVMinimalPerfectHashFunction.Builder<T> signed(int signatureWidth) {
            this.signatureWidth = signatureWidth;
            return this;
        }

        public GOVMinimalPerfectHashFunction.Builder<T> tempDir(File tempDir) {
            this.tempDir = tempDir;
            return this;
        }

        public GOVMinimalPerfectHashFunction.Builder<T> store(BucketedHashStore<T> bucketedHashStore) {
            this.bucketedHashStore = bucketedHashStore;
            return this;
        }

        public List<IPV4Address> build() throws IOException {
            if (this.built) {
                throw new IllegalStateException("This builder has been already used");
            } else {
                this.built = true;
                if (this.transform == null) {
                    if (this.bucketedHashStore == null) {
                        throw new IllegalArgumentException("You must specify a TransformationStrategy, either explicitly or via a given BucketedHashStore");
                    }

                    this.transform = this.bucketedHashStore.transform();
                }

                return null;
            }
        }
    }
}
