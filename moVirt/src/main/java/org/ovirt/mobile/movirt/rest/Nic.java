package org.ovirt.mobile.movirt.rest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by yixin on 2015/3/24.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Nic {
    public String name;
    public boolean linked;
    public Mac mac;
    public boolean active;
    public boolean plugged;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Mac {
        public String address;
    }

}
