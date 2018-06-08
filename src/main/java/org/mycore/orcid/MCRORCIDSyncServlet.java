package org.mycore.orcid;

import javax.servlet.http.HttpServletResponse;

import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.servlets.MCRServlet;
import org.mycore.frontend.servlets.MCRServletJob;
import org.mycore.orcid.works.MCRWorksSection;

public class MCRORCIDSyncServlet extends MCRServlet {

    @Override
    protected void doGetPost(MCRServletJob job) throws Exception {
        String id = job.getRequest().getParameter("id");

        MCRObjectID oid = MCRObjectID.getInstance(id);
        if (!MCRMetadataManager.exists(oid)) {
            job.getResponse().sendError(HttpServletResponse.SC_BAD_REQUEST,
                "Publication with ID " + id + " does not exist");
            return;
        }

        if (!MCRORCIDUser.weAreTrustedParty()) {
            job.getResponse().sendError(HttpServletResponse.SC_UNAUTHORIZED,
                "Current user did not authorize the application to update his ORCID profile");
            return;
        }

        String status = MCRORCIDSynchronizer.getStatus(id);
        if (status.equals("not_mine")) {
            job.getResponse().sendError(HttpServletResponse.SC_UNAUTHORIZED,
                "Current user is not related to publication " + id);
            return;
        }

        MCRORCIDProfile profile = MCRORCIDUser.getProfile();
        MCRWorksSection works = profile.getWorksSection();

        if (status.equals("not_in_my_orcid_profile")) {
            works.addWorkFrom(oid);
        } else if (status.equals("in_my_orcid_profile")) {
            works.findWork(oid).get().update();
        }

        job.getResponse().sendRedirect("DozBibEntryServlet?id=" + id);
    }
}
