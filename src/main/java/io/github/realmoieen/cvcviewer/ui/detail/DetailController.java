package io.github.realmoieen.cvcviewer.ui.detail;

import io.github.realmoieen.cvcviewer.core.format.CertificateDetailFormatter;
import io.github.realmoieen.cvcviewer.core.format.DetailRow;
import io.github.realmoieen.cvcviewer.core.model.CVCertificate;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;

public class DetailController {

    @FXML
    private TableView<DetailRow> detailTable;
    @FXML
    private TableColumn<DetailRow, String> fieldColumn;
    @FXML
    private TableColumn<DetailRow, String> valueColumn;
    @FXML
    private TextArea selectedRowDetail;

    @FXML
    private void initialize() {
        fieldColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().field()));
        valueColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().value()));

        detailTable.getSelectionModel().selectedItemProperty().addListener((obs, oldRow, newRow) ->
                selectedRowDetail.setText(newRow != null ? newRow.value() : ""));
    }

    public void setCertificate(CVCertificate certificate) {
        detailTable.setItems(FXCollections.observableArrayList(CertificateDetailFormatter.format(certificate)));
        selectedRowDetail.clear();
    }
}
