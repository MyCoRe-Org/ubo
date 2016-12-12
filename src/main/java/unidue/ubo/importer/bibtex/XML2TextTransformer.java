/**
 * Copyright (c) 2016 Duisburg-Essen University Library
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 **/

package unidue.ubo.importer.bibtex;

import java.io.IOException;
import java.util.List;

import org.jdom2.Content;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Text;
import org.mycore.common.MCRException;
import org.mycore.common.content.MCRContent;
import org.mycore.common.content.MCRStringContent;
import org.mycore.common.content.transformer.MCRContentTransformer;
import org.xml.sax.SAXException;

/**
 * Extracts text content from the given xml element and its descendant elements.
 */
class XML2TextTransformer extends MCRContentTransformer {

    public MCRContent transform(MCRContent source) throws IOException {
        try {
            Element root = source.asXML().getRootElement();
            String txt = xml2text(root);
            return new MCRStringContent(txt);
        } catch (SAXException ex) {
            throw new MCRException(ex);
        } catch (JDOMException ex) {
            throw new MCRException(ex);
        }
    }

    public static String xml2text(Element xml) {
        StringBuffer sb = new StringBuffer();
        xml2txt(xml, sb);
        return sb.toString();
    }

    private static void xml2txt(Element element, StringBuffer sb) {
        for (Content content : (List<Content>) (element.getContent()))
            if (content instanceof Element)
                xml2txt((Element) content, sb);
            else if (content instanceof Text)
                sb.append(((Text) content).getText());
    }
}
