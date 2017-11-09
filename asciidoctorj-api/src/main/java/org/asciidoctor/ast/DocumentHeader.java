package org.asciidoctor.ast;

import java.util.List;
import java.util.Map;

public interface DocumentHeader {

    public List<Author> getAuthors();

    public Title getDocumentTitle();

    public String getPageTitle();

    public Author getAuthor();

    public RevisionInfo getRevisionInfo();

    public Map<String, Object> getAttributes();
}
