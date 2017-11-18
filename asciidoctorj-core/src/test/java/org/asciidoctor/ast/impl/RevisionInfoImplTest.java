package org.asciidoctor.ast.impl;

import org.asciidoctor.ast.RevisionInfo;
import org.asciidoctor.testing.ast.RevisionInfoTesting;

public class RevisionInfoImplTest extends RevisionInfoTesting {

    @Override
    protected RevisionInfo createInstance(String date, String number, String remark) {
        RevisionInfoImpl revisionInfo = new RevisionInfoImpl();
        revisionInfo.setDate(date);
        revisionInfo.setNumber(number);
        revisionInfo.setRemark(remark);
        return revisionInfo;
    }
}
