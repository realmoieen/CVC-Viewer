package io.github.realmoieen.cvcviewer.ui.path;

import io.github.realmoieen.cvcviewer.core.model.CVCertificate;
import io.github.realmoieen.cvcviewer.ui.tree.CvcTreeBuilder;
import io.github.realmoieen.cvcviewer.ui.tree.CvcTreeCellFactory;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class PathController {

    @FXML
    private TreeView<CVCertificate> pathTree;
    @FXML
    private Button viewCertButton;
    @FXML
    private TextArea certStatusTextArea;

    private Consumer<List<CVCertificate>> onViewCertificate;

    @FXML
    private void initialize() {
        pathTree.setCellFactory(new CvcTreeCellFactory());
        pathTree.getSelectionModel().selectedItemProperty().addListener((obs, oldItem, selected) -> {
            if (selected == null) return;
            certStatusTextArea.setText(selected.getValue().getStatusString());
            viewCertButton.setDisable(selected.isLeaf());
        });
        viewCertButton.setOnAction(e -> {
            TreeItem<CVCertificate> selected = pathTree.getSelectionModel().getSelectedItem();
            if (selected == null || onViewCertificate == null) return;
            onViewCertificate.accept(buildChainToRoot(selected));
        });
    }

    public void setChain(List<CVCertificate> chain) {
        TreeItem<CVCertificate> root = CvcTreeBuilder.buildChainTree(chain);
        pathTree.setRoot(root);
        TreeItem<CVCertificate> leaf = CvcTreeBuilder.leafOf(root);
        pathTree.getSelectionModel().select(leaf);
    }

    public void setOnViewCertificate(Consumer<List<CVCertificate>> handler) {
        this.onViewCertificate = handler;
    }

    private List<CVCertificate> buildChainToRoot(TreeItem<CVCertificate> node) {
        List<CVCertificate> chain = new ArrayList<>();
        TreeItem<CVCertificate> current = node;
        while (current != null) {
            chain.add(current.getValue());
            current = current.getParent();
        }
        return chain;
    }
}
