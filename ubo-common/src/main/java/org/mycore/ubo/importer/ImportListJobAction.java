package org.mycore.ubo.importer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.mycore.access.MCRAccessException;
import org.mycore.common.MCRMailer;
import org.mycore.common.MCRPersistenceException;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.MCRFrontendUtil;
import org.mycore.services.i18n.MCRTranslation;
import org.mycore.services.queuedjob.MCRJob;
import org.mycore.services.queuedjob.MCRJobAction;
import org.mycore.ubo.importer.evaluna.EvalunaImportJob;
import org.mycore.user2.MCRUser;
import org.mycore.user2.MCRUserManager;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

/**
 * Allows to execute a list import asynchronously. It executes the same tasks as in {@link DozBibImportServlet}.
 *
 * @author shermann (Silvio Hermann)
 * */
public class ImportListJobAction extends MCRJobAction {

    private static final String PROJECT_ID = MCRConfiguration2.getString("UBO.projectid.default").get();

    public static final String EDITOR_SUBMISSION_PARAMETER = "xEditorSubmission";
    public static final String IMPORT_JOB_ID_PARAMETER = "importJobId";
    public static final String USER_ID_PARAMETER = "userName";

    protected static final Logger LOGGER = LogManager.getLogger(ImportListJobAction.class);

    protected static final Optional<String> DEFAULT_EMAIL_FROM = MCRConfiguration2.getString("UBO.Mail.From");

    public ImportListJobAction() {
    }

    public ImportListJobAction(MCRJob mcrJob) {
        super(mcrJob);
    }

    @Override
    public void execute() throws ExecutionException {
        String xEditorSubmission = job.getParameter(ImportListJobAction.EDITOR_SUBMISSION_PARAMETER);

        if (xEditorSubmission == null) {
            LOGGER.error("No {} parameter provided", ImportListJobAction.EDITOR_SUBMISSION_PARAMETER);
            return;
        }

        try (InputStream inputStream = new ByteArrayInputStream(xEditorSubmission.getBytes(StandardCharsets.UTF_8))) {
            Document doc = new SAXBuilder().build(inputStream);
            Element formInput = doc.detachRootElement();

            String sourceType = formInput.getAttributeValue("sourceType");
            ImportJob importJob = "Evaluna".equals(sourceType) ? new EvalunaImportJob() : new ListImportJob(sourceType);
            importJob.transform(formInput);
            job.setParameter(IMPORT_JOB_ID_PARAMETER, importJob.getID());

            if ("true".equals(formInput.getAttributeValue("enrich"))) {
                importJob.enrich();
            }
            savePublications(importJob);
            sendMail(importJob);
        } catch (Exception e) {
            LOGGER.error("Could not transform form input", e);
        }
    }

    private void savePublications(ImportJob importJob) throws MCRPersistenceException, MCRAccessException {
        for (Document publication : importJob.getPublications()) {
            if (importJob.getFilterTransformer().isPresent()) {
                publication = importJob.applyFilterTransformer(publication, importJob.getFilterTransformer().get());
            }

            MCRObject obj = new MCRObject(publication);
            MCRObjectID oid = MCRObjectID.getNextFreeId(PROJECT_ID + "_mods");
            obj.setId(oid);
            obj.getService().addFlag("importID", importJob.getID());

            MCRMetadataManager.create(obj);
        }
    }

    private void sendMail(ImportJob importJob) {
        String userName = job.getParameter(ImportListJobAction.USER_ID_PARAMETER);
        MCRUser mcrUser = MCRUserManager.getUser(userName);
        String eMailAddress = mcrUser.getEMailAddress();

        if (eMailAddress == null) {
            LOGGER.warn("Cannot send e-mail to user {} as user has no e-mail address", userName);
            return;
        }

        if (ImportListJobAction.DEFAULT_EMAIL_FROM.isEmpty()) {
            LOGGER.warn("Cannot send e-mail to user {} as property UBO.Mail.From is not set", userName);
            return;
        }

        String subject = MCRTranslation.translate("ubo.import.list.email.subject", importJob.getID());
        MCRMailer.send(DEFAULT_EMAIL_FROM.get(), eMailAddress, subject, getBody(importJob));
    }

    private String getBody(ImportJob importJob) {
        String url = MCRFrontendUtil.getBaseURL() + "servlets/solr/select?fq=%2BobjectType%3Amods&q=importID%3A%22"
            + URLEncoder.encode(importJob.getID(), StandardCharsets.UTF_8) + "%22";
        return MCRTranslation.translate("ubo.import.list.email.body", url);
    }

    @Override
    public boolean isActivated() {
        return true;
    }

    @Override
    public String name() {
        return getClass().getSimpleName();
    }

    @Override
    public void rollback() {
        LOGGER.warn("Rolling back in {} is not implemented", ImportListJobAction.class.getName());
    }
}
