package org.asciidoctor;

import static org.hamcrest.Matchers.either;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsMapContaining.hasKey;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.asciidoctor.arquillian.api.Unshared;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.Section;
import org.asciidoctor.internal.IOUtils;
import org.asciidoctor.util.ClasspathResources;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class WhenAsciiDocIsRenderedToDocument {

    private static final String DOCUMENT = "= Document Title\n" +
            "\n" +
            "preamble\n" +
            "\n" +
            "== Section A\n" +
            "\n" +
            "paragraph\n" +
            "\n" +
            "--\n" +
            "Exhibit A::\n" +
            "+\n" +
            "[#tiger.animal]\n" +
            "image::tiger.png[Tiger]\n" +
            "--\n" +
            "\n" +
            "image::cat.png[Cat]\n" +
            "\n" +
            "== Section B\n" +
            "\n" +
            "paragraph";

    private static final String ROLE = "[\"quote\", \"author\", \"source\", role=\"famous\"]\n" +
            "____\n" +
            "A famous quote.\n" +
            "____";

    private static final String REFTEXT = "[reftext=\"the first section\"]\n" +
            "== Section One\n" +
            "\n" +
            "content";

    @ArquillianResource(Unshared.class)
    private Asciidoctor asciidoctor;

    @ArquillianResource
    private ClasspathResources classpath = new ClasspathResources();

    @Test
    public void should_return_section_blocks() {
        Document document = asciidoctor.load(DOCUMENT, new HashMap<String, Object>());
        Section section = (Section) document.getBlocks().get(1);
        assertThat(section.getIndex(), is(0));
        assertThat(section.getSectionName(), either(is("sect1")).or(is("section")));
        assertThat(section.isSpecial(), is(false));
    }

    @Test
    public void should_return_blocks_from_a_document() {

        Document document = asciidoctor.load(DOCUMENT, new HashMap<String, Object>());
        assertThat(document.getDoctitle(), is("Document Title"));

    }

    @Test
    public void should_return_a_document_object_from_string() {

        Document document = asciidoctor.load(DOCUMENT, new HashMap<String, Object>());
        assertThat(document.getDoctitle(), is("Document Title"));
    }

    @Test
    public void should_find_elements_from_document() {

        Document document = asciidoctor.load(DOCUMENT, new HashMap<String, Object>());
        Map<Object, Object> selector = new HashMap<Object, Object>();
        selector.put("context", ":image");
        List<StructuralNode> findBy = document.findBy(selector);
        assertThat(findBy, hasSize(2));

        assertThat((String)findBy.get(0).getAttributes().get("target"), is("tiger.png"));
        assertThat(findBy.get(0).getLevel(), greaterThan(0));

    }

    @Test
    public void should_return_options_from_document() {
        Map<String, Object> options = OptionsBuilder.options().compact(true).asMap();
        Document document = asciidoctor.load(DOCUMENT, options);

        Map<Object, Object> documentOptions = document.getOptions();

        assertThat((Boolean) documentOptions.get("compact"), is(true));
    }

    @Test
    public void should_return_node_name() {
        Document document = asciidoctor.load(DOCUMENT, new HashMap<String, Object>());
        assertThat(document.getNodeName(), is("document"));
    }

    @Test
    public void should_return_if_it_is_inline() {
        Document document = asciidoctor.load(DOCUMENT, new HashMap<String, Object>());
        assertThat(document.isInline(), is(false));
    }

    @Test
    public void should_return_if_it_is_block() {
        Document document = asciidoctor.load(DOCUMENT, new HashMap<String, Object>());
        assertThat(document.isBlock(), is(true));
    }

    @Test
    public void should_be_able_to_manipulate_attributes() {
        Map<String, Object> options = OptionsBuilder.options()
                                                    .attributes(AttributesBuilder.attributes().dataUri(true))
                                                    .compact(true).asMap();
        Document document = asciidoctor.load(DOCUMENT, options);
        assertThat(document.getAttributes(), hasKey("toc-placement"));
        assertThat(document.hasAttribute("toc-placement"), is(true));
        assertThat(document.isAttribute("toc-placement", "auto", false), is(true));
        assertThat(document.getAttribute("toc-placement", "", false).toString(), is("auto"));
    }

    @Test
    public void should_be_able_to_get_roles() {
        Document document = asciidoctor.load(ROLE, new HashMap<String, Object>());
        StructuralNode abstractBlock = document.getBlocks().get(0);
        assertThat(abstractBlock.getRole(), is("famous"));
        assertThat(abstractBlock.hasRole("famous"), is(true));
        assertThat(abstractBlock.isRole(), is(true));
        assertThat(abstractBlock.getRoles(), contains("famous"));
    }

    @Test
    public void should_be_able_to_add_role() {
        final String tmpRole = "tmpRole";
        Document document = asciidoctor.load(ROLE, new HashMap<String, Object>());
        StructuralNode abstractBlock = document.getBlocks().get(0);
        assertThat(abstractBlock.hasRole(tmpRole), is(false));
        abstractBlock.addRole(tmpRole);
        assertThat(abstractBlock.hasRole(tmpRole), is(true));
    }

    @Test
    public void should_be_able_to_remove_role() {
        final String famousRole = "famous";
        Document document = asciidoctor.load(ROLE, new HashMap<String, Object>());
        StructuralNode abstractBlock = document.getBlocks().get(0);
        assertThat(abstractBlock.hasRole(famousRole), is(true));
        abstractBlock.removeRole(famousRole);
        assertThat(abstractBlock.hasRole(famousRole), is(false));
    }

    @Test
    public void should_be_able_to_get_reftext() {
        Document document = asciidoctor.load(REFTEXT, new HashMap<String, Object>());
        StructuralNode abstractBlock = document.getBlocks().get(0);
        assertThat(abstractBlock.getReftext(), is("the first section"));
        assertThat(abstractBlock.isReftext(), is(true));
    }

    @Test
    public void should_be_able_to_get_icon_uri_string_reference() {
        Map<String, Object> options = OptionsBuilder.options()
                .attributes(AttributesBuilder.attributes().dataUri(false))
                .compact(true).asMap();
        Document document = asciidoctor.load(DOCUMENT, options);
        assertThat(document.iconUri("note"), is("./images/icons/note.png"));
    }

    @Test
    public void should_be_able_to_get_icon_uri() {
        Map<String, Object> options = OptionsBuilder.options().safe(SafeMode.SAFE)
                .attributes(AttributesBuilder.attributes().dataUri(true).icons("font"))
                .compact(true).asMap();
        Document document = asciidoctor.load(DOCUMENT, options);
        assertThat(document.iconUri("note"),
            either(
                is("data:image/png:base64,") // <= Asciidoctor 1.5.5
            )
                .or(is("data:image/png;base64,"))); // >= Asciidoctor 1.5.6
    }

    @Test
    public void should_be_able_to_get_media_uri() {
        Document document = asciidoctor.load(DOCUMENT, new HashMap<String, Object>());
        assertThat(document.mediaUri("target"), is("target"));
    }

    @Test
    public void should_be_able_to_get_image_uri() {
        Map<String, Object> options = OptionsBuilder.options().safe(SafeMode.SAFE)
                .attributes(AttributesBuilder.attributes().dataUri(false))
                .compact(true).asMap();
        Document document = asciidoctor.load(DOCUMENT, options);
        assertThat(document.imageUri("target.jpg"), is("target.jpg"));
        assertThat(document.imageUri("target.jpg", "imagesdir"), is("target.jpg"));
    }

    @Test
    public void should_be_able_to_normalize_web_path() {
        Document document = asciidoctor.load(DOCUMENT, new HashMap<String, Object>());
        assertThat(document.normalizeWebPath("target", null, true), is("target"));
    }

    @Test
    public void should_be_able_to_read_asset() throws FileNotFoundException {
        Map<String, Object> options = OptionsBuilder.options().safe(SafeMode.SAFE)
                .attributes(AttributesBuilder.attributes().dataUri(false))
                .compact(true).asMap();
        Document document = asciidoctor.load(DOCUMENT, options);
        File inputFile = classpath.getResource("rendersample.asciidoc");
        String content = document.readAsset(inputFile.getAbsolutePath(), new HashMap<Object, Object>());
        assertThat(content.replace("\r", ""), is(IOUtils.readFull(new FileReader(inputFile)).replace("\r", "")));
    }

    @Test
    public void should_be_able_to_set_attribute() {
        final Object attributeName = "testattribute";
        final Object attributeValue = "testvalue";
        Document document = asciidoctor.load(DOCUMENT, new HashMap<String, Object>());
        assertThat(document.setAttribute(attributeName, attributeValue, true), is(true));
        assertThat(document.getAttribute(attributeName), is(attributeValue));
        assertThat(document.getAttributes().get(attributeName), is(attributeValue));
    }

    @Test
    public void should_be_able_to_set_attribute_on_attributes_map() {
        final String attributeName = "testattribute";
        final Object attributeValue = "testvalue";
        Document document = asciidoctor.load(DOCUMENT, new HashMap<String, Object>());
        document.getAttributes().put(attributeName, attributeValue);
        assertThat(document.getAttribute(attributeName), is(attributeValue));
        assertThat(document.getAttributes().get(attributeName), is(attributeValue));
    }

    @Test
    public void should_be_able_to_get_source_location() {
        // Given
        File file = classpath.getResource("sourcelocation.adoc");

        // When
        Document document = asciidoctor.loadFile(file, OptionsBuilder.options().option("sourcemap", "true").docType("book").asMap());
        Map<Object, Object> selector = new HashMap<Object, Object>();
        selector.put("context", ":paragraph");
        List<StructuralNode> findBy = document.findBy(selector);
        StructuralNode block = findBy.get(0);

        // Then
        StructuralNode block1 = findBy.get(0);
        assertThat(block1.getSourceLocation().getLineNumber(), is(3));
        assertThat(block1.getSourceLocation().getPath(), is(file.getName()));
        assertThat(block1.getSourceLocation().getFile(), is(file.getName()));
        assertThat(block1.getSourceLocation().getDir(), is(file.getParent().replaceAll("\\\\", "/")));

        StructuralNode block2 = findBy.get(1);
        assertThat(block2.getSourceLocation().getLineNumber(), is(8));
        assertThat(block2.getSourceLocation().getPath(), is(file.getName()));
        assertThat(block2.getSourceLocation().getFile(), is(file.getName()));
        assertThat(block2.getSourceLocation().getDir(), is(file.getParent().replaceAll("\\\\", "/")));
    }

    @Test
    public void should_get_attributes() {

        final String documentWithAttributes = "= Document Title\n" +
            ":docattr: docvalue\n" +
            "\n" +
            "preamble\n" +
            "\n" +
            "== Section A\n" +
            "\n" +
            "paragraph\n" +
            "\n";

        Document document = asciidoctor.load(documentWithAttributes, new HashMap<String, Object>());
        List<StructuralNode> blocks = document.getBlocks();

        Section section = (Section) blocks.get(1);
        section.setAttribute("testattr", "testvalue", true);

        assertThat(document.hasAttribute("testattr"), is(false));

        assertThat(section.hasAttribute("testattr"), is(true));
        assertThat(section.hasAttribute("testattr", true), is(true));
        assertThat(section.hasAttribute("testattr", false), is(true));
        assertThat(section.isAttribute("testattr", "testvalue"), is(true));

        assertThat(section.hasAttribute("docattr", true), is(true));
        assertThat(section.hasAttribute("docattr", false), is(false));
    }

    @Test
    public void should_get_content_model() {
    	final String documentWithPreambleAndSection = ""
    			+ "= Document Title\n"
    			+ "\n"
    			+ "A test document with a preamble and a section.\n"
    			+ "\n"
    			+ "The preamble contains multiple paragraphs to force the outer block to be compound.\n"
    			+ "\n"
    			+ "== First Section\n"
    			+ "\n"
    			+ "And herein lies the problem.\n"
    			+ "\n";

        Document document = asciidoctor.load(documentWithPreambleAndSection, new HashMap<String, Object>());
        List<StructuralNode> blocks = document.getBlocks();

        StructuralNode preambleContainer = blocks.get(0);
        assertThat(preambleContainer.getContentModel(), is("compound"));

        assertThat(preambleContainer.getBlocks().get(0).getContentModel(), is("simple"));
        assertThat(preambleContainer.getBlocks().get(1).getContentModel(), is("simple"));

        Section section = (Section) blocks.get(1);
        assertThat(section.getContentModel(), is("compound"));
        assertThat(section.getBlocks().get(0).getContentModel(), is("simple"));
    }
}
