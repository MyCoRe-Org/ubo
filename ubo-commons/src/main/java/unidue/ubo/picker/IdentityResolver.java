package unidue.ubo.picker;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Hashtable;

import javax.xml.transform.Source;
import javax.xml.transform.URIResolver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;
import org.jdom2.transform.JDOMSource;
import org.mycore.common.MCRUsageException;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.common.xml.MCRURIResolver;

public class IdentityResolver implements URIResolver {

    private final static Logger LOGGER = LogManager.getLogger(IdentityResolver.class);

    private static final String STRATEGY_CONFIG_STRING = "MCR.IdentityPicker.strategy";

    private static final String RESOLVE_TYPE_SEARCH = "search";
    private static final String RESOLVE_TYPE_DETAIL = "detail";

    private String readIdentityStrategy() {
        return MCRConfiguration2.getString(STRATEGY_CONFIG_STRING).get();
    }

    public Source resolve(String href, String base) {
        Element result = null;
        String[] splitHref = href.split(":");
        //LOGGER.info("splitHref: {}", Arrays.toString(splitHref));
        boolean error = false;
        if (splitHref.length == 3) {
            String resolveType = splitHref[1];
            String paramsString = splitHref[2];
            if(resolveType.equals(RESOLVE_TYPE_DETAIL) || resolveType.equals(RESOLVE_TYPE_SEARCH)) {

                String decodedParamsString = "";
                try {
                    // decode for umlaute
                    decodedParamsString = URLDecoder.decode(paramsString, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                Hashtable<String, String> params = MCRURIResolver.getParameterMap(decodedParamsString);
                String strategy = readIdentityStrategy();
                String serviceClassName = strategy + "Service";
                LOGGER.info("PARAMS: " + params.toString());
                LOGGER.info("Using identity strategy: {}", strategy);
                LOGGER.info("Trying to load class: {}", serviceClassName);
                try {
                    IdentityService service = (IdentityService)Class.forName(serviceClassName).getDeclaredConstructor().newInstance();
                    if(resolveType.equals(RESOLVE_TYPE_SEARCH)) {
                        result = service.searchPerson(params);
                    } else if(resolveType.equals(RESOLVE_TYPE_DETAIL)) {
                        result = service.getPersonDetails(params);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                error = true;
            }
        } else {
           error = true;
        }
        if (error) {
            throw new MCRUsageException("Identity Resolver not used correctly, format: " +
                    "ires:{search|detail}:{attributes/params}");
        }
        return new JDOMSource(result);
    }
}
