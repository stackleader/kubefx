package com.stackleader.kubefx.kubernetes.api.model;

/**
 *
 * @author dcnorris
 */
public class Node {

    io.fabric8.kubernetes.api.model.Node node;

    public Node(io.fabric8.kubernetes.api.model.Node node) {
        this.node = node;
    }

}
