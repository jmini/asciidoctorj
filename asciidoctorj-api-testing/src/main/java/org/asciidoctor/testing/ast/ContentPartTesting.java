package org.asciidoctor.testing.ast;

import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsFirst;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Comparator;
import java.util.List;

import org.asciidoctor.ast.ContentPart;
import org.junit.Test;

public abstract class ContentPartTesting {

    protected abstract ContentPart createInstance(String id, String context, String role, String style);

    @Test
    public void testAuthor() throws Exception {
        ContentPart ContentPart = createInstance("contentId", "contentContext", "contentRole", "contentStyle");
        assertThat(ContentPart.getId()).isEqualTo("contentId");
        assertThat(ContentPart.getContext()).isEqualTo("contentContext");
        assertThat(ContentPart.getRole()).isEqualTo("contentRole");
        assertThat(ContentPart.getStyle()).isEqualTo("contentStyle");
    }

    public static ContentPart createMock(String mockName, String id, String context, String role, String style) {
        ContentPart contentPart = mock(ContentPart.class, mockName);
        when(contentPart.getId()).thenReturn(id);
        when(contentPart.getContext()).thenReturn(context);
        when(contentPart.getRole()).thenReturn(role);
        when(contentPart.getStyle()).thenReturn(style);
        return contentPart;
    }

    public static Comparator<ContentPart> comparator() {
        return Comparator.comparing(ContentPart::getId, nullsFirst(naturalOrder()))
                .thenComparingInt(ContentPart::getLevel)
                .thenComparing(ContentPart::getContext, nullsFirst(naturalOrder()))
                .thenComparing(ContentPart::getStyle, nullsFirst(naturalOrder()))
                .thenComparing(ContentPart::getRole, nullsFirst(naturalOrder()))
                .thenComparing(ContentPart::getTitle, nullsFirst(naturalOrder()))
                // TODO: .thenComparing(ContentPart::getAttributes, nullsFirst(naturalOrder()))
                .thenComparing(ContentPart::getParts, nullsFirst(listComparator()));
    }

    private static Comparator<List<? extends ContentPart>> listComparator() {
        return Comparator.<List<? extends ContentPart>>comparingInt(List::size)
                .thenComparing((Comparator<List<? extends ContentPart>>) (l1, l2) -> {
                    for (int i = 0; i < l1.size(); i++) {
                        int c = comparator().compare(l1.get(i), l2.get(i));
                        if (c != 0) {
                            return c;
                        }
                    }
                    return 0;
                });
    }

}
