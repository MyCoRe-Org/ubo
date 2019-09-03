package unidue.ubo.publication;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.mycore.common.events.MCREvent;
import org.mycore.common.events.MCREventHandlerBase;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.user2.MCRUser;
import unidue.ubo.matcher.MCRAuthorMatcherLDAP;
import unidue.ubo.matcher.MCRAuthorMatcherUtils;
import unidue.ubo.matcher.MCRAuthorMatcher;
import unidue.ubo.matcher.MCRAuthorMatcherLocal;

import java.util.List;


/**
 * EventHandler for new publications in MODS-format.
 *
 * 1. Match all authors of the publication with the configured implementations of MCRAuthorMatcher, starting with the
 * local MCRUsers (MCRAuthorMatcherLocal)
 *
 * 1.1 Any MCRAuthorMatcher implementation should either create new MCRUsers or (if applicable) enrich the attributes
 * of an existing MCRUser by possible new identifiers (mods:nameIdentifier of the author)
 *
 * 2. Extend the mods:name -> mods:nameIdentifier element of the publication with the configured "lead-ID" if it is
 * not present but available in the matched MCRUsers attributes.
 *
 * @author Pascal Rost
 */
public class PublicationEventHandler extends MCREventHandlerBase {

    private final static Logger LOGGER = LogManager.getLogger(PublicationEventHandler.class);

    @Override
    protected void handleObjectUpdated(MCREvent evt, MCRObject obj) {
        // TODO: remove this, since this EventHandler should only work for "ObjectCreated" events!
        handleObjectCreated(evt, obj);
    }

    @Override
    protected void handleObjectCreated(MCREvent evt, MCRObject obj) {
        // get all mods:name from authors of publication
        List<Element> modsAuthors = MCRAuthorMatcherUtils.getAuthors(obj);

        // for every mods:name (Author), call our Implementation(s) of MCRAuthorMatcher
        MCRAuthorMatcher localMatcher = new MCRAuthorMatcherLocal();
        MCRAuthorMatcher ldapMatcher = new MCRAuthorMatcherLDAP();
        // TODO: instead of static order of matchers, make them configurable by mycore.properties
        for(Element modsAuthor: modsAuthors) {
            // returned users are either matched (existing local users) or created new (with specific realm)
            // or null (no match and no new user created)
            MCRUser user = localMatcher.matchModsAuthor(modsAuthor);
            if(user != null) {
                LOGGER.debug("Got matching or new user: {} for mods:name: {}",
                        user.getUserName(), new XMLOutputter(Format.getPrettyFormat()).outputString(modsAuthor));
            } else {
                LOGGER.debug("Found no matching user for mods:name: {}",
                        new XMLOutputter(Format.getPrettyFormat()).outputString(modsAuthor));
                user = ldapMatcher.matchModsAuthor(modsAuthor);
            }
        }
        // TODO: extend publication with configured "lead-ID" of the matched authors/MCRUsers
    }
}
