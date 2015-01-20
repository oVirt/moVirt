package org.ovirt.mobile.movirt.rest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonRootName;

/**
 * Created by sphoorti on 16/1/15.
 */
@JsonRootName("action")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ActionTicket {
    public Ticket ticket;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Ticket {
      public String value;
    }
}
