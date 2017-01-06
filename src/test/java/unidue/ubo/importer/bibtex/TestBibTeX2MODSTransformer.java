/**
 * Copyright (c) 2016 Duisburg-Essen University Library
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 **/

package unidue.ubo.importer.bibtex;

import org.mycore.common.MCRConstants;
import org.mycore.common.MCRJPATestCase;
import org.mycore.common.content.MCRContent;
import org.mycore.common.content.MCRFileContent;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.common.content.MCRStringContent;
import org.mycore.common.content.MCRVFSContent;
import org.mycore.common.content.transformer.MCRContentTransformer;
import org.mycore.common.content.transformer.MCRContentTransformerFactory;
import org.mycore.common.xml.MCRXMLParserFactory;
import org.mycore.datamodel.classifications2.MCRCategory;
import org.mycore.datamodel.classifications2.MCRCategoryDAO;
import org.mycore.datamodel.classifications2.MCRCategoryDAOFactory;
import org.mycore.datamodel.classifications2.utils.MCRXMLTransformer;
import org.mycore.common.xml.MCRNodeBuilder;

import unidue.ubo.importer.bibtex.BibTeX2MODSTransformer;

import org.jdom2.Document;
import org.jdom2.Element;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

import java.net.URL;

public class TestBibTeX2MODSTransformer extends MCRJPATestCase {

    @Ignore
    @Test
    public void testLocalFile() throws Exception {
        String path = "E:\\home\\frank\\documents\\Universitätsbibliographie\\Spielberg.bib";
        MCRContent source = new MCRFileContent(path);
        MCRContent result = new BibTeX2MODSTransformer().transform(source);
        System.out.println(result.asString());
    }

    @Test
    public void testField2XPathTransformation() throws Exception {
        String src, res;

        src = "@article{Doe2015, title={MyCoRe in a nutshell}, author={Doe, John}}";
        res = "mods:mods[mods:genre='article']" + "[mods:titleInfo/mods:title='MyCoRe in a nutshell']"
            + "[mods:name[@type='personal'][mods:role/mods:roleTerm[@type='code'][@authority='marcrelator']='aut'][mods:namePart[@type='family']='Doe'][mods:namePart[@type='given']='John']]";
        testTransformation(src, res);

        src = "@inproceedings{Doe2015, title={MyCoRe in a nutshell}, author={Doe, John}, booktitle={Proceedings of the 5th MyCoRe User workshop}}";
        res = "mods:mods[mods:genre='inproceedings']" + "[mods:titleInfo/mods:title='MyCoRe in a nutshell']"
            + "[mods:name[@type='personal'][mods:role/mods:roleTerm[@type='code'][@authority='marcrelator']='aut'][mods:namePart[@type='family']='Doe'][mods:namePart[@type='given']='John']]"
            + "[mods:relatedItem[@type='host'][mods:genre='proceedings'][mods:titleInfo/mods:title='Proceedings of the 5th MyCoRe User workshop']]";
        testTransformation(src, res);

        src = "@article{Doe2015, title={MyCoRe in a nutshell}, author={Doe, John}, journal={Journal of Advanced Repository Software}}";
        res = "mods:mods[mods:genre='article']" + "[mods:titleInfo/mods:title='MyCoRe in a nutshell']"
            + "[mods:name[@type='personal'][mods:role/mods:roleTerm[@type='code'][@authority='marcrelator']='aut'][mods:namePart[@type='family']='Doe'][mods:namePart[@type='given']='John']]"
            + "[mods:relatedItem[@type='host'][mods:genre='journal'][mods:titleInfo/mods:title='Journal of Advanced Repository Software']]";
        testTransformation(src, res);

        src = "@article{Doe2015, title={MyCoRe}, author={Doe, John}, journal={Journal}, pages={34 -- 91}}";
        res = "mods:mods[mods:genre='article']" + "[mods:titleInfo/mods:title='MyCoRe']"
            + "[mods:name[@type='personal'][mods:role/mods:roleTerm[@type='code'][@authority='marcrelator']='aut'][mods:namePart[@type='family']='Doe'][mods:namePart[@type='given']='John']]"
            + "[mods:relatedItem[@type='host'][mods:genre='journal']" + "[mods:titleInfo/mods:title='Journal']"
            + "[mods:part/mods:extent[@unit='pages'][mods:start='34'][mods:end='91']]]";
        testTransformation(src, res);
    }

    @Test
    public void testMoveToRelatedItemTransformations() throws Exception {
        String src, res;

        src = "@book{Doe2015, title={MyCoRe unleashed}, editor={Doe, John}}";
        res = "mods:mods[mods:genre='book']" + "[mods:titleInfo/mods:title='MyCoRe unleashed']"
            + "[mods:name[@type='personal'][mods:role/mods:roleTerm[@type='code'][@authority='marcrelator']='edt'][mods:namePart[@type='family']='Doe'][mods:namePart[@type='given']='John']]";
        testTransformation(src, res);

        src = "@incollection{Doe2015, title={Introduction}, author={Foo, Bar}, booktitle={MyCoRe unleashed}, editor={Doe, John}}";
        res = "mods:mods[mods:genre='incollection']" + "[mods:titleInfo/mods:title='Introduction']"
            + "[mods:name[@type='personal'][mods:role/mods:roleTerm[@type='code'][@authority='marcrelator']='aut'][mods:namePart[@type='family']='Foo'][mods:namePart[@type='given']='Bar']]"
            + "[mods:relatedItem[@type='host'][mods:genre='collection']"
            + "[mods:titleInfo/mods:title='MyCoRe unleashed']"
            + "[mods:name[@type='personal'][mods:role/mods:roleTerm[@type='code'][@authority='marcrelator']='edt'][mods:namePart[@type='family']='Doe'][mods:namePart[@type='given']='John']]]";
        testTransformation(src, res);

        src = "@book{Doe2015, title={Introduction}, author={Foo, Bar}, edition={4th}}";
        res = "mods:mods[mods:genre='book']" + "[mods:titleInfo/mods:title='Introduction']"
            + "[mods:name[@type='personal'][mods:role/mods:roleTerm[@type='code'][@authority='marcrelator']='aut'][mods:namePart[@type='family']='Foo'][mods:namePart[@type='given']='Bar']]"
            + "[mods:originInfo/mods:edition='4th']";
        testTransformation(src, res);

        src = "@incollection{Doe2015, title={Introduction}, author={Foo, Bar}, booktitle={MyCoRe unleashed}, edition={4th}}";
        res = "mods:mods[mods:genre='incollection']" + "[mods:titleInfo/mods:title='Introduction']"
            + "[mods:name[@type='personal'][mods:role/mods:roleTerm[@type='code'][@authority='marcrelator']='aut'][mods:namePart[@type='family']='Foo'][mods:namePart[@type='given']='Bar']]"
            + "[mods:relatedItem[@type='host'][mods:genre='collection']"
            + "[mods:titleInfo/mods:title='MyCoRe unleashed']" + "[mods:originInfo/mods:edition='4th']]";
        testTransformation(src, res);

        src = "@article{Doe2015, title={MyCoRe in a nutshell}, author={Doe, John}, journal={Journal of Advanced Repository Software}, issn={1234-5678}}";
        res = "mods:mods[mods:genre='article']" + "[mods:titleInfo/mods:title='MyCoRe in a nutshell']"
            + "[mods:name[@type='personal'][mods:role/mods:roleTerm[@type='code'][@authority='marcrelator']='aut'][mods:namePart[@type='family']='Doe'][mods:namePart[@type='given']='John']]"
            + "[mods:relatedItem[@type='host'][mods:genre='journal'][mods:titleInfo/mods:title='Journal of Advanced Repository Software']"
            + "[mods:identifier[@type='issn']='1234-5678']]";
        testTransformation(src, res);

        src = "@incollection{Doe2015, title={MyCoRe in a nutshell}, author={Doe, John}, booktitle={Advanced Repository Software}, series={LNCS}, issn={1234-5678}}";
        res = "mods:mods[mods:genre='incollection']" + "[mods:titleInfo/mods:title='MyCoRe in a nutshell']"
            + "[mods:name[@type='personal'][mods:role/mods:roleTerm[@type='code'][@authority='marcrelator']='aut'][mods:namePart[@type='family']='Doe'][mods:namePart[@type='given']='John']]"
            + "[mods:relatedItem[@type='host'][mods:genre='collection'][mods:titleInfo/mods:title='Advanced Repository Software']"
            + "[mods:relatedItem[@type='series'][mods:titleInfo/mods:title='LNCS'][mods:identifier[@type='issn']='1234-5678']]]";
        testTransformation(src, res);
    }

    @Test
    public void testUnsupportedField() throws Exception {
        String src, res;

        src = "@book{Doe2015, title={MyCoRe unleashed}, author={Doe, John}, rating={ugly}}";
        res = "mods:mods[mods:genre='book']" + "[mods:titleInfo/mods:title='MyCoRe unleashed']"
            + "[mods:name[@type='personal'][mods:role/mods:roleTerm[@type='code'][@authority='marcrelator']='aut'][mods:namePart[@type='family']='Doe'][mods:namePart[@type='given']='John']]"
            + "[mods:extension[@type='fields'][field[@name='rating']='ugly']]";
        testTransformation(src, res);
    }

    private void testTransformation(String bibTeX, String expectedMODSXPath) throws Exception {
        MCRJDOMContent resultingContent = new BibTeX2MODSTransformer().transform(new MCRStringContent(bibTeX));
        Element resultingMODS = resultingContent.asXML().getRootElement().getChild("mods", MCRConstants.MODS_NAMESPACE)
            .detach();

        for (Element extension : resultingMODS.getChildren("extension", MCRConstants.MODS_NAMESPACE))
            if ("bibtex".equals(extension.getAttributeValue("format")))
                extension.detach();

        String result = new MCRJDOMContent(resultingMODS).asString();
        String expected = new MCRJDOMContent(new MCRNodeBuilder().buildElement(expectedMODSXPath, null, null))
            .asString();
        assertEquals(expected, result);
    }

    @Test
    public void testGenreMapping() throws Exception {
        URL url = this.getClass().getResource("/ubogenre.xml");
        org.jdom2.Document xml = MCRXMLParserFactory.getParser().parseXML(new MCRVFSContent(url));
        MCRCategory ubogenre = MCRXMLTransformer.getCategory(xml);

        startNewTransaction();
        MCRCategoryDAO DAO = MCRCategoryDAOFactory.getInstance();
        DAO.addCategory(null, ubogenre);

        String bibtex = "@incollection{Doe2015, title={MyCoRe in a nutshell}, booktitle={Advanced Repository Software}}";
        testGenreMapping(bibtex, "chapter", "collection");

        bibtex = "@inproceedings{Doe2015, title={MyCoRe in a nutshell}, booktitle={Advanced Repository Software}}";
        testGenreMapping(bibtex, "chapter", "proceedings");

        bibtex = "@article{Doe2015, title={MyCoRe in a nutshell}, author={Doe, John}, journal={Journal of Advanced Repository Software}}";
        testGenreMapping(bibtex, "article", "journal");
    }

    private void testGenreMapping(String bibtex, String genre, String host) throws Exception {

        MCRContentTransformer transformer = MCRContentTransformerFactory.getTransformer("import.BibTeX");

        Document bibentries = transformer.transform(new MCRStringContent(bibtex)).asXML();
        Element mods = bibentries.getRootElement().getChild("bibentry").getChild("mods", MCRConstants.MODS_NAMESPACE);

        assertEquals(genre, mods.getChildText("genre", MCRConstants.MODS_NAMESPACE));
        assertEquals(host, mods.getChild("relatedItem", MCRConstants.MODS_NAMESPACE).getChildText("genre",
            MCRConstants.MODS_NAMESPACE));
    }
}
