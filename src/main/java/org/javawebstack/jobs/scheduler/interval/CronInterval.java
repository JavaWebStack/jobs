package org.javawebstack.jobs.scheduler.interval;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;

import javax.annotation.Nullable;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

public class CronInterval implements Interval {

    final ExecutionTime executionTime;
    final String cron;

    public CronInterval(String cron) {
        this.cron = cron;
        switch (cron) {
            case "@yearly":
            case "@annually":
                cron = "0 0 1 1 *";
                break;
            case "@monthly":
                cron = "0 0 1 * *";
                break;
            case "@daily":
                cron = "0 0 * * *";
                break;
            case "@hourly":
                cron = "0 * * * *";
                break;
        }
        CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX));
        Cron parsedCron = parser.parse(cron);
        executionTime = ExecutionTime.forCron(parsedCron);
    }

    public Date next(@Nullable Date last) {
        if(last == null)
            last = Date.from(Instant.now());
        return Date.from(
                executionTime.nextExecution(ZonedDateTime.ofInstant(last.toInstant(), ZoneId.of("UTC")))
                        .orElseThrow(() -> new RuntimeException("Unable to find next execution date"))
                        .toInstant()
        );
    }

    public String serialize() {
        return cron;
    }

    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (obj instanceof String)
            return obj.equals(cron);
        if (obj instanceof CronInterval)
            return ((CronInterval) obj).cron.equals(cron);

        return false;
    }
}
