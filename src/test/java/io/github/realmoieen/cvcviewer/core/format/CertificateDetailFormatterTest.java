package io.github.realmoieen.cvcviewer.core.format;

import io.github.realmoieen.cvcviewer.core.model.CVCertificate;
import io.github.realmoieen.cvcviewer.core.parser.CVCertificatePEMUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CertificateDetailFormatterTest {

    static Stream<File> certificateFiles() throws URISyntaxException {
        File dir = new File(
                Objects.requireNonNull(CVCertificatePEMUtil.class
                        .getClassLoader()
                        .getResource("certificates")
                        .toURI()
                ));
        return Stream.of(Objects.requireNonNull(dir.listFiles()))
                .filter(File::isFile);
    }

    @DisplayName("Formats every certificate/request fixture into non-empty, ordered, de-duplicated detail rows")
    @ParameterizedTest(name = "{index} => {0}")
    @MethodSource("certificateFiles")
    void shouldFormatCertificateDetails(File certificateFile) throws Exception {
        List<CVCertificate> certs = CVCertificatePEMUtil.loadCertificates(certificateFile);

        for (CVCertificate cert : certs) {
            List<DetailRow> rows = CertificateDetailFormatter.format(cert);

            Assertions.assertFalse(rows.isEmpty(), () -> "No detail rows produced for " + certificateFile.getName());

            Assertions.assertEquals("Version", rows.get(0).field(),
                    () -> "Version must be the first row for " + certificateFile.getName());

            Set<String> fieldNames = new HashSet<>();
            for (DetailRow row : rows) {
                Assertions.assertNotNull(row.field());
                Assertions.assertNotNull(row.value());
                Assertions.assertTrue(fieldNames.add(row.field()),
                        () -> "Duplicate field '" + row.field() + "' for " + certificateFile.getName());
            }

            boolean hasSignatureAlgorithm = fieldNames.contains("Signature Algorithm");
            Assertions.assertTrue(hasSignatureAlgorithm,
                    () -> "Missing Signature Algorithm row for " + certificateFile.getName());

            // Authenticated requests carry an outer CA reference/signature pair, or neither - never one without the other.
            Assertions.assertEquals(fieldNames.contains("Outer CA Reference"), fieldNames.contains("Outer Signature"),
                    () -> "Outer CA Reference/Outer Signature must appear together for " + certificateFile.getName());
        }
    }
}
