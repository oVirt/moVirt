package org.ovirt.mobile.movirt.ui.auth.certificatemanagement.data;

import android.content.Context;

import com.unnamed.b.atv.model.TreeNode;

import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.auth.properties.property.Cert;
import org.ovirt.mobile.movirt.ui.TreeHolder;
import org.ovirt.mobile.movirt.util.CertHelper;

import java.lang.ref.WeakReference;
import java.security.cert.Certificate;

public class CertTreeItem implements TreeHolder.HolderNode {
    private String location;
    private Certificate certificate;
    private WeakReference<CertificateSelectedListener> certificateSelectedListener;

    public CertTreeItem(Cert cert, CertificateSelectedListener listener) {
        location = cert.getLocation();
        certificate = cert.asCertificate();
        this.certificateSelectedListener = new WeakReference<>(listener);
    }

    @Override
    public void onSelect() {
        CertificateSelectedListener listener = certificateSelectedListener.get();
        if (listener != null) {
            listener.onSelect(certificate, location);
        }
    }

    @Override
    public void onLongClick() {
    }

    @Override
    public String getDescription(Context context, TreeNode treeNode) {
        String certText = CertHelper.getCommonName(certificate);

        if (CertHelper.isCA(certificate)) {
            String certDesc = context.getString(treeNode.isLeaf() ? R.string.cert_tree_item_self_signed : R.string.cert_tree_item_ca);
            certText = certDesc + certText;
        }

        return certText;
    }

    public interface CertificateSelectedListener {
        void onSelect(Certificate certificate, String location);
    }
}
