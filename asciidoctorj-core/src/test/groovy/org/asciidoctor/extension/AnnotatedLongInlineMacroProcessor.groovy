package org.asciidoctor.extension

import groovy.transform.CompileStatic
import org.asciidoctor.ast.AbstractBlock

@CompileStatic
@Name('man')
@Format(FormatType.LONG)
class AnnotatedLongInlineMacroProcessor extends InlineMacroProcessor {

    public static final String RESULT = 'This content is added by this macro!'

    AnnotatedLongInlineMacroProcessor(String macroName) {
        super(macroName)
    }

    @Override
    Object process(AbstractBlock parent, String target, Map<String, Object> attributes) {
        Map<String, Object> options = new HashMap<String, Object>()
        options['type'] = ':link'
        options['target'] = "${target}.html"
        createInline(parent, 'anchor', target, attributes, options).convert()
    }
}