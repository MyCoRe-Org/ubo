package org.mycore.ubo;

import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.filter.Filters;
import org.mycore.datamodel.classifications2.MCRCategory;
import org.mycore.datamodel.classifications2.MCRCategoryDAO;
import org.mycore.datamodel.classifications2.MCRCategoryDAOFactory;
import org.mycore.datamodel.classifications2.MCRCategoryID;
import org.mycore.datamodel.classifications2.MCRLabel;
import org.mycore.datamodel.common.MCRXMLMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.List;

import static org.mycore.common.MCRConstants.MODS_NAMESPACE;
import static org.mycore.common.MCRConstants.XPATH_FACTORY;

/**
 * Collections of methods to be used in XSLT-1 stylesheets.
 *
 * @author shermann (Silvio Hermann)
 * */
public class DozBibXMLFunctions {

    /**
     * Retrieves a list of {@link MCRCategory}s having an <code>x-destatis</code> {@link MCRLabel}.
     *
     * @param mcrid the id of the {@link org.mycore.datamodel.metadata.MCRObject}
     *
     * @return a list of {@link MCRCategory}s having an <code>x-destatis</code> {@link MCRLabel}
     * @throws IOException
     * @throws JDOMException
     * @throws SAXException
     */
    public static List<MCRCategory> getDestatisCategories(MCRObjectID mcrid)
        throws IOException, JDOMException, SAXException {

        Document xml = MCRXMLMetadataManager.instance().retrieveXML(mcrid);
        MCRCategoryDAO dao = MCRCategoryDAOFactory.getInstance();

        return XPATH_FACTORY.compile(
                "//mods:mods/mods:classification[contains(@authorityURI,'ORIGIN')]", Filters.element(),
                null, MODS_NAMESPACE).evaluate(xml)
            .stream()
            .filter(element -> element.getAttributeValue("valueURI") != null)
            .map(element -> element.getAttributeValue("valueURI"))
            .map(valueURI -> valueURI.substring(valueURI.indexOf("#") + 1))
            .map(categId -> dao.getCategory(MCRCategoryID.fromString("ORIGIN:" + categId), 0))
            .filter(mcrCategory -> mcrCategory.getLabel("x-destatis").isPresent())
            .toList();
    }

    /**
     * Creates a list of unique destatis category identifiers as string.
     *
     * @param mcrid the id of the {@link MCRObject}
     *
     * @return a {@link String} of destatis identifiers separated by blanks
     * @throws IOException
     * @throws JDOMException
     * @throws SAXException
     */
    public static String getUniqueDestatisCategories(String mcrid) throws IOException, JDOMException, SAXException {
        List<String> list = DozBibXMLFunctions.getDestatisCategories(MCRObjectID.getInstance(mcrid))
            .stream()
            .map(mcrCategory -> mcrCategory.getLabel("x-destatis"))
            .map(mcrLabel -> mcrLabel.get())
            .distinct()
            .map(mcrLabel -> mcrLabel.getText())
            .toList();
        return String.join(" ", list);
    }
}
