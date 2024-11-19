package org.mycore.ubo.modsperson;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
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
import java.util.Objects;
import java.util.Set;

import static org.junit.Assert.*;

public class MODSPersonLookupTest extends MCRTestCase {

    /**
     * Tests that only people added to the lookup can be found through that same lookup. Test that people
     * that are returned contain all added data.
     * @throws IOException in case of error
     * @throws JDOMException in case of error
     */
    @Test
    public void testLookup() throws IOException, JDOMException {
        URL url1 = MCRObjectMetadataTest.class.getResource("/MODSPersonLookupTest/junit_modsperson_00000010.xml");
        Document doc1 = new MCRURLContent(url1).asXML();
        MCRObject obj1 = new MCRObject(doc1);

        URL url2 = MCRObjectMetadataTest.class.getResource("/MODSPersonLookupTest/junit_modsperson_00000011.xml");
        Document doc2 = new MCRURLContent(url2).asXML();
        MCRObject obj2 = new MCRObject(doc2);

        URL url3 = MCRObjectMetadataTest.class.getResource("/MODSPersonLookupTest/junit_modsperson_00000012.xml");
        Document doc3 = new MCRURLContent(url3).asXML();
        MCRObject obj3 = new MCRObject(doc3);

        MODSPersonLookup.add(obj1);
        MODSPersonLookup.add(obj2);

        MCRMODSWrapper wrapper = new MCRMODSWrapper(obj1);
        Element name1 = wrapper.getElement("mods:name[@type='personal']");
        MODSPersonLookup.PersonCache person1 = Objects.requireNonNull(MODSPersonLookup.lookup(name1)).iterator().next();
        assertPerson(person1, "Müller", "Adam", "98765", "2222222333");

        assertEquals(2, person1.getAlternativeNames().size());
        person1.getAlternativeNames().forEach(name -> {
            if (name.getKey().equals("Müller")) {
                assertEquals("A", name.getValue());
            } else if (name.getKey().equals("Müller Meyer")) {
                assertEquals("Adam", name.getValue());
            } else {
                fail("Unknown alternative name:" + name.getKey() + ", " + name.getValue());
            }
        });

        wrapper = new MCRMODSWrapper(obj2);
        Element name2 = wrapper.getElement("mods:name[@type='personal']");
        MODSPersonLookup.PersonCache person2 = Objects.requireNonNull(MODSPersonLookup.lookup(name2)).iterator().next();
        assertPerson(person2, "Meyer", "Gustav", "112233", "2222222444");

        wrapper = new MCRMODSWrapper(obj3);
        Element name3 = wrapper.getElement("mods:name[@type='personal']");
        Set<MODSPersonLookup.PersonCache> persons3 = MODSPersonLookup.lookup(name3);
        assertNull(persons3);
    }

    /**
     * Tests if a widely different alternative name can be used to match a person.
     * @throws IOException in case of error
     * @throws JDOMException in case of error
     */
    @Test
    public void testLookupAlternateName() throws IOException, JDOMException {
        URL url1 = MCRObjectMetadataTest.class.getResource("/MODSPersonLookupTest/junit_modsperson_00000012.xml");
        Document doc1 = new MCRURLContent(url1).asXML();
        MCRObject person1 = new MCRObject(doc1);

        MODSPersonLookup.add(person1);

        URL url2 = MCRObjectMetadataTest.class.getResource("/MODSPersonLookupTest/junit_mods_00000013.xml");
        Document doc2 = new MCRURLContent(url2).asXML();
        MCRObject obj1 = new MCRObject(doc2);

        MCRMODSWrapper wrapper = new MCRMODSWrapper(obj1);
        Element name1 = wrapper.getElement("mods:name[@type='personal']");

        MODSPersonLookup.PersonCache personAlt = Objects.requireNonNull(MODSPersonLookup.lookup(name1)).iterator().next();
        assertNotNull(personAlt);
        assertPerson(personAlt, "Müller", "Gustavo", "77777", "555555666");
    }



    private void assertPerson(MODSPersonLookup.PersonCache assertPerson, String familyName,
        String givenName, String lsfId, String scopusId) {
        assertEquals(familyName, assertPerson.getFamilyName());
        assertEquals(givenName, assertPerson.getGivenName());
        assertEquals(2, assertPerson.getKeys().size());

        String lsfIdKey = String.join("|", lsfId, "lsf");
        String scopusIdKey = String.join("|", scopusId, "scopus");

        assertPerson.getKeys().forEach(key -> {
            assertTrue(key.equals(lsfIdKey) || key.equals(scopusIdKey));
        });
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
