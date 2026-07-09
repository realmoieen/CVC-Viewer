package io.github.realmoieen.cvcviewer.core.format;

import de.bsi.testbedutils.cvc.cvcertificate.DataBuffer;
import de.bsi.testbedutils.cvc.cvcertificate.ECCCurves;
import de.bsi.testbedutils.cvc.cvcertificate.ECPubPoint;
import de.bsi.testbedutils.cvc.cvcertificate.exception.CVInvalidKeySourceException;
import de.bsi.testbedutils.cvc.cvcertificate.exception.CVKeyTypeNotSupportedException;
import de.bsi.testbedutils.cvc.cvcertificate.exception.CVMissingKeyException;
import io.github.realmoieen.cvcviewer.core.model.CVCertificate;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.math.ec.ECCurve;

import java.security.spec.RSAPublicKeySpec;
import java.util.LinkedHashMap;
import java.util.Map;

public final class PublicKeyFormatter {

    private PublicKeyFormatter() {
    }

    public static Map<String, String> describe(CVCertificate certificate) throws Exception {
        if (certificate.getPublicKey().getAlgorithm().name().contains("RSA")) {
            return describeRSA(certificate);
        } else if (certificate.getPublicKey().getAlgorithm().name().contains("ECDSA")) {
            return describeEC(certificate);
        }
        return Map.of();
    }

    private static Map<String, String> describeRSA(CVCertificate certificate) throws CVInvalidKeySourceException, CVMissingKeyException, CVKeyTypeNotSupportedException {
        Map<String, String> rows = new LinkedHashMap<>();
        RSAPublicKeySpec rsaPublicKeySpec = certificate.getPublicKey().getRSAKey();
        rows.put("Public Key Algorithm", certificate.getPublicKey().getAlgorithm().getKeyType().name());
        rows.put("Public Key Length", "(" + certificate.getPublicKey().getKeyLength() + " bit)");
        rows.put("Modulus", "(" + rsaPublicKeySpec.getModulus().bitLength() + " bit)");
        rows.put("Exponent", rsaPublicKeySpec.getPublicExponent().toString() + " (0x" + rsaPublicKeySpec.getPublicExponent().toString(16) + ")");
        rows.put("Public Key Detail", new DataBuffer(rsaPublicKeySpec.getModulus().toByteArray()).getHexSplit(":", "", 48));
        return rows;
    }

    private static Map<String, String> describeEC(CVCertificate certificate) throws CVInvalidKeySourceException, CVMissingKeyException, CVKeyTypeNotSupportedException {
        Map<String, String> rows = new LinkedHashMap<>();
        StringBuilder out = new StringBuilder(2000);
        rows.put("Public Key Algorithm", certificate.getPublicKey().getAlgorithm().getKeyType().name());
        rows.put("Public Key Length", "(" + certificate.getPublicKey().getKeyLength() + " bit)");

        ECPubPoint ecPublicPoint = certificate.getPublicKey().getECPublicPoint();

        ECParameterSpec domainParams = null;
        ECCCurves curve = null;
        if (certificate.getPublicKey().isDomainParamPresent()) {
            domainParams = certificate.getPublicKey().getDomainParam();

            curve = ECCCurves.getECCCuveEnum(domainParams);
            rows.put("EC Curve", curve == null ? "Unknown" : curve.name());
        }
        if (domainParams != null) {
            if (domainParams.getCurve() instanceof ECCurve.Fp) {
                out.append("P:\n");
                out.append(new DataBuffer(((ECCurve.Fp) domainParams.getCurve()).getQ().toByteArray()).getHexSplit(":", "\t", 48));
            }
            out.append("A:\n");
            out.append(new DataBuffer(domainParams.getCurve().getA().toBigInteger().toByteArray()).getHexSplit(":", "\t", 48));

            out.append("B:\n");
            out.append(new DataBuffer(domainParams.getCurve().getB().toBigInteger().toByteArray()).getHexSplit(":", "\t", 48));
        }

        out.append("X:\n");
        out.append(new DataBuffer(ecPublicPoint.getX().toByteArray()).getHexSplit(":", "\t", 48));

        out.append("Y:\n");
        out.append(new DataBuffer(ecPublicPoint.getY().toByteArray()).getHexSplit(":", "\t", 48));

        if (domainParams != null) {
            out.append("Q:\n");
            out.append(new DataBuffer(domainParams.getN().toByteArray()).getHexSplit(":", "\t", 48));

            out.append("Gx:\n");
            out.append(new DataBuffer(domainParams.getG().normalize().getXCoord().toBigInteger().toByteArray()).getHexSplit(":", "\t", 48));

            out.append("Gy:\n");
            out.append(new DataBuffer(domainParams.getG().normalize().getYCoord().toBigInteger().toByteArray()).getHexSplit(":", "\t", 48));

            out.append("Cofactor: ");
            out.append(domainParams.getH());
            out.append(" (0x");
            out.append(Integer.toHexString(domainParams.getH().intValue()));
            out.append(")\n");
        } else if (curve != null) {
            out.append("Q:\n");
            out.append(new DataBuffer(curve.getECParameter().getOrder().toByteArray()).getHexSplit(":", "\t", 48));

            out.append("Gx:\n");
            out.append(new DataBuffer(curve.getECParameter().getGenerator().getAffineX().toByteArray()).getHexSplit(":", "\t", 48));

            out.append("Gy:\n");
            out.append(new DataBuffer(curve.getECParameter().getGenerator().getAffineY().toByteArray()).getHexSplit(":", "\t", 48));

            out.append("Cofactor: ");
            out.append(curve.getECParameter().getCofactor());
            out.append(" (0x");
            out.append(Integer.toHexString(curve.getECParameter().getCofactor()));
            out.append(")\n");
        }

        if (domainParams != null && domainParams.getSeed() != null) {
            out.append("Seed:\n");
            out.append(new DataBuffer(domainParams.getSeed()).getHexSplit(":", "\t", 48));
            out.append("\n");
        }
        rows.put("Public Key Detail", out.toString());
        return rows;
    }
}
