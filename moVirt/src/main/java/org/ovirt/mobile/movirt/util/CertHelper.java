package org.ovirt.mobile.movirt.util;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.ovirt.mobile.movirt.auth.account.AccountDeletedException;
import org.ovirt.mobile.movirt.auth.properties.AccountProperty;
import org.ovirt.mobile.movirt.auth.properties.manager.AccountPropertiesManager;
import org.ovirt.mobile.movirt.auth.properties.manager.OnThread;
import org.ovirt.mobile.movirt.auth.properties.property.Cert;
import org.spongycastle.asn1.ASN1OctetString;
import org.spongycastle.asn1.x500.RDN;
import org.spongycastle.asn1.x500.X500Name;
import org.spongycastle.asn1.x500.style.BCStyle;
import org.spongycastle.asn1.x500.style.IETFUtils;
import org.spongycastle.asn1.x509.AccessDescription;
import org.spongycastle.asn1.x509.AuthorityInformationAccess;
import org.spongycastle.asn1.x509.Extension;
import org.spongycastle.cert.jcajce.JcaX509CertificateHolder;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CertHelper {
    private static final String TAG = CertHelper.class.getSimpleName();

    /**
     * Deletes certificate chain and valid hostnames
     */
    public static void deleteAllCerts(AccountPropertiesManager propertiesManager) throws AccountDeletedException {
        propertiesManager.setCertificateChain(new Cert[]{});
        propertiesManager.setValidHostnameList(new String[]{});
    }

    public static void loadAndStoreCert(AccountPropertiesManager propertiesManager, Context context, Uri file, URL validHostname, boolean startNewChain) throws AccountDeletedException {
        try {
            loadAndStoreCert(propertiesManager, context.getContentResolver().openInputStream(file), file.toString(), validHostname, startNewChain);
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("Could not open file. Please check moVirt's permissions.");
        }
    }

    public static void downloadAndStoreCert(AccountPropertiesManager propertiesManager, @NonNull URL url, @NonNull URL validHostname, boolean startNewChain) throws AccountDeletedException {
        try {
            loadAndStoreCert(propertiesManager, url.openStream(), url.toString(), validHostname, startNewChain);
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not open url" + e.getMessage());
        }
    }

    /**
     * @param inputStream   inputStream of file
     * @param startNewChain if true deletes old certificates before starting new chain, otherwise appends to the chain
     * @throws IllegalStateException if failed, can also delete all certificates if it gets to inconsistent state
     */
    private static void loadAndStoreCert(AccountPropertiesManager propertiesManager, InputStream inputStream, String inputName, URL validHostname, boolean startNewChain) throws AccountDeletedException {
        CertificateFactory cf;

        InputStream caInput = null;
        ByteArrayOutputStream caOutput = null;

        try {
            cf = CertHelper.getX509CertificateFactory();

            caInput = new BufferedInputStream(inputStream);

            caOutput = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = caInput.read(buffer, 0, buffer.length)) != -1) {
                caOutput.write(buffer, 0, len);
            }
            caOutput.flush();

            caInput = new ByteArrayInputStream(caOutput.toByteArray());
        } catch (IOException e) {
            ObjectUtils.closeSilently(caInput, caOutput);
            throw new IllegalStateException("Error loading certificate: " + e.getMessage());
        }
        List<Cert> certs;
        try {
            certs = startNewChain ? new ArrayList<>(1) : new ArrayList<>(Arrays.asList(propertiesManager.getCertificateChain()));
            Certificate issuer = cf.generateCertificate(caInput);
            if (!startNewChain) {
                Certificate lastInChain = certs.get(certs.size() - 1).asCertificate();
                lastInChain.verify(issuer.getPublicKey());
            }
            Cert cert = Cert.fromCertificate(issuer);
            cert.setLocation(inputName);
            certs.add(cert);
        } catch (AccountDeletedException e) {
            throw e;
        } catch (CertificateException e) {
            throw new IllegalStateException("Error parsing certificate: " + e.getMessage());
        } catch (Exception e) {
            throw new IllegalStateException("New certificate doesn't sign last certificate in the chain: " + e.getMessage());
        } finally {
            ObjectUtils.closeSilently(caInput, caOutput);
        }

        try {
            propertiesManager.setAndNotify(AccountProperty.CERTIFICATE_CHAIN, certs.toArray(new Cert[certs.size()]), OnThread.BACKGROUND);
            if (startNewChain) {
                propertiesManager.setAndNotify(AccountProperty.VALID_HOSTNAME_LIST, new String[]{validHostname.getHost()}, OnThread.BACKGROUND);
            }
        } catch (AccountDeletedException e) {
            throw e;
        } catch (Exception e) {
            deleteAllCerts(propertiesManager); // hostname and ca must be atomic
            throw new IllegalStateException("Error storing certificate: " + e.getMessage());
        } finally {
            ObjectUtils.closeSilently(caInput, caOutput);
        }
    }

    /**
     * @param certificate certificate
     * @return common name
     * @throws IllegalArgumentException if certificate is incorrect type
     */
    @NonNull
    public static String getCommonName(Certificate certificate) {
        assertX509Certificate(certificate);
        String result = null;
        try {
            X500Name x500name = new JcaX509CertificateHolder((X509Certificate) certificate).getSubject();
            RDN cn = x500name.getRDNs(BCStyle.CN)[0];
            result = IETFUtils.valueToString(cn.getFirst().getValue());
        } catch (CertificateEncodingException ignored) {
        }

        return (result == null) ? "" : result;
    }

    public static boolean isCA(Certificate certificate) {
        try {
            certificate.verify(certificate.getPublicKey());
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    @Nullable
    public static String getIssuerUrl(Certificate certificate) {
        assertX509Certificate(certificate);
        byte[] encodedExtensionValue = ((X509Certificate) certificate).getExtensionValue(Extension.authorityInfoAccess.getId());

        if (encodedExtensionValue == null) {
            return null;
        }
        ASN1OctetString octetString = ASN1OctetString.getInstance(encodedExtensionValue);
        AuthorityInformationAccess informationAccess = AuthorityInformationAccess.getInstance(octetString.getOctets());
        for (AccessDescription description : informationAccess.getAccessDescriptions()) {
            if (description.getAccessMethod().equals(AccessDescription.id_ad_caIssuers)) {
                return description.getAccessLocation().getName().toString();
            }
        }
        return null;
    }

    /**
     * @return CertificateFactory
     * @throws IllegalStateException
     */
    private static CertificateFactory getX509CertificateFactory() {
        try {
            return CertificateFactory.getInstance("X.509");
        } catch (CertificateException e) {
            Log.i(TAG, ObjectUtils.throwableToString(e));
            throw new IllegalStateException("Problem getting the certificate factory");
        }
    }

    private static void assertX509Certificate(Certificate certificate) {
        if (!(certificate instanceof X509Certificate)) {
            throw new IllegalArgumentException("Certificate is not X509Certificate");
        }
    }
}
