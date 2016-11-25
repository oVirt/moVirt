package org.ovirt.mobile.movirt.rest.dto;

import org.ovirt.mobile.movirt.model.ConsoleProtocol;

/**
 * Created by suomiy on 10/17/16.
 */
public class ConsoleConnectionDetails {
    private ConsoleProtocol protocol;
    private String address;
    private int port;
    private int tlsPort;
    private String password;
    private String certificateSubject;
    private String certificate;

    public ConsoleConnectionDetails() {
    }

    public ConsoleConnectionDetails(ConsoleProtocol protocol, String address, int port, int tlsPort, String password, String certificateSubject, String certificate) {
        this.protocol = protocol;
        this.address = address;
        this.port = port;
        this.tlsPort = tlsPort;
        this.password = password;
        this.certificateSubject = certificateSubject;
        this.certificate = certificate;
    }

    public ConsoleProtocol getProtocol() {
        return protocol;
    }

    public void setProtocol(ConsoleProtocol protocol) {
        this.protocol = protocol;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getTlsPort() {
        return tlsPort;
    }

    public void setTlsPort(int tlsPort) {
        this.tlsPort = tlsPort;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCertificateSubject() {
        return certificateSubject;
    }

    public void setCertificateSubject(String certificateSubject) {
        this.certificateSubject = certificateSubject;
    }

    public String getCertificate() {
        return certificate;
    }

    public void setCertificate(String certificate) {
        this.certificate = certificate;
    }
}
