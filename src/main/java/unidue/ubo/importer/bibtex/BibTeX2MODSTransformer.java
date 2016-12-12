/**
 * $Revision$ 
 * $Date$
 *
 * This file is part of the MILESS repository software.
 * Copyright (C) 2011 MILESS/MyCoRe developer team
 * See http://duepublico.uni-duisburg-essen.de/ and http://www.mycore.de/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **/

package unidue.ubo.importer.bibtex;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.jaxen.JaxenException;
import org.jdom2.Comment;
import org.jdom2.Element;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.mycore.common.MCRConstants;
import org.mycore.common.MCRException;
import org.mycore.common.content.MCRContent;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.common.content.transformer.MCRContentTransformer;
import org.mycore.frontend.xeditor.MCRNodeBuilder;
import org.mycore.mods.MCRMODSPagesHelper;

import bibtex.dom.BibtexAbstractValue;
import bibtex.dom.BibtexEntry;
import bibtex.dom.BibtexFile;
import bibtex.dom.BibtexPerson;
import bibtex.dom.BibtexPersonList;
import bibtex.dom.BibtexString;
import bibtex.expansions.CrossReferenceExpander;
import bibtex.expansions.Expander;
import bibtex.expansions.ExpansionException;
import bibtex.expansions.MacroReferenceExpander;
import bibtex.expansions.PersonListExpander;
import bibtex.parser.BibtexMultipleFieldValuesPolicy;
import bibtex.parser.BibtexParser;
import bibtex.parser.ParseException;

public class BibTeX2MODSTransformer extends MCRContentTransformer {

    public MCRJDOMContent transform(MCRContent source) throws IOException {
        String input = source.asString();
        input = fixMissingEntryKeys(input);
        BibtexFile bibtexFile = parse(input);
        Element collection = new BibTeXFileTransformer().transform(bibtexFile);
        return new MCRJDOMContent(collection);
    }

    private Pattern MISSING_KEYS_PATTERN = Pattern.compile("(@[a-zA-Z0-9]+\\s*\\{)(\\s*[a-zA-Z0-9]+\\s*\\=)");

    private String fixMissingEntryKeys(String input) {
        StringBuffer sb = new StringBuffer();
        int i = 0;

        Matcher m = MISSING_KEYS_PATTERN.matcher(input);
        while (m.find()) {
            String entryKey = "key" + (++i);
            m.appendReplacement(sb, m.group(1) + entryKey + ", " + m.group(2));
        }
        m.appendTail(sb);

        return sb.toString();
    }

    private BibtexFile parse(String input) throws UnsupportedEncodingException, IOException {
        BibtexFile bibtexFile = new BibtexFile();
        BibtexParser parser = new BibtexParser(false);
        parser.setMultipleFieldValuesPolicy(BibtexMultipleFieldValuesPolicy.KEEP_ALL);
        try {
            parser.parse(bibtexFile, new StringReader(input));
        } catch (ParseException ex) {
            MessageLogger.logMessage(ex.toString());
        }
        return bibtexFile;
    }

}

class BibTeXFileTransformer {

    Element collection = new Element("modsCollection", MCRConstants.MODS_NAMESPACE);

    Element transform(BibtexFile file) {
        expandReferences(file);

        BibTeXEntryTransformer transformer = new BibTeXEntryTransformer();

        for (Object obj : file.getEntries())
            if (obj instanceof BibtexEntry) {
                BibtexEntry entry = (BibtexEntry) obj;
                if (entry.getFields().isEmpty())
                    MessageLogger.logMessage("Skipping entry of type " + entry.getEntryType() + ", has no fields",
                        collection);
                else
                    collection.addContent(transformer.transform(entry));
            }

        return collection;
    }

    private void expandReferences(BibtexFile file) {
        expand(new MacroReferenceExpander(true, true, true, false), file);
        expand(new CrossReferenceExpander(false), file);
        expand(new PersonListExpander(true, true, false), file);
    }

    private void expand(Expander expander, BibtexFile file) {
        try {
            expander.expand(file);
        } catch (ExpansionException ex) {
            MessageLogger.logMessage(ex.toString(), collection);
        }
        for (Exception ex : expander.getExceptions())
            MessageLogger.logMessage(ex.toString(), collection);
    }
}

class BibTeXEntryTransformer {

    private List<FieldTransformer> fieldTransformers = new ArrayList<FieldTransformer>();

    BibTeXEntryTransformer() {
        fieldTransformers
            .add(new Field2XPathTransformer("document_type", "mods:genre" + FieldTransformer.AS_NEW_ELEMENT));
        fieldTransformers.add(
            new Field2XPathTransformer("title", "mods:titleInfo" + FieldTransformer.AS_NEW_ELEMENT + "/mods:title"));
        fieldTransformers.add(new PersonListTransformer("author", "aut"));
        fieldTransformers.add(new Field2XPathTransformer("journal",
            "mods:relatedItem[@type='host'][mods:genre='journal']/mods:titleInfo/mods:title"));
        fieldTransformers.add(new Field2XPathTransformer("booktitle",
            "mods:relatedItem[@type='host'][mods:genre='collection']/mods:titleInfo/mods:title"));
        fieldTransformers.add(new MoveToRelatedItemIfExists("mods:relatedItem[@type='host']",
            new PersonListTransformer("editor", "edt")));
        fieldTransformers.add(new MoveToRelatedItemIfExists("mods:relatedItem[@type='host']",
            new Field2XPathTransformer("edition", "mods:originInfo/mods:edition")));
        fieldTransformers.add(new MoveToRelatedItemIfExists("mods:relatedItem[@type='host']",
            new Field2XPathTransformer("howpublished", "mods:originInfo/mods:edition")));
        fieldTransformers.add(new MoveToRelatedItemIfExists("mods:relatedItem[@type='host']",
            new Field2XPathTransformer("publisher", "mods:originInfo/mods:publisher")));
        fieldTransformers.add(new MoveToRelatedItemIfExists("mods:relatedItem[@type='host']",
            new Field2XPathTransformer("address", "mods:originInfo/mods:place/mods:placeTerm[@type='text']")));
        fieldTransformers.add(
            new MoveToRelatedItemIfExists("mods:relatedItem[@type='host'][mods:genre='collection']", new YearTransformer()));
        fieldTransformers.add(new MoveToRelatedItemIfExists(
            "mods:relatedItem[@type='host'][mods:genre='journal']|descendant::mods:relatedItem[@type='series']",
            new Field2XPathTransformer("volume", "mods:part/mods:detail[@type='volume']/mods:number")));
        fieldTransformers.add(new Field2XPathTransformer("number",
            "mods:relatedItem[@type='host']/mods:part/mods:detail[@type='issue']/mods:number"));
        fieldTransformers.add(new Field2XPathTransformer("issue",
            "mods:relatedItem[@type='host']/mods:part/mods:detail[@type='issue']/mods:number"));
        fieldTransformers.add(new PagesTransformer());
        fieldTransformers.add(new MoveToRelatedItemIfExists("mods:relatedItem[@type='host']",
            new Field2XPathTransformer("isbn", "mods:identifier[@type='isbn']" + FieldTransformer.AS_NEW_ELEMENT)));
        fieldTransformers.add(new MoveToRelatedItemIfExists("mods:relatedItem[@type='host']",
            new Field2XPathTransformer("series", "mods:relatedItem[@type='series']/mods:titleInfo/mods:title")));
        fieldTransformers.add(new MoveToRelatedItemIfExists(
            "mods:relatedItem[@type='host'][mods:genre='journal']|descendant::mods:relatedItem[@type='series']",
            new Field2XPathTransformer("issn", "mods:identifier[@type='issn']" + FieldTransformer.AS_NEW_ELEMENT)));
        fieldTransformers
            .add(new Field2XPathTransformer("doi", "mods:identifier[@type='doi']" + FieldTransformer.AS_NEW_ELEMENT));
        fieldTransformers
            .add(new Field2XPathTransformer("urn", "mods:identifier[@type='urn']" + FieldTransformer.AS_NEW_ELEMENT));
        fieldTransformers
            .add(new Field2XPathTransformer("url", "mods:location/mods:url" + FieldTransformer.AS_NEW_ELEMENT));
        fieldTransformers.add(
            new Field2XPathTransformer("keywords", "mods:subject" + FieldTransformer.AS_NEW_ELEMENT + "/mods:topic"));
        fieldTransformers.add(new Field2XPathTransformer("author_keywords",
            "mods:subject" + FieldTransformer.AS_NEW_ELEMENT + "/mods:topic"));
        fieldTransformers
            .add(new Field2XPathTransformer("abstract", "mods:abstract" + FieldTransformer.AS_NEW_ELEMENT));
        fieldTransformers.add(new Field2XPathTransformer("note", "mods:note" + FieldTransformer.AS_NEW_ELEMENT));
        fieldTransformers.add(new Field2XPathTransformer("type", "mods:note" + FieldTransformer.AS_NEW_ELEMENT));
        fieldTransformers.add(new Field2XPathTransformer("source", "mods:recordInfo/mods:recordOrigin"));
        fieldTransformers.add(new UnsupportedFieldTransformer(fieldTransformers));
    }

    Element transform(BibtexEntry entry) {
        Element mods = new Element("mods", MCRConstants.MODS_NAMESPACE);
        Element source = buildSourceExtension(entry);
        GenreTransformer.setGenre(entry, mods);
        transformFields(entry, mods);
        GenreTransformer.fixHostGenre(entry, mods);
        mods.addContent(source);
        return mods;
    }

    private Element buildSourceExtension(BibtexEntry entry) {
        Element source = new Element("extension", MCRConstants.MODS_NAMESPACE);
        source.setAttribute("type", "source");
        source.setAttribute("format", "bibtex");
        source.setText(entry.toString());
        return source;
    }

    private void transformFields(BibtexEntry entry, Element mods) {
        for (FieldTransformer transformer : fieldTransformers)
            transformer.transformField(entry, mods);
    }

}

class GenreTransformer {

    static void setGenre(BibtexEntry entry, Element mods) {
        String type = entry.getEntryType().toLowerCase();
        FieldTransformer.buildElement("mods:genre", type, mods);
    }

    static void fixHostGenre(BibtexEntry entry, Element mods) {
        String type = entry.getEntryType().toLowerCase();
        if (type.equals("incollection") || type.equals("inproceedings") || type.equals("inbook")) {
            type = type.substring(2);

            Element genre = getHostGenre(mods);
            if (genre != null)
                genre.setText(type);
            else
                FieldTransformer.buildElement("mods:relatedItem[@type='host']/mods:genre", type, mods);
        }
    }

    private static Element getHostGenre(Element mods) {
        XPathExpression<Element> expr = XPathFactory.instance().compile("mods:relatedItem[@type='host']/mods:genre",
            Filters.element(), null, MCRConstants.getStandardNamespaces());
        Element genre = expr.evaluateFirst(mods);
        return genre;
    }

}

class MessageLogger {

    private final static Logger LOGGER = Logger.getLogger(MessageLogger.class);

    static void logMessage(String message) {
        LOGGER.warn(message);
    }

    static void logMessage(String message, Element parent) {
        logMessage(message);
        parent.addContent(new Comment(message));
    }
}

class FieldTransformer {

    protected String field;

    FieldTransformer(String field) {
        this.field = field;
    }

    String getField() {
        return field;
    }

    final static String AS_NEW_ELEMENT = "[999]";

    /** Converts german umlauts and other special LaTeX characters */
    protected String normalizeValue(String value) {
        value = value.replaceAll("\\s+", " ").trim();

        value = value.replace("{\\\"a}", "\u00e4");
        value = value.replace("{\\\"o}", "\u00f6");
        value = value.replace("{\\\"u}", "\u00fc");
        value = value.replace("{\\\"A}", "\u00c4");
        value = value.replace("{\\\"O}", "\u00d6");
        value = value.replace("{\\\"U}", "\u00dc");
        value = value.replace("{\\ss}", "\u00df");
        value = value.replace("\\\"a", "\u00e4");
        value = value.replace("\\\"o", "\u00f6");
        value = value.replace("\\\"u", "\u00fc");
        value = value.replace("\\\"A", "\u00c4");
        value = value.replace("\\\"O", "\u00d6");
        value = value.replace("\\\"U", "\u00dc");
        value = value.replace("{\\'a}", "\u00e1");
        value = value.replace("{\\'e}", "\u00e9");
        value = value.replace("{\\'i}", "\u00ed");
        value = value.replace("{\\'o}", "\u00f3");
        value = value.replace("{\\'u}", "\u00fa");
        value = value.replace("{\\`a}", "\u00e0");
        value = value.replace("{\\`e}", "\u00e8");
        value = value.replace("{\\`i}", "\u00ec");
        value = value.replace("{\\`o}", "\u00f2");
        value = value.replace("{\\`u}", "\u00f9");
        value = value.replace("{\\'\\i}", "\u00ed");
        value = value.replace("{\\`\\i}", "\u00ec");

        value = value.replace("{", "").replace("}", "");
        value = value.replace("---", "-").replace("--", "-");
        return value;
    }

    void transformField(BibtexEntry entry, Element parent) {
        for (BibtexAbstractValue value : getFieldValues(entry, field))
            buildField(value, parent);
    }

    private List<BibtexAbstractValue> getFieldValues(BibtexEntry entry, String field) {
        List<BibtexAbstractValue> fieldValues = entry.getFieldValuesAsList(field);
        if (fieldValues == null)
            fieldValues = entry.getFieldValuesAsList(field.toUpperCase());
        return fieldValues == null ? Collections.<BibtexAbstractValue> emptyList() : fieldValues;
    }

    void buildField(BibtexAbstractValue value, Element parent) {
        String type = value.getClass().getSimpleName();
        MessageLogger.logMessage("Field " + field + " returns unsupported abstract value of type " + type, parent);
    }

    static Element buildElement(String xPath, String content, Element parent) {
        try {
            return new MCRNodeBuilder().buildElement(xPath, content, parent);
        } catch (JaxenException ex) {
            throw new MCRException("Unable to build field " + xPath, ex);
        }
    }
}

class Field2XPathTransformer extends FieldTransformer {

    protected String xPath;

    Field2XPathTransformer(String field, String xPath) {
        super(field);
        this.xPath = xPath;
    }

    void buildField(BibtexAbstractValue value, Element parent) {
        String content = ((BibtexString) value).getContent();
        content = normalizeValue(content);
        buildElement(xPath, content, parent);
    }
}

class PersonListTransformer extends FieldTransformer {

    private PersonTransformer personTransformer;

    private AndOthersTransformer andOthers;

    PersonListTransformer(String field, String role) {
        super(field);
        this.personTransformer = new PersonTransformer(field, role);
        this.andOthers = new AndOthersTransformer(field, role);
    }

    @SuppressWarnings("unchecked")
    void buildField(BibtexAbstractValue value, Element parent) {
        BibtexPersonList personList = (BibtexPersonList) value;
        for (BibtexPerson person : (List<BibtexPerson>) (personList.getList()))
            (person.isOthers() ? andOthers : personTransformer).buildField(person, parent);
    }

}

class PersonTransformer extends FieldTransformer {

    protected String xPath;

    PersonTransformer(String field, String role) {
        super(field);
        this.xPath = "mods:name[@type='personal'][mods:role/mods:roleTerm[@type='code'][@authority='marcrelator']='"
            + role + "']" + FieldTransformer.AS_NEW_ELEMENT;
    }

    void buildField(BibtexPerson person, Element parent) {
        Element modsName = buildElement(xPath, null, parent);

        String lastName = normalizeValue(person.getLast());
        buildElement("mods:namePart[@type='family']", lastName, modsName);

        String firstName = getFirstName(person);
        if (!firstName.isEmpty())
            buildElement("mods:namePart[@type='given']", firstName, modsName);

        String lineage = person.getLineage();
        if (lineage != null)
            buildElement("mods:namePart[@type='termsOfAddress']", lineage, modsName);
    }

    private String getFirstName(BibtexPerson person) {
        StringBuffer first = new StringBuffer();
        if (person.getFirst() != null)
            first.append(person.getFirst());
        if (person.getPreLast() != null)
            first.append(" ").append(person.getPreLast());
        String firstName = normalizeValue(first.toString().trim());
        return firstName;
    }
}

class AndOthersTransformer extends PersonTransformer {

    AndOthersTransformer(String field, String role) {
        super(field, role);
    }

    void buildField(BibtexPerson person, Element parent) {
        Element modsName = buildElement(xPath, null, parent);
        buildElement("mods:etAl", null, modsName);
    }
}

class PagesTransformer extends Field2XPathTransformer {

    PagesTransformer() {
        super("pages", "mods:relatedItem[@type='host']/mods:part");
    }

    void buildField(BibtexAbstractValue value, Element parent) {
        String pages = ((BibtexString) value).getContent();
        pages = normalizeValue(pages);
        Element part = buildElement(xPath, null, parent);
        part.addContent(MCRMODSPagesHelper.buildExtentPages(pages));
    }
}

class YearTransformer extends Field2XPathTransformer {

    YearTransformer() {
        super("year", "mods:originInfo/mods:dateIssued[@encoding='w3cdtf']");
    }

    void buildField(BibtexAbstractValue value, Element parent) {
        String content = ((BibtexString) value).getContent();
        content = normalizeValue(content);
        String year = getFourDigitYear(content);
        if (year != null)
            buildElement(xPath, year, parent);
        else
            MessageLogger.logMessage("Field year: No 4-digit year found: " + content, parent);
    }

    private final static Pattern YEAR_PATTERN = Pattern.compile(".*(\\d{4}).*");

    private String getFourDigitYear(String text) {
        Matcher m = YEAR_PATTERN.matcher(text);
        return m.matches() ? m.group(1) : null;
    }
}

class UnsupportedFieldTransformer extends FieldTransformer {

    Set<String> supportedFields = new HashSet<String>();

    UnsupportedFieldTransformer(Collection<FieldTransformer> supportedTransformers) {
        super("*");
        determineSupportedFields(supportedTransformers);
    }

    private void determineSupportedFields(Collection<FieldTransformer> supportedTransformers) {
        for (FieldTransformer transformer : supportedTransformers)
            supportedFields.add(transformer.getField());
    }

    private boolean isUnsupported(String field) {
        return !supportedFields.contains(field);
    }

    void transformField(BibtexEntry entry, Element parent) {
        for (String field : (Set<String>) (entry.getFields().keySet())) {
            if (isUnsupported(field)) {
                this.field = field;
                super.transformField(entry, parent);
            }
        }
    }

    void buildField(BibtexAbstractValue value, Element parent) {
        MessageLogger.logMessage("Field " + field + " is unsupported: " + value.toString().replaceAll("\\s+", " "));
        String xPath = "mods:extension[@type='fields']/field[@name='" + field + "']" + FieldTransformer.AS_NEW_ELEMENT;
        String content = ((BibtexString) value).getContent();
        content = normalizeValue(content);
        buildElement(xPath, content, parent);
    }
}

class MoveToRelatedItemIfExists extends FieldTransformer {

    private String xPathOfRelatedItem;

    private FieldTransformer wrappedTransformer;

    MoveToRelatedItemIfExists(String xPathOfRelatedItem, FieldTransformer wrappedTransformer) {
        super(wrappedTransformer.field);
        this.xPathOfRelatedItem = xPathOfRelatedItem;
        this.wrappedTransformer = wrappedTransformer;
    }

    @Override
    void transformField(BibtexEntry entry, Element parent) {
        parent = getRelatedItemIfExists(parent);
        wrappedTransformer.transformField(entry, parent);
    }

    Element getRelatedItemIfExists(Element parent) {
        XPathExpression<Element> xPath = XPathFactory.instance().compile(xPathOfRelatedItem, Filters.element(), null,
            MCRConstants.getStandardNamespaces());
        Element fixedParent = xPath.evaluateFirst(parent);
        return fixedParent != null ? fixedParent : parent;
    }
}
