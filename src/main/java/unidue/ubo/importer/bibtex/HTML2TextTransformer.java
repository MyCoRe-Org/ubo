package unidue.ubo.importer.bibtex;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.mycore.common.MCRException;
import org.mycore.common.content.MCRByteContent;
import org.mycore.common.content.MCRContent;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.common.content.transformer.MCRContentTransformer;
import org.w3c.tidy.Tidy;
import org.xml.sax.SAXException;

/**
 * Converts html from given InputStream to xhtml and extracts the
 * text content of all elements below the body element.
 */
class HTML2TextTransformer extends MCRContentTransformer {

    final static Logger LOGGER = Logger.getLogger(HTML2TextTransformer.class);

    public MCRContent transform(MCRContent source) throws IOException {
        return transform(source, System.getProperty("file.encoding"));
    }

    public MCRContent transform(MCRContent source, String encoding) throws IOException {
        return transform(source.getInputStream(), encoding);
    }

    public MCRContent transform(InputStream in, String encoding) throws IOException {
        try {
            Tidy tidy = buildTidy(encoding);
            MCRContent xhtml = tidyHTML(in, tidy);
            MCRContent body = new MCRJDOMContent(detachBodyElement(xhtml));
            return new XML2TextTransformer().transform(body);
        } catch (SAXException ex) {
            throw new MCRException(ex);
        } catch (JDOMException ex) {
            throw new MCRException(ex);
        }
    }

    private Element detachBodyElement(MCRContent xhtml) throws JDOMException, IOException, SAXException {
        return xhtml.asXML().getRootElement().getChild("body").detach();
    }

    private MCRContent tidyHTML(InputStream in, Tidy tidy) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        tidy.parse(in, buffer);
        buffer.close();
        return new MCRByteContent(buffer.toByteArray());
    }

    private Tidy buildTidy(String encoding) {
        Tidy tidy = new Tidy();
        tidy.setInputEncoding(encoding);
        tidy.setOutputEncoding("UTF-8");
        tidy.setForceOutput(true);
        tidy.setFixComments(true);
        tidy.setHideEndTags(false);
        tidy.setQuiet(!LOGGER.isDebugEnabled());
        tidy.setShowWarnings(LOGGER.isDebugEnabled());
        tidy.setXmlOut(true);
        tidy.setXmlTags(false);
        tidy.setPrintBodyOnly(false);
        tidy.setNumEntities(true);
        return tidy;
    }
}