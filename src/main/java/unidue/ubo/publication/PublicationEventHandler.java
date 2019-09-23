package unidue.ubo.publication;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.mycore.common.config.MCRConfiguration;
import org.mycore.common.config.MCRConfigurationException;
import org.mycore.common.events.MCREvent;
import org.mycore.common.events.MCREventHandlerBase;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.user2.MCRUser;
import org.mycore.user2.MCRUserManager;
import unidue.ubo.matcher.MCRUserMatcherUtils;
import unidue.ubo.matcher.MCRUserMatcher;
import unidue.ubo.matcher.MCRUserMatcherLocal;

import java.util.ArrayList;
import java.util.List;


/**
 * EventHandler for new publications in MODS-format.
 *
 * 1. For all persons that are listed in a given publication, create a new MCRUser.
 *
 * 2. Match all new MCRUsers against the configured chain of implementations of MCRUserMatcher
 *
 * 2.1 When a match is found, the MCRUserMatcher implementations shall enrich the MCRUsers attributes with the matched
 * Users attributes of the target system
 *
 * 3. At last, match the new MCRUser against the locally persisted MCRUsers. If a match is found, the already persisted
 * MCRUsers attributes are enriched with the new MCRUsers attributes. Otherwise, persist the new MCRUser in a special
 * realm
 *
 * 4. Extend the mods:name -> mods:nameIdentifier element of the publication with the configured "lead-ID" if it is
 * not present but available in the matched MCRUsers attributes.
 *
 * The following properties in the mycore.properties are used:
 * MCR.user2.matching.chain (Multiple implementations separated by ",")
 * Example:
 * MCR.user2.matching.chain=unidue.ubo.matcher.MCRUserMatcherLDAP,unidue.ubo.matcher.MCRUserMatcherDummy
 *
 * @author Pascal Rost
 */
public class PublicationEventHandler extends MCREventHandlerBase {

    private final static Logger LOGGER = LogManager.getLogger(PublicationEventHandler.class);

    private final static String CONFIG_MATCHERS = "MCR.user2.matching.chain";

    private List<MCRUserMatcher> loadMatcherImplementationChain() {
        List<MCRUserMatcher> matchers = new ArrayList<>();

        MCRConfiguration config = MCRConfiguration.instance();
        String matcherConfig = config.getString(CONFIG_MATCHERS, "");
        if(StringUtils.isNotEmpty(matcherConfig)) {
            String[] matcherClasses = matcherConfig.split(",");
            for (int i = 0; i < matcherClasses.length; i++) {
                String matcherClass = matcherClasses[i];
                try {
                    matchers.add((MCRUserMatcher) Class.forName(matcherClass).newInstance());
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new MCRConfigurationException("Property key " + CONFIG_MATCHERS + " not valid.");
                }
            }
        }
        return matchers;
    }

    @Override
    protected void handleObjectUpdated(MCREvent evt, MCRObject obj) {
        // TODO: remove this, since this EventHandler should only work for "ObjectCreated" events!
        handleObjectCreated(evt, obj);
    }

    @Override
    protected void handleObjectCreated(MCREvent evt, MCRObject obj) {
        // get all mods:name from persons (authors etc.) of the publication
        List<Element> modsNameElements = MCRUserMatcherUtils.getNameElements(obj);

        // for every mods:name element, call our configured Implementation(s) of MCRUserMatcher
        List<MCRUserMatcher> matchers = loadMatcherImplementationChain();
        MCRUserMatcher localMatcher = new MCRUserMatcherLocal();

        for(Element modsNameElement : modsNameElements) {

            MCRUser mcrUser = MCRUserMatcherUtils.createNewMCRUserFromModsNameElement(modsNameElement);

            for(MCRUserMatcher matcher : matchers) {
                MCRUser enrichedMCRUser = matcher.matchUser(mcrUser);
                LOGGER.debug("Current attributes for user: {} of mods:name: {}, attributes: {}",
                        enrichedMCRUser.getUserName(),
                        new XMLOutputter(Format.getPrettyFormat()).outputString(modsNameElement),
                        enrichedMCRUser.getAttributes());
            }

            MCRUser mcrUserFinal = localMatcher.matchUser(mcrUser);
            MCRUserManager.updateUser(mcrUserFinal);
        }

        // TODO: extend publication with configured "lead-ID" of the matched authors/MCRUsers
    }
}
