package io.github.realmoieen.cvcviewer.core.format;

import de.bsi.testbedutils.cvc.cvcertificate.CVExtension;
import de.bsi.testbedutils.cvc.cvcertificate.CVExtensionData;
import de.bsi.testbedutils.cvc.cvcertificate.CVExtensionType;

public final class ExtensionFormatter {

    private ExtensionFormatter() {
    }

    public static String describe(CVExtension extension) {
        StringBuilder out = new StringBuilder();
        if (extension != null && extension.getExtensions() != null) {
            for (CVExtensionData cvExtensionData : extension.getExtensions()) {
                if (CVExtensionType.extDescription.equals(cvExtensionData.getType())) {
                    out.append("Extended Description:\n");
                } else if (CVExtensionType.extSector.equals(cvExtensionData.getType())) {
                    out.append("Extended Sector:\n");
                } else {
                    out.append(cvExtensionData.getType().name()).append(":\n");
                }
                out.append(cvExtensionData.getHash1().getHexSplit(":", "\t", 48));
                if (cvExtensionData.getHash2() != null) {
                    out.append(cvExtensionData.getHash2().getHexSplit(":", "\t", 48));
                }
            }
        }
        return out.toString();
    }
}
