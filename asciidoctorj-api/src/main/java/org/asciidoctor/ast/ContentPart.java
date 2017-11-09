package org.asciidoctor.ast;

import java.util.List;
import java.util.Map;

public interface ContentPart {

    public String getId();

    public int getLevel();

    public String getContext();

    public String getStyle();

    public String getRole();

    public String getTitle();

    public Map<String, Object> getAttributes();

    public String getContent();

    public List<ContentPart> getParts();

    public void setParts(List<ContentPart> parts);
}
