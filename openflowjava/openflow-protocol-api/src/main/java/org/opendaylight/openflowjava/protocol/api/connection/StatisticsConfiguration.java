/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.api.connection;

/**
 * Used for StatisticsCounter configuration.
 *
 * @author madamjak
 */
public interface StatisticsConfiguration {

    /**
     * Determines if statistics are enabled.
     *
     * @return true if statistics are / will be collected, false otherwise
     */
    boolean getStatisticsCollect();

    /**
     * Returns the delay between two statistics logs.
     *
     * @return delay between two statistics logs (in milliseconds)
     */
    int getLogReportDelay();
}
