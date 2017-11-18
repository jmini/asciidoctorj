package org.asciidoctor.testing.ast.comparator;

import static org.assertj.core.api.Assertions.assertThat;

import org.asciidoctor.ast.ContentPart;
import org.asciidoctor.testing.ast.ContentPartTesting;
import org.asciidoctor.testing.ast.comparator.ContentPartComparator;
import org.junit.Test;

public class ContentPartComparableTest {

    @Test
    public void testSame() throws Exception {
        ContentPart cp = ContentPartTesting.createMock("cp1", "id", "context", "role", "value");

        assertThat(cp).isSameAs(cp);
        assertThat(cp).isEqualTo(cp);
        assertThat(cp).usingComparator(ContentPartComparator.get()).isEqualTo(cp);
    }

    @Test
    public void testEqualTo() throws Exception {
        ContentPart cp1 = ContentPartTesting.createMock("cp1", "id", "context", "role", "value");
        ContentPart cp2 = ContentPartTesting.createMock("cp2", "id", "context", "role", "value");

        assertThat(cp1).isNotSameAs(cp2);
        assertThat(cp1).isNotEqualTo(cp2);
        assertThat(cp1).usingComparator(ContentPartComparator.get()).isEqualTo(cp2);
    }

    @Test
    public void testNotEqualTo() throws Exception {
        ContentPart cp1 = ContentPartTesting.createMock("cp1", "id1", "context", "role", "value");
        ContentPart cp2 = ContentPartTesting.createMock("cp2", "id2", "context", "role", "value");

        assertThat(cp1).isNotSameAs(cp2);
        assertThat(cp1).isNotEqualTo(cp2);
        assertThat(cp1).usingComparator(ContentPartComparator.get()).isNotEqualTo(cp2);
    }
}
