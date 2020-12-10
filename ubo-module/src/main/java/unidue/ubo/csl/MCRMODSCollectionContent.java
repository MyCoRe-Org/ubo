package unidue.ubo.csl;

import org.jdom2.Element;
import org.mycore.common.MCRConstants;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.mods.MCRMODSWrapper;

public class MCRMODSCollectionContent extends MCRJDOMContent {

    private Element modsCollection;

    public MCRMODSCollectionContent() {
        super(new Element("modsCollection", MCRConstants.MODS_NAMESPACE));
        modsCollection = super.asXML().getRootElement();
    }

    public void addMODSFrom(String... objectIDs) {
        for (String id : objectIDs) {
            addMODSFrom(id);
        }
    }

    private void addMODSFrom(String objectID) {
        addMODSFrom(MCRObjectID.getInstance(objectID));
    }

    private void addMODSFrom(MCRObjectID oid) {
        MCRObject obj = MCRMetadataManager.retrieveMCRObject(oid);
        MCRMODSWrapper wrapper = new MCRMODSWrapper(obj);
        Element mods = wrapper.getMODS().detach();
        modsCollection.addContent(mods);
    }
}