package org.mycore.ubo.ratelimit;

import io.github.bucket4j.Bucket;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.transform.JDOMSource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mycore.common.MCRException;
import org.mycore.common.MCRTestConfiguration;
import org.mycore.common.MCRTestProperty;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.common.config.MCRConfigurationException;
import org.mycore.common.xml.MCRMockResolver;
import org.mycore.common.xml.MCRURIResolver;
import org.mycore.test.MCRJPAExtension;
import org.mycore.test.MyCoReTest;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MyCoReTest
@ExtendWith({ MCRJPAExtension.class })
@MCRTestConfiguration(properties = {
    @MCRTestProperty(key = "MCR.URIResolver.ModuleResolver.ratelimit",
        string = "org.mycore.ubo.ratelimit.MCRRateLimitResolver"),
})
public class MCRRateLimitResolverTest {

    final static JDOMSource resultSource = new JDOMSource(new Document(new Element("result")));

    public static final String RATE_LIMIT_CALL = "ratelimit:Test:Mock:nothing";

    @BeforeAll
    public static void setUp() {
        MCRMockResolver.setResultSource(resultSource);
        MCRRateLimitBuckets.clearAllBuckets();
    }

    @AfterAll
    public static void tearDown() {
        MCRMockResolver.clearCalls();
    }

    /**
     * Tests, if the rate-limiting throws an exception upon consuming all tokens.
     */
    @Test
    public void testResolveError() {
        MCRConfiguration2.set("MCR.RateLimitResolver.Test.Behavior", "error");
        MCRConfiguration2.set("MCR.RateLimitResolver.Test.Limits", "100/D, 12/h, 6/min");
        final Bucket bucket = MCRRateLimitBuckets.getOrCreateBucket("Test");
        bucket.tryConsumeAsMuchAsPossible();
        assertThrows(MCRException.class, () -> MCRURIResolver.obtainInstance().resolve(RATE_LIMIT_CALL, null));
    }

    /**
     * Tests, if the blocking of downstream processing by the RateLimitResolver works properly.
     */
    @Test
    public void testResolveBlocking() throws TransformerException {
        MCRConfiguration2.set("MCR.RateLimitResolver.Test.Behavior", "block");
        MCRConfiguration2.set("MCR.RateLimitResolver.Test.Limits", "10/s");
        final Bucket bucket = MCRRateLimitBuckets.getOrCreateBucket("Test");
        bucket.tryConsumeAsMuchAsPossible();
        MCRURIResolver.obtainInstance().resolve(RATE_LIMIT_CALL, null);
    }

    /**
     * Tests, if the RateLimitResolver returns an empty Source-object if the configured behavior upon
     * reaching the rate limit is "empty".
     */
    @Test
    public void testResolveEmpty() throws TransformerException {
        MCRConfiguration2.set("MCR.RateLimitResolver.Test.Behavior", "empty");
        MCRConfiguration2.set("MCR.RateLimitResolver.Test.Limits", "10/min");
        final Bucket bucket = MCRRateLimitBuckets.getOrCreateBucket("Test");
        bucket.tryConsumeAsMuchAsPossible();
        Source emptySource = MCRURIResolver.obtainInstance().resolve(RATE_LIMIT_CALL, null);
        assertNotNull(emptySource);
        assertFalse(emptySource.isEmpty());
        assertEquals("", emptySource.getSystemId());
    }

    /**
     * Tests, if configured factored time units are resolved without error.
     */
    @Test
    public void testResolveFactoredUnit() throws TransformerException {
        MCRConfiguration2.set("MCR.RateLimitResolver.Test.Behavior", "error");
        MCRConfiguration2.set("MCR.RateLimitResolver.Test.Limits", "100/5m");
        MCRURIResolver.obtainInstance().resolve(RATE_LIMIT_CALL, null);
    }

    /**
     * Tests the behavior of the {@link MCRRateLimitResolver} and {@link MCRRateLimitBuckets} in case
     * of missing or wrong config.
     */
    @Test
    public void testResolveMalformedConfig() {
        // Test wrong value for behavior
        MCRConfiguration2.set("MCR.RateLimitResolver.Test.Behavior", "blocking");
        MCRConfiguration2.set("MCR.RateLimitResolver.Test.Limits", "10/s");

        MCRConfigurationException mcrConfigurationException = assertThrows(MCRConfigurationException.class,
            () -> MCRURIResolver.obtainInstance().resolve(RATE_LIMIT_CALL, null));

        assertTrue(mcrConfigurationException.getMessage()
            .contains("The behavior for ID Test is not correctly configured"));
        assertTrue(mcrConfigurationException.getCause().getMessage()
            .contains("not a valid type of enum RateLimitBehavior: blocking"));

        // Test wrong value for time unit
        MCRConfiguration2.set("MCR.RateLimitResolver.Test1.Behavior", "block");
        MCRConfiguration2.set("MCR.RateLimitResolver.Test1.Limits", "10/second");
        MCRConfigurationException mcrConfigurationException1 = assertThrows(MCRConfigurationException.class,
            () -> MCRRateLimitBuckets.getOrCreateBucket("Test1"));
        assertTrue(mcrConfigurationException1.getMessage().contains("10 tokens per second"));
        assertTrue(mcrConfigurationException1.getMessage().contains("Test1"));

        // Test negative value for token amount
        MCRConfiguration2.set("MCR.RateLimitResolver.Test2.Behavior", "block");
        MCRConfiguration2.set("MCR.RateLimitResolver.Test2.Limits", "-10/s");
        IllegalArgumentException illegalArgumentException
            = assertThrows(IllegalArgumentException.class, () -> MCRRateLimitBuckets.getOrCreateBucket("Test2"));
        assertTrue(illegalArgumentException.getMessage().contains("-10"));
        assertTrue(illegalArgumentException.getMessage().contains("capacity should be positive"));

        // Test non-number value for token amount
        MCRConfiguration2.set("MCR.RateLimitResolver.Test3.Behavior", "block");
        MCRConfiguration2.set("MCR.RateLimitResolver.Test3.Limits", "abc/s");
        NumberFormatException numberFormatException
            = assertThrows(NumberFormatException.class, () -> MCRRateLimitBuckets.getOrCreateBucket("Test3"));
        assertTrue(numberFormatException.getMessage().contains("abc"));

        // Test negative value for time unit factor
        MCRConfiguration2.set("MCR.RateLimitResolver.Test4.Behavior", "block");
        MCRConfiguration2.set("MCR.RateLimitResolver.Test4.Limits", "100/-5s");
        IllegalArgumentException illegalArgumentException2
            = assertThrows(IllegalArgumentException.class, () -> MCRRateLimitBuckets.getOrCreateBucket("Test4"));
        assertTrue(illegalArgumentException2.getMessage().contains("-5"));
        assertTrue(illegalArgumentException2.getMessage().contains("period should be positive"));

        // Test missing config
        MCRConfigurationException mcrConfigurationException2 = assertThrows(MCRConfigurationException.class,
            () -> MCRRateLimitBuckets.getOrCreateBucket("Test5"));
        assertTrue(mcrConfigurationException2.getMessage()
            .contains("Configuration property MCR.RateLimitResolver.Test5."));
    }
}
