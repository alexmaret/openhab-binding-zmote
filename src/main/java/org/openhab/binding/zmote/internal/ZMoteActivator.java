package org.openhab.binding.zmote.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZMoteActivator implements BundleActivator {

    private static final Logger logger = LoggerFactory.getLogger(ZMoteActivator.class);

    @Override
    public void start(final BundleContext bundleContext) throws Exception {

        if (logger.isDebugEnabled()) {
            logger.debug("ZMote bundle started.");
        }
    }

    @Override
    public void stop(final BundleContext context) throws Exception {

        if (logger.isDebugEnabled()) {
            logger.debug("ZMote bundle stopped.");
        }
    }
}
