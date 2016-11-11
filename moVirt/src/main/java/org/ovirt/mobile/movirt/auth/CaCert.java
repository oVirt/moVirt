package org.ovirt.mobile.movirt.auth;

import android.text.TextUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CaCert {

    private byte[] content;

    private String validFor;

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public void setValidFor(String validFor) {
        this.validFor = validFor;
    }

    public List<String> validForAsList() {
        if (TextUtils.isEmpty(validFor)) {
            return Collections.EMPTY_LIST;
        }

        List<String> validForHostnames = new ArrayList<>();
        for (String hostname : validFor.split(",")) {
            validForHostnames.add(hostname.trim());
        }

        return validForHostnames;
    }

    public String getValidFor() {
        return validFor;
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
        if (!(o instanceof CaCert)) return false;

        CaCert caCert = (CaCert) o;

        if (!Arrays.equals(content, caCert.content)) return false;
        return validFor != null ? validFor.equals(caCert.validFor) : caCert.validFor == null;
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(content);
        result = 31 * result + (validFor != null ? validFor.hashCode() : 0);
        return result;
    }
}
