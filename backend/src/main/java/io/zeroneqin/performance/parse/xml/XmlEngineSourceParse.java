package io.zeroneqin.performance.parse.xml;

import io.zeroneqin.performance.engine.EngineContext;
import io.zeroneqin.performance.parse.EngineSourceParser;
import io.zeroneqin.performance.parse.xml.reader.DocumentParser;
import io.zeroneqin.performance.parse.xml.reader.DocumentParserFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;

public class XmlEngineSourceParse implements EngineSourceParser {
    @Override
    public String parse(EngineContext context, InputStream source) throws Exception {
        final InputSource inputSource = new InputSource(source);

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        DocumentBuilder docBuilder = factory.newDocumentBuilder();
        final Document document = docBuilder.parse(inputSource);

        final DocumentParser documentParser = createDocumentParser(context.getFileType());

        return documentParser.parse(context, document);
    }

    private DocumentParser createDocumentParser(String type) {
        return DocumentParserFactory.createDocumentParser(type);
    }
}
