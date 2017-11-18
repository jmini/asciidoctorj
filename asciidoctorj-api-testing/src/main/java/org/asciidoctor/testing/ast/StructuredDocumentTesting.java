package org.asciidoctor.testing.ast;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.asciidoctor.ast.ContentPart;
import org.asciidoctor.ast.DocumentHeader;
import org.asciidoctor.ast.StructuredDocument;
import org.assertj.core.api.Assertions;
import org.junit.Test;

public abstract class StructuredDocumentTesting {

    protected abstract StructuredDocument createInstance(DocumentHeader header, List<ContentPart> parts);

    @Test
    public void testEmptyStructuredDocument() throws Exception {
        StructuredDocument structuredDocument = createInstance(null, Collections.emptyList());

        assertThat(structuredDocument.getHeader()).isNull();
        assertThat(structuredDocument.getParts()).isEmpty();

        assertThat(structuredDocument.getPartById("id")).isNull();
        assertThat(structuredDocument.getPartByRole("role")).isNull();
        assertThat(structuredDocument.getPartByStyle("style")).isNull();
        assertThat(structuredDocument.getPartsByContext("ctxt")).isEmpty();
        assertThat(structuredDocument.getPartsByRole("role")).isEmpty();
        assertThat(structuredDocument.getPartsByStyle("style")).isEmpty();

        assertThat(structuredDocument.getPartById(null)).isNull();
        assertThat(structuredDocument.getPartByRole(null)).isNull();
        assertThat(structuredDocument.getPartByStyle(null)).isNull();
        assertThat(structuredDocument.getPartsByContext(null)).isEmpty();
        assertThat(structuredDocument.getPartsByRole(null)).isEmpty();
        assertThat(structuredDocument.getPartsByStyle(null)).isEmpty();
    }

    @Test
    public void testStructuredDocument() throws Exception {
        DocumentHeader header = null;
        ContentPart part1 = ContentPartTesting.createMock("part1", "p1", "ctxt", "role", "style");
        ContentPart part2 = ContentPartTesting.createMock("part2", "p2", null, "role", null);
        ContentPart part3 = ContentPartTesting.createMock("part3", "p3", "ctxt", null, "style");

        List<ContentPart> parts = Arrays.asList(part1, part2, part3);
        StructuredDocument structuredDocument = createInstance(header, parts);

        assertThat(structuredDocument.getHeader()).isEqualTo(header);
        Assertions.<ContentPart>assertThat(structuredDocument.getParts()).usingElementComparator(ContentPartTesting.comparator()).containsExactly(part1, part2, part3);

        assertThat(structuredDocument.getPartById("p1")).usingComparator(ContentPartTesting.comparator()).isEqualTo(part1);
        assertThat(structuredDocument.getPartById("p2")).usingComparator(ContentPartTesting.comparator()).isEqualTo(part2);
        assertThat(structuredDocument.getPartById("p3")).usingComparator(ContentPartTesting.comparator()).isEqualTo(part3);
        assertThat(structuredDocument.getPartByRole("role")).usingComparator(ContentPartTesting.comparator()).isEqualTo(part1);
        assertThat(structuredDocument.getPartByStyle("style")).usingComparator(ContentPartTesting.comparator()).isEqualTo(part1);
        Assertions.<ContentPart>assertThat(structuredDocument.getPartsByContext("ctxt")).usingElementComparator(ContentPartTesting.comparator()).containsExactly(part1, part3);
        Assertions.<ContentPart>assertThat(structuredDocument.getPartsByRole("role")).usingElementComparator(ContentPartTesting.comparator()).containsExactly(part1, part2);
        Assertions.<ContentPart>assertThat(structuredDocument.getPartsByStyle("style")).usingElementComparator(ContentPartTesting.comparator()).containsExactly(part1, part3);

        assertThat(structuredDocument.getPartById("i")).isNull();
        assertThat(structuredDocument.getPartByRole("r")).isNull();
        assertThat(structuredDocument.getPartByStyle("s")).isNull();
        assertThat(structuredDocument.getPartsByContext("c")).isEmpty();
        assertThat(structuredDocument.getPartsByRole("r")).isEmpty();
        assertThat(structuredDocument.getPartsByStyle("s")).isEmpty();

        assertThat(structuredDocument.getPartById(null)).isNull();
        assertThat(structuredDocument.getPartByRole(null)).isNull();
        assertThat(structuredDocument.getPartByStyle(null)).isNull();
        assertThat(structuredDocument.getPartsByContext(null)).isEmpty();
        assertThat(structuredDocument.getPartsByRole(null)).isEmpty();
        assertThat(structuredDocument.getPartsByStyle(null)).isEmpty();
    }
}
