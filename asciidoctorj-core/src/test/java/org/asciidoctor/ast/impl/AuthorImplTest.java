package org.asciidoctor.ast.impl;

import org.asciidoctor.ast.Author;
import org.asciidoctor.testing.ast.AuthorTesting;

public class AuthorImplTest extends AuthorTesting {

    @Override
    protected Author createInstance(String email, String fullName, String firstName, String lastName, String middleName, String initials) {
        AuthorImpl author = new AuthorImpl();
        author.setEmail(email);
        author.setFullName(fullName);
        author.setFirstName(firstName);
        author.setLastName(lastName);
        author.setMiddleName(middleName);
        author.setInitials(initials);
        return author;
    }
}
