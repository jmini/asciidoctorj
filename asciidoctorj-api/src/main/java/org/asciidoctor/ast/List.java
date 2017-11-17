package org.asciidoctor.ast;

public interface List extends StructuralNode {

    java.util.List<? extends StructuralNode> getItems();

    boolean hasItems();

    @Deprecated
    String render();

    String convert();

}
