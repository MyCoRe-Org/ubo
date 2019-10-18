package unidue.ubo.lsf;

import org.jdom2.Element;
import org.mycore.common.MCRCache;
import org.mycore.common.config.MCRConfiguration;

public class LSFServiceCache {

    /** The singleton instance */
    protected static LSFServiceCache singleton;

    protected long maxAge;

    protected MCRCache<String, Element> cache;

    private LSFServiceCache() {
        maxAge = MCRConfiguration.instance().getLong("UBO.LSFClient.Cache.MaxAge");
        int capacity = MCRConfiguration.instance().getInt("UBO.LSFClient.Cache.Capacity");
        cache = new MCRCache<>(capacity, "LSF Client");
    }

    /** Returns the LSF client cache instance */
    public static synchronized LSFServiceCache instance() {
        if (singleton == null) {
            singleton = new LSFServiceCache();
        }
        return singleton;
    }

    public Element getIfUpToDate(String pid) {
        long time = System.currentTimeMillis() - maxAge;
        return cache.getIfUpToDate(pid, time);
    }

    public void put(String pid, Element elem) {
        cache.put(pid, elem);
    }

}
