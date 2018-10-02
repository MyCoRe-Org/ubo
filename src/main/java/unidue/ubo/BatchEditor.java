package unidue.ubo;

import java.io.IOException;
import java.text.MessageFormat;

import org.jaxen.JaxenException;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.mycore.access.MCRAccessException;
import org.mycore.common.MCRConstants;
import org.mycore.common.MCRPersistenceException;
import org.mycore.common.config.MCRConfiguration;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.common.xml.MCRNodeBuilder;
import org.mycore.common.xml.MCRXPathBuilder;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.cli.MCRAbstractCommands;
import org.mycore.frontend.cli.annotation.MCRCommand;
import org.mycore.frontend.cli.annotation.MCRCommandGroup;

/**
 * Commands to batch add/remove/replace values like categories and tags within XML.
 *
 * @author Frank L\u00FCtzenkirchen
 */
@MCRCommandGroup(name = "Batch Editor")
public class BatchEditor extends MCRAbstractCommands {

    private static final String CFG_PREFIX = "UBO.BatchEditor.";

    @MCRCommand(syntax = "batch edit object {0} remove {1} {2}",
        help = "Edit XML of object {0}, remove field {1} where value is {2}",
        order = 10)
    public static void batchRemove(String oid, String field, String value)
        throws MCRPersistenceException, MCRAccessException {
        MCRObject obj = getObject(oid);
        Document xml = obj.createXML();
        Element base = getBase(xml, field);
        String fieldPath = getFieldPath(field, value);
        remove(base, fieldPath);
        obj = new MCRObject(xml);
        MCRMetadataManager.update(obj);
    }

    private static void remove(Element base, String fieldPath) {
        XPathExpression<Element> xPath = XPathFactory.instance().compile(fieldPath, Filters.element(), null,
            MCRConstants.getStandardNamespaces());
        for (Element selected : xPath.evaluate(base)) {
            System.out.println("Removing " + MCRXPathBuilder.buildXPath(selected));
            selected.detach();
        }
    }

    @MCRCommand(syntax = "batch edit object {0} add {1} {2}",
        help = "Edit XML of object {0}, add field {1} where value is {2}",
        order = 10)
    public static void batchAdd(String oid, String field, String value)
        throws JaxenException, MCRPersistenceException, MCRAccessException {
        MCRObject obj = getObject(oid);
        Document xml = obj.createXML();
        Element base = getBase(xml, field);
        String fieldPath = getFieldPath(field, value);
        new MCRNodeBuilder().buildNode(fieldPath, null, base);
        obj = new MCRObject(xml);
        MCRMetadataManager.update(obj);
    }

    @MCRCommand(syntax = "batch edit object {0} replace {1} {2} with {3}",
        help = "Edit XML of object {0}, replace field {1} with value {2} by value {3}",
        order = 10)
    public static void batchReplace(String oid, String field, String oldValue, String newValue)
        throws JaxenException, MCRPersistenceException, MCRAccessException, IOException {
        MCRObject obj = getObject(oid);
        Document xml = obj.createXML();
        Element base = getBase(xml, field);
        remove(base, getFieldPath(field, oldValue));
        new MCRNodeBuilder().buildNode(getFieldPath(field, newValue), null, base);
        obj = new MCRObject(xml);
        System.out.println(new MCRJDOMContent(xml).asString());
        MCRMetadataManager.update(obj);
    }

    private static String getFieldPath(String field, String value) {
        String fieldPath = MCRConfiguration.instance().getString(CFG_PREFIX + field + ".Path");
        fieldPath = MessageFormat.format(fieldPath, value);
        return fieldPath;
    }

    private static Element getBase(Document xml, String field) {
        String basePath = MCRConfiguration.instance().getString(CFG_PREFIX + field + ".Base");
        XPathExpression<Element> xPath = XPathFactory.instance().compile(basePath, Filters.element(), null,
            MCRConstants.getStandardNamespaces());
        return xPath.evaluateFirst(xml);
    }

    private static MCRObject getObject(String oid) {
        MCRObjectID id = MCRObjectID.getInstance(oid);
        return MCRMetadataManager.retrieveMCRObject(id);
    }
}
