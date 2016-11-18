package org.ovirt.mobile.movirt.auth.properties.property;

import org.ovirt.mobile.movirt.util.ObjectUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.cert.Certificate;
import java.util.Arrays;

public class Cert {

    private byte[] content;

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public static Cert fromCertificate(Certificate certificate) throws Exception {
        if (certificate == null) {
            throw new IllegalArgumentException("Certificate is null");
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = null;
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(certificate);
            byte[] caAsBlob = bos.toByteArray();
            Cert cert = new Cert();
            cert.setContent(caAsBlob);
            return cert;
        } finally {
            ObjectUtils.close(out, bos);
        }
    }

    public Certificate asCertificate() {
        ByteArrayInputStream bis = new ByteArrayInputStream(getContent());
        ObjectInputStream in = null;
        try {
            try {
                in = new ObjectInputStream(bis);
                Object o = in.readObject();
                if (o instanceof Certificate) {
                    return (Certificate) o;
                } else {
                    throw new IllegalStateException("The result object is not a Certificate");
                }
            } catch (IOException | ClassNotFoundException e) {
                throw new IllegalStateException("Error creating caCert from the blob provided: " + e.getMessage());
            }
        } finally {
            ObjectUtils.closeSilently(bis, in);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Cert)) return false;

        Cert cert = (Cert) o;

        return Arrays.equals(content, cert.content);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(content);
    }
}
