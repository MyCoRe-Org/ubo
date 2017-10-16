/**
 * Copyright (c) 2016 Duisburg-Essen University Library
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 **/

package unidue.ubo.importer;

import org.jdom2.Element;
import org.mycore.common.content.MCRContent;
import org.mycore.common.content.MCRStringContent;

import unidue.ubo.importer.ImportJob;

public class ListImportJob extends ImportJob {

    private String type;

    public ListImportJob(String type) {
        this.type = type;
    }

    protected String getTransformerID() {
        return "import." + type;
    }

    protected MCRContent getSource(Element formInput) throws Exception {
        String code = formInput.getChildTextTrim("source");
        return new MCRStringContent(code);
    }
}
