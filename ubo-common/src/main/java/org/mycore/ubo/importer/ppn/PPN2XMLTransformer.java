/**
 * Copyright (c) 2017 Duisburg-Essen University Library
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 **/

package org.mycore.ubo.importer.ppn;

import java.io.IOException;
import java.util.Arrays;

import org.jdom2.Document;
import org.jdom2.Element;
import org.mycore.common.content.MCRContent;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.common.content.transformer.MCRContentTransformer;

public class PPN2XMLTransformer extends MCRContentTransformer {

    @Override
    public MCRJDOMContent transform(MCRContent source) throws IOException {
        Element root = new Element("list");
        Document xml = new Document(root);

        Arrays.stream(source.asString().split("\\s")).filter(p -> p.trim().length() > 0)
            .forEach(ppn -> {
                Element e = new Element("ppn");
                e.setText(ppn);
                root.addContent(e);
            });
        return new MCRJDOMContent(xml);
    }
}
