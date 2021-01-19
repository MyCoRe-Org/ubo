/**
 * Copyright (c) 2018 Duisburg-Essen University Library
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 **/

package org.mycore.ubo.importer;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.jdom2.Element;
import org.jdom2.transform.JDOMSource;
import org.mycore.common.MCRConstants;
import org.mycore.common.config.MCRConfiguration2;

/**
 * ISSN-Matching of Gold OA Journals (ISSN-GOLD-OA) 2.0
 * Rimmert C, Bruns A, Lenke C, Taubert NC (2017) : Bielefeld University.
 * Source: http://doi.org/10.4119/unibi/2913654
 *
 * @author Frank L\u00FCtzenkirchen
 */

public class ISSNGoldResolver implements URIResolver {

    private final static String CSV_FILE = MCRConfiguration2.getString("UBO.ISSNGold.File").get();

    private final static String ENCODING = "UTF-8";

    private final static String AUTHORITY_URI = "https://bibliographie.ub.uni-due.de/classifications/oa";

    private Map<String, String> issn2label = new HashMap<String, String>();

    public ISSNGoldResolver() throws IOException {
        InputStream in = ISSNGoldResolver.class.getResourceAsStream("/" + CSV_FILE);
        Reader reader = new InputStreamReader(in, ENCODING);

        CSVFormat format = buildCSVFormat();
        CSVParser parser = format.parse(reader);

        for (CSVRecord record : parser.getRecords()) {
            String issn1 = record.get(0);
            String issn2 = record.get(1);
            String label = record.get(14);

            issn2label.put(issn1, label);
            issn2label.put(issn2, label);
        }
    }

    private CSVFormat buildCSVFormat() {
        CSVFormat format = CSVFormat.DEFAULT.withDelimiter(',').withQuote('"').withEscape('\\');
        format = format.withIgnoreEmptyLines(true).withIgnoreSurroundingSpaces();
        format = format.withCommentMarker('#');
        format = format.withFirstRecordAsHeader();
        return format;
    }

    public Source resolve(String href, String base) throws TransformerException {
        String issn = href.substring(href.indexOf(":") + 1);

        try {
            Element mods = new Element("mods", MCRConstants.MODS_NAMESPACE);
            if (issn2label.containsKey(issn)) {
                String label = issn2label.get(issn);
                if (label.endsWith(".")) {
                    label = label.substring(0, label.length() - 1);
                }

                Element titleInfo = new Element("titleInfo", MCRConstants.MODS_NAMESPACE);
                titleInfo.addContent(new Element("title", MCRConstants.MODS_NAMESPACE).setText(label));
                mods.addContent(titleInfo);

                Element identifier = new Element("identifier", MCRConstants.MODS_NAMESPACE);
                identifier.setAttribute("type", "issn");
                identifier.setText(issn);
                mods.addContent(identifier);

                Element classification = new Element("classification", MCRConstants.MODS_NAMESPACE);
                classification.setAttribute("authorityURI", AUTHORITY_URI);
                classification.setAttribute("valueURI", AUTHORITY_URI + "#gold");
                mods.addContent(classification);
            }
            return new JDOMSource(mods);
        } catch (Exception ex) {
            throw new TransformerException(ex);
        }
    }
}
