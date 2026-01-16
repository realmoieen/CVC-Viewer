package com.moieen.cvc.gui.cvcviewer;

import com.moieen.cvc.gui.cvcviewer.path.CvcTreeBuilder;
import com.moieen.cvc.gui.cvcviewer.path.CvcTreeNode;
import com.moieen.cvc.gui.cvcviewer.update.UpdateChecker;
import de.bsi.testbedutils.cvc.cvcertificate.*;
import de.bsi.testbedutils.cvc.cvcertificate.exception.CVBaseException;
import de.bsi.testbedutils.cvc.cvcertificate.exception.CVInvalidKeySourceException;
import de.bsi.testbedutils.cvc.cvcertificate.exception.CVKeyTypeNotSupportedException;
import de.bsi.testbedutils.cvc.cvcertificate.exception.CVMissingKeyException;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.math.ec.ECCurve;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.TreeNode;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.spec.RSAPublicKeySpec;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Moieen Abbas
 */
public class CVCViewer extends JFrame {
    private static boolean isUpdatedChecked = false;
    private DateFormat dateFormat = new SimpleDateFormat("dd/MM/yy");
    private Map<String, String> map_tableData = new LinkedHashMap<>();
    private List<CVCertificate> currentChain;   // leaf → root
    private CVCertificate currentCertificate;   // selected certificate
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton btn_ok;
    private JTextArea cert_status_textarea;
    private JButton copyToFileButton;
    private JTable detail_table;
    private JLabel general_caReference;
    private JLabel general_holderRefernce;
    private JLabel general_labelTO;
    private JLabel general_purposeLable;
    private JLabel general_validFrom;
    private JLabel general_validFromLabel;
    private JLabel general_validTo;
    private JLabel jLabel1;
    private JLabel jLabel2;
    private JLabel jLabel3;
    private JLabel jLabel4;
    private JLabel jLabel5;
    private JLayeredPane jLayeredPane1;
    private JPanel jPanel1;
    private JPanel jPanel2;
    private JPanel jPanel4;
    private JScrollPane jScrollPane1;
    private JScrollPane jScrollPane2;
    private JScrollPane jScrollPane3;
    private JScrollPane jScrollPane4;
    private JSeparator jSeparator1;
    private JSeparator jSeparator2;
    private JTabbedPane jTabbedPane3;
    private JLabel label_certIcon;
    private JTextArea label_generlPurpose;
    private JPanel path_tab;
    private JTree path_tree;
    private JTextArea selectedRowDetailTextArea;
    private JTabbedPane tab_detail;
    private JButton view_cert_button;
    // End of variables declaration//GEN-END:variables

    /**
     * Creates new form NewJFrame
     */
    public CVCViewer(List<CVCertificate> list_cvcerts, int defaultCloseOperation) {
        init();
        this.setDefaultCloseOperation(defaultCloseOperation);
        setLocationRelativeTo(null);
        CVCViewer cvcViewer = this;
        try {
            setCertificateDetail(list_cvcerts);
            if (!isUpdatedChecked) {
                new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() throws Exception {
                        UpdateChecker.checkForUpdate(cvcViewer);
                        isUpdatedChecked = true;
                        return null;
                    }
                }.execute();
            }
        } catch (Exception e) {
            ErrorDialogUtil.showError(this, e);
            System.exit(0);
        }
    }

    /**
     * @param objCertificate the command line arguments
     */
    public static void display(List<CVCertificate> objCertificate, int defaultCloseOperation) {
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
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                new CVCViewer(objCertificate, defaultCloseOperation).setVisible(true);
            }
        });
    }

    private void init() {
        this.setTitle("CV Certificate Viewer - "+Lunch.APP_VERSION);
        this.setResizable(false);
        this.setIconImage(new ImageIcon(getClass().getResource("/images/cvc-logo.png")).getImage());
        initComponents();
        label_certIcon.setIconTextGap(5);
        detail_table.getSelectionModel().addListSelectionListener((ListSelectionEvent event) -> {
            String selectedData = null;
            int selectedRow = detail_table.getSelectedRow();
            selectedData = (String) detail_table.getValueAt(selectedRow, 0);
            selectedData = map_tableData.getOrDefault(selectedData, selectedData);
            selectedRowDetailTextArea.setText(selectedData);
        });
        path_tree.addTreeSelectionListener(e -> {

            CvcTreeNode selectedNode = (CvcTreeNode) path_tree.getLastSelectedPathComponent();
            if (selectedNode == null) return;

            CVCertificate cert = selectedNode.getCertificate();

            // 1) Perform validity check
            String status = cert.getStatusString();

            // 2) Update textarea
            cert_status_textarea.setText(status);

            // 3) Enable / disable View button
            boolean isLeaf = selectedNode.isLeaf();
            view_cert_button.setEnabled(!isLeaf);
        });
    }

    private List<CVCertificate> buildChainToRoot(CvcTreeNode node) {

        List<CVCertificate> chain = new ArrayList<>();

        TreeNode current = node;

        while (current instanceof CvcTreeNode) {
            CvcTreeNode cvcNode = (CvcTreeNode) current;
            chain.add(cvcNode.getCertificate());
            current = cvcNode.getParent();
        }

        return chain; // leaf → root order
    }


    private void setCertificateDetail(List<CVCertificate> list_CVCertificate) {
        currentChain = list_CVCertificate;
        currentCertificate = currentChain.get(0);
        general_holderRefernce.setText(currentCertificate.getCertHolderRef());
        general_caReference.setText(currentCertificate.getCertAuthRef());
        CVHolderAuth cert_holderAuth = currentCertificate.getCertHolderAuth();
        ImageIcon imageIcon;
        try {
            currentCertificate.checkValidity();
            imageIcon = new ImageIcon(getClass().getResource("/images/cvc-valid.png"));
        } catch (CertificateNotYetValidException | CertificateExpiredException e) {
            imageIcon = new ImageIcon(getClass().getResource("/images/cvc-warning.png"));
        }
        label_certIcon.setIcon(new ImageIcon(imageIcon.getImage().getScaledInstance(52, label_certIcon.getHeight(), Image.SCALE_SMOOTH)));
        this.setIconImage(imageIcon.getImage());
        String status = "\n\n\n" + currentCertificate.getStatusString();
        if (currentCertificate.isReqCert()) {
            if (currentCertificate.hasOuterSignature()) {
                general_purposeLable.setText("This is a CVC Authenticated Request with outer signature");
                label_generlPurpose.setText("\u2022 Authenticated Request" + status);
                general_validFromLabel.setText("Outer CA Reference:");
                general_validFrom.setText(currentCertificate.getOuterAuthRef());
                general_labelTO.setText("");
                general_validTo.setText("");
            } else {
                general_purposeLable.setText("This is a CV Certificate Request with no purpose(s): ");
                label_generlPurpose.setText("\u2022 CV Request" + status);
                general_validFromLabel.setText("");
                general_validFrom.setText("");
                general_labelTO.setText("");
                general_validTo.setText("");
            }
        } else {
            general_purposeLable.setText("This CV Certificate (CVC) is intented for the following purpose(s):");
            if (cert_holderAuth != null && cert_holderAuth.getAuth() != null) {
                CVAuthorization cert_auth = currentCertificate.getCertHolderAuth().getAuth();
                label_generlPurpose.setText("\u2022 " + cert_auth.getRole().name() + status);
            }
            general_validFrom.setText(dateFormat.format(currentCertificate.getEffDate().getDate()));
            general_validTo.setText(dateFormat.format(currentCertificate.getExpDate().getDate()));
        }
        populateDetailTableData();
        CvcTreeBuilder.buildChainTree(path_tree, currentChain);
    }

    void populateDetailTableData() {

        map_tableData.put("Version", currentCertificate.getProfileId() + "");
        if (currentCertificate.getCertHolderRef() != null) {
            map_tableData.put("Holder Reference", currentCertificate.getCertHolderRef());
        }
        if (currentCertificate.getCertHolderAuth() != null && currentCertificate.getCertHolderAuth().getAuth() != null) {
            CVAuthorization auth = currentCertificate.getCertHolderAuth().getAuth();
            map_tableData.put("Role", auth.getRole().name());
            map_tableData.put("Term Type", auth.getTermType().name());
            map_tableData.put("Authorization", getCVAutorizationInfo(currentCertificate.getCertHolderAuth()));
        }
        if (currentCertificate.getCertAuthRef() != null) {
            map_tableData.put("CA Reference", currentCertificate.getCertAuthRef());
        }
        if (currentCertificate.hasOuterSignature()) {
            map_tableData.put("Outer CA Reference", currentCertificate.getOuterAuthRef());
        }
        if (currentCertificate.getEffDate() != null && currentCertificate.getEffDate().getDate() != null) {
            map_tableData.put("Valid From", dateFormat.format(currentCertificate.getEffDate().getDate()));
        }
        if (currentCertificate.getExpDate() != null && currentCertificate.getExpDate().getDate() != null) {
            map_tableData.put("Valid To", dateFormat.format(currentCertificate.getExpDate().getDate()));
        }
        getCVExtention(currentCertificate.getExtension());
        try {
            getPublicKeyDetails(currentCertificate);
        } catch (Exception e) {
            map_tableData.put("Public Key Detail", "Not available");
        }
        if (currentCertificate.getSignature() != null) {
            map_tableData.put("Signature", currentCertificate.getSignature().getHexSplit(":", "", 48));
        }
        if (currentCertificate.hasOuterSignature()) {
            map_tableData.put("Outer Signature", currentCertificate.getOuterSignature().getHexSplit(":", "", 48));
        }
        String[][] table_data = new String[map_tableData.size()][2];
        int i = 0;
        for (Map.Entry<String, String> entry : map_tableData.entrySet()) {

            table_data[i][0] = entry.getKey();
            table_data[i][1] = entry.getValue();
            i++;
        }
        detail_table.setModel(new DefaultTableModel(table_data, new String[]{"Field", "Value"}));
    }

    public void getPublicKeyDetails(CVCertificate obj_cvCert) throws Exception {
        if (obj_cvCert.getPublicKey().getAlgorithm().name().contains("RSA")) {
            getRSAPublicKeyInfos(obj_cvCert);
        } else if (obj_cvCert.getPublicKey().getAlgorithm().name().contains("ECDSA")) {
            getECPublicKeyInfos(obj_cvCert);
            ;
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

        jTabbedPane3 = new JTabbedPane();
        tab_detail = new JTabbedPane();
        jPanel1 = new JPanel();
        jLayeredPane1 = new JLayeredPane();
        label_certIcon = new JLabel();
        jSeparator1 = new JSeparator();
        jLabel1 = new JLabel();
        jSeparator2 = new JSeparator();
        jLabel2 = new JLabel();
        jLabel3 = new JLabel();
        general_validFromLabel = new JLabel();
        general_holderRefernce = new JLabel();
        general_caReference = new JLabel();
        general_validFrom = new JLabel();
        general_labelTO = new JLabel();
        general_validTo = new JLabel();
        general_purposeLable = new JLabel();
        label_generlPurpose = new JTextArea();
        jPanel2 = new JPanel();
        jScrollPane2 = new JScrollPane();
        selectedRowDetailTextArea = new JTextArea();
        jScrollPane1 = new JScrollPane();
        detail_table = new JTable() {
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return false;
            }
        };
        path_tab = new JPanel();
        jPanel4 = new JPanel();
        jScrollPane3 = new JScrollPane();
        path_tree = new JTree();
        view_cert_button = new JButton();
        jLabel4 = new JLabel();
        jLabel5 = new JLabel();
        jScrollPane4 = new JScrollPane();
        cert_status_textarea = new JTextArea();
        btn_ok = new JButton();
        copyToFileButton = new JButton();

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new Color(255, 255, 255));

        jLayeredPane1.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));

        label_certIcon.setFont(new Font("Tahoma", 1, 12)); // NOI18N
        label_certIcon.setText("Card Verifiable Certificate (CVC) Information");

        jSeparator1.setBackground(new Color(0, 0, 0));

        jSeparator2.setBackground(new Color(0, 0, 0));

        jLabel2.setFont(new Font("Tahoma", 1, 11)); // NOI18N
        jLabel2.setText("Holder Reference:");

        jLabel3.setFont(new Font("Tahoma", 1, 11)); // NOI18N
        jLabel3.setText("CA Reference:");

        general_validFromLabel.setFont(new Font("Tahoma", 1, 11)); // NOI18N
        general_validFromLabel.setText("Valid from");

        general_holderRefernce.setText("PK/PKDVCA1/DVPK1");

        general_caReference.setText("PK/CVPK1/PKCV1");

        general_validFrom.setText("20/01/2020");

        general_labelTO.setFont(new Font("Tahoma", 1, 11)); // NOI18N
        general_labelTO.setText("to");

        general_validTo.setText("20/09/2020");

        general_purposeLable.setFont(new Font("Tahoma", 1, 10)); // NOI18N
        general_purposeLable.setText("This CV Certificate (CVC) is intented for the following purpose(s):");

        label_generlPurpose.setEditable(false);
        label_generlPurpose.setFont(new Font("Tahoma", 0, 13)); // NOI18N
        label_generlPurpose.setRows(7);
        label_generlPurpose.setText("� CVCA ");
        label_generlPurpose.setToolTipText("");

        jLayeredPane1.setLayer(label_certIcon, JLayeredPane.DEFAULT_LAYER);
        jLayeredPane1.setLayer(jSeparator1, JLayeredPane.DEFAULT_LAYER);
        jLayeredPane1.setLayer(jLabel1, JLayeredPane.DEFAULT_LAYER);
        jLayeredPane1.setLayer(jSeparator2, JLayeredPane.DEFAULT_LAYER);
        jLayeredPane1.setLayer(jLabel2, JLayeredPane.DEFAULT_LAYER);
        jLayeredPane1.setLayer(jLabel3, JLayeredPane.DEFAULT_LAYER);
        jLayeredPane1.setLayer(general_validFromLabel, JLayeredPane.DEFAULT_LAYER);
        jLayeredPane1.setLayer(general_holderRefernce, JLayeredPane.DEFAULT_LAYER);
        jLayeredPane1.setLayer(general_caReference, JLayeredPane.DEFAULT_LAYER);
        jLayeredPane1.setLayer(general_validFrom, JLayeredPane.DEFAULT_LAYER);
        jLayeredPane1.setLayer(general_labelTO, JLayeredPane.DEFAULT_LAYER);
        jLayeredPane1.setLayer(general_validTo, JLayeredPane.DEFAULT_LAYER);
        jLayeredPane1.setLayer(general_purposeLable, JLayeredPane.DEFAULT_LAYER);
        jLayeredPane1.setLayer(label_generlPurpose, JLayeredPane.DEFAULT_LAYER);

        GroupLayout jLayeredPane1Layout = new GroupLayout(jLayeredPane1);
        jLayeredPane1.setLayout(jLayeredPane1Layout);
        jLayeredPane1Layout.setHorizontalGroup(
                jLayeredPane1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(jLayeredPane1Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jSeparator1))
                        .addGroup(GroupLayout.Alignment.TRAILING, jLayeredPane1Layout.createSequentialGroup()
                                .addGroup(jLayeredPane1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addGroup(GroupLayout.Alignment.TRAILING, jLayeredPane1Layout.createSequentialGroup()
                                                .addContainerGap()
                                                .addComponent(label_certIcon, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addGroup(jLayeredPane1Layout.createSequentialGroup()
                                                .addContainerGap()
                                                .addGroup(jLayeredPane1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                        .addGroup(jLayeredPane1Layout.createSequentialGroup()
                                                                .addGap(24, 24, 24)
                                                                .addGroup(jLayeredPane1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                                        .addComponent(jLabel2)
                                                                        .addComponent(jLabel3, GroupLayout.Alignment.TRAILING)
                                                                        .addComponent(general_validFromLabel, GroupLayout.Alignment.TRAILING))
                                                                .addGap(18, 18, 18)
                                                                .addGroup(jLayeredPane1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                                        .addGroup(jLayeredPane1Layout.createSequentialGroup()
                                                                                .addComponent(general_validFrom, GroupLayout.PREFERRED_SIZE, 83, GroupLayout.PREFERRED_SIZE)
                                                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                                                .addComponent(general_labelTO)
                                                                                .addGap(18, 18, 18)
                                                                                .addComponent(general_validTo, GroupLayout.PREFERRED_SIZE, 123, GroupLayout.PREFERRED_SIZE))
                                                                        .addGroup(jLayeredPane1Layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                                                                .addComponent(general_holderRefernce, GroupLayout.DEFAULT_SIZE, 242, Short.MAX_VALUE)
                                                                                .addComponent(general_caReference, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                                                        .addComponent(jSeparator2)
                                                        .addGroup(GroupLayout.Alignment.TRAILING, jLayeredPane1Layout.createSequentialGroup()
                                                                .addGap(0, 16, Short.MAX_VALUE)
                                                                .addComponent(general_purposeLable, GroupLayout.PREFERRED_SIZE, 374, GroupLayout.PREFERRED_SIZE))))
                                        .addGroup(jLayeredPane1Layout.createSequentialGroup()
                                                .addGap(27, 27, 27)
                                                .addComponent(jLabel1)
                                                .addGap(0, 0, Short.MAX_VALUE))
                                        .addGroup(GroupLayout.Alignment.TRAILING, jLayeredPane1Layout.createSequentialGroup()
                                                .addGap(0, 0, Short.MAX_VALUE)
                                                .addComponent(label_generlPurpose, GroupLayout.PREFERRED_SIZE, 364, GroupLayout.PREFERRED_SIZE)))
                                .addContainerGap())
        );
        jLayeredPane1Layout.setVerticalGroup(
                jLayeredPane1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(jLayeredPane1Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(label_certIcon, GroupLayout.PREFERRED_SIZE, 37, GroupLayout.PREFERRED_SIZE)
                                .addGap(2, 2, 2)
                                .addComponent(jLabel1)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jSeparator1, GroupLayout.PREFERRED_SIZE, 10, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(general_purposeLable)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(label_generlPurpose, GroupLayout.PREFERRED_SIZE, 71, GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jSeparator2, GroupLayout.PREFERRED_SIZE, 10, GroupLayout.PREFERRED_SIZE)
                                .addGap(27, 27, 27)
                                .addGroup(jLayeredPane1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel2)
                                        .addComponent(general_holderRefernce))
                                .addGap(25, 25, 25)
                                .addGroup(jLayeredPane1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel3)
                                        .addComponent(general_caReference))
                                .addGap(25, 25, 25)
                                .addGroup(jLayeredPane1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(general_validFromLabel)
                                        .addComponent(general_validFrom)
                                        .addComponent(general_labelTO)
                                        .addComponent(general_validTo))
                                .addContainerGap(107, Short.MAX_VALUE))
        );

        GroupLayout jPanel1Layout = new GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
                jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jLayeredPane1)
                                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
                jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jLayeredPane1))
        );

        tab_detail.addTab("General", jPanel1);

        jPanel2.setBackground(new Color(255, 255, 255));

        selectedRowDetailTextArea.setEditable(false);
        selectedRowDetailTextArea.setColumns(20);
        selectedRowDetailTextArea.setRows(5);
        jScrollPane2.setViewportView(selectedRowDetailTextArea);

        jScrollPane1.setBackground(new Color(255, 255, 255));
        jScrollPane1.setBorder(null);

        detail_table.setModel(new DefaultTableModel(
                new Object[][]{
                        {null, null},
                        {null, null},
                        {null, null},
                        {null, null}
                },
                new String[]{
                        "Field", "Value"
                }
        ));
        detail_table.setGridColor(new Color(255, 255, 255));
        detail_table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        detail_table.setShowHorizontalLines(false);
        detail_table.setShowVerticalLines(false);
        detail_table.getTableHeader().setResizingAllowed(false);
        detail_table.getTableHeader().setReorderingAllowed(false);
        jScrollPane1.setViewportView(detail_table);

        GroupLayout jPanel2Layout = new GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
                jPanel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanel2Layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                        .addComponent(jScrollPane2)
                                        .addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 409, Short.MAX_VALUE))
                                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
                jPanel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel2Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jScrollPane1, GroupLayout.PREFERRED_SIZE, 210, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jScrollPane2, GroupLayout.PREFERRED_SIZE, 177, GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(32, Short.MAX_VALUE))
        );

        tab_detail.addTab("Details", jPanel2);

        path_tab.setBackground(new Color(255, 255, 255));

        jPanel4.setBackground(new Color(255, 255, 255));
        jPanel4.setBorder(BorderFactory.createEtchedBorder());

        path_tree.setShowsRootHandles(true);
        path_tree.setRootVisible(true);
        jScrollPane3.setViewportView(path_tree);

        view_cert_button.setText("View Certificate");
        view_cert_button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                view_cert_buttonActionPerformed(evt);
            }
        });

        GroupLayout jPanel4Layout = new GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
                jPanel4Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel4Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanel4Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(jScrollPane3)
                                        .addGroup(GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                                                .addGap(0, 0, Short.MAX_VALUE)
                                                .addComponent(view_cert_button)))
                                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
                jPanel4Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel4Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jScrollPane3, GroupLayout.PREFERRED_SIZE, 242, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(view_cert_button)
                                .addContainerGap(12, Short.MAX_VALUE))
        );

        jLabel4.setText("CV Certificate Path");

        jLabel5.setText("Certificate Status:");

        cert_status_textarea.setEditable(false);
        cert_status_textarea.setColumns(20);
        cert_status_textarea.setFont(new Font("Tahoma", 0, 13)); // NOI18N
        cert_status_textarea.setRows(2);
        cert_status_textarea.setText("This Certificate is OK");
        cert_status_textarea.setBorder(BorderFactory.createCompoundBorder());
        jScrollPane4.setViewportView(cert_status_textarea);

        GroupLayout path_tabLayout = new GroupLayout(path_tab);
        path_tab.setLayout(path_tabLayout);
        path_tabLayout.setHorizontalGroup(
                path_tabLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(GroupLayout.Alignment.TRAILING, path_tabLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(path_tabLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                        .addComponent(jScrollPane4, GroupLayout.DEFAULT_SIZE, 414, Short.MAX_VALUE)
                                        .addComponent(jPanel4, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGroup(GroupLayout.Alignment.LEADING, path_tabLayout.createSequentialGroup()
                                                .addGroup(path_tabLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                        .addComponent(jLabel4)
                                                        .addComponent(jLabel5))
                                                .addGap(0, 0, Short.MAX_VALUE)))
                                .addContainerGap())
        );
        path_tabLayout.setVerticalGroup(
                path_tabLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(path_tabLayout.createSequentialGroup()
                                .addGap(5, 5, 5)
                                .addComponent(jLabel4, GroupLayout.PREFERRED_SIZE, 16, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jPanel4, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel5)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jScrollPane4, GroupLayout.DEFAULT_SIZE, 64, Short.MAX_VALUE)
                                .addContainerGap())
        );

        tab_detail.addTab("Path", path_tab);

        btn_ok.setText("OK");
        btn_ok.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                btn_okActionPerformed(evt);
            }
        });

        copyToFileButton.setText("Copy To File...");
        copyToFileButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                copyToFileButtonActionPerformed(evt);
            }
        });

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                        .addComponent(tab_detail)
                                        .addGroup(layout.createSequentialGroup()
                                                .addGap(0, 0, Short.MAX_VALUE)
                                                .addComponent(copyToFileButton)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(btn_ok, GroupLayout.PREFERRED_SIZE, 78, GroupLayout.PREFERRED_SIZE)))
                                .addContainerGap())
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(tab_detail)
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(btn_ok)
                                        .addComponent(copyToFileButton))
                                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btn_okActionPerformed(ActionEvent evt) {//GEN-FIRST:event_btn_okActionPerformed
        // TODO add your handling code here:
        if (getDefaultCloseOperation() == WindowConstants.EXIT_ON_CLOSE) {
            System.exit(0);
        } else {
            this.dispose();
        }
    }//GEN-LAST:event_btn_okActionPerformed

    private void view_cert_buttonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_view_cert_buttonActionPerformed
        CvcTreeNode selectedNode = (CvcTreeNode) path_tree.getLastSelectedPathComponent();
        if (selectedNode == null) return;

        // Build chain from selected node to root
        List<CVCertificate> chain = buildChainToRoot(selectedNode);

        // Display in viewer
        CVCViewer.display(chain, WindowConstants.DISPOSE_ON_CLOSE);
    }//GEN-LAST:event_view_cert_buttonActionPerformed

    private void copyToFileButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_copyToFileButtonActionPerformed
        if (currentCertificate == null) {
            JOptionPane.showMessageDialog(this, "No certificate selected");
            return;
        }

        JFileChooser chooser = new JFileChooser(System.getProperty("user.home"));
        chooser.setDialogTitle("Save Certificate");
        String fileName = currentCertificate.getCertHolderRef() != null ? currentCertificate.getCertHolderRef() : "certificate";
        if (currentCertificate.isReqCert()) {
            fileName = fileName + ".cvreq";
        } else {
            fileName = fileName + ".cvcert";
        }
        chooser.setSelectedFile(new File(fileName));
        String[] formats;
        if (currentChain.size() > 1) {
            formats = new String[]{
                    "Binary DER",
                    "Base64 Single Certificate",
                    "Base64 Chain (Comma Separated)",
                    "PEM Single Certificate",
                    "PEM Chain"
            };
        } else {
            formats = new String[]{
                    "Binary DER",
                    "Base64",
                    "PEM",
            };
        }

        String format = (String) JOptionPane.showInputDialog(
                this,
                "Select output format:",
                "Save As",
                JOptionPane.QUESTION_MESSAGE,
                null,
                formats,
                formats[0]
        );

        if (format == null) return;

        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();
        byte[] filedata = null;
        try {
            switch (format) {

                case "Binary DER":
                    filedata = CVCertificatePEMUtil.encodeCertificateToDer(currentCertificate);
                    break;

                case "Base64 Single Certificate":
                case "Base64":
                    filedata = CVCertificatePEMUtil.encodeCertificateToBase64(currentCertificate).getBytes(StandardCharsets.UTF_8);
                    break;

                case "Base64 Chain (Comma Separated)":
                    filedata = CVCertificatePEMUtil.encodeCertificateChainToBase64(currentChain).getBytes(StandardCharsets.UTF_8);
                    break;
                case "PEM Single Certificate":
                case "PEM":
                    filedata = CVCertificatePEMUtil.encodeCertificateToPEM(currentCertificate).getBytes(StandardCharsets.UTF_8);
                    break;

                case "PEM Chain":
                    filedata = CVCertificatePEMUtil.encodeCertificateChainToPEM(currentChain).getBytes(StandardCharsets.UTF_8);
                    break;
            }
            if (filedata != null) {
                int confirmDialog = JOptionPane.YES_OPTION;
                if (file.exists()) {
                    confirmDialog = JOptionPane.showConfirmDialog(this, "File already exists!\nDo you want to overwrite the existing certificate file?", "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                }
                if (confirmDialog == JOptionPane.YES_OPTION) {
                    Files.write(file.toPath(), filedata);
                    JOptionPane.showMessageDialog(this, "Certificate saved successfully");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Failed to save certificate. Content is empty");
            }

        } catch (CVBaseException | IOException ex) {
            ErrorDialogUtil.showError(this,
                    "Failed to save certificate:\n" + ex.getMessage(),
                    "File Not Saved!");
        }
    }//GEN-LAST:event_copyToFileButtonActionPerformed

    /**
     * @deprecated @return
     */
    public ImageIcon getCertificateIcon() {
        return new ImageIcon(new ImageIcon("icon.png").getImage().getScaledInstance(label_certIcon.getWidth(), label_certIcon.getHeight(), Image.SCALE_DEFAULT));
    }

}
