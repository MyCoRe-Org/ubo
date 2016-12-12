package unidue.ubo.importer.evaluna;

import java.io.IOException;

import org.apache.commons.httpclient.HttpException;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.xml.sax.SAXException;

import unidue.ubo.importer.ImportJob;

public class EvalunaImportJob extends ImportJob {
    
   public EvalunaImportJob( Element request ) throws HttpException, IOException, JDOMException, SAXException
   {
       super("Evaluna");
       this.label = this.type;
       this.source = new EvalunaConnection().addInstitutionRequest().addPublicationRequest(request).getResponse();
   }
}
