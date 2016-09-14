package com.stackleader.kubefx.ui.tabs;

import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 *
 * @author dcnorris
 */
public class NodeStatusTable<S> extends TableView<S> {

    public NodeStatusTable(ObservableList<S> items) {
        super(items);
        initializeTable();
    }

    public void initializeTable() {
        TableColumn<S, String> nameColumn = new TableColumn<>("Name");
        TableColumn<S, String> labelsColumn = new TableColumn<>("Labels");
        TableColumn<S, String> statusColumn = new TableColumn<>("Status");
        TableColumn<S, String> ageColumn = new TableColumn<>("Age");
        nameColumn.setCellValueFactory(new PropertyValueFactory<S, String>("name"));
        labelsColumn.setCellValueFactory(new PropertyValueFactory<S, String>("labelString"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<S, String>("status"));
        ageColumn.setCellValueFactory(new PropertyValueFactory<S, String>("age"));
        nameColumn.prefWidthProperty().bind(widthProperty().multiply(.40));
        labelsColumn.prefWidthProperty().bind(widthProperty().multiply(.35));
        statusColumn.prefWidthProperty().bind(widthProperty().multiply(.15));
        ageColumn.prefWidthProperty().bind(widthProperty().multiply(.10));
        getColumns().setAll(nameColumn, labelsColumn, statusColumn, ageColumn);
    }
}
