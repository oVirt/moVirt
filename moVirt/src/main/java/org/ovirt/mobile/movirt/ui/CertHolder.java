package org.ovirt.mobile.movirt.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

import org.ovirt.mobile.movirt.R;

import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class CertHolder extends TreeNode.BaseNodeViewHolder<CertHolder.TreeItem> {
    private static final int rightIconId = R.drawable.ic_chevron_right_white_24dp;
    private static final int moreIconId = R.drawable.ic_expand_more_white_24dp;

    private ImageView arrowIcon;
    private TextView textView;

    private TreeNode node;
    private Certificate certificate;

    private CertificateSelectedListener certificateSelectedListener;

    CertHolder(Context context) {
        super(context);
    }

    interface CertificateSelectedListener {
        void onSelect(Certificate certificate);
    }

    static class TreeItem {
        private Certificate certificate;
        private CertificateSelectedListener certificateSelectedListener;

        TreeItem(Certificate certificate, CertificateSelectedListener listener) {
            this.certificate = certificate;
            this.certificateSelectedListener = listener;
        }
    }

    @Override
    public View createNodeView(final TreeNode treeNode, TreeItem item) {
        this.certificate = item.certificate;
        this.certificateSelectedListener = item.certificateSelectedListener;
        this.node = treeNode;

        final LayoutInflater inflater = LayoutInflater.from(context);
        final View view = inflater.inflate(R.layout.cert_tree_node, null, false);
        this.textView = (TextView) view.findViewById(R.id.node_value);

        String certText = getCommonName(certificate);
        certText = isCA(node, certificate) ? context.getString(R.string.cert_tree_item_ca) + certText : certText;
        textView.setText(certText);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectNode();
            }
        });

        if (!node.isLeaf()) {
            arrowIcon = (ImageView) view.findViewById(R.id.arrow_icon);
            toggle(false);

            // expand/collapse onClick and deselect all collapsed nodes, also potentially select this node if it was a parent of collapsed node
            arrowIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AndroidTreeView treeView = node.getViewHolder().getTreeView();
                    if (node.isExpanded()) {
                        treeView.collapseNode(node);

                        boolean deselectedAncestor = false;
                        for (TreeNode n : treeView.getSelected()) {
                            if (isAncestorOf(node, n)) {
                                deselectedAncestor = true;
                                n.setSelected(false);
                                ((CertHolder) n.getViewHolder()).setSelectedBackground(false);
                            }
                        }

                        if (deselectedAncestor) {
                            selectNodeAndDeselectOtherNodes(false);
                        }
                    } else {
                        treeView.expandNode(node);
                    }
                }
            });
        }

        return view;
    }

    void selectNode() {
        selectNodeAndDeselectOtherNodes(true);
    }

    private void selectNodeAndDeselectOtherNodes(boolean deselectOtherNodes) {
        if (deselectOtherNodes) {
            AndroidTreeView treeView = node.getViewHolder().getTreeView();

            for (TreeNode n : treeView.getSelected()) {
                n.setSelected(false);
                ((CertHolder) n.getViewHolder()).setSelectedBackground(false);
            }
        }

        node.setSelected(true);
        setSelectedBackground(true);

        certificateSelectedListener.onSelect(certificate);
    }

    private boolean isAncestorOf(TreeNode ancestor, TreeNode child) {
        if (ancestor.equals(child) || child.isRoot()) {
            return false;
        }
        TreeNode childsParent = child.getParent();

        return childsParent.equals(ancestor) || isAncestorOf(ancestor, childsParent);
    }

    private void setSelectedBackground(boolean selected) {
        textView.setContentDescription(selected ? context.getString(R.string.cert_tree_item_selected, textView.getText()) : textView.getText());
        textView.setBackground(context.getResources().getDrawable(selected ?
                R.drawable.abc_list_pressed_holo_dark : R.drawable.abc_item_background_holo_dark));
    }

    private String getCommonName(Certificate certificate) {
        if (!(certificate instanceof X509Certificate)) {
            throw new IllegalArgumentException("Certificate is not X509Certificate");
        }

        String subject = ((X509Certificate) certificate).getSubjectX500Principal().getName();
        Pattern pattern = Pattern.compile("CN=([^,]*)");
        Matcher matcher = pattern.matcher(subject);
        return matcher.find() ? matcher.group(1) : subject;
    }

    private boolean isCA(TreeNode node, Certificate certificate) { // TODO: move into some CertUtils in the future
        try {
            if (!node.getParent().isRoot()) {
                return false;
            }
            certificate.verify(certificate.getPublicKey());
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    @Override
    public void toggle(boolean active) {
        if (arrowIcon != null) {
            arrowIcon.setContentDescription(context.getString(active ? R.string.cert_tree_item_expanded_tree_node : R.string.cert_tree_item_collapsed_tree_node));
            arrowIcon.setImageDrawable(context.getResources().getDrawable(active ? moreIconId : rightIconId));
        }
    }
}
