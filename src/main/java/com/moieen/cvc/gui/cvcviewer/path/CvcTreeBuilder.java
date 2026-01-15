package com.moieen.cvc.gui.cvcviewer.path;

import com.moieen.cvc.gui.cvcviewer.CVCertificate;

import javax.swing.*;
import javax.swing.tree.*;
import java.util.List;

public class CvcTreeBuilder {

    public static void buildChainTree(JTree path_tree, List<CVCertificate> chain) {

        if (chain == null || chain.isEmpty()) return;

        // Root = last element
        CvcTreeNode rootNode = new CvcTreeNode(chain.get(chain.size() - 1));

        CvcTreeNode parent = rootNode;

        // Build hierarchy backwards
        for (int i = chain.size() - 2; i >= 0; i--) {
            CvcTreeNode child = new CvcTreeNode(chain.get(i));
            parent.add(child);
            parent = child;
        }

        DefaultTreeModel model = new DefaultTreeModel(rootNode);
        path_tree.setModel(model);

        // Set custom renderer
        path_tree.setCellRenderer(new CvcTreeRenderer());

        // Expand all
        expandAll(path_tree);

        // Select leaf node (index 0)
        selectLeaf(path_tree, parent);
    }

    private static void expandAll(JTree tree) {
        for (int i = 0; i < tree.getRowCount(); i++) {
            tree.expandRow(i);
        }
    }

    private static void selectLeaf(JTree tree, DefaultMutableTreeNode leafNode) {
        TreePath path = new TreePath(leafNode.getPath());
        tree.setSelectionPath(path);
        tree.scrollPathToVisible(path);
    }
}
