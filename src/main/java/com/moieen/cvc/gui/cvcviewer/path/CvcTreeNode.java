package com.moieen.cvc.gui.cvcviewer.path;

import com.moieen.cvc.gui.cvcviewer.CVCertificate;

import javax.swing.tree.DefaultMutableTreeNode;

public class CvcTreeNode extends DefaultMutableTreeNode {

    private final CVCertificate certificate;

    public CvcTreeNode(CVCertificate certificate) {
        super(certificate.getCertHolderRef());
        this.certificate = certificate;
    }

    public CVCertificate getCertificate() {
        return certificate;
    }
}
