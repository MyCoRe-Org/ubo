package org.mycore.ubo.importer.scopus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.mycore.common.config.MCRConfiguration2;
import org.xml.sax.SAXException;

abstract class AbstractScopusQuery {

    private static final Namespace NS_ATOM = Namespace.getNamespace("http://www.w3.org/2005/Atom");

    private static final String EID_PREFIX = "2-s2.0-";

    protected static String API_KEY;

    protected static String API_URL;

    protected static String INST_TOKEN;

    static {
        String prefix = "UBO.Scopus.API.";
        API_KEY = MCRConfiguration2.getStringOrThrow(prefix + "Key");
        API_URL = MCRConfiguration2.getStringOrThrow(prefix + "URL");
        INST_TOKEN = MCRConfiguration2.getString(prefix + "Insttoken").orElse("");
    }


    AbstractScopusQuery(){
    }

    public abstract List<String> resolveIDs() throws JDOMException,IOException,SAXException;


    protected List<String> getEntryScopusIDs(Document response) {
        List<String> scopusIDs = new ArrayList<String>();
        Element root = response.getRootElement();

        for (Element entry : root.getChildren("entry", NS_ATOM)) {
            Optional.ofNullable(getScopusIDFromContainer(entry))
                    .ifPresent(scopusIDs::add);
        }

        return scopusIDs;
    }

    protected String getScopusIDFromContainer(Element entry) {
        Optional<String> eid = Optional.ofNullable(entry.getChildTextTrim("eid", NS_ATOM))
                .or(()-> Optional.ofNullable(entry.getChildTextTrim("eid")));
        if (eid.isPresent() && !eid.get().isEmpty() && eid.get().startsWith(EID_PREFIX)) {
            return eid.get().replace(EID_PREFIX, "");

        }
        return null;
    }
}
