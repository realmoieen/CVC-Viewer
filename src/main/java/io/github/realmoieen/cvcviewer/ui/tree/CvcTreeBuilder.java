package io.github.realmoieen.cvcviewer.ui.tree;

import io.github.realmoieen.cvcviewer.core.model.CVCertificate;
import javafx.scene.control.TreeItem;

import java.util.List;

public final class CvcTreeBuilder {

    private CvcTreeBuilder() {
    }

    /**
     * Builds a tree rooted at the last element of the chain (leaf -> root order),
     * mirroring the certificate hierarchy CVCA -> DV -> Terminal.
     */
    public static TreeItem<CVCertificate> buildChainTree(List<CVCertificate> chain) {
        if (chain == null || chain.isEmpty()) {
            return null;
        }

        TreeItem<CVCertificate> rootItem = new TreeItem<>(chain.get(chain.size() - 1));
        TreeItem<CVCertificate> parent = rootItem;

        for (int i = chain.size() - 2; i >= 0; i--) {
            TreeItem<CVCertificate> child = new TreeItem<>(chain.get(i));
            parent.getChildren().add(child);
            parent = child;
        }

        expandAll(rootItem);
        return rootItem;
    }

    /**
     * Returns the leaf item (deepest child) of the tree built by {@link #buildChainTree}.
     */
    public static TreeItem<CVCertificate> leafOf(TreeItem<CVCertificate> root) {
        TreeItem<CVCertificate> current = root;
        while (current != null && !current.getChildren().isEmpty()) {
            current = current.getChildren().get(0);
        }
        return current;
    }

    private static void expandAll(TreeItem<CVCertificate> item) {
        item.setExpanded(true);
        for (TreeItem<CVCertificate> child : item.getChildren()) {
            expandAll(child);
        }
    }
}
