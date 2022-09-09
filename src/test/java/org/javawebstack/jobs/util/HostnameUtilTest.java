package org.javawebstack.jobs.util;

import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

public class HostnameUtilTest {

    @Test
    public void testGetHostname() {
        String hostname = HostnameUtil.getHostname();
        assertNotNull(hostname);
        assertTrue(hostname.length() > 0);
        Pattern hostnamePattern = Pattern.compile("[^\\n\\s]*");
        assertTrue(hostnamePattern.matcher(hostname).matches());
    }

}
