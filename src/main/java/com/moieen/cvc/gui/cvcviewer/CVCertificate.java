package com.moieen.cvc.gui.cvcviewer;

import de.bsi.testbedutils.cvc.cvcertificate.DataBuffer;
import de.bsi.testbedutils.cvc.cvcertificate.exception.*;

import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.util.Date;

public class CVCertificate extends de.bsi.testbedutils.cvc.cvcertificate.CVCertificate {
    public CVCertificate(boolean createReqCert) {
        super(createReqCert);
    }

    public CVCertificate(DataBuffer rawData) throws CVTagNotFoundException, CVBufferNotEmptyException, CVInvalidOidException, CVDecodeErrorException, CVInvalidDateException, CVInvalidECPointLengthException {
        super(rawData);
    }

    public CVCertificate() {
        super();
    }

    public DataBuffer getOuterSignature() {
        return m_outerSign;
    }

    public void checkValidity() throws CertificateExpiredException, CertificateNotYetValidException {
        Date now = new Date();

        if (getEffDate() != null && getEffDate().getDate() != null) {
            Date notBefore = getEffDate().getDate();
            if (now.before(notBefore)) {
                throw new CertificateNotYetValidException("This Certificate is not yet valid");
            }
        }

        if (getExpDate() != null && getExpDate().getDate() != null) {
            Date notAfter = getExpDate().getDate();
            if (now.after(notAfter)) {
                throw new CertificateExpiredException("This Certificate is expired");
            }
        }
    }

    public String getStatusString() {
        StringBuilder sb = new StringBuilder();

        if (isReqCert()) {
            if (hasOuterSignature()) {
                sb.append("This CV Authenticated Certificate Request is OK");
            } else {
                sb.append("This CV Certificate Request is OK");
            }
        } else {
            try {
                checkValidity();
                sb.append("This Certificate is OK");
            } catch (CertificateNotYetValidException | CertificateExpiredException e) {
                sb.append(e.getMessage());
            }
        }
        return sb.toString();
    }
}
