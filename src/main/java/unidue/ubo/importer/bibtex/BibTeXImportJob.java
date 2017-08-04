/**
 * Copyright (c) 2016 Duisburg-Essen University Library
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 **/

package unidue.ubo.importer.bibtex;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URLConnection;
import java.net.URL;

import org.jdom2.Document;
import org.mycore.common.content.MCRContent;
import org.mycore.common.content.MCRStreamContent;
import org.mycore.common.content.MCRStringContent;

import unidue.ubo.importer.ImportJob;

public class BibTeXImportJob extends ImportJob {

    private BibTeXImportJob() {
        super("BibTeX");
    }

    public static BibTeXImportJob buildFrom(Document xml) throws MalformedURLException, IOException {
        BibTeXImportJob job = new BibTeXImportJob();

        //job.parameters = sub.getXML().getRootElement();
        String url = job.parameters.getChildTextTrim("url");
        String encoding = job.parameters.getChildText("encoding");
        String text = job.parameters.getChildTextTrim("text");

        if (url != null) {
            job.from(url, encoding);
        } else if (text != null) {
            job.from(text);
            //else
            //    job.from((FileItem) (sub.getFiles().get(0)), encoding);
        }

        return job;
    }

    private void from(String label, MCRContent source) {
        this.label = "label";
        this.source = source;
    }

    private void from(String text) {
        this.label = "text";
        this.source = new MCRStringContent(text);
    }
    //
    //    private void from(FileItem file, String encoding) throws IOException {
    //        this.label = "file " + file.getName();
    //        this.source = new MCRStringContent(stream2String(file.getInputStream(), encoding));
    //    }

    private void from(String url, String defaultEncoding) throws MalformedURLException, IOException {
        URLConnection uc = new URL(url).openConnection();
        String encoding = uc.getContentEncoding() == null ? defaultEncoding : uc.getContentEncoding();
        InputStream in = uc.getInputStream();

        if (uc.getContentType().contains("html")) {
            from(url, new HTML2TextTransformer().transform(in, encoding));
        } else {
            from(url, new MCRStringContent(stream2String(in, encoding)));
        }
    }

    private static String stream2String(InputStream in, String encoding) throws IOException {
        MCRStreamContent stream = new MCRStreamContent(in);
        stream.setEncoding(encoding);
        return stream.asString();
    }
}
