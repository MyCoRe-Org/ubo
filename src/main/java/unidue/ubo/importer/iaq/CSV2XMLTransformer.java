/**
 * Copyright (c) 2017 Duisburg-Essen University Library
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 **/

package unidue.ubo.importer.iaq;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.jdom2.Element;
import org.mycore.common.content.MCRContent;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.common.content.transformer.MCRContentTransformer;

public class CSV2XMLTransformer extends MCRContentTransformer {

    private final static char DELIMITER = ',';
    private final static char QUOTE = '"';
    private final static char ESCAPE = '\\';
    private final static boolean WITH_HEADER = true;

    private CSVFormat format = buildCSVFormat();

    private CSVFormat buildCSVFormat() {
        format = CSVFormat.DEFAULT.withDelimiter(DELIMITER).withQuote(QUOTE).withEscape(ESCAPE);
        format = format.withIgnoreEmptyLines(true).withIgnoreSurroundingSpaces();
        return WITH_HEADER ? format.withFirstRecordAsHeader() : format;
    }

    public MCRJDOMContent transform(MCRContent source) throws IOException {
        String encoding = getSourceEncoding(source);
        Reader in = new InputStreamReader(source.getInputStream(), encoding);

        CSVParser parser = format.parse(in);
        String[] columnNames = getColumnNames(parser);

        Element root = new Element("csv2xml");
        for (CSVRecord record : parser.getRecords()) {
            Element row = row2xml(columnNames, record);
            root.addContent(row);
        }
        return new MCRJDOMContent(root);
    }

    private String getSourceEncoding(MCRContent source) {
        String encoding = source.getEncoding();
        return encoding == null ? "UTF-8" : encoding;
    }

    private Element row2xml(String[] columnNames, CSVRecord record) {
        Element row = new Element("row");

        for (String name : columnNames) {
            cell2xml(record, row, name);
        }
        return row;
    }

    private void cell2xml(CSVRecord record, Element row, String name) {
        String value = record.get(name);
        if ((value == null) || value.isEmpty()) {
            return;
        }

        Element cell = new Element(name.replace(' ', '_'));
        cell.setText(value);
        row.addContent(cell);
    }

    private String[] getColumnNames(CSVParser parser) {
        Map<String, Integer> headerMap = parser.getHeaderMap();
        Set<Entry<String, Integer>> headerSet = headerMap.entrySet();
        String[] names = new String[headerSet.size()];

        for (Entry<String, Integer> entry : headerSet) {
            names[entry.getValue()] = entry.getKey();
        }
        return names;
    }
}
