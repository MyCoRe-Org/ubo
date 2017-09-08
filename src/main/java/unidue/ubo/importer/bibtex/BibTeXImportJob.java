/**
 * Copyright (c) 2016 Duisburg-Essen University Library
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 **/

package unidue.ubo.importer.bibtex;

import org.jdom2.Element;
import org.mycore.common.content.MCRContent;
import org.mycore.common.content.MCRStringContent;

import unidue.ubo.importer.ImportJob;

public class BibTeXImportJob extends ImportJob {

    protected String getTransformerID() {
        return "import.BibTeX";
    }

    protected MCRContent getSource(Element formInput) throws Exception {
        String code = formInput.getChildTextTrim("bibtex");
        return new MCRStringContent(code);
    }
}
