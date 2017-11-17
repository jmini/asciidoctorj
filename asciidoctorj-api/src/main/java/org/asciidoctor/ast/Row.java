package org.asciidoctor.ast;

import java.util.List;

public interface Row {

    List<? extends Cell> getCells();

}
