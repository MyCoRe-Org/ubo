package unidue.ubo.importer.scopus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.mycore.access.MCRAccessException;
import org.mycore.common.MCRSessionMgr;
import org.mycore.common.MCRUserInformation;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.common.content.MCRContent;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.common.content.transformer.MCRXSLTransformer;
import org.mycore.common.xsl.MCRParameterCollector;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.util.concurrent.MCRFixedUserCallable;
import org.xml.sax.SAXException;

import javax.xml.transform.Source;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Stream;

public class ScopusInitialImporter {

    private static final ExecutorService poolExecutor = Executors.newSingleThreadExecutor();

    public static void initialImport(String affiliationIDs){
        final MCRUserInformation currentUser = MCRSessionMgr.getCurrentSession().getUserInformation();
        final ScopusImporter scopusImporter = new ScopusImporter();
        Stream.of(affiliationIDs.split(","))
                .map(ScopusAffiliationQuery::new)
                .forEach((query)-> {
                    query.resolveAllIDs(id -> {
                        final MCRFixedUserCallable<MCRObject> userCallable = new MCRFixedUserCallable<MCRObject>(() -> scopusImporter.doImport(id), currentUser);
                        poolExecutor.submit(userCallable);
                    });
                });
    }




}
