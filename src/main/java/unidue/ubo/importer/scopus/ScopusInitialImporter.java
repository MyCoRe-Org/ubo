package unidue.ubo.importer.scopus;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

import org.mycore.common.MCRSessionMgr;
import org.mycore.common.MCRUserInformation;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.util.concurrent.MCRFixedUserCallable;

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
