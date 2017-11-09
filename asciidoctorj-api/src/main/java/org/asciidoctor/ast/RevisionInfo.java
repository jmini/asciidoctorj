package org.asciidoctor.ast;

public interface RevisionInfo {

    public String getDate();

    public void setDate(String date);

    public String getNumber();

    public void setNumber(String number);

    public String getRemark();

    public void setRemark(String remark);
}
