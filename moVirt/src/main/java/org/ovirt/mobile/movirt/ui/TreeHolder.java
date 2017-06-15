package org.ovirt.mobile.movirt.ui;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

import org.ovirt.mobile.movirt.R;

public class TreeHolder<E extends TreeHolder.HolderNode> extends TreeNode.BaseNodeViewHolder<E> {

    private ImageView arrowIcon;
    private TextView textView;

    private TreeNode node;
    private E dataNode;

    private boolean standalone;

    @LayoutRes
    private int nodeResource;
    @LayoutRes
    private int imageNodeResource;
    private IconDimension iconDimension;
    private boolean collapsibleRoot;

    public TreeHolder(Context context, @LayoutRes int nodeResource, @LayoutRes int imageNodeResource,
                      IconDimension iconDimension, boolean collapsibleRoot) {
        super(context);
        this.nodeResource = nodeResource;
        this.imageNodeResource = imageNodeResource;
        this.iconDimension = iconDimension;
        this.collapsibleRoot = collapsibleRoot;
    }

    public E getDataNode() {
        return dataNode;
    }

    public interface HolderNode {
        void onSelect();

        void onLongClick();

        String getDescription(Context context, TreeNode treeNode);
    }

    @Override
    public View createNodeView(final TreeNode treeNode, E item) {
        this.dataNode = item;
        this.node = treeNode;
        this.standalone = node.isLeaf() && node.getParent().isRoot();

        final LayoutInflater inflater = LayoutInflater.from(context);
        final View view = inflater.inflate(standalone ? nodeResource :
                imageNodeResource, null, false);
        this.textView = (TextView) view.findViewById(R.id.node_value);

        textView.setText(dataNode.getDescription(context, node));
        textView.setOnClickListener(v -> selectNode());
        textView.setOnLongClickListener(v -> {
            dataNode.onLongClick();
            return true;
        });

        if (!node.isLeaf()) {
            arrowIcon = (ImageView) view.findViewById(R.id.arrow_icon);
            toggle(false);

            // expand/collapse onClick and deselect all collapsed nodes, also potentially select this node if it was a parent of collapsed node
            if (!node.getParent().isRoot() || collapsibleRoot) {
                arrowIcon.setOnClickListener(v -> {
                    AndroidTreeView treeView = node.getViewHolder().getTreeView();
                    if (node.isExpanded()) {
                        treeView.collapseNode(node);

                        boolean deselectedAncestor = false;
                        for (TreeNode n : treeView.getSelected()) {
                            if (isAncestorOf(node, n)) {
                                deselectedAncestor = true;
                                n.setSelected(false);
                                ((TreeHolder) n.getViewHolder()).setSelectedBackground(false);
                            }
                        }

                        if (deselectedAncestor) {
                            selectNodeAndDeselectOtherNodes(false);
                        }
                    } else {
                        treeView.expandNode(node);
                    }
                });
            }
        }

        return view;
    }

    public void selectNode() {
        selectNodeAndDeselectOtherNodes(true);
    }

    private void selectNodeAndDeselectOtherNodes(boolean deselectOtherNodes) {
        if (deselectOtherNodes) {
            AndroidTreeView treeView = node.getViewHolder().getTreeView();

            for (TreeNode n : treeView.getSelected()) {
                n.setSelected(false);
                ((TreeHolder) n.getViewHolder()).setSelectedBackground(false);
            }
        }

        node.setSelected(true);

        if (!standalone) {
            setSelectedBackground(true);
        }

        dataNode.onSelect();
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
        if (selected) {
            textView.setBackgroundColor(context.getResources().getColor(R.color.material_blue_700));
        } else {
            textView.setBackground(context.getResources().getDrawable(R.drawable.abc_item_background_holo_dark));
        }
    }

    @Override
    public void toggle(boolean active) {
        if (arrowIcon != null) {
            arrowIcon.setContentDescription(context.getString(active ? R.string.cert_tree_item_expanded_tree_node : R.string.cert_tree_item_collapsed_tree_node));
            arrowIcon.setImageDrawable(context.getResources().getDrawable(iconDimension.getResource(active)));
        }
    }
}
