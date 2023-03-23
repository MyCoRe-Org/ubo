/**
 * Copyright (c) 2016 Duisburg-Essen University Library
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 **/

package org.mycore.ubo.importer.evaluna;

import org.jdom2.Element;
import org.mycore.common.content.MCRContent;
import org.mycore.ubo.importer.ImportJob;

public class EvalunaImportJob extends ImportJob {

    protected String getTransformerID() {
        return "import.Evaluna";
    }

    protected MCRContent getSource(Element formInput) throws Exception {
        Element request = formInput.getChild("request").clone();
        EvalunaConnection evaluna = new EvalunaConnection().addInstitutionRequest().addPublicationRequest(request);
        return evaluna.getResponse();
    }
}
