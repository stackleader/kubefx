package com.stackleader.kubefx.ui.tabs;

import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 *
 * @author dcnorris
 */
public class ServiceStatusTable<S> extends TableView<S> {

    public ServiceStatusTable(ObservableList<S> items) {
        super(items);
        initializeTable();
    }

    public void initializeTable() {
        TableColumn<S, String> nameColumn = new TableColumn<>("NAME");
        TableColumn<S, String> clusterIpColumn = new TableColumn<>("CLUSTER_IP");
        TableColumn<S, String> externalIpColumn = new TableColumn<>("EXTERNAL_IP");
        TableColumn<S, String> portsColumn = new TableColumn<>("PORT(S)");
        TableColumn<S, String> ageColumn = new TableColumn<>("AGE");

        nameColumn.setCellValueFactory(new PropertyValueFactory<S, String>("name"));
        clusterIpColumn.setCellValueFactory(new PropertyValueFactory<S, String>("clusterIp"));
        externalIpColumn.setCellValueFactory(new PropertyValueFactory<S, String>("externalIp"));
        portsColumn.setCellValueFactory(new PropertyValueFactory<S, String>("ports"));
        ageColumn.setCellValueFactory(new PropertyValueFactory<S, String>("age"));

        nameColumn.prefWidthProperty().bind(widthProperty().multiply(.25));
        clusterIpColumn.prefWidthProperty().bind(widthProperty().multiply(.20));
        externalIpColumn.prefWidthProperty().bind(widthProperty().multiply(.20));
        portsColumn.prefWidthProperty().bind(widthProperty().multiply(.15));
        ageColumn.prefWidthProperty().bind(widthProperty().multiply(.10));

        getColumns().setAll(nameColumn, clusterIpColumn, externalIpColumn, portsColumn, ageColumn);
    }
}
