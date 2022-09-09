package org.javawebstack.jobs.storage.model;

import org.javawebstack.jobs.JobStatus;
import org.javawebstack.jobs.test.jobs.NoOpJob;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class JobInfoTest {

    @Test
    public void testClone() {
        JobInfo info = new JobInfo()
                .setId(UUID.randomUUID())
                .setType(NoOpJob.class.getName())
                .setStatus(JobStatus.ENQUEUED)
                .setCreatedAt(Date.from(Instant.now()));
        JobInfo clone = info.clone();
        assertEquals(info.getId(), clone.getId());
        assertEquals(info.getType(), clone.getType());
        assertEquals(info.getStatus(), clone.getStatus());
        assertEquals(info.getCreatedAt(), clone.getCreatedAt());
    }

    @Test
    public void testCheckRequired() {
        JobInfo infoWithoutType = new JobInfo();
        JobInfo infoWithRequired = new JobInfo()
                .setType(NoOpJob.class.getName());
        assertThrows(IllegalArgumentException.class, infoWithoutType::checkRequired);
        assertDoesNotThrow(infoWithRequired::checkRequired);
    }

    @Test
    public void testSanitizeDoesNotManipulateGivenFields() {
        UUID expectedId = UUID.randomUUID();
        String expectedType = NoOpJob.class.getName();
        JobStatus expectedStatus = JobStatus.SCHEDULED;
        Date expectedCreatedAt = Date.from(Instant.now());

        JobInfo completeInfo = new JobInfo()
                .setId(expectedId)
                .setType(expectedType)
                .setStatus(expectedStatus)
                .setCreatedAt(expectedCreatedAt);
        completeInfo.sanitize();

        assertEquals(expectedId, completeInfo.getId());
        assertEquals(expectedType, completeInfo.getType());
        assertEquals(expectedStatus, completeInfo.getStatus());
        assertEquals(expectedCreatedAt, completeInfo.getCreatedAt());
    }

    @Test
    public void testSanitizeAddsMissingFields() {
        JobInfo incompleteInfo = new JobInfo()
                .setType(NoOpJob.class.getName());
        incompleteInfo.sanitize();
        assertNotNull(incompleteInfo.getId());
        assertEquals(JobStatus.CREATED, incompleteInfo.getStatus());
        assertNotNull(incompleteInfo.getCreatedAt());
    }

}
