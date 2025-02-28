package org.mycore.ubo.dedup.jpa;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;
import org.jdom2.transform.JDOMSource;
import org.mycore.common.MCRConstants;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.mods.MCRMODSWrapper;
import org.mycore.ubo.dedup.DeDupCriteriaBuilder;
import org.mycore.ubo.dedup.DeDupCriterion;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Performs queries related to actions
 *
 * Searching duplicates of an id.
 * Format:
 * dedup:search:$relation:$id
 *
 * $relation
 *  - base: search for duplicates of the objects with the $id
 *  - parent: the same as base.
 *  - host: search for duplicates of the host(not parent) in the object with the $id
 *
 *
 *
 */
public class DeduplicationCriterionResolver implements URIResolver {

    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public Source resolve(String href, String base) throws TransformerException {

        String[] parts = href.split(":");
        if (parts.length < 3) {
            return null;
        }
        String action = parts[1];

        if(!action.equals("search")) {
            LOGGER.error("Unknown action: {} in {}", action, href);
            return null;
        }

        String relation = parts[2];
        if(!Objects.equals("base", relation) && Objects.equals("parent", relation) && Objects.equals("host", relation)) {
            LOGGER.error("Unknown relation: {} in {}", relation, href);
            return null;
        }

        String idString = parts[3];
        if(!MCRObjectID.isValid(idString)) {
            LOGGER.error("Invalid id: {} in {}", idString, href);
            return null;
        }
        MCRObjectID id = MCRObjectID.getInstance(idString);

        if(!MCRMetadataManager.exists(id)) {
            LOGGER.error("Object with id {} does not exist", id);
            return null;
        }
        MCRObject obj = MCRMetadataManager.retrieveMCRObject(id);

        DeDupCriteriaBuilder deDupCriteriaBuilder = new DeDupCriteriaBuilder();
        Element mods = new MCRMODSWrapper(obj).getMODS();
        Element result = new Element("result");

        List<DeduplicationKey> possibleDuplicates = new ArrayList<>();
        if(Objects.equals("base", relation) || Objects.equals("parent", relation)) {
            Set<DeDupCriterion> criteria = deDupCriteriaBuilder.buildFromMODS(mods);
            criteria.forEach(criterion -> {
                possibleDuplicates.addAll(DeduplicationKeyManager.getInstance().getDuplicates(id.toString(), criterion.getType(), criterion.getKey()));
            });
        } else {
            for (Element host : deDupCriteriaBuilder.getNodes(mods, "mods:relatedItem[@type='host']")) {
                String externalID = host.getAttributeValue("href", MCRConstants.XLINK_NAMESPACE);
                if(externalID != null) {
                    // skip external hosts, they get handled by their own object id
                    continue;
                }
               deDupCriteriaBuilder.buildFromMODS(host).forEach(criterion -> {
                   possibleDuplicates.addAll(DeduplicationKeyManager.getInstance().getDuplicates(id.toString(), criterion.getType(), criterion.getKey()));
               });
            }
        }

        possibleDuplicates
                .stream()
                .map(DeduplicationKey::getMcrId)
                .distinct()
                .forEach(mcrid -> {
            Element dedup = new Element("duplicate");
            dedup.setAttribute("id", mcrid);
            result.addContent(dedup);
        });


        return new JDOMSource(result);
    }

}
