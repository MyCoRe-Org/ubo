package unidue.ubo.picker;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.frontend.servlets.MCRServlet;
import org.mycore.frontend.servlets.MCRServletJob;


public class IdentityPicker extends MCRServlet {

    private final static Logger LOGGER = LogManager.getLogger(IdentityResolver.class);

    private static final String STRATEGY_CONFIG_STRING = "MCR.IdentityPicker.strategy";

    private String readIdentityStrategy() {
        return MCRConfiguration2.getString(STRATEGY_CONFIG_STRING).get();
    }

    public void doGetPost(MCRServletJob job) {

        String serviceClassName = readIdentityStrategy() + "PIDSearch"; // TODO: fix/align class names
        try {
            IdentityPickerService pickerService = (IdentityPickerService)Class.forName(serviceClassName)
                    .getDeclaredConstructor().newInstance();
            LOGGER.info("Using identity strategy: {}", readIdentityStrategy());
            LOGGER.info("Trying to load class: {}", serviceClassName);
            pickerService.handleRequest(job);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
