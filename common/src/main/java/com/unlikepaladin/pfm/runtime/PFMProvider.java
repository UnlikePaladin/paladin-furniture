package com.unlikepaladin.pfm.runtime;

import com.google.common.base.Stopwatch;
import net.minecraft.data.DataCache;

import java.util.concurrent.TimeUnit;

public abstract class PFMProvider {
    private final PFMGenerator parent;
    private final String providerName;
    private final Stopwatch stopwatch;
    public PFMProvider(PFMGenerator parent, String providerName) {
        this.parent = parent;
        this.providerName = providerName;
        this.stopwatch = Stopwatch.createUnstarted();
    }

    protected void startProviderRun() {
        parent.log("Starting provider: {}", providerName);
        this.stopwatch.start();
    }

    protected void endProviderRun() {
        stopwatch.stop();
        parent.log( "{} finished after {} ms", providerName, stopwatch.elapsed(TimeUnit.MILLISECONDS));
        parent.incrementCount();
    }

    public abstract void run(DataCache cache);

    public PFMGenerator getParent() {
        return parent;
    }
}
