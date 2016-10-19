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
    private String certificateSubject;
    private String password;

    public ConsoleConnectionDetails() {
    }

    public ConsoleConnectionDetails(ConsoleProtocol protocol, String address, int port, int tlsPort, String certificateSubject, String password) {
        this.protocol = protocol;
        this.address = address;
        this.port = port;
        this.tlsPort = tlsPort;
        this.certificateSubject = certificateSubject;
        this.password = password;
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

    public String getCertificateSubject() {
        return certificateSubject;
    }

    public void setCertificateSubject(String certificateSubject) {
        this.certificateSubject = certificateSubject;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
