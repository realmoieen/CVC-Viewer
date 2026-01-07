package com.moieen.cvc.gui.cvcviewer;

import de.bsi.testbedutils.cvc.cvcertificate.DataBuffer;
import de.bsi.testbedutils.cvc.cvcertificate.exception.*;

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
}
