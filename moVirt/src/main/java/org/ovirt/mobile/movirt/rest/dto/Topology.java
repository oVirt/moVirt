package org.ovirt.mobile.movirt.rest.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by sphoorti on 28/1/15.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Topology {
    public String sockets, cores, threads;
}
