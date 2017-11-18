package org.asciidoctor.testing.ast;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.asciidoctor.ast.RevisionInfo;
import org.junit.Test;

public abstract class RevisionInfoTesting {

    protected abstract RevisionInfo createInstance(String date, String number, String remark);

    @Test
    public void testRevisionInfo() throws Exception {
        RevisionInfo revisionInfo = createInstance("01.01.2017", "1.0.0", "Foo");
        assertThat(revisionInfo.getDate()).isEqualTo("01.01.2017");
        assertThat(revisionInfo.getNumber()).isEqualTo("1.0.0");
        assertThat(revisionInfo.getRemark()).isEqualTo("Foo");
    }

    public static RevisionInfo createMock(String mockName, String date, String number, String remark) {
        RevisionInfo revisionInfo = mock(RevisionInfo.class, mockName);
        when(revisionInfo.getDate()).thenReturn(date);
        when(revisionInfo.getNumber()).thenReturn(number);
        when(revisionInfo.getRemark()).thenReturn(remark);
        return revisionInfo;
    }

}
