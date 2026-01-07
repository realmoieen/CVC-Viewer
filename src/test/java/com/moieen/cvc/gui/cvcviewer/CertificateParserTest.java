package com.moieen.cvc.gui.cvcviewer;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CertificateParserTest {

    /**
     * Supplies all certificate files from resources/certificates
     */
    static Stream<File> certificateFiles() throws URISyntaxException {

        File dir = new File(
                Objects.requireNonNull(CertificateParser.class
                        .getClassLoader()
                        .getResource("certificates")
                        .toURI()
                ));

        Assertions.assertTrue(dir.exists(), "Certificate directory not found");
        Assertions.assertTrue(dir.isDirectory(), "Path is not a directory");

        return Stream.of(Objects.requireNonNull(dir.listFiles()))
                .filter(File::isFile);
    }

    @DisplayName("Parse all CV certificates in directory")
    @ParameterizedTest(name = "{index} => {0}")
    @MethodSource("certificateFiles")
    void shouldParseCertificate(File certificateFile) throws Exception {

        List<CVCertificate> certs = CertificateParser.loadCertificates(certificateFile);


        for (CVCertificate cert : certs) {
            Assertions.assertNotNull(cert);
            Assertions.assertNotNull(
                    cert,
                    () -> "Failed parsing: " + certificateFile.getName()
            );
        }
    }
}
