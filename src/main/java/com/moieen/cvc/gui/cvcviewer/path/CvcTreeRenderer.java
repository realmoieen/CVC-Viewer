package com.moieen.cvc.gui.cvcviewer.path;

import com.moieen.cvc.gui.cvcviewer.CVCertificate;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;

public class CvcTreeRenderer extends DefaultTreeCellRenderer {

    public static final ImageIcon validIcon = new ImageIcon(CvcTreeNode.class.getResource("/images/cvc-valid.png"));
    public static final ImageIcon expiredIcon = new ImageIcon(CvcTreeNode.class.getResource("/images/cvc-warning.png"));
    public static final ImageIcon invalidIcon = new ImageIcon(CvcTreeNode.class.getResource("/images/cvc-invalid.png"));


    public CvcTreeRenderer() {
    }

    @Override
    public Component getTreeCellRendererComponent(
            JTree tree, Object value, boolean sel, boolean expanded,
            boolean leaf, int row, boolean hasFocus) {

        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
        if (value instanceof CvcTreeNode) {
            CvcTreeNode node = (CvcTreeNode) value;
            CVCertificate cert = node.getCertificate();
            try {
                cert.checkValidity();
                setIcon(new ImageIcon(validIcon.getImage().getScaledInstance(20, 16, Image.SCALE_SMOOTH)));
            } catch (CertificateNotYetValidException | CertificateExpiredException e) {
                setIcon(new ImageIcon(expiredIcon.getImage().getScaledInstance(20, 16, Image.SCALE_SMOOTH)));
            }
        }
        setIconTextGap(10);  // spacing between icon and label
        return this;
    }
}
