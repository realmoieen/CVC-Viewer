package com.moieen.cvc.gui.cvcviewer;

import de.bsi.testbedutils.cvc.cvcertificate.DataBuffer;
import de.bsi.testbedutils.cvc.cvcertificate.exception.CVBaseException;
import de.bsi.testbedutils.cvc.cvcertificate.exception.CVTagNotFoundException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CertificateParser {

    private static final String TYPE_CERTIFICATE = "CERTIFICATE";
    private static final String TYPE_CV_CERTIFICATE = "CV CERTIFICATE";
    private static final String TYPE_CV_LINK_CERTIFICATE = "CV LINK CERTIFICATE";
    private static final String TYPE_CV_REQUEST = "CV REQUEST";
    private static final String TYPE_CV_AUTHENTICATED_REQUEST = "CV AUTHENTICATED REQUEST";


    private static final Pattern PEM_CERTIFICATE_PATTERN =
            Pattern.compile(
                    "-----BEGIN " + TYPE_CERTIFICATE + "-----(.*?)-----END " + TYPE_CERTIFICATE + "-----",
                    Pattern.DOTALL
            );
    private static final Pattern PEM_CV_CERTIFICATE_PATTERN =
            Pattern.compile(
                    "-----BEGIN " + TYPE_CV_CERTIFICATE + "-----(.*?)-----END " + TYPE_CV_CERTIFICATE + "-----",
                    Pattern.DOTALL
            );
    private static final Pattern PEM_CV_REQUEST_PATTERN =
            Pattern.compile(
                    "-----BEGIN " + TYPE_CV_REQUEST + "-----(.*?)-----END " + TYPE_CV_REQUEST + "-----",
                    Pattern.DOTALL
            );
    private static final Pattern PEM_CV_AUTHENTICATED_REQUEST_PATTERN =
            Pattern.compile(
                    "-----BEGIN " + TYPE_CV_AUTHENTICATED_REQUEST + "-----(.*?)-----END " + TYPE_CV_AUTHENTICATED_REQUEST + "-----",
                    Pattern.DOTALL
            );
    private static final Pattern PEM_CV_LINK_CERTIFICATE_PATTERN =
            Pattern.compile(
                    "-----BEGIN " + TYPE_CV_LINK_CERTIFICATE + "-----(.*?)-----END " + TYPE_CV_LINK_CERTIFICATE + "-----",
                    Pattern.DOTALL
            );

    private CertificateParser() {
        // Utility class
    }


    /**
     * Loads one or more CV Certificates from file.
     */
    public static List<CVCertificate> loadCertificates(File certificateFile) throws IOException, CVBaseException {

        byte[] fileBytes = Files.readAllBytes(certificateFile.toPath());

        // Attempt text-based parsing first
        String content = new String(fileBytes, StandardCharsets.US_ASCII).trim();

        // 1️⃣ PEM with headers (single or multiple)
        if (content.startsWith("-----BEGIN")) {
            List<DataBuffer> dataBuffers = parsePemCertificates(content);
            List<CVCertificate> list = new ArrayList<>();
            for (DataBuffer dataBuffer : dataBuffers) {
                CVCertificate cvCertificate = new CVCertificate(dataBuffer);
                list.add(cvCertificate);
            }
            return list;
        } else {
            try {
                // 2️⃣ Raw binary certificate (DER / CV)
                DataBuffer dataBuffer = DataBuffer.fromInputStream(new ByteArrayInputStream(fileBytes));
                return Collections.singletonList(new CVCertificate(dataBuffer));
            } catch (CVTagNotFoundException e) {
                if (content.contains(",")) {
                    // 3️⃣ Comma-separated Base64 certificates
                    List<CVCertificate> list = new ArrayList<>();
                    List<DataBuffer> dataBuffers = parseCommaSeparatedBase64(content);
                    for (DataBuffer dataBuffer : dataBuffers) {
                        CVCertificate cvCertificate = new CVCertificate(dataBuffer);
                        list.add(cvCertificate);
                    }
                    return list;
                }
                // 4️⃣ Plain Base64 without headers
                if (looksLikeBase64(content)) {
                    return Collections.singletonList(new CVCertificate(DataBuffer.decodeB64(content)));
                }
                throw e;
            }
        }
    }

    /**
     * Parses PEM certificates (single or chain).
     */
    private static List<DataBuffer> parsePemCertificates(String content) {

        List<DataBuffer> certificates = new ArrayList<>();
        Matcher matcher = getPemMatcher(content);
        while (matcher.find()) {
            String base64 = matcher.group(1)
                    .replaceAll("\\s+", "");

            certificates.add(DataBuffer.decodeB64(base64));
        }

        if (certificates.isEmpty()) {
            throw new IllegalArgumentException("Invalid PEM certificate format");
        }

        return certificates;
    }

    private static Matcher getPemMatcher(String content) {
        Matcher matcher;
        if (content.contains(TYPE_CV_CERTIFICATE)) {
            matcher = PEM_CV_CERTIFICATE_PATTERN.matcher(content);
        } else if (content.contains(TYPE_CV_REQUEST)) {
            matcher = PEM_CV_REQUEST_PATTERN.matcher(content);
        } else if (content.contains(TYPE_CV_AUTHENTICATED_REQUEST)) {
            matcher = PEM_CV_AUTHENTICATED_REQUEST_PATTERN.matcher(content);
        } else if (content.contains(TYPE_CERTIFICATE)) {
            matcher = PEM_CERTIFICATE_PATTERN.matcher(content);
        } else if (content.contains(TYPE_CV_LINK_CERTIFICATE)) {
            matcher = PEM_CV_LINK_CERTIFICATE_PATTERN.matcher(content);
        } else {
            throw new IllegalArgumentException("Invalid PEM Format");
        }
        return matcher;
    }

    /**
     * Parses comma-separated Base64 certificates.
     */
    private static List<DataBuffer> parseCommaSeparatedBase64(String content) {

        List<DataBuffer> certificates = new ArrayList<>();
        String[] parts = content.split(",");

        for (String part : parts) {
            String base64 = part.trim();
            if (!base64.isEmpty()) {
                certificates.add(DataBuffer.decodeB64(base64));
            }
        }

        return certificates;
    }

    /**
     * Heuristic check for Base64 data.
     */
    private static boolean looksLikeBase64(String data) {
        return data.matches("^[A-Za-z0-9+/=\\r\\n]+$");
    }
}

