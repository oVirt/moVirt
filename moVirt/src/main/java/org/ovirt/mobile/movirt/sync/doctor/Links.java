package org.ovirt.mobile.movirt.sync.doctor;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Links {
    public String cluster;
}
