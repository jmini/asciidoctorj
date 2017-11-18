package org.asciidoctor.testing.ast;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.asciidoctor.ast.Author;
import org.junit.Test;

public abstract class AuthorTesting {

    protected abstract Author createInstance(String email, String fullName, String firstName, String lastName, String middleName, String initials);

    @Test
    public void testAuthor() throws Exception {
        Author author = createInstance("doc.writer@asciidoc.org", "Doc Writer", "Doc", "Writer", "Foo", "DW");
        assertThat(author.getEmail()).isEqualTo("doc.writer@asciidoc.org");
        assertThat(author.getFullName()).isEqualTo("Doc Writer");
        assertThat(author.getFirstName()).isEqualTo("Doc");
        assertThat(author.getLastName()).isEqualTo("Writer");
        assertThat(author.getMiddleName()).isEqualTo("Foo");
        assertThat(author.getInitials()).isEqualTo("DW");
    }

    public static Author createMock(String mockName, String email, String fullName, String firstName, String lastName, String middleName, String initials) {
        Author author = mock(Author.class, mockName);
        when(author.getEmail()).thenReturn(email);
        when(author.getFullName()).thenReturn(fullName);
        when(author.getFirstName()).thenReturn(firstName);
        when(author.getLastName()).thenReturn(lastName);
        when(author.getMiddleName()).thenReturn(middleName);
        when(author.getInitials()).thenReturn(initials);
        return author;
    }

}
