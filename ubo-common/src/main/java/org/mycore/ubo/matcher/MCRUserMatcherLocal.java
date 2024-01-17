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
import org.mycore.user2.MCRUser;
import org.mycore.user2.MCRUserAttribute;
import org.mycore.user2.MCRUserManager;

/**
 * Given a MCRUser, match against the local MCRUsers, returning the given User or an existing local one if matched.
 * If matched, the returned local MCRUsers attributes are enriched by attributes from the given MCRUser.
 *
 * @author Pascal Rost
 */
public class MCRUserMatcherLocal implements MCRUserMatcher {

    private final static Logger LOGGER = LogManager.getLogger(MCRUserMatcherLocal.class);

    @Override
    public MCRUserMatcherDTO matchUser(MCRUserMatcherDTO matcherDTO) {

        MCRUser mcrUser = matcherDTO.getMCRUser();
        List<MCRUser> matchingUsers = new ArrayList<>(getUsersForGivenAttributes(mcrUser.getAttributes()));
        if(matchingUsers.size() >= 1) {
            MCRUser matchingUser = matchingUsers.get(0);

            LOGGER.info("Found local matching user! Matched user: {} and attributes: {} with local user: {} and attributes: {}",
                    mcrUser.getUserName(),
                    mcrUser.getAttributes().stream().map(a -> a.getName() + "=" + a.getValue()).collect(Collectors.joining(" | ")),
                    matchingUser.getUserName(),
                    matchingUser.getAttributes().stream().map(a -> a.getName() + "=" + a.getValue()).collect(Collectors.joining(" | ")));

            // only add not attributes which are not present
            matchingUser.getAttributes()
                    .addAll(mcrUser.getAttributes().stream()
                            .filter(Predicate.not(matchingUser.getAttributes()::contains))
                            .collect(Collectors.toUnmodifiableList()));

            mcrUser = matchingUser;
            matcherDTO.setMCRUser(mcrUser);
            matcherDTO.setMatchedOrEnriched(true);
        }
        return matcherDTO;
    }

    private Set<MCRUser> getUsersForGivenAttributes(SortedSet<MCRUserAttribute> mcrAttributes) {
        Set<MCRUser> users = new HashSet<>();
        for(MCRUserAttribute mcrAttribute : mcrAttributes) {
            String attributeName = mcrAttribute.getName();
            String attributeValue = mcrAttribute.getValue();
            users.addAll(MCRUserManager.getUsers(attributeName, attributeValue).collect(Collectors.toList()));
        }
        return users;
    }

}
