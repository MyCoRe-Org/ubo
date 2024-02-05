package org.mycore.ubo.matcher;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;
import org.mycore.common.MCRException;
import org.mycore.common.xml.MCRNodeBuilder;
import org.mycore.mods.merger.MCRMerger;
import org.mycore.mods.merger.MCRMergerFactory;
import org.mycore.user2.MCRUser;
import org.mycore.user2.MCRUserAttribute;
import org.mycore.user2.MCRUserManager;

/**
 * Given a MCRUser, match against the local MCRUsers, returning the given User or an existing local one if matched.
 * If matched, the returned local MCRUsers attributes are enriched by attributes from the given MCRUser.
 * The attribute id_connection is considered unique and can not be enriched/added a second time.
 *
 * @author Pascal Rost
 */
public class MCRUserMatcherLocal implements MCRUserMatcher {

    private final static Logger LOGGER = LogManager.getLogger(MCRUserMatcherLocal.class);

    private final static String CONNECTION_TYPE_NAME = "id_connection";

    private static final String XPATH_TO_BUILD_MODSNAME = "mods:name[@type='personal']";

    @Override
    public MCRUserMatcherDTO matchUser(MCRUserMatcherDTO matcherDTO) {
        MCRUser mcrUser = matcherDTO.getMCRUser();
        List<MCRUser> matchingUsers = new ArrayList<>(getUsersForGivenAttributes(mcrUser.getAttributes()));

        MCRMerger nameThatShouldMatch = buildNameMergerFrom(mcrUser);
        matchingUsers.removeIf(userToTest -> !buildNameMergerFrom(userToTest).isProbablySameAs(nameThatShouldMatch));

        if (!matchingUsers.isEmpty()) {
            MCRUser matchingUser = matchingUsers.get(0);

            LOGGER.info(
                "Found local matching user! Matched user: {} and attributes: {} with local user: {} and attributes: {}",
                mcrUser.getUserName(),
                mcrUser.getAttributes().stream().map(a -> a.getName() + "=" + a.getValue())
                    .collect(Collectors.joining(" | ")),
                matchingUser.getUserName(),
                matchingUser.getAttributes().stream().map(a -> a.getName() + "=" + a.getValue())
                    .collect(Collectors.joining(" | ")));

            final boolean hasMatchingUserConnectionKey = matchingUser.getUserAttribute(CONNECTION_TYPE_NAME) != null;

            // only add attributes which are not present, don't add duplicate connection attributes
            matchingUser.getAttributes().addAll(mcrUser.getAttributes().stream()
                .filter(attribute -> !attribute.getName().equals(CONNECTION_TYPE_NAME) || !hasMatchingUserConnectionKey)
                .filter(Predicate.not(matchingUser.getAttributes()::contains))
                .toList());

            matcherDTO.setMCRUser(matchingUser);
            matcherDTO.setMatchedOrEnriched(true);
        }
        return matcherDTO;
    }

    private Set<MCRUser> getUsersForGivenAttributes(SortedSet<MCRUserAttribute> mcrAttributes) {
        Set<MCRUser> users = new HashSet<>();
        for (MCRUserAttribute mcrAttribute : mcrAttributes) {
            String attributeName = mcrAttribute.getName();
            String attributeValue = mcrAttribute.getValue();
            users.addAll(MCRUserManager.getUsers(attributeName, attributeValue).toList());
        }
        return users;
    }

    private MCRMerger buildNameMergerFrom(MCRUser user) {
        try {
            Element modsName = new MCRNodeBuilder().buildElement(XPATH_TO_BUILD_MODSNAME
                + "[mods:namePart='" + user.getRealName() + "']", null, null);
            return MCRMergerFactory.buildFrom(modsName);
        } catch (Exception shouldNeverOccur) {
            throw new MCRException(shouldNeverOccur);
        }
    }

}
