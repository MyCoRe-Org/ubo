package unidue.ubo.matcher;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.user2.MCRUser;

/**
 * Dummy class to test and demonstrate configuration of matcher-chain.
 * @author Pascal Rost
 */
public class MCRUserMatcherDummy implements MCRUserMatcher {

    private final static Logger LOGGER = LogManager.getLogger(MCRUserMatcherDummy.class);

    @Override
    public MCRUserMatcherDTO matchUser(MCRUserMatcherDTO matcherDTO) {
        LOGGER.debug("MCRUserMatcherDummy got called!");
        return matcherDTO;
    }
}
