package org.asciidoctor.ast;

public interface Author {

    public String getFullName();

    public void setFullName(String fullName);

    public String getLastName();

    public void setLastName(String lastName);

    public String getFirstName();

    public void setFirstName(String firstName);

    public String getMiddleName();

    public void setMiddleName(String middleName);

    public String getEmail();

    public void setEmail(String email);

    public String getInitials();

    public void setInitials(String initials);
}
