package io.github.realmoieen.cvcviewer.core.format;

import de.bsi.testbedutils.cvc.cvcertificate.CVAuthorization;
import de.bsi.testbedutils.cvc.cvcertificate.CVAuthorizationAT;
import de.bsi.testbedutils.cvc.cvcertificate.CVAuthorizationIS;
import de.bsi.testbedutils.cvc.cvcertificate.CVAuthorizationST;
import de.bsi.testbedutils.cvc.cvcertificate.CVHolderAuth;
import de.bsi.testbedutils.cvc.cvcertificate.TermType;

public final class AuthorizationFormatter {

    private AuthorizationFormatter() {
    }

    public static String describe(CVHolderAuth cert_holderAuth) {
        StringBuilder out = new StringBuilder(1000);

        if (cert_holderAuth == null
                || cert_holderAuth.getAuth() == null) {
            return "";
        }

        CVAuthorization cvAuthorization = cert_holderAuth.getAuth();

        if (TermType.InspectionSystem.equals(cvAuthorization.getTermType())) {
            out.append("Common:\n");
            out.append(getCVAutorizationAT(cvAuthorization, CVAuthorizationIS.auth_Read_eID));

            out.append("Read:\n");
            out.append("\t");
            out.append(getSingleCVAutorizationIS(cvAuthorization, CVAuthorizationIS.auth_Read_DG3));
            out.append(getSingleCVAutorizationIS(cvAuthorization, CVAuthorizationIS.auth_Read_DG4));
            out.append("\n");
        }
        if (TermType.AuthenticationTerminal.equals(cvAuthorization.getTermType())) {
            out.append("Common:\n");
            out.append(getCVAutorizationAT(cvAuthorization, CVAuthorizationAT.auth_AgeVerification));
            out.append(getCVAutorizationAT(cvAuthorization, CVAuthorizationAT.auth_CommunityIDVerification));
            out.append(getCVAutorizationAT(cvAuthorization, CVAuthorizationAT.auth_RestrictedIdentification));
            out.append(getCVAutorizationAT(cvAuthorization, CVAuthorizationAT.auth_PrivilegedTerminal));
            out.append(getCVAutorizationAT(cvAuthorization, CVAuthorizationAT.auth_CANAllowed));
            out.append(getCVAutorizationAT(cvAuthorization, CVAuthorizationAT.auth_PINManagement));
            out.append(getCVAutorizationAT(cvAuthorization, CVAuthorizationAT.auth_InstallCertificate));
            out.append(getCVAutorizationAT(cvAuthorization, CVAuthorizationAT.auth_InstallQulifiedCertificate));

            out.append("Read:\n");
            out.append("\t");
            out.append(getSingleCVAutorizationAT(cvAuthorization, CVAuthorizationAT.auth_Read_DG1));
            out.append(getSingleCVAutorizationAT(cvAuthorization, CVAuthorizationAT.auth_Read_DG2));
            out.append(getSingleCVAutorizationAT(cvAuthorization, CVAuthorizationAT.auth_Read_DG3));
            out.append(getSingleCVAutorizationAT(cvAuthorization, CVAuthorizationAT.auth_Read_DG4));
            out.append(getSingleCVAutorizationAT(cvAuthorization, CVAuthorizationAT.auth_Read_DG5));
            out.append(getSingleCVAutorizationAT(cvAuthorization, CVAuthorizationAT.auth_Read_DG6));
            out.append(getSingleCVAutorizationAT(cvAuthorization, CVAuthorizationAT.auth_Read_DG7));
            out.append(getSingleCVAutorizationAT(cvAuthorization, CVAuthorizationAT.auth_Read_DG8));
            out.append(getSingleCVAutorizationAT(cvAuthorization, CVAuthorizationAT.auth_Read_DG9));
            out.append(getSingleCVAutorizationAT(cvAuthorization, CVAuthorizationAT.auth_Read_DG10));
            out.append(getSingleCVAutorizationAT(cvAuthorization, CVAuthorizationAT.auth_Read_DG11));
            out.append(getSingleCVAutorizationAT(cvAuthorization, CVAuthorizationAT.auth_Read_DG12));
            out.append(getSingleCVAutorizationAT(cvAuthorization, CVAuthorizationAT.auth_Read_DG13));
            out.append(getSingleCVAutorizationAT(cvAuthorization, CVAuthorizationAT.auth_Read_DG14));
            out.append(getSingleCVAutorizationAT(cvAuthorization, CVAuthorizationAT.auth_Read_DG15));
            out.append(getSingleCVAutorizationAT(cvAuthorization, CVAuthorizationAT.auth_Read_DG16));
            out.append(getSingleCVAutorizationAT(cvAuthorization, CVAuthorizationAT.auth_Read_DG17));
            out.append(getSingleCVAutorizationAT(cvAuthorization, CVAuthorizationAT.auth_Read_DG18));
            out.append(getSingleCVAutorizationAT(cvAuthorization, CVAuthorizationAT.auth_Read_DG19));
            out.append(getSingleCVAutorizationAT(cvAuthorization, CVAuthorizationAT.auth_Read_DG20));
            out.append(getSingleCVAutorizationAT(cvAuthorization, CVAuthorizationAT.auth_Read_DG21));
            out.append("\n");

            out.append("Write:\n");
            out.append("\t");
            out.append(getSingleCVAutorizationAT(cvAuthorization, CVAuthorizationAT.auth_Write_DG17));
            out.append(getSingleCVAutorizationAT(cvAuthorization, CVAuthorizationAT.auth_Write_DG18));
            out.append(getSingleCVAutorizationAT(cvAuthorization, CVAuthorizationAT.auth_Write_DG19));
            out.append(getSingleCVAutorizationAT(cvAuthorization, CVAuthorizationAT.auth_Write_DG20));
            out.append(getSingleCVAutorizationAT(cvAuthorization, CVAuthorizationAT.auth_Write_DG21));
            out.append("\n");
        }
        if (TermType.SignatureTerminal.equals(cvAuthorization.getTermType())) {
            out.append("\tCommon:\n");
            out.append(getCVAutorizationST(cvAuthorization, CVAuthorizationST.auth_GenerateQualifiedSignature));
            out.append(getCVAutorizationST(cvAuthorization, CVAuthorizationST.auth_GenerateSignature));
        }

        return out.toString();
    }

    private static String getCVAutorizationAT(CVAuthorization cvAuthorization, int cvAuthorizationAT) {
        if (cvAuthorization.getAuth(cvAuthorizationAT)) {
            StringBuilder out = new StringBuilder(50);
            out.append("\t");
            out.append(CVAuthorizationAT.getText(cvAuthorizationAT));
            out.append("\n");
            return out.toString();
        } else {
            return "";
        }
    }

    private static String getCVAutorizationST(CVAuthorization cvAuthorization, int cvAuthorizationST) {
        if (cvAuthorization.getAuth(cvAuthorizationST)) {
            StringBuilder out = new StringBuilder(50);
            out.append("\t\t\t\t");
            out.append(CVAuthorizationST.getText(cvAuthorizationST));
            out.append("\n");
            return out.toString();
        } else {
            return "";
        }
    }

    private static String getSingleCVAutorizationAT(CVAuthorization cvAuthorization, int cvAuthorizationAT) {
        if (cvAuthorization.getAuth(cvAuthorizationAT)) {
            return CVAuthorizationAT.getText(cvAuthorizationAT) + " ";
        } else {
            return "";
        }
    }

    private static String getSingleCVAutorizationIS(CVAuthorization cvAuthorization, int cvAuthorizationIS) {
        if (cvAuthorization.getAuth(cvAuthorizationIS)) {
            return CVAuthorizationIS.getText(cvAuthorizationIS) + " ";
        } else {
            return "";
        }
    }
}
