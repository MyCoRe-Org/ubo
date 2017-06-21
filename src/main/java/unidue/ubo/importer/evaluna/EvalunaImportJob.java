/**
 * Copyright (c) 2016 Duisburg-Essen University Library
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 **/

package unidue.ubo.importer.evaluna;

import java.io.IOException;

import org.apache.commons.httpclient.HttpException;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.xml.sax.SAXException;

import unidue.ubo.importer.ImportJob;

public class EvalunaImportJob extends ImportJob {

    public EvalunaImportJob(Element root) throws HttpException, IOException, JDOMException, SAXException {
        super("Evaluna");

        this.label = this.type;
        this.parameters = root;

        Element request = root.getChild("request").clone();
        this.source = new EvalunaConnection().addInstitutionRequest().addPublicationRequest(request).getResponse();
    }
}
