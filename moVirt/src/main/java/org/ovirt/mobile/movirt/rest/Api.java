package org.ovirt.mobile.movirt.rest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Api {
    public ProductInfo product_info;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public class ProductInfo {
        public Version version;

        public Version getVersion() {
            return version;
        }
    }

    public ProductInfo getProductInfo() {
        return product_info;
    }
}
