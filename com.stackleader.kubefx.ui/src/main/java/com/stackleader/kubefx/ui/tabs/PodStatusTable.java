package com.stackleader.kubefx.ui.tabs;

import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;

/**
 *
 * @author dcnorris
 */
public class PodStatusTable<S> extends TableView<S> {

    public PodStatusTable(ObservableList<S> items) {
        super(items);
        setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        final StackPane placeHolder = new StackPane();
        placeHolder.getStyleClass().add("base");
        setPlaceholder(placeHolder);
        initializeTable();
    }

    public void initializeTable() {
        TableColumn<S, String> nameColumn = new TableColumn<>("Name");
        TableColumn<S, String> readyColumn = new TableColumn<>("Ready");
        TableColumn<S, String> statusColumn = new TableColumn<>("Status");
        TableColumn<S, String> ageColumn = new TableColumn<>("Age");
        nameColumn.setCellValueFactory(new PropertyValueFactory<S, String>("name"));
        readyColumn.setCellValueFactory(new PropertyValueFactory<S, String>("ready"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<S, String>("status"));
        ageColumn.setCellValueFactory(new PropertyValueFactory<S, String>("age"));
        nameColumn.prefWidthProperty().bind(widthProperty().multiply(.50));
        readyColumn.prefWidthProperty().bind(widthProperty().multiply(.15));
        statusColumn.prefWidthProperty().bind(widthProperty().multiply(.25));
        ageColumn.prefWidthProperty().bind(widthProperty().multiply(.10));
        getColumns().setAll(nameColumn, readyColumn, statusColumn, ageColumn);
    }
}
