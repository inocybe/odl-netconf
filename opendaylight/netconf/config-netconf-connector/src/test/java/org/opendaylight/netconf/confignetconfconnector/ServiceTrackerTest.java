/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.netconf.confignetconfconnector;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.opendaylight.controller.config.facade.xml.mapping.config.Services;
import org.opendaylight.controller.config.facade.xml.mapping.config.Services.ServiceInstance;

public class ServiceTrackerTest {

    @Test
    public void test() {
        Services.ServiceInstance serviceInstance = new ServiceInstance("module", "serviceInstance");

        String string = serviceInstance.toString();

        Services.ServiceInstance serviceInstance2 = Services.ServiceInstance.fromString(string);

        assertEquals(serviceInstance, serviceInstance2);
    }

}
