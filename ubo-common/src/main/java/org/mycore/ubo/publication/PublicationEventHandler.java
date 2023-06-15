package org.mycore.ubo.publication;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.mycore.common.MCRClassTools;
import org.mycore.common.MCRConstants;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.common.config.MCRConfigurationException;
import org.mycore.common.events.MCREvent;
import org.mycore.common.events.MCREventHandlerBase;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.ubo.matcher.MCRUserMatcher;
import org.mycore.ubo.matcher.MCRUserMatcherDTO;
import org.mycore.ubo.matcher.MCRUserMatcherLocal;
import org.mycore.ubo.matcher.MCRUserMatcherUtils;
import org.mycore.user2.MCRUser;
import org.mycore.user2.MCRUserAttribute;
import org.mycore.user2.MCRUserManager;


/**
 * EventHandler for new publications in MODS-format.
 *
 * 1. For all persons that are listed in a given publication, create a new MCRUser.
 *
 * 2. Match all new MCRUsers against the configured chain of implementations of MCRUserMatcher
 *
 * 2.1 When a match is found, the MCRUserMatcher implementations shall enrich the MCRUsers attributes with the matched
 * Users attributes of the target system, it is also the MCRUserMatcher implementations task to set the realm of the
 * created user appropriately
 *
 * 3. At last, match the new MCRUser against the locally persisted MCRUsers. If a match is found, the already persisted
 * MCRUsers attributes are enriched with the new MCRUsers attributes. Otherwise, persist the new MCRUser
 *
 * 4. Persist all new MCRUsers ONLY if they where matched/enriched in 2.1. or in 3.
 *
 * 5. Extend the mods:name -&gt; mods:nameIdentifier element of the publication with the configured "lead-ID" if it is
 * not present but available in the matched MCRUsers attributes.
 *
 * 6. If no MCRUser was created because there was neither Match found nor attributes enriched (2.1. or 3.), check each
 * person in the publication for affiliation. If an affiliation is found, create a new MCRUser in a special realm and
 * persist it.
 *
 * The following properties in the mycore.properties are used:
 *
 * # Default Role that is assigned to newly created users
 * MCR.user2.IdentityManagement.UserCreation.DefaultRole=submitter
 *
 * # Realm of unvalidated MCRUsers
 * MCR.user2.IdentityManagement.UserCreation.Unvalidated.Realm=unvalidated
 *
 * MCR.user2.matching.chain (Multiple implementations separated by ",")
 * Example:
 * MCR.user2.matching.chain=org.mycore.ubo.matcher.MCRUserMatcherLDAP,org.mycore.ubo.matcher.MCRUserMatcherDummy
 *
 * MCR.user2.matching.lead_id TODO: anpassen...
 * Example:
 * MCR.user2.matching.lead_id=id_scopus
 *
 * # currently only "uuid" can be used, leave empty if no explicit connection should be inserted
 * MCR.user2.matching.publication.connection.strategy=uuid
 *
 * @author Pascal Rost
 */
public class PublicationEventHandler extends MCREventHandlerBase {

    private final static Logger LOGGER = LogManager.getLogger(PublicationEventHandler.class);

    private final static String CONFIG_MATCHERS = "MCR.user2.matching.chain";
    private final static String CONFIG_LEAD_ID = "MCR.user2.matching.lead_id";
    private final static String CONFIG_CONNECTION_STRATEGY = "MCR.user2.matching.publication.connection.strategy";
    private final static String CONFIG_DEFAULT_ROLE = "MCR.user2.IdentityManagement.UserCreation.DefaultRole";
    private final static String CONFIG_UNVALIDATED_REALM = "MCR.user2.IdentityManagement.UserCreation.Unvalidated.Realm";

    private final static String CONFIG_SKIP_LEAD_ID = "MCR.user2.matching.lead_id.skip";

    private final static String CONNECTION_TYPE_NAME = "id_connection";

    private List<MCRUserMatcher> loadMatcherImplementationChain() {
        List<MCRUserMatcher> matchers = new ArrayList<>();

        Optional<String> matcherConfig = MCRConfiguration2.getString(CONFIG_MATCHERS);
        if(matcherConfig.isPresent()) {
            String[] matcherClasses = matcherConfig.get().split(",");
            for (int i = 0; i < matcherClasses.length; i++) {
                String matcherClass = matcherClasses[i];
                try {
                    matchers.add((MCRUserMatcher) MCRClassTools.forName(matcherClass).getDeclaredConstructor().newInstance());
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new MCRConfigurationException("Property key " + CONFIG_MATCHERS + " not valid.");
                }
            }
        }
        return matchers;
    }

    private String loadLeadIDName() {
        return MCRConfiguration2.getString(CONFIG_LEAD_ID).orElse("");
    }

    private String loadDefaultRoleConfig() {
        return MCRConfiguration2.getString(CONFIG_DEFAULT_ROLE).orElse("submitter");
    }

    private String loadUnvalidatedRealmConfig() {
        return MCRConfiguration2.getString(CONFIG_UNVALIDATED_REALM).get();
    }

    /**
     * Returns the configured connection strategy to "connect" publications to MCRUsers
     * @return String, null if no connection strategy has been set
     */
    private String loadConnectionStrategyConfig() {
        return MCRConfiguration2.getString(CONFIG_CONNECTION_STRATEGY).get();
    }

    @Override
    protected void handleObjectUpdated(MCREvent evt, MCRObject obj) {
        // TODO: remove this, since this EventHandler should only work for "ObjectCreated" events!
        handleObjectCreated(evt, obj);
    }

    @Override
    protected void handleObjectCreated(MCREvent evt, MCRObject obj) {
        // get default role for new users
        String defaultRole = loadDefaultRoleConfig();

        // get realmID for unvalidated MCRUsers
        final String UNVALIDATED_REALM = loadUnvalidatedRealmConfig();

        // get all mods:name from persons (authors etc.) of the publication
        List<Element> modsNameElements = MCRUserMatcherUtils.getNameElements(obj);

        // leadIDName -> if the matched MCRUser has this ID set in the attributes, enrich the publication with it
        String leadIDName = loadLeadIDName();

        // for every mods:name element, call our configured Implementation(s) of MCRUserMatcher
        List<MCRUserMatcher> matchers = loadMatcherImplementationChain();
        MCRUserMatcher localMatcher = new MCRUserMatcherLocal();

        for(Element modsNameElement : modsNameElements) {

            MCRUserMatcherDTO matcherDTO = new MCRUserMatcherDTO(
                    MCRUserMatcherUtils.createNewMCRUserFromModsNameElement(modsNameElement));

            for (MCRUserMatcher matcher : matchers) {
                matcherDTO = matcher.matchUser(matcherDTO);
                MCRUser mcrUser = matcherDTO.getMCRUser();
                if (matcherDTO.wasMatchedOrEnriched()) {
                    LOGGER.info("Found a match for user: {} of mods:name: {}, with attributes: {}, using " +
                                    "matcher: {}",
                            mcrUser.getUserName(),
                            new XMLOutputter(Format.getPrettyFormat()).outputString(modsNameElement),
                            mcrUser.getAttributes().stream().map(a -> a.getName() + "=" + a.getValue())
                                    .collect(Collectors.joining(" | ")),
                            matcher.getClass());
                }
            }

            MCRUserMatcherDTO localMatcherDTO = localMatcher.matchUser(matcherDTO);
            if (localMatcherDTO.wasMatchedOrEnriched()) {
                MCRUser mcrUserFinal = localMatcherDTO.getMCRUser();
                mcrUserFinal.assignRole(defaultRole);
                MCRUserManager.updateUser(mcrUserFinal);
                enrichModsNameElementByLeadID(modsNameElement, leadIDName, mcrUserFinal);
                connectModsNameElementWithMCRUser(modsNameElement, mcrUserFinal);
            } else {
                if(MCRUserMatcherUtils.checkAffiliation(modsNameElement) &&
                        (MCRUserMatcherUtils.getNameIdentifiers(modsNameElement).size() > 0)) {
                    MCRUser affiliatedUser = MCRUserMatcherUtils.createNewMCRUserFromModsNameElement(modsNameElement, UNVALIDATED_REALM);
                    affiliatedUser.assignRole(defaultRole);
                    MCRUserManager.updateUser(affiliatedUser);
                    enrichModsNameElementByLeadID(modsNameElement, leadIDName, affiliatedUser);
                    connectModsNameElementWithMCRUser(modsNameElement, affiliatedUser);
                } else {
                    // ignore Person, do NOT create a new MCRUser
                }
            }

            MCRConfiguration2.getBoolean(CONFIG_SKIP_LEAD_ID)
                .filter(Boolean::booleanValue)
                .ifPresent(trueValue -> {
                    List<Element> elementsToRemove = modsNameElement.getChildren("nameIdentifier", MCRConstants.MODS_NAMESPACE)
                            .stream()
                            .filter(element -> element.getAttributeValue("type").equals(leadIDName))
                            .collect(Collectors.toList());
                    elementsToRemove.forEach(modsNameElement::removeContent);
                });
        }
        LOGGER.debug("Final document: {}", new XMLOutputter(Format.getPrettyFormat()).outputString(obj.createXML()));
    }

    /**
     * Enriches the mods:name-element that corresponds to the given MCRUser with a mods:nameIdentifier-element if the
     * given MCRUser has an attribute with the name of the so called "lead_id" that is configured in the
     * mycore.properties and given as parameter "leadIDmods".
     * A new mods:nameIdentifier-element with type "lead_id" and its value will only be created if no other
     * mods:nameIdentifier-element with the same ID/type exists as a sub-element of the given modsNameElement.

     * @param modsNameElement the mods:name-element which will be enriched
     * @param leadIDmods the "lead_id" (as configured in the mycore.properties) in mods format (no prefix "id_")
     * @param mcrUser the MCRUser corresponding to the modsNameElement
     */
    private void enrichModsNameElementByLeadID(Element modsNameElement, String leadIDmods, MCRUser mcrUser) {
        String leadIDmycore = "id_" + leadIDmods;
        if(StringUtils.isNotEmpty(mcrUser.getUserAttribute(leadIDmycore))) {
            String leadIDValue = mcrUser.getUserAttribute(leadIDmycore);
            if(StringUtils.isNotEmpty(leadIDValue)) {
                if(!MCRUserMatcherUtils.containsNameIdentifierWithType(modsNameElement, leadIDmods)) {
                    LOGGER.info("Enriched publication for MCRUser: {}, with nameIdentifier of type: {} (lead_id) " +
                            "and value: {}", mcrUser.getUserName(), leadIDmods, leadIDValue);
                    enrichModsNameElementByNameIdentifierElement(modsNameElement, leadIDmods, leadIDValue);
                }
            }
        }
    }

    private void enrichModsNameElementByNameIdentifierElement(Element modsNameElement,
                                                              String attributeType, String attributeValue) {
        Element nameIdentifier = new Element("nameIdentifier", MCRUserMatcherUtils.MODS_NAMESPACE)
                .setAttribute("type", attributeType)
                .setText(attributeValue);
        modsNameElement.addContent(nameIdentifier);
    }

    /**
     *
     * @param modsNameElement
     * @param mcrUser
     */
    private void connectModsNameElementWithMCRUser(Element modsNameElement, MCRUser mcrUser) {
        String connectionStrategy = loadConnectionStrategyConfig();
        if(StringUtils.isNotEmpty(connectionStrategy) && connectionStrategy.equals("uuid")) {
            // check if MCRUser already has a "connection" UUID
            String uuid = mcrUser.getUserAttribute(CONNECTION_TYPE_NAME);
            String modsTypeName = CONNECTION_TYPE_NAME.replace("id_", "");
            if(uuid == null) {
                // create new UUID and persist it for mcrUser
                uuid = UUID.randomUUID().toString();
                mcrUser.getAttributes().add(new MCRUserAttribute(CONNECTION_TYPE_NAME, uuid));
                MCRUserManager.updateUser(mcrUser);
            }
            // if not already present, persist connection in mods:name - nameIdentifier-Element
            if(!MCRUserMatcherUtils.containsNameIdentifierWithType(modsNameElement, modsTypeName)) {
                LOGGER.info("Connecting publication with MCRUser: {}, via nameIdentifier of type: {} " +
                        "and value: {}", mcrUser.getUserName(), modsTypeName, uuid);
                enrichModsNameElementByNameIdentifierElement(modsNameElement, modsTypeName, uuid);
            }
        }
    }
}
