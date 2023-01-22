package org.javawebstack.jobs.scheduler.interval;

import javax.annotation.Nullable;
import java.util.Date;

public interface Interval {

    Date next(@Nullable Date last);

}
