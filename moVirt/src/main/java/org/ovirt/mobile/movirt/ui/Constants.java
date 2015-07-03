package org.ovirt.mobile.movirt.ui;

import android.content.Context;

public class Constants {
    public static final String DEFAULT_CA_CERT_FILE_NAME = "ca.crt";

    // URI Parameters
    public static final String PARAM_SPICE_PWD = "SpicePassword";
    public static final String PARAM_VNC_PWD = "VncPassword";
    public static final String PARAM_SSH_HOST = "SshHost";
    public static final String PARAM_SSH_PORT = "SshPort";
    public static final String PARAM_SSH_USER = "SshUser";
    public static final String PARAM_SSH_PWD = "SshPassword";
    public static final String PARAM_TLS_PORT = "TlsPort";
    public static final String PARAM_CA_CERT_PATH = "CaCertPath";
    public static final String PARAM_CERT_SUBJECT = "CertSubject";

    public static String getCaCertPath(Context context){
        return context.getExternalCacheDir() + "/" + Constants.DEFAULT_CA_CERT_FILE_NAME;
    }
}
