/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.libraries.liblldp;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Abstract base class for a Datalink Address.
 */
@XmlRootElement
public abstract class DataLinkAddress implements Serializable, Cloneable {
    private static final long serialVersionUID = 1L;
    private String name;

    public DataLinkAddress() {

    }

    /**
     * Constructor of super class.
     *
     * @param name Create a new DataLink, not for general use but
     *     available only for sub classes
     */
    protected DataLinkAddress(final String name) {
        this.name = name;
    }

    /**
     * Allow to distinguish among different data link addresses.
     *
     * @return Name of the DataLinkAdress we are working on
     */
    public String getName() {
        return this.name;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (name == null ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        DataLinkAddress other = (DataLinkAddress) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "DataLinkAddress [name=" + name + "]";
    }
}
