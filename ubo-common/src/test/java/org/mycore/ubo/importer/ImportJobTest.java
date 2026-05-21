package org.mycore.ubo.importer;

import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.junit.jupiter.api.Test;
import org.mycore.common.MCRConstants;
import org.mycore.common.xml.MCRURIResolver;
import org.mycore.common.xsl.MCRLazyStreamSource;
import org.mycore.test.MyCoReTest;

import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@MyCoReTest
public class ImportJobTest {

    public static final String DUEPUBLICO_IMPORT_CALL = "xslStyle:import/duepublico2mods:resource:ImportJobTest/duepublico_mods_00000001.xml";

    /**
     * Tests the stylesheet import/duepublico2mods.xsl.
     */
    @Test
    public void testResolveDuepublico2Mods() throws TransformerException, IOException, JDOMException {
        MCRLazyStreamSource content = (MCRLazyStreamSource) MCRURIResolver
            .obtainInstance().resolve(DUEPUBLICO_IMPORT_CALL, null);
        Element mods = new SAXBuilder().build(content.getInputStream()).getRootElement();
        Element duepublicoIdentifier = mods.getChild("identifier", MCRConstants.MODS_NAMESPACE);
        assertEquals("duepublico2", duepublicoIdentifier.getAttribute("type").getValue());
        assertEquals("duepublico_mods_00000001", duepublicoIdentifier.getText());
        Element modsName = mods.getChild("name", MCRConstants.MODS_NAMESPACE);
        List<Element> nameIdentifiers = modsName.getChildren("nameIdentifier", MCRConstants.MODS_NAMESPACE);
        assertEquals(3, nameIdentifiers.size());

        // no other attributes like typeURI
        nameIdentifiers.forEach(element -> {
            assertEquals(1, element.getAttributes().size());
            assertNotNull(element.getAttribute("type"));
        });

        Set<String> expectedTypes = Set.of("lsf", "gnd", "orcid");
        Set<String> actualTypes = nameIdentifiers.stream()
            .map(element -> element.getAttributeValue("type"))
            .collect(Collectors.toSet());
        assertEquals(expectedTypes, actualTypes);

        Set<String> expectedValues = Set.of("1000000000", "0000-0000-0000-0001", "00000");
        Set<String> actualValues = nameIdentifiers.stream()
            .map(Element::getText)
            .collect(Collectors.toSet());
        assertEquals(expectedValues, actualValues);
    }
}
