package org.ovirt.mobile.movirt.ui.auth;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.auth.properties.property.Cert;
import org.ovirt.mobile.movirt.util.CertHelper;

import java.security.cert.Certificate;

class CertHolder extends TreeNode.BaseNodeViewHolder<CertHolder.TreeItem> {
    private static final int rightIconId = R.drawable.ic_chevron_right_white_24dp;
    private static final int moreIconId = R.drawable.ic_expand_more_white_24dp;

    private ImageView arrowIcon;
    private TextView textView;

    private TreeNode node;
    private Certificate certificate;
    private String location;

    private boolean standalone;

    private CertificateSelectedListener certificateSelectedListener;

    CertHolder(Context context) {
        super(context);
    }

    interface CertificateSelectedListener {
        void onSelect(Certificate certificate, String location);
    }

    static class TreeItem {
        private Cert cert;
        private CertificateSelectedListener certificateSelectedListener;

        TreeItem(Cert cert, CertificateSelectedListener listener) {
            this.cert = cert;
            this.certificateSelectedListener = listener;
        }
    }

    @Override
    public View createNodeView(final TreeNode treeNode, TreeItem item) {
        this.certificate = item.cert.asCertificate();
        this.location = item.cert.getLocation();
        this.certificateSelectedListener = item.certificateSelectedListener;
        this.node = treeNode;
        this.standalone = node.isLeaf() && node.getParent().isRoot();

        final LayoutInflater inflater = LayoutInflater.from(context);
        final View view = inflater.inflate(standalone ? R.layout.cert_tree_node :
                R.layout.cert_tree_image_node, null, false);
        this.textView = (TextView) view.findViewById(R.id.node_value);

        String certText = CertHelper.getCommonName(certificate);

        if (CertHelper.isCA(certificate)) {
            String certDesc = context.getString(node.isLeaf() ? R.string.cert_tree_item_self_signed : R.string.cert_tree_item_ca);
            certText = certDesc + certText;
        }

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

        if (!standalone) {
            setSelectedBackground(true);
        }

        certificateSelectedListener.onSelect(certificate, location);
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

    @Override
    public void toggle(boolean active) {
        if (arrowIcon != null) {
            arrowIcon.setContentDescription(context.getString(active ? R.string.cert_tree_item_expanded_tree_node : R.string.cert_tree_item_collapsed_tree_node));
            arrowIcon.setImageDrawable(context.getResources().getDrawable(active ? moreIconId : rightIconId));
        }
    }
}
