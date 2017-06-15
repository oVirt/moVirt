package org.ovirt.mobile.movirt.ui.auth.certificatemanagement.data;

import org.ovirt.mobile.movirt.auth.properties.property.Cert;
import org.ovirt.mobile.movirt.auth.properties.property.CertHandlingStrategy;
import org.ovirt.mobile.movirt.auth.properties.property.CertLocation;

import java.util.Arrays;

public class CertContext {
    public CertHandlingStrategy certHandlingStrategy;
    public Cert[] certChain;
    public CertLocation certificateLocation;

    public CertContext(CertHandlingStrategy certHandlingStrategy, Cert[] certChain, CertLocation certificateLocation) {
        this.certHandlingStrategy = certHandlingStrategy;
        this.certChain = certChain;
        this.certificateLocation = certificateLocation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CertContext)) return false;

        CertContext that = (CertContext) o;

        if (certHandlingStrategy != that.certHandlingStrategy) return false;
        if (!Arrays.equals(certChain, that.certChain)) return false;
        return certificateLocation == that.certificateLocation;
    }

    @Override
    public int hashCode() {
        int result = certHandlingStrategy != null ? certHandlingStrategy.hashCode() : 0;
        result = 31 * result + Arrays.hashCode(certChain);
        result = 31 * result + (certificateLocation != null ? certificateLocation.hashCode() : 0);
        return result;
    }
}
