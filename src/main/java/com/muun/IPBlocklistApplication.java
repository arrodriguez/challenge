package com.muun;

import com.muun.cli.BlockListRepoFetcherCommand;
import com.muun.core.IPAddressListExtractor;
import com.muun.db.LockFreeBlackListDao;
import com.muun.health.LockFreeBlackListHealthCheck;
import com.muun.resources.BlocklistResource;
import io.dropwizard.core.Application;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;

public class IPBlocklistApplication extends Application<IPBlocklistConfiguration> {

    public static void main(final String[] args) throws Exception {
        new IPBlocklistApplication().run(args);
    }

    @Override
    public String getName() {
        return "IPBlocklist";
    }

    @Override
    public void initialize(final Bootstrap<IPBlocklistConfiguration> bootstrap) {
        HttpClient client = HttpClients.createDefault();
        bootstrap.addCommand(new BlockListRepoFetcherCommand(client));
    }

    @Override
    public void run(final IPBlocklistConfiguration configuration,
                    final Environment environment) throws IOException {
        final LockFreeBlackListDao lockFreeBlackListDao = new LockFreeBlackListDao();
        IPAddressListExtractor extractor = new IPAddressListExtractor(configuration.getBlockListPath(),environment.metrics());
        lockFreeBlackListDao.loadAndSwapKeys(extractor.extractIPAddresses());

        environment.healthChecks().register("lockFreeBlackListDao",new LockFreeBlackListHealthCheck());
        environment.jersey().register(new BlocklistResource(lockFreeBlackListDao, extractor));
    }

}
