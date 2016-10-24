package com.stackleader.kubefx.ui.actions;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.input.KeyCombination;

/**
 *
 * @author dcnorris
 */
public abstract class AbstractKubeAction implements KubeAction {

    private final BooleanProperty disabledProperty = new SimpleBooleanProperty(false);
    private final ObjectProperty<KeyCombination> acceleratorProperty = new SimpleObjectProperty<>();
    private final ObjectProperty<Node> graphicProperty = new SimpleObjectProperty<Node>(this, "graphic");
    private final StringProperty textProperty = new SimpleStringProperty();

    public AbstractKubeAction() {
    }

    public AbstractKubeAction(String text) {
        setText(text);
    }

    public AbstractKubeAction(String text, Node graphic) {
        setText(text);
        setGraphic(graphic);
    }

    public AbstractKubeAction(String text, Node graphic, KeyCombination accelerator) {
        setText(text);
        setGraphic(graphic);
        setAccelerator(accelerator);
    }

    @Override
    public final ObjectProperty<KeyCombination> acceleratorProperty() {
        return acceleratorProperty;
    }

    @Override
    public final KeyCombination getAccelerator() {
        return acceleratorProperty.get();
    }

    @Override
    public final void setAccelerator(KeyCombination keyCombination) {
        acceleratorProperty.set(keyCombination);
    }

    @Override
    public final ObjectProperty<Node> graphicProperty() {
        return graphicProperty;
    }

    @Override
    public final Node getGraphic() {
        return graphicProperty.get();
    }

    @Override
    public final void setGraphic(Node graphic) {
        graphicProperty.set(graphic);
    }

    @Override
    public final BooleanProperty disabledProperty() {
        return disabledProperty;
    }

    @Override
    public final boolean isDisabled() {
        return disabledProperty.get();
    }

    @Override
    public final void setDisabled(boolean value) {
        disabledProperty.set(value);
    }

    @Override
    public final StringProperty textProperty() {
        return textProperty;
    }

    @Override
    public final String getText() {
        return textProperty.get();
    }

    @Override
    public final void setText(String text) {
        textProperty.set(text);
    }

}
