package org.asciidoctor.ast.impl;

import java.util.List;

import org.asciidoctor.ast.ContentPart;
import org.asciidoctor.ast.DocumentHeader;
import org.asciidoctor.ast.StructuredDocument;
import org.asciidoctor.testing.ast.StructuredDocumentTesting;

public class StructuredDocumentImplTest extends StructuredDocumentTesting {

    @Override
    protected StructuredDocument createInstance(DocumentHeader header, List<ContentPart> parts) {
        return StructuredDocumentImpl.createStructuredDocument(header, parts);
    }

}
