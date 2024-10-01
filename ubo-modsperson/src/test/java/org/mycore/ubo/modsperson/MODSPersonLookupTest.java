package org.mycore.ubo.modsperson;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.junit.Test;
import org.mycore.common.MCRConstants;
import org.mycore.common.MCRTestCase;
import org.mycore.common.content.MCRURLContent;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectMetadataTest;
import org.mycore.mods.MCRMODSWrapper;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import static org.junit.Assert.*;

public class MODSPersonLookupTest extends MCRTestCase {

    @Test
    public void testLookup() throws IOException, JDOMException {
        URL url1 = MCRObjectMetadataTest.class.getResource("/MODSPersonLookupTest/junit_mods_00000001.xml");
        Document doc1 = new MCRURLContent(url1).asXML();
        MCRObject obj1 = new MCRObject(doc1);

        URL url2 = MCRObjectMetadataTest.class.getResource("/MODSPersonLookupTest/junit_mods_00000002.xml");
        Document doc2 = new MCRURLContent(url2).asXML();
        MCRObject obj2 = new MCRObject(doc2);

        URL url3 = MCRObjectMetadataTest.class.getResource("/MODSPersonLookupTest/junit_mods_00000003.xml");
        Document doc3 = new MCRURLContent(url3).asXML();
        MCRObject obj3 = new MCRObject(doc3);

        MODSPersonLookup.add(obj1);
        MODSPersonLookup.add(obj2);

        MCRMODSWrapper wrapper = new MCRMODSWrapper(obj1);
        List<Element> names1 = wrapper.getElements("mods:name[@type='personal']");

        Element person1 = MODSPersonLookup.lookup(names1.get(0));
        System.out.println(new XMLOutputter(Format.getPrettyFormat()).outputString(person1));

        assertPersonElement(person1, "Müller", "Lisa", "11111", "1112222333");

        wrapper = new MCRMODSWrapper(obj2);
        List<Element> names2 = wrapper.getElements("mods:name[@type='personal']");
        Element person2 = MODSPersonLookup.lookup(names2.get(0));
        System.out.println(new XMLOutputter(Format.getPrettyFormat()).outputString(person2));

        assertPersonElement(person2, "Meyer", "Lena", "12345", "1112222444");

        wrapper = new MCRMODSWrapper(obj3);
        List<Element> names3 = wrapper.getElements("mods:name[@type='personal']");
        Element person3 = MODSPersonLookup.lookup(names3.get(0));
        assertNull(person3);
    }

    /**
     * Assert that the Element contains the two name parts and an LSF and SCOPUS ID
     * @param person tested Element
     * @param familyName the expected family name
     * @param givenName the expected given name
     * @param lsfId the expected LSF ID
     * @param scopusId the expected Scopus ID
     */
    private void assertPersonElement(Element person, String familyName,
        String givenName, String lsfId, String scopusId) {

        List<Element> nameElements = person.getChild("name", MCRConstants.MODS_NAMESPACE)
            .getChildren();
        List<Element> familyNameElements = nameElements.stream()
            .filter(e -> "family".equals(e.getAttributeValue("type")))
            .toList();

        assertEquals(1, familyNameElements.size());
        assertEquals(familyName, familyNameElements.get(0).getText());

        List<Element> givenNameElements = nameElements.stream()
            .filter(e -> "given".equals(e.getAttributeValue("type")))
            .toList();

        assertEquals(1, givenNameElements.size());
        assertEquals(givenName, givenNameElements.get(0).getText());

        List<Element> lsfElements = nameElements.stream()
            .filter(e -> "lsf".equals(e.getAttributeValue("type")))
            .toList();

        assertEquals(1, lsfElements.size());
        assertEquals(lsfId, lsfElements.get(0).getText());

        List<Element> scopusElements = nameElements.stream()
            .filter(e -> "scopus".equals(e.getAttributeValue("type")))
            .toList();

        assertEquals(1, scopusElements.size());
        assertEquals(scopusId, scopusElements.get(0).getText());
    }

}
