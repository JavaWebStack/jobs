package org.javawebstack.jobs.time;

import java.text.SimpleDateFormat;
import java.util.Date;

public class BasicTimeProvider implements TimeProvider {

    public long time() {
        return System.currentTimeMillis() / 1000;
    }

    public String format(long time) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(time));
    }

}
