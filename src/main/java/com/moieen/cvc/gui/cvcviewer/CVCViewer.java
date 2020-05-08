package com.moieen.cvc.gui.cvcviewer;

import com.secunet.testbedutils.cvc.cvcertificate.CVAuthorization;
import com.secunet.testbedutils.cvc.cvcertificate.CVAuthorizationAT;
import com.secunet.testbedutils.cvc.cvcertificate.CVAuthorizationIS;
import com.secunet.testbedutils.cvc.cvcertificate.CVAuthorizationST;
import com.secunet.testbedutils.cvc.cvcertificate.CVCertificate;
import com.secunet.testbedutils.cvc.cvcertificate.CVExtension;
import com.secunet.testbedutils.cvc.cvcertificate.CVExtensionData;
import com.secunet.testbedutils.cvc.cvcertificate.CVExtensionType;
import com.secunet.testbedutils.cvc.cvcertificate.CVHolderAuth;
import com.secunet.testbedutils.cvc.cvcertificate.DataBuffer;
import com.secunet.testbedutils.cvc.cvcertificate.ECCCurves;
import com.secunet.testbedutils.cvc.cvcertificate.ECPubPoint;
import com.secunet.testbedutils.cvc.cvcertificate.TermType;
import com.secunet.testbedutils.cvc.cvcertificate.exception.CVInvalidKeySourceException;
import com.secunet.testbedutils.cvc.cvcertificate.exception.CVKeyTypeNotSupportedException;
import com.secunet.testbedutils.cvc.cvcertificate.exception.CVMissingKeyException;
import java.awt.Image;
import java.security.spec.RSAPublicKeySpec;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.ImageIcon;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.math.ec.ECCurve;

/**
 *
 * @author Moieen Abbas
 */
public class CVCViewer extends javax.swing.JFrame {

    DateFormat dateFormat = new SimpleDateFormat("dd/MM/yy");
    Map<String, String> map_tableData = new LinkedHashMap<>();

    /**
     * Creates new form NewJFrame
     */
    public CVCViewer(CVCertificate obj_CVCertificate) {
        init();
        try {
            setCertificateDetail(obj_CVCertificate);
        } catch (Exception e) {
            Lunch.showError(e);
            System.exit(0);
        }
    }

    public CVCViewer() {
        init();
    }

    private void init() {
        this.setTitle("CV Certificate Viewer");
        this.setResizable(false);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setIconImage(new javax.swing.ImageIcon(getClass().getResource("/resized.png")).getImage());
        initComponents();
        label_certIcon.setIcon(new ImageIcon(new ImageIcon("/certificateicon.png").getImage().getScaledInstance(20, 20, Image.SCALE_DEFAULT)));
        detail_table.getSelectionModel().addListSelectionListener((ListSelectionEvent event) -> {
            String selectedData = null;
            int selectedRow = detail_table.getSelectedRow();
            selectedData = (String) detail_table.getValueAt(selectedRow, 0);
            selectedData = map_tableData.getOrDefault(selectedData, selectedData);
            selectedRowDetailTextArea.setText(selectedData);
        });
    }

    private void setCertificateDetail(CVCertificate obj_CVCertificate) {

        general_holderRefernce.setText(obj_CVCertificate.getCertHolderRef());
        general_caReference.setText(obj_CVCertificate.getCertAuthRef());
        CVHolderAuth cert_holderAuth = obj_CVCertificate.getCertHolderAuth();
        if (obj_CVCertificate.isReqCert()) {
            general_purposeLable.setText("This is a CV Certificate Request with no purpose(s): ");
            label_generlPurpose.setText("• CV Request\n");
            general_validFromLabel.setText("");
            general_validFrom.setText("");
            general_labelTO.setText("");
            general_validTo.setText("");
        } else if (obj_CVCertificate.hasOuterSignature()) {
            general_purposeLable.setText("This is a CVC Authenticated Request with outer signature");
            label_generlPurpose.setText("• Authenticated Request\n"
                    + "• Outer CA Reference: " + obj_CVCertificate.getOuterAuthRef());
            general_validFromLabel.setText("");
            general_validFrom.setText("");
            general_labelTO.setText("");
            general_validTo.setText("");
        } else {
            general_purposeLable.setText("This CV Certificate (CVC) is intented for the following purpose(s):");
            if (cert_holderAuth != null && cert_holderAuth.getAuth() != null) {
                CVAuthorization cert_auth = obj_CVCertificate.getCertHolderAuth().getAuth();
                label_generlPurpose.setText("• " + cert_auth.getRole().name());
            }
            general_validFrom.setText(dateFormat.format(obj_CVCertificate.getEffDate().getDate()));
            general_validTo.setText(dateFormat.format(obj_CVCertificate.getExpDate().getDate()));
        }
        populateDetailTableData(obj_CVCertificate);
    }

    void populateDetailTableData(CVCertificate obj_CVCertificate) {

        map_tableData.put("Version", obj_CVCertificate.getProfileId() + "");
        if (obj_CVCertificate.getCertHolderRef() != null) {
            map_tableData.put("Holder Reference", obj_CVCertificate.getCertHolderRef());
        }
        if (obj_CVCertificate.getCertHolderAuth() != null && obj_CVCertificate.getCertHolderAuth().getAuth() != null) {
            CVAuthorization auth = obj_CVCertificate.getCertHolderAuth().getAuth();
            map_tableData.put("Role", auth.getRole().name());
            map_tableData.put("Term Type", auth.getTermType().name());
            map_tableData.put("Authorization", getCVAutorizationInfo(obj_CVCertificate.getCertHolderAuth()));
        }
        if (obj_CVCertificate.getCertAuthRef() != null) {
            map_tableData.put("CA Reference", obj_CVCertificate.getCertAuthRef());
        }
        if (obj_CVCertificate.hasOuterSignature()) {
            map_tableData.put("Outer CA Reference", obj_CVCertificate.getOuterAuthRef());
        }
        if (obj_CVCertificate.getEffDate() != null && obj_CVCertificate.getEffDate().getDate() != null) {
            map_tableData.put("Valid From", dateFormat.format(obj_CVCertificate.getEffDate().getDate()));
        }
        if (obj_CVCertificate.getExpDate() != null && obj_CVCertificate.getExpDate().getDate() != null) {
            map_tableData.put("Valid To", dateFormat.format(obj_CVCertificate.getExpDate().getDate()));
        }
        getCVExtention(obj_CVCertificate.getExtension());
        try {
            getPublicKeyDetails(obj_CVCertificate);
        } catch (Exception e) {
            map_tableData.put("Public Key Detail", "Not available");
        }
        if (obj_CVCertificate.getSignature() != null) {
            map_tableData.put("Signature", obj_CVCertificate.getSignature().getHexSplit(":", "", 48));
        }
        if (obj_CVCertificate.hasOuterSignature()) {
            map_tableData.put("Outer Signature", obj_CVCertificate.getOuterSignature().getHexSplit(":", "", 48));
        }
        String[][] table_data = new String[map_tableData.size()][2];
        int i = 0;
        for (Map.Entry<String, String> entry : map_tableData.entrySet()) {

            table_data[i][0] = entry.getKey();
            table_data[i][1] = entry.getValue();
            i++;
        }
        detail_table.setModel(new javax.swing.table.DefaultTableModel(table_data, new String[]{"Field", "Value"}));
    }

    public void getPublicKeyDetails(CVCertificate obj_cvCert) throws Exception {
        if (obj_cvCert.getPublicKey().getAlgorithm().name().contains("RSA")) {
            getRSAPublicKeyInfos(obj_cvCert);
        } else if (obj_cvCert.getPublicKey().getAlgorithm().name().contains("ECDSA")) {
            getECPublicKeyInfos(obj_cvCert);;
        }
    }

    private void getRSAPublicKeyInfos(CVCertificate obj_cvCert) throws CVInvalidKeySourceException, CVMissingKeyException, CVKeyTypeNotSupportedException {
        RSAPublicKeySpec rsaPublicKeySpec = obj_cvCert.getPublicKey().getRSAKey();
        map_tableData.put("Public Key Algorithm", obj_cvCert.getPublicKey().getAlgorithm().toString().replaceAll("_", " "));
        map_tableData.put("Public Key Length", "(" + obj_cvCert.getPublicKey().getKeyLength() + " bit)");
        map_tableData.put("Modulus", "(" + rsaPublicKeySpec.getModulus().bitLength() + " bit)");
        map_tableData.put("Exponent", rsaPublicKeySpec.getPublicExponent().toString() + " (0x" + rsaPublicKeySpec.getPublicExponent().toString(16) + ")");
        map_tableData.put("Public Key Detail", new DataBuffer(rsaPublicKeySpec.getModulus().toByteArray()).getHexSplit(":", "", 48));
    }

    private void getECPublicKeyInfos(CVCertificate obj_cvCert) throws CVInvalidKeySourceException, CVMissingKeyException, CVKeyTypeNotSupportedException {
        StringBuilder out = new StringBuilder(2000);
        map_tableData.put("Public Key Algorithm", obj_cvCert.getPublicKey().getAlgorithm().toString().replaceAll("_", " "));
        map_tableData.put("Public Key Length", "(" + obj_cvCert.getPublicKey().getKeyLength() + " bit)");

        ECPubPoint ecPublicPoint = obj_cvCert.getPublicKey().getECPublicPoint();

        ECParameterSpec domainParams = null;
        ECCCurves curve = null;
        if (obj_cvCert.getPublicKey().isDomainParamPresent()) {
            domainParams = obj_cvCert.getPublicKey().getDomainParam();

            curve = ECCCurves.getECCCuveEnum(domainParams);
            map_tableData.put("EC Curve", curve == null ? "Unknown" : curve.name());
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
        map_tableData.put("Public Key Detail", out.toString());
    }

    public String getCVAutorizationInfo(CVHolderAuth cert_holderAuth) {
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
            out.append(getSingleCVAutorizationAT(cvAuthorization, CVAuthorizationAT.auth_Read_DG17));
            out.append(getSingleCVAutorizationAT(cvAuthorization, CVAuthorizationAT.auth_Read_DG18));
            out.append(getSingleCVAutorizationAT(cvAuthorization, CVAuthorizationAT.auth_Read_DG19));
            out.append(getSingleCVAutorizationAT(cvAuthorization, CVAuthorizationAT.auth_Read_DG20));
            out.append(getSingleCVAutorizationAT(cvAuthorization, CVAuthorizationAT.auth_Read_DG21));
            out.append("\n");
        }
        if (TermType.SignatureTerminal.equals(cvAuthorization.getTermType())) {
            out.append("\tCommon:\n");
            out.append(getCVAutorizationST(cvAuthorization, CVAuthorizationST.auth_GenerateQualifiedSignature));
            out.append(getCVAutorizationST(cvAuthorization, CVAuthorizationST.auth_GenerateSignature));
        }

        return out.toString();
    }

    private String getCVAutorizationAT(CVAuthorization cvAuthorization, int cvAuthorizationAT) {
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

    private String getCVAutorizationST(CVAuthorization cvAuthorization, int cvAuthorizationST) {
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

    private String getSingleCVAutorizationAT(CVAuthorization cvAuthorization, int cvAuthorizationAT) {
        if (cvAuthorization.getAuth(cvAuthorizationAT)) {
            return CVAuthorizationAT.getText(cvAuthorizationAT) + " ";
        } else {
            return "";
        }
    }

    private String getSingleCVAutorizationIS(CVAuthorization cvAuthorization, int cvAuthorizationIS) {
        if (cvAuthorization.getAuth(cvAuthorizationIS)) {
            return CVAuthorizationIS.getText(cvAuthorizationIS) + " ";
        } else {
            return "";
        }
    }

    private String getCVExtention(CVExtension extension) {
        StringBuilder out = new StringBuilder();
        if (extension != null && extension.getExtensions() != null) {
            for (CVExtensionData cvExtensionData : extension.getExtensions()) {
                if (CVExtensionType.extDescription.equals(cvExtensionData.getType())) {
                    out.append("Extended Description:\n");
                    out.append(cvExtensionData.getHash1().getHexSplit(":", "\t", 48));
                }
                if (CVExtensionType.extSector.equals(cvExtensionData.getType())) {
                    out.append("Extended Sector:\n");
                    out.append(cvExtensionData.getHash1().getHexSplit(":", "\t", 48));
                    if (cvExtensionData.getHash2() != null) {
                        out.append(cvExtensionData.getHash2().getHexSplit(":", "\t", 48));
                    }
                }
            }
            map_tableData.put("CV extensions", out.toString());
        }
        return out.toString();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane3 = new javax.swing.JTabbedPane();
        tab_detail = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jLayeredPane1 = new javax.swing.JLayeredPane();
        label_certIcon = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel1 = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        general_validFromLabel = new javax.swing.JLabel();
        general_holderRefernce = new javax.swing.JLabel();
        general_caReference = new javax.swing.JLabel();
        general_validFrom = new javax.swing.JLabel();
        general_labelTO = new javax.swing.JLabel();
        general_validTo = new javax.swing.JLabel();
        general_purposeLable = new javax.swing.JLabel();
        label_generlPurpose = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        selectedRowDetailTextArea = new javax.swing.JTextArea();
        jScrollPane1 = new javax.swing.JScrollPane();
        detail_table = new javax.swing.JTable(){
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return false;
            }
        };
        btn_ok = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jLayeredPane1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));

        label_certIcon.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        label_certIcon.setText("Card Verifiable Certificate (CVC) Information");

        jSeparator1.setBackground(new java.awt.Color(0, 0, 0));

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resized.png"))); // NOI18N

        jSeparator2.setBackground(new java.awt.Color(0, 0, 0));

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel2.setText("Holder Reference:");

        jLabel3.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel3.setText("CA Reference:");

        general_validFromLabel.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        general_validFromLabel.setText("Valid from");

        general_holderRefernce.setText("PK/PKDVCA1/DVPK1");

        general_caReference.setText("PK/CVPK1/PKCV1");

        general_validFrom.setText("20/01/2020");

        general_labelTO.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        general_labelTO.setText("to");

        general_validTo.setText("20/09/2020");

        general_purposeLable.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        general_purposeLable.setText("This CV Certificate (CVC) is intented for the following purpose(s):");

        label_generlPurpose.setText("• CVCA ");
        label_generlPurpose.setToolTipText("");
        label_generlPurpose.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        javax.swing.GroupLayout jLayeredPane1Layout = new javax.swing.GroupLayout(jLayeredPane1);
        jLayeredPane1.setLayout(jLayeredPane1Layout);
        jLayeredPane1Layout.setHorizontalGroup(
            jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jLayeredPane1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator1)
                    .addGroup(jLayeredPane1Layout.createSequentialGroup()
                        .addGap(36, 36, 36)
                        .addComponent(label_generlPurpose, javax.swing.GroupLayout.PREFERRED_SIZE, 304, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))))
            .addGroup(jLayeredPane1Layout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(label_certIcon)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(jLayeredPane1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jLayeredPane1Layout.createSequentialGroup()
                        .addGap(24, 24, 24)
                        .addGroup(jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(general_validFromLabel, javax.swing.GroupLayout.Alignment.TRAILING))
                        .addGap(18, 18, 18)
                        .addGroup(jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jLayeredPane1Layout.createSequentialGroup()
                                .addComponent(general_validFrom, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(general_labelTO)
                                .addGap(18, 18, 18)
                                .addComponent(general_validTo, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(general_holderRefernce, javax.swing.GroupLayout.DEFAULT_SIZE, 242, Short.MAX_VALUE)
                                .addComponent(general_caReference, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                    .addComponent(jSeparator2)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jLayeredPane1Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(general_purposeLable, javax.swing.GroupLayout.PREFERRED_SIZE, 374, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jLayeredPane1Layout.setVerticalGroup(
            jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jLayeredPane1Layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jLayeredPane1Layout.createSequentialGroup()
                        .addGap(5, 5, 5)
                        .addComponent(label_certIcon, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(2, 2, 2))
                    .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(general_purposeLable)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(label_generlPurpose, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(27, 27, 27)
                .addGroup(jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(general_holderRefernce))
                .addGap(25, 25, 25)
                .addGroup(jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(general_caReference))
                .addGap(25, 25, 25)
                .addGroup(jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(general_validFromLabel)
                    .addComponent(general_validFrom)
                    .addComponent(general_labelTO)
                    .addComponent(general_validTo))
                .addContainerGap(98, Short.MAX_VALUE))
        );
        jLayeredPane1.setLayer(label_certIcon, javax.swing.JLayeredPane.DEFAULT_LAYER);
        jLayeredPane1.setLayer(jSeparator1, javax.swing.JLayeredPane.DEFAULT_LAYER);
        jLayeredPane1.setLayer(jLabel1, javax.swing.JLayeredPane.DEFAULT_LAYER);
        jLayeredPane1.setLayer(jSeparator2, javax.swing.JLayeredPane.DEFAULT_LAYER);
        jLayeredPane1.setLayer(jLabel2, javax.swing.JLayeredPane.DEFAULT_LAYER);
        jLayeredPane1.setLayer(jLabel3, javax.swing.JLayeredPane.DEFAULT_LAYER);
        jLayeredPane1.setLayer(general_validFromLabel, javax.swing.JLayeredPane.DEFAULT_LAYER);
        jLayeredPane1.setLayer(general_holderRefernce, javax.swing.JLayeredPane.DEFAULT_LAYER);
        jLayeredPane1.setLayer(general_caReference, javax.swing.JLayeredPane.DEFAULT_LAYER);
        jLayeredPane1.setLayer(general_validFrom, javax.swing.JLayeredPane.DEFAULT_LAYER);
        jLayeredPane1.setLayer(general_labelTO, javax.swing.JLayeredPane.DEFAULT_LAYER);
        jLayeredPane1.setLayer(general_validTo, javax.swing.JLayeredPane.DEFAULT_LAYER);
        jLayeredPane1.setLayer(general_purposeLable, javax.swing.JLayeredPane.DEFAULT_LAYER);
        jLayeredPane1.setLayer(label_generlPurpose, javax.swing.JLayeredPane.DEFAULT_LAYER);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLayeredPane1)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLayeredPane1))
        );

        tab_detail.addTab("General", jPanel1);

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));

        selectedRowDetailTextArea.setEditable(false);
        selectedRowDetailTextArea.setColumns(20);
        selectedRowDetailTextArea.setRows(5);
        jScrollPane2.setViewportView(selectedRowDetailTextArea);

        jScrollPane1.setBackground(new java.awt.Color(255, 255, 255));
        jScrollPane1.setBorder(null);

        detail_table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null},
                {null, null},
                {null, null},
                {null, null}
            },
            new String [] {
                "Field", "Value"
            }
        ));
        detail_table.setGridColor(new java.awt.Color(255, 255, 255));
        detail_table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        detail_table.setShowHorizontalLines(false);
        detail_table.setShowVerticalLines(false);
        detail_table.getTableHeader().setResizingAllowed(false);
        detail_table.getTableHeader().setReorderingAllowed(false);
        jScrollPane1.setViewportView(detail_table);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane2)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 409, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 177, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(29, Short.MAX_VALUE))
        );

        tab_detail.addTab("Details", jPanel2);

        btn_ok.setText("OK");
        btn_ok.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_okActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(tab_detail)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btn_ok, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tab_detail)
                .addGap(18, 18, 18)
                .addComponent(btn_ok)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btn_okActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_okActionPerformed
        // TODO add your handling code here:
        System.exit(0);
    }//GEN-LAST:event_btn_okActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void display(CVCertificate objCertificate) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {

        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new CVCViewer(objCertificate).setVisible(true);
            }
        });
    }

    /**
     * @deprecated @return
     */
    public ImageIcon getCertificateIcon() {
        return new ImageIcon(new ImageIcon("icon.png").getImage().getScaledInstance(label_certIcon.getWidth(), label_certIcon.getHeight(), Image.SCALE_DEFAULT));
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btn_ok;
    private javax.swing.JTable detail_table;
    private javax.swing.JLabel general_caReference;
    private javax.swing.JLabel general_holderRefernce;
    private javax.swing.JLabel general_labelTO;
    private javax.swing.JLabel general_purposeLable;
    private javax.swing.JLabel general_validFrom;
    private javax.swing.JLabel general_validFromLabel;
    private javax.swing.JLabel general_validTo;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLayeredPane jLayeredPane1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JTabbedPane jTabbedPane3;
    private javax.swing.JLabel label_certIcon;
    private javax.swing.JLabel label_generlPurpose;
    private javax.swing.JTextArea selectedRowDetailTextArea;
    private javax.swing.JTabbedPane tab_detail;
    // End of variables declaration//GEN-END:variables
}
