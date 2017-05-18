package org.ovirt.mobile.movirt.util;

import org.ovirt.mobile.movirt.model.enums.ConsoleProtocol;
import org.ovirt.mobile.movirt.rest.dto.ConsoleConnectionDetails;
import org.ovirt.mobile.movirt.ui.Constants;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;

public class ConsoleHelper {

    public static void saveConsoleCertToFile(String caCertPath, String certificate) {
        if (StringUtils.isEmpty(certificate)) {
            throw new IllegalArgumentException("Certificate is missing");
        }
        Writer writer = null;

        try {
            writer = new FileWriter(new File(caCertPath));
            writer.write(certificate);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error storing certificate to file: " + e.getMessage());
        } finally {
            ObjectUtils.closeSilently(writer);
        }
    }

    /**
     * Returns URL for running console intent.
     *
     * @throws java.lang.IllegalArgumentException with description
     *                                            if the URL can't be created from input.
     */
    public static String makeConsoleUrl(ConsoleConnectionDetails details, String caCertPath)
            throws IllegalArgumentException {
        ConsoleProtocol protocol = details.getProtocol();

        if (protocol == null) {
            throw new IllegalArgumentException("Vm's protocol is missing");
        }

        if (StringUtils.isEmpty(details.getPassword())) {
            throw new IllegalArgumentException("Password is missing");
        }

        if (StringUtils.isEmpty(details.getAddress())) {
            throw new IllegalArgumentException("Address is missing");
        }

        String parameters = "";
        switch (protocol) {
            case VNC:
                parameters = Constants.PARAM_VNC_PWD + "=" + details.getPassword(); // vnc password
                break;
            case SPICE:
                parameters = Constants.PARAM_SPICE_PWD + "=" + details.getPassword(); // spice password
                if (details.getTlsPort() > 0) {
                    if (StringUtils.isEmpty(details.getCertificateSubject())) {
                        throw new IllegalArgumentException("Certificate subject is missing");
                    }

                    if (StringUtils.isEmpty(caCertPath)) {
                        throw new IllegalArgumentException("Certificate path is missing");
                    }

                    String tlsPortPart = Constants.PARAM_TLS_PORT + "=" + details.getTlsPort();
                    String certSubjectPart = Constants.PARAM_CERT_SUBJECT + "=" + details.getCertificateSubject();
                    String caCertPathPart = Constants.PARAM_CA_CERT_PATH + "=" + caCertPath;

                    parameters += "&" + tlsPortPart + "&" + certSubjectPart + "&" + caCertPathPart;
                }
                break;
        }

        return protocol.getProtocol() + "://" + details.getAddress() + ":" + details.getPort() + "?" + parameters;
    }
}
