package org.asciidoctor.internal;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Attributes;
import org.asciidoctor.DirectoryWalker;
import org.asciidoctor.Options;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.ast.ContentPart;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.DocumentHeader;
import org.asciidoctor.ast.NodeConverter;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.ast.StructuredDocument;
import org.asciidoctor.ast.Title;
import org.asciidoctor.ast.impl.ContentPartImpl;
import org.asciidoctor.ast.impl.DocumentHeaderImpl;
import org.asciidoctor.ast.impl.StructuredDocumentImpl;
import org.asciidoctor.converter.JavaConverterRegistry;
import org.asciidoctor.converter.internal.ConverterRegistryExecutor;
import org.asciidoctor.extension.ExtensionGroup;
import org.asciidoctor.extension.JavaExtensionRegistry;
import org.asciidoctor.extension.RubyExtensionRegistry;
import org.asciidoctor.extension.internal.ExtensionRegistryExecutor;
import org.jruby.Ruby;
import org.jruby.RubyHash;
import org.jruby.RubyInstanceConfig;
import org.jruby.exceptions.RaiseException;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

public class JRubyAsciidoctor implements Asciidoctor {

    private static final Logger logger = Logger.getLogger(JRubyAsciidoctor.class.getName());

    private static final String GEM_PATH = "GEM_PATH";

    private static final int DEFAULT_MAX_LEVEL = 1;

    private AsciidoctorModule asciidoctorModule;
    protected RubyGemsPreloader rubyGemsPreloader;
    protected Ruby rubyRuntime;

    private JRubyAsciidoctor(AsciidoctorModule asciidoctorModule, Ruby rubyRuntime) {
        super();
        this.asciidoctorModule = asciidoctorModule;
        this.rubyRuntime = rubyRuntime;
        this.rubyGemsPreloader = new RubyGemsPreloader(this.rubyRuntime);
    }

    public static JRubyAsciidoctor create() {
        return create((String) null);
    }

    public static JRubyAsciidoctor create(String gemPath) {
        return processRegistrations(createJRubyAsciidoctorInstance(Collections.singletonMap(GEM_PATH, gemPath), new ArrayList<String>(), null));
    }

    public static JRubyAsciidoctor create(List<String> loadPaths) {
        return processRegistrations(createJRubyAsciidoctorInstance(null, loadPaths, null));
    }

    public static JRubyAsciidoctor create(ClassLoader classloader) {
        return processRegistrations(createJRubyAsciidoctorInstance(null, new ArrayList<String>(), classloader));
    }

    public static JRubyAsciidoctor create(ClassLoader classloader, String gemPath) {
        return processRegistrations(createJRubyAsciidoctorInstance(Collections.singletonMap(GEM_PATH, gemPath), new ArrayList<String>(), classloader));
    }

    public static JRubyAsciidoctor create(List<String> loadPaths, String gemPath) {
        return processRegistrations(createJRubyAsciidoctorInstance(Collections.singletonMap(GEM_PATH, gemPath), loadPaths, null));
    }

    private static JRubyAsciidoctor processRegistrations(JRubyAsciidoctor asciidoctor) {
        registerExtensions(asciidoctor);
        registerConverters(asciidoctor);
        return asciidoctor;
    }

    private static void registerConverters(Asciidoctor asciidoctor) {
        new ConverterRegistryExecutor(asciidoctor).registerAllConverters();
    }

    private static void registerExtensions(Asciidoctor asciidoctor) {
        new ExtensionRegistryExecutor(asciidoctor).registerAllExtensions();
    }

    private static JRubyAsciidoctor createJRubyAsciidoctorInstance(Map<String, String> environmentVars, List<String> loadPaths, ClassLoader classloader) {

        Map<String, String> env = environmentVars != null ?
                new HashMap<String, String>(environmentVars) : new HashMap<String, String>();

        RubyInstanceConfig config = createOptimizedConfiguration();
        if (classloader != null) {
            config.setLoader(classloader);
        }
        injectEnvironmentVariables(config, env);

        Ruby rubyRuntime = JavaEmbedUtils.initialize(loadPaths, config);

        JRubyAsciidoctorModuleFactory jRubyAsciidoctorModuleFactory = new JRubyAsciidoctorModuleFactory(rubyRuntime);

        AsciidoctorModule asciidoctorModule = jRubyAsciidoctorModuleFactory.createAsciidoctorModule();
        JRubyAsciidoctor jRubyAsciidoctor = new JRubyAsciidoctor(asciidoctorModule, rubyRuntime);

        return jRubyAsciidoctor;
    }

    private static void injectEnvironmentVariables(RubyInstanceConfig config, Map<String, String> environmentVars) {
        EnvironmentInjector environmentInjector = new EnvironmentInjector(config);
        environmentInjector.inject(environmentVars);
    }

    private static RubyInstanceConfig createOptimizedConfiguration() {
        RubyInstanceConfig config = new RubyInstanceConfig();
        return config;
    }

    public Ruby getRubyRuntime() {
        return rubyRuntime;
    }

    private DocumentHeader toDocumentHeader(Document document) {

        Document documentImpl = (Document) NodeConverter.createASTNode(document);

        return DocumentHeaderImpl.createDocumentHeader((Title) documentImpl.getStructuredDoctitle(), documentImpl.getDoctitle(),
                documentImpl.getAttributes());
    }

    private StructuredDocument toDocument(Document document, Ruby rubyRuntime, int maxDeepLevel) {

        Document documentImpl = (Document) NodeConverter.createASTNode(document);
        List<ContentPart> contentParts = getContents(documentImpl.getBlocks(), 1, maxDeepLevel);
        return StructuredDocumentImpl.createStructuredDocument(toDocumentHeader(documentImpl), contentParts);
    }

    private List<ContentPart> getContents(List<StructuralNode> blocks, int level, int maxDeepLevel) {
        // finish getting childs if max structure level was riched
        if (level > maxDeepLevel) {
            return null;
        }
        // if document has only one child don't treat as actual contentpart
        // unless
        // it has no childs
        /*
         * if (blocks.size() == 1 && blocks.get(0).blocks().size() > 0) { return getContents(blocks.get(0).blocks(), 0,
         * maxDeepLevel); }
         */
        // add next level of contentParts
        List<ContentPart> parts = new ArrayList<ContentPart>();
        for (StructuralNode block : blocks) {
            parts.add(getContentPartFromBlock(block, level, maxDeepLevel));
        }
        return parts;
    }

    private ContentPart getContentPartFromBlock(StructuralNode child, int level, int maxDeepLevel) {
        Object content = child.content();
        String textContent;
        if (content instanceof String) {
            textContent = (String) content;
        } else {
            textContent = child.convert();
        }
        ContentPartImpl contentPart = ContentPartImpl.createContentPart(child.id(), level, child.context(), child.title(),
                child.style(), child.role(), child.getAttributes(), textContent);
        contentPart.setParts(getContents(child.blocks(), level + 1, maxDeepLevel));
        return contentPart;
    }

    @SuppressWarnings("unchecked")
    @Override
    public StructuredDocument readDocumentStructure(File filename, Map<String, Object> options) {

        this.rubyGemsPreloader.preloadRequiredLibraries(options);

        RubyHash rubyHash = RubyHashUtil.convertMapToRubyHashWithSymbols(rubyRuntime, options);
        Document document = this.asciidoctorModule.load_file(filename.getAbsolutePath(), rubyHash);
        int maxDeepLevel = options.containsKey(STRUCTURE_MAX_LEVEL) ? (Integer) (options.get(STRUCTURE_MAX_LEVEL))
                : DEFAULT_MAX_LEVEL;
        return toDocument(document, rubyRuntime, maxDeepLevel);
    }

    @SuppressWarnings("unchecked")
    @Override
    public StructuredDocument readDocumentStructure(String content, Map<String, Object> options) {

        this.rubyGemsPreloader.preloadRequiredLibraries(options);

        RubyHash rubyHash = RubyHashUtil.convertMapToRubyHashWithSymbols(rubyRuntime, options);

        Document document = this.asciidoctorModule.load(content, rubyHash);
        int maxDeepLevel = options.containsKey(STRUCTURE_MAX_LEVEL) ? (Integer) (options.get(STRUCTURE_MAX_LEVEL))
                : DEFAULT_MAX_LEVEL;
        return toDocument(document, rubyRuntime, maxDeepLevel);
    }

    @Override
    public StructuredDocument readDocumentStructure(Reader contentReader, Map<String, Object> options) {
        String content = IOUtils.readFull(contentReader);
        return readDocumentStructure(content, options);
    }

    @SuppressWarnings("unchecked")
    @Override
    public DocumentHeader readDocumentHeader(File filename) {

        RubyHash rubyHash = getParseHeaderOnlyOption();

        Document document = this.asciidoctorModule.load_file(filename.getAbsolutePath(), rubyHash);
        return toDocumentHeader(document);
    }

    @SuppressWarnings("unchecked")
    @Override
    public DocumentHeader readDocumentHeader(String content) {

        RubyHash rubyHash = getParseHeaderOnlyOption();

        Document document = this.asciidoctorModule.load(content, rubyHash);
        return toDocumentHeader(document);
    }

    @Override
    public DocumentHeader readDocumentHeader(Reader contentReader) {
        String content = IOUtils.readFull(contentReader);
        return this.readDocumentHeader(content);
    }

    private RubyHash getParseHeaderOnlyOption() {
        Map<String, Object> options = new HashMap<String, Object>();
        options.put("parse_header_only", true);
        RubyHash rubyHash = RubyHashUtil.convertMapToRubyHashWithSymbols(rubyRuntime, options);
        return rubyHash;
    }

    @SuppressWarnings("unchecked")
    @Override
    @Deprecated
    public String render(String content, Map<String, Object> options) {
        return convert(content, options);
    }

    @SuppressWarnings("unchecked")
    @Override
    @Deprecated
    public String renderFile(File filename, Map<String, Object> options) {
        return convertFile(filename, options);
    }

    /**
     * This method has been added to deal with the fact that asciidoctor 0.1.2 can return an Asciidoctor::Document or a
     * String depending if content is write to disk or not. This may change in the future
     * (https://github.com/asciidoctor/asciidoctor/issues/286)
     * 
     * @param object
     * @return
     */
    private String returnExpectedValue(Object object) {
        if (object instanceof String) {
            return object.toString();
        } else {
            return null;
        }
    }

    @Override
    @Deprecated
    public void render(Reader contentReader, Writer rendererWriter, Map<String, Object> options) throws IOException {
        String content = IOUtils.readFull(contentReader);
        String renderedContent = render(content, options);
        IOUtils.writeFull(rendererWriter, renderedContent);
    }

    @Override
    @Deprecated
    public String[] renderFiles(Collection<File> asciidoctorFiles, Map<String, Object> options) {
        List<String> asciidoctorContent = renderAllFiles(options, asciidoctorFiles);
        return asciidoctorContent.toArray(new String[asciidoctorContent.size()]);
    }

    @Override
    @Deprecated
    public String[] renderFiles(Collection<File> asciidoctorFiles, Options options) {
        return this.renderFiles(asciidoctorFiles, options.map());
    }

    @Override
    @Deprecated
    public String[] renderDirectory(DirectoryWalker directoryWalker, Map<String, Object> options) {

        final List<File> asciidoctorFiles = scanForAsciiDocFiles(directoryWalker);
        List<String> asciidoctorContent = renderAllFiles(options, asciidoctorFiles);

        return asciidoctorContent.toArray(new String[asciidoctorContent.size()]);
    }

    private List<String> renderAllFiles(Map<String, Object> options, final Collection<File> asciidoctorFiles) {
        List<String> asciidoctorContent = new ArrayList<String>();

        for (File asciidoctorFile : asciidoctorFiles) {
            String renderedFile = renderFile(asciidoctorFile, options);

            if (renderedFile != null) {
                asciidoctorContent.add(renderedFile);
            }

        }

        return asciidoctorContent;
    }

    private List<File> scanForAsciiDocFiles(DirectoryWalker directoryWalker) {
        final List<File> asciidoctorFiles = directoryWalker.scan();
        return asciidoctorFiles;
    }

    @Override
    @Deprecated
    public String render(String content, Options options) {
        return this.render(content, options.map());
    }

    @Override
    @Deprecated
    public void render(Reader contentReader, Writer rendererWriter, Options options) throws IOException {
        this.render(contentReader, rendererWriter, options.map());
    }

    @Override
    @Deprecated
    public String renderFile(File filename, Options options) {
        return this.renderFile(filename, options.map());
    }

    @Override
    @Deprecated
    public String[] renderDirectory(DirectoryWalker directoryWalker, Options options) {
        return this.renderDirectory(directoryWalker, options.map());
    }

    @Override
    @Deprecated
    public String render(String content, OptionsBuilder options) {
        return this.render(content, options.asMap());
    }

    @Override
    @Deprecated
    public void render(Reader contentReader, Writer rendererWriter, OptionsBuilder options) throws IOException {
        this.render(contentReader, rendererWriter, options.asMap());
    }

    @Override
    @Deprecated
    public String renderFile(File filename, OptionsBuilder options) {
        return this.renderFile(filename, options.asMap());
    }

    @Override
    @Deprecated
    public String[] renderDirectory(DirectoryWalker directoryWalker, OptionsBuilder options) {
        return this.renderDirectory(directoryWalker, options.asMap());
    }

    @Override
    @Deprecated
    public String[] renderFiles(Collection<File> asciidoctorFiles, OptionsBuilder options) {
        return this.renderFiles(asciidoctorFiles, options.asMap());
    }

    @Override
    public void requireLibrary(String... library) {
        requireLibraries(Arrays.asList(library));
    }

    @Override
    public void requireLibraries(Collection<String> libraries) {
        if (libraries != null) {
            for (String library : libraries) {
                RubyUtils.requireLibrary(rubyRuntime, library);
            }
        }
    }

    @Override
    public JavaExtensionRegistry javaExtensionRegistry() {
        return new JavaExtensionRegistry(asciidoctorModule, rubyRuntime);
    }

    @Override
    public RubyExtensionRegistry rubyExtensionRegistry() {
        return new RubyExtensionRegistry(asciidoctorModule, rubyRuntime);
    }

    @Override
    public JavaConverterRegistry javaConverterRegistry() {
        return new JavaConverterRegistry(asciidoctorModule, rubyRuntime);
    }

    @Override
    public void unregisterAllExtensions() {
        this.asciidoctorModule.unregister_all_extensions();
    }

    @Override
    public void shutdown() {
        this.rubyRuntime.tearDown();
    }

    @Override
    public String asciidoctorVersion() {
        return this.asciidoctorModule.asciidoctorRuntimeEnvironmentVersion();
    }

    @Override
    public String convert(String content, Map<String, Object> options) {
        return convert(content, options, String.class);
    }

    public <T> T convert(String content, Map<String, Object> options, Class<T> expectedResult) {

        this.rubyGemsPreloader.preloadRequiredLibraries(options);

        logger.fine(AsciidoctorUtils.toAsciidoctorCommand(options, "-"));

        if (AsciidoctorUtils.isOptionWithAttribute(options, Attributes.SOURCE_HIGHLIGHTER, "pygments")) {
            logger.fine("In order to use Pygments with Asciidoctor, you need to install Pygments (and Python, if you don't have it yet). Read http://asciidoctor.org/news/#syntax-highlighting-with-pygments.");
        }

        String currentDirectory = rubyRuntime.getCurrentDirectory();

        if (options.containsKey(Options.BASEDIR)) {
            rubyRuntime.setCurrentDirectory((String) options.get(Options.BASEDIR));
        }

        RubyHash rubyHash = RubyHashUtil.convertMapToRubyHashWithSymbols(rubyRuntime, options);

        try {
            Object object = this.asciidoctorModule.convert(content, rubyHash);
            if (object instanceof IRubyObject && NodeConverter.NodeType.DOCUMENT_CLASS.isInstance((IRubyObject) object)) {
                // If a document is rendered to a file Asciidoctor returns the document, we return null
                return null;
            }
            return (T) object;
        } catch(RaiseException e) {
            logger.severe(e.getException().getClass().getCanonicalName());
            throw new AsciidoctorCoreException(e);
        } finally {
            // we restore current directory to its original value.
            rubyRuntime.setCurrentDirectory(currentDirectory);
        }

    }


    @Override
    public String convert(String content, Options options) {
        return convert(content, options, String.class);
    }

    @Override
    public <T> T convert(String content, Options options, Class<T> expectedResult) {
        return convert(content, options.map(), expectedResult);
    }

    @Override
    public String convert(String content, OptionsBuilder options) {
        return convert(content, options, String.class);
    }

    @Override
    public <T> T convert(String content, OptionsBuilder options, Class<T> expectedResult) {
        return convert(content, options.asMap(), expectedResult);
    }

    @Override
    public void convert(Reader contentReader, Writer rendererWriter, Map<String, Object> options) throws IOException {
        this.render(contentReader, rendererWriter, options);
    }

    @Override
    public void convert(Reader contentReader, Writer rendererWriter, Options options) throws IOException {
        this.render(contentReader, rendererWriter, options);
    }

    @Override
    public void convert(Reader contentReader, Writer rendererWriter, OptionsBuilder options) throws IOException {
        this.render(contentReader, rendererWriter, options);
    }

    @Override
    public String convertFile(File filename, Map<String, Object> options) {
        return convertFile(filename, options, String.class);
    }

    @Override
    public <T> T convertFile(File filename, Map<String, Object> options, Class<T> expectedResult) {

        this.rubyGemsPreloader.preloadRequiredLibraries(options);

        logger.fine(AsciidoctorUtils.toAsciidoctorCommand(options, filename.getAbsolutePath()));

        String currentDirectory = rubyRuntime.getCurrentDirectory();

        if (options.containsKey(Options.BASEDIR)) {
            rubyRuntime.setCurrentDirectory((String) options.get(Options.BASEDIR));
        }

        RubyHash rubyHash = RubyHashUtil.convertMapToRubyHashWithSymbols(rubyRuntime, options);

        try {
            Object object = this.asciidoctorModule.convertFile(filename.getAbsolutePath(), rubyHash);
            if (object instanceof IRubyObject && NodeConverter.NodeType.DOCUMENT_CLASS.isInstance((IRubyObject) object)) {
                // If a document is rendered to a file Asciidoctor returns the document, we return null
                return null;
            }
            return (T) object;
        } catch(RaiseException e) {
            logger.severe(e.getMessage());

            throw new AsciidoctorCoreException(e);
        } finally {
            // we restore current directory to its original value.
            rubyRuntime.setCurrentDirectory(currentDirectory);
        }
    }

    @Override
    public String convertFile(File filename, Options options) {
        return convertFile(filename, options, String.class);
    }

    @Override
    public <T> T convertFile(File filename, Options options, Class<T> expectedResult) {
        return convertFile(filename, options.map(), expectedResult);
    }

    @Override
    public String convertFile(File filename, OptionsBuilder options) {
        return convertFile(filename, options.asMap(), String.class);
    }

    @Override
    public <T> T convertFile(File filename, OptionsBuilder options, Class<T> expectedResult) {
        return convertFile(filename, options.asMap(), expectedResult);
    }

    @Override
    public String[] convertDirectory(DirectoryWalker directoryWalker, Map<String, Object> options) {
        return renderDirectory(directoryWalker, options);
    }

    @Override
    public String[] convertDirectory(DirectoryWalker directoryWalker, Options options) {
        return renderDirectory(directoryWalker, options);
    }

    @Override
    public String[] convertDirectory(DirectoryWalker directoryWalker, OptionsBuilder options) {
        return renderDirectory(directoryWalker, options);
    }

    @Override
    public String[] convertFiles(Collection<File> asciidoctorFiles, Map<String, Object> options) {
        return renderFiles(asciidoctorFiles, options);
    }

    @Override
    public String[] convertFiles(Collection<File> asciidoctorFiles, Options options) {
        return renderFiles(asciidoctorFiles, options);
    }

    @Override
    public String[] convertFiles(Collection<File> asciidoctorFiles, OptionsBuilder options) {
        return renderFiles(asciidoctorFiles, options);
    }

    @Override
    public Document load(String content, Map<String, Object> options) {
        RubyHash rubyHash = RubyHashUtil.convertMapToRubyHashWithSymbols(rubyRuntime, options);
        return (Document) NodeConverter.createASTNode(this.asciidoctorModule.load(content, rubyHash));
    }

    @Override
    public Document loadFile(File file, Map<String, Object> options) {
        RubyHash rubyHash = RubyHashUtil.convertMapToRubyHashWithSymbols(rubyRuntime, options);
        return (Document) NodeConverter.createASTNode(this.asciidoctorModule.load_file(file.getAbsolutePath(), rubyHash));
    }

    AsciidoctorModule getAsciidoctorModule() {
        return asciidoctorModule;
    }

    @Override
    public ExtensionGroup createGroup() {
        return new ExtensionGroupImpl(UUID.randomUUID().toString(), this);
    }

    @Override
    public ExtensionGroup createGroup(String groupName) {
        return new ExtensionGroupImpl(groupName, this);
    }
}
