package io.github.realmoieen.cvcviewer.core.format;

import de.bsi.testbedutils.cvc.cvcertificate.CVAuthorization;
import io.github.realmoieen.cvcviewer.core.model.CVCertificate;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class CertificateDetailFormatter {

    private CertificateDetailFormatter() {
    }

    public static List<DetailRow> format(CVCertificate certificate) {
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yy");
        Map<String, String> rows = new LinkedHashMap<>();

        rows.put("Version", certificate.getProfileId() + "");
        if (certificate.getCertHolderRef() != null) {
            rows.put("Holder Reference", certificate.getCertHolderRef());
        }
        if (certificate.getCertHolderAuth() != null && certificate.getCertHolderAuth().getAuth() != null) {
            CVAuthorization auth = certificate.getCertHolderAuth().getAuth();
            rows.put("Role", auth.getRole().name());
            rows.put("Term Type", auth.getTermType().name());
            rows.put("Authorization", AuthorizationFormatter.describe(certificate.getCertHolderAuth()));
        }
        if (certificate.getCertAuthRef() != null) {
            rows.put("CA Reference", certificate.getCertAuthRef());
        }
        if (certificate.hasOuterSignature()) {
            rows.put("Outer CA Reference", certificate.getOuterAuthRef());
        }
        if (certificate.getEffDate() != null && certificate.getEffDate().getDate() != null) {
            rows.put("Valid From", dateFormat.format(certificate.getEffDate().getDate()));
        }
        if (certificate.getExpDate() != null && certificate.getExpDate().getDate() != null) {
            rows.put("Valid To", dateFormat.format(certificate.getExpDate().getDate()));
        }

        String extensions = ExtensionFormatter.describe(certificate.getExtension());
        if (!extensions.isEmpty()) {
            rows.put("CV extensions", extensions);
        }

        try {
            rows.putAll(PublicKeyFormatter.describe(certificate));
        } catch (Exception e) {
            rows.put("Public Key Detail", "Not available");
        }

        rows.put("Signature Algorithm", certificate.getPublicKey().getAlgorithm().getSignAlgo() + " (" + certificate.getPublicKey().getAlgorithm().getOID() + ")");
        if (certificate.getSignature() != null) {
            rows.put("Signature", certificate.getSignature().getHexSplit(":", "", 48));
        }
        if (certificate.hasOuterSignature()) {
            rows.put("Outer Signature", certificate.getOuterSignature().getHexSplit(":", "", 48));
        }

        return rows.entrySet().stream()
                .map(e -> new DetailRow(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }
}
