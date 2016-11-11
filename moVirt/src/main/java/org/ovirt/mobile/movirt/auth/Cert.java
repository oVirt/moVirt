package org.ovirt.mobile.movirt.auth;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
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

    public Certificate asCertificate() {
        ByteArrayInputStream bis = new ByteArrayInputStream(getContent());
        ObjectInput in = null;
        try {
            try {
                in = new ObjectInputStream(bis);
                Object o = in.readObject();
                if (o instanceof Certificate) {
                    return (Certificate) o;
                } else {
                    throw new IllegalStateException("The result object is not a Certificate");
                }
            } catch (IOException e) {
                throw new IllegalStateException("Error creating caCert from the blob provided: " + e.getMessage());
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("Error creating caCert from the blob provided: " + e.getMessage());
            }
        } finally {
            try {
                bis.close();
            } catch (IOException ignore) {
            }
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ignore) {
            }
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
