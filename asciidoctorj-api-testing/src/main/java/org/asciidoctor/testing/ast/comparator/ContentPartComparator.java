package org.asciidoctor.testing.ast.comparator;

import java.util.Comparator;

import org.asciidoctor.ast.ContentPart;

public class ContentPartComparator implements Comparator<ContentPart> {

    @Override
    public int compare(ContentPart o1, ContentPart o2) {
        return Comparator.comparing(ContentPart::getId).thenComparing(ContentPart::getContext).thenComparing(ContentPart::getStyle).compare(o1, o2);
    }

    public static Comparator<ContentPart> get() {
        return new ContentPartComparator();
    }

}
