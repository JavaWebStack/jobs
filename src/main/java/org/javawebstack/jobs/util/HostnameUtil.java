package org.javawebstack.jobs.util;

import java.io.IOException;
import java.util.Scanner;

public class HostnameUtil {

    private static String hostname;

    public static String getHostname() {
        if(hostname == null) {
            try {
                Scanner s = new Scanner(Runtime.getRuntime().exec("hostname").getInputStream()).useDelimiter("\\A");
                hostname = s.hasNext() ? s.next() : "";
                s.close();
            } catch (IOException e) {}
        }
        return hostname;
    }

}
