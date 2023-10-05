package com.muun;

import com.muun.cli.BlockListMonitorChangesCommand;
import com.muun.core.IPAddressExtractor;
import com.muun.db.LockFreeBlackListDao;
import com.muun.health.LockFreeBlackListHealthCheck;
import com.muun.resources.BlocklistResource;
import io.dropwizard.core.Application;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;

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
        bootstrap.addCommand(new BlockListMonitorChangesCommand());
    }

    @Override
    public void run(final IPBlocklistConfiguration configuration,
                    final Environment environment) throws IOException {
        final LockFreeBlackListDao lockFreeBlackListDao = new LockFreeBlackListDao();
        IPAddressExtractor extractor = new IPAddressExtractor(configuration.getBlockListPath());
        lockFreeBlackListDao.loadAndSwapKeys(extractor.extractIPAddresses());

        environment.healthChecks().register("lockFreeBlackListDao",new LockFreeBlackListHealthCheck());
        environment.jersey().register(new BlocklistResource(lockFreeBlackListDao, configuration.getBlockListPath()));
    }

}
