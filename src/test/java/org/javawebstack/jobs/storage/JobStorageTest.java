package org.javawebstack.jobs.storage;

import org.javawebstack.jobs.JobStatus;
import org.javawebstack.jobs.LogLevel;
import org.javawebstack.jobs.scheduler.interval.CronInterval;
import org.javawebstack.jobs.storage.model.*;
import org.javawebstack.jobs.test.jobs.JobWithData;
import org.javawebstack.jobs.test.jobs.NoOpJob;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class JobStorageTest {

    private static final String NOOP_PAYLOAD = "{}";
    private static final String TEST_QUEUE = "storage_test_queue";
    private static final String EXAMPLE_HOSTNAME = "example.com";
    private static final long MILLENNIUM = 946681200000L; // 2000-01-01 00:00:00
    private static final long END_OF_THE_WORLD = 1356044400000L; // 2012-12-21 00:00:00

    protected JobStorage storage;

    @BeforeEach
    public void up() {
        storage = initStorage();
        generateVarietyOfJobs();
    }

    protected abstract JobStorage initStorage();

    private void generateVarietyOfJobs() {
        JobInfo info = new JobInfo().setType(NoOpJob.class.getName());
        for(JobStatus status : JobStatus.values()) {
            storage.createJob(info.clone().setStatus(status), NOOP_PAYLOAD);
        }
        storage.createJob(new JobInfo().setStatus(JobStatus.CREATED).setType(JobWithData.class.getName()), "{\"data\":\"a_string\"}");

        storage.createRecurringJob(new RecurringJobInfo().setQueue(TEST_QUEUE).setType(NoOpJob.class.getName()).setPayload(NOOP_PAYLOAD).setCron(new CronInterval("@daily")));
        storage.createRecurringJob(new RecurringJobInfo().setQueue(TEST_QUEUE).setType(NoOpJob.class.getName()).setPayload(NOOP_PAYLOAD).setCron(new CronInterval("@yearly")));
        storage.createRecurringJob(new RecurringJobInfo().setQueue(TEST_QUEUE).setType(NoOpJob.class.getName()).setPayload(NOOP_PAYLOAD).setCron(new CronInterval("@monthly")));
    }

    @Test
    public void testCreateAndGetJob() {
        UUID expectedId = UUID.randomUUID();
        JobStatus expectedStatus = JobStatus.SUCCESS;
        String expectedType = NoOpJob.class.getName();
        Date expectedCreatedAt = new Date(MILLENNIUM);
        String expectedPayload = NOOP_PAYLOAD;
        JobInfo createInfo = new JobInfo()
                .setId(expectedId)
                .setStatus(expectedStatus)
                .setType(expectedType)
                .setCreatedAt(expectedCreatedAt);
        storage.createJob(createInfo, expectedPayload);
        JobInfo retrievedInfo = storage.getJob(expectedId);
        assertNotNull(retrievedInfo);
        assertEquals(expectedId, retrievedInfo.getId());
        assertEquals(expectedType, retrievedInfo.getType());
        assertEquals(expectedStatus, retrievedInfo.getStatus());
        assertEquals(expectedCreatedAt, retrievedInfo.getCreatedAt());
        String retrievedPayload = storage.getJobPayload(expectedId);
        assertEquals(expectedPayload, retrievedPayload);
    }

    @Test
    public void testCreateJobWithMissingOptionalFields() {
        JobInfo info = new JobInfo().setType(NoOpJob.class.getName());
        storage.createJob(info, NOOP_PAYLOAD);
        assertNotNull(info.getId());
        assertEquals(JobStatus.CREATED, info.getStatus());
        assertNotNull(info.getCreatedAt());
        assertNotNull(storage.getJob(info.getId()));
    }

    @Test
    public void testCreateJobWithMissingRequiredFields() {
        JobInfo info = new JobInfo().setId(UUID.randomUUID());
        assertThrows(IllegalArgumentException.class, () -> storage.createJob(info, NOOP_PAYLOAD));
        assertNull(storage.getJob(info.getId()));
    }

    @Test
    public void testQueryAllJobs() {
        assertTrue(storage.queryJobs(new JobQuery()).size() > 0);
    }

    @Test
    public void testQueryAllRecurringJobs() {
        assertTrue(storage.queryRecurringJobs(new RecurringJobQuery()).size() > 0);
    }

    @Test
    public void testQueryJobsBySingleStatus() {
        JobStatus expectedStatus = JobStatus.SUCCESS;
        List<JobInfo> jobs = storage.queryJobs(new JobQuery().setStatus(expectedStatus));
        assertTrue(jobs.stream().anyMatch(j -> j.getStatus() == expectedStatus));
        assertTrue(jobs.stream().allMatch(j -> j.getStatus() == expectedStatus));
    }

    @Test
    public void testQueryJobsByMultipleStatuses() {
        JobStatus firstExpectedStatus = JobStatus.SUCCESS;
        JobStatus secondExpectedStatus = JobStatus.FAILED;
        List<JobInfo> jobs = storage.queryJobs(new JobQuery().setStatus(firstExpectedStatus, secondExpectedStatus));
        assertTrue(jobs.stream().anyMatch(j -> j.getStatus() == firstExpectedStatus));
        assertTrue(jobs.stream().anyMatch(j -> j.getStatus() == secondExpectedStatus));
        assertTrue(jobs.stream().allMatch(j -> j.getStatus() == firstExpectedStatus || j.getStatus() == secondExpectedStatus));
    }

    @Test
    public void testQueryJobsByType() {
        String expectedType = NoOpJob.class.getName();
        List<JobInfo> jobs = storage.queryJobs(new JobQuery().setType(expectedType));
        assertTrue(jobs.stream().anyMatch(j -> j.getType().equals(expectedType)));
        assertTrue(jobs.stream().allMatch(j -> j.getType().equals(expectedType)));
    }

    @Test
    public void testQueryRecurringJobsByType() {
        String expectedType = NoOpJob.class.getName();
        List<RecurringJobInfo> jobs = storage.queryRecurringJobs(new RecurringJobQuery().setType(expectedType));
        assertNotEquals(0, jobs.size());
        assertTrue(jobs.stream().anyMatch(j -> j.getType().equals(expectedType)));
        assertTrue(jobs.stream().allMatch(j -> j.getType().equals(expectedType)));
    }

    @Test
    public void testQueryJobsWithOffsetAndLimit() {
        List<JobInfo> allJobs = storage.queryJobs(new JobQuery());
        JobInfo expectedFirstFiltered = allJobs.get(0);
        List<JobInfo> filteredJobs = storage.queryJobs(new JobQuery().setOffset(1).setLimit(3));
        assertNotEquals(expectedFirstFiltered.getId(), filteredJobs.get(0).getId());
        assertEquals(3, filteredJobs.size());
    }

    @Test
    public void testQueryRecurringJobsWithOffsetAndLimit() {
        List<RecurringJobInfo> allJobs = storage.queryRecurringJobs(new RecurringJobQuery());
        RecurringJobInfo expectedFirstFiltered = allJobs.get(1);
        List<RecurringJobInfo> filteredJobs = storage.queryRecurringJobs(new RecurringJobQuery().setOffset(1));
        assertEquals(expectedFirstFiltered.getId(), filteredJobs.get(0).getId());
        assertEquals(2, filteredJobs.size());
    }

    @Test
    public void testGettingJobCountsByStatuses() {
        Map<JobStatus, Integer> counts = storage.getJobCountsByStatuses();
        for(JobStatus status : JobStatus.values())
            assertNotNull(counts.get(status));
    }

    @Test
    public void testSetJobStatus() {
        JobInfo info = new JobInfo().setType(NoOpJob.class.getName());
        storage.createJob(info, NOOP_PAYLOAD);
        storage.setJobStatus(info.getId(), JobStatus.SUCCESS);
        JobInfo newInfo = storage.getJob(info.getId());
        assertEquals(JobStatus.SUCCESS, newInfo.getStatus());
    }

    @Test
    public void testDeleteJob() {
        JobInfo info = new JobInfo().setType(NoOpJob.class.getName());
        storage.createJob(info, NOOP_PAYLOAD);
        storage.deleteJob(info.getId());
        assertNull(storage.getJob(info.getId()));
    }

    @Test
    public void testCreateAndGetEvent() {
        UUID expectedId = UUID.randomUUID();
        UUID expectedJobId = UUID.randomUUID();
        JobEvent.Type expectedType = JobEvent.Type.ENQUEUED;
        Date expectedCreatedAt = new Date(MILLENNIUM);
        JobEvent createEvent = new JobEvent()
                .setId(expectedId)
                .setJobId(expectedJobId)
                .setType(expectedType)
                .setCreatedAt(expectedCreatedAt);
        storage.createEvent(createEvent);
        JobEvent retrievedEvent = storage.getEvent(expectedId);
        assertNotNull(retrievedEvent);
        assertEquals(expectedId, retrievedEvent.getId());
        assertEquals(expectedJobId, retrievedEvent.getJobId());
        assertEquals(expectedType, retrievedEvent.getType());
        assertEquals(expectedCreatedAt, retrievedEvent.getCreatedAt());
    }

    @Test
    public void testCreateEventWithMissingOptionalFields() {
        JobEvent event = new JobEvent()
                .setJobId(UUID.randomUUID())
                .setType(JobEvent.Type.ENQUEUED);
        storage.createEvent(event);
        assertNotNull(event.getId());
        assertNotNull(event.getCreatedAt());
        assertNotNull(storage.getEvent(event.getId()));
    }

    @Test
    public void testCreateEventWithMissingRequiredFields() {
        JobEvent eventWithoutJobId = new JobEvent().setId(UUID.randomUUID()).setType(JobEvent.Type.ENQUEUED);
        assertThrows(IllegalArgumentException.class, () -> storage.createEvent(eventWithoutJobId));
        assertNull(storage.getEvent(eventWithoutJobId.getId()));
        JobEvent eventWithoutType = new JobEvent().setId(UUID.randomUUID()).setJobId(UUID.randomUUID());
        assertThrows(IllegalArgumentException.class, () -> storage.createEvent(eventWithoutType));
        assertNull(storage.getEvent(eventWithoutType.getId()));
    }

    @Test
    public void testQueryEvents() {
        JobInfo job = new JobInfo()
                .setType(NoOpJob.class.getName());
        storage.createJob(job, NOOP_PAYLOAD);
        JobEvent firstEvent = new JobEvent()
                .setJobId(job.getId())
                .setType(JobEvent.Type.SCHEDULED);
        storage.createEvent(firstEvent);
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        JobEvent secondEvent = new JobEvent()
                .setJobId(job.getId())
                .setType(JobEvent.Type.ENQUEUED);
        storage.createEvent(secondEvent);
        List<JobEvent> receivedEvents = storage.queryEvents(job.getId());
        assertEquals(2, receivedEvents.size());
        assertEquals(firstEvent.getId(), receivedEvents.get(0).getId());
        assertEquals(secondEvent.getId(), receivedEvents.get(1).getId());
    }

    @Test
    public void testCreateAndGetLogEntry() {
        UUID expectedId = UUID.randomUUID();
        UUID expectedEventId = UUID.randomUUID();
        LogLevel expectedLevel = LogLevel.WARNING;
        String expectedMessage = "a string";
        Date expectedCreatedAt = new Date(MILLENNIUM);
        JobLogEntry createEntry = new JobLogEntry()
                .setId(expectedId)
                .setEventId(expectedEventId)
                .setLevel(expectedLevel)
                .setMessage(expectedMessage)
                .setCreatedAt(expectedCreatedAt);
        storage.createLogEntry(createEntry);
        JobLogEntry retrievedEntry = storage.getLogEntry(expectedEventId, expectedId);
        assertNotNull(retrievedEntry);
        assertEquals(expectedId, retrievedEntry.getId());
        assertEquals(expectedEventId, retrievedEntry.getEventId());
        assertEquals(expectedLevel, retrievedEntry.getLevel());
        assertEquals(expectedMessage, retrievedEntry.getMessage());
        assertEquals(expectedCreatedAt, retrievedEntry.getCreatedAt());
    }

    @Test
    public void testCreateLogEntryWithMissingOptionalFields() {
        UUID eventId = UUID.randomUUID();
        JobLogEntry entry = new JobLogEntry()
                .setEventId(eventId)
                .setLevel(LogLevel.WARNING)
                .setMessage("a string");
        storage.createLogEntry(entry);
        assertNotNull(entry.getId());
        assertNotNull(entry.getCreatedAt());
        assertNotNull(storage.getLogEntry(eventId, entry.getId()));
    }

    @Test
    public void testCreateLogEntryWithMissingRequiredFields() {
        UUID eventId = UUID.randomUUID();
        JobLogEntry entryWithoutEventId = new JobLogEntry().setId(UUID.randomUUID()).setLevel(LogLevel.WARNING).setMessage("a string");
        assertThrows(IllegalArgumentException.class, () -> storage.createLogEntry(entryWithoutEventId));
        JobLogEntry entryWithoutLevel = new JobLogEntry().setId(UUID.randomUUID()).setEventId(eventId).setMessage("a string");
        assertThrows(IllegalArgumentException.class, () -> storage.createLogEntry(entryWithoutLevel));
        assertNull(storage.getLogEntry(eventId, entryWithoutLevel.getId()));
        JobLogEntry entryWithoutMessage = new JobLogEntry().setId(UUID.randomUUID()).setEventId(eventId).setLevel(LogLevel.WARNING);
        assertThrows(IllegalArgumentException.class, () -> storage.createLogEntry(entryWithoutMessage));
        assertNull(storage.getLogEntry(eventId, entryWithoutMessage.getId()));
    }

    @Test
    public void testQueryLogEntries() {
        JobInfo job = new JobInfo()
                .setType(NoOpJob.class.getName());
        storage.createJob(job, NOOP_PAYLOAD);
        JobEvent event = new JobEvent()
                .setJobId(job.getId())
                .setType(JobEvent.Type.SCHEDULED);
        storage.createEvent(event);
        JobLogEntry firstEntry = new JobLogEntry()
                .setEventId(event.getId())
                .setLevel(LogLevel.INFO)
                .setMessage("a message");
        storage.createLogEntry(firstEntry);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        JobLogEntry secondEntry = new JobLogEntry()
                .setEventId(event.getId())
                .setLevel(LogLevel.INFO)
                .setMessage("another message");
        storage.createLogEntry(secondEntry);
        List<JobLogEntry> receivedEntries = storage.queryLogEntries(event.getId());
        assertEquals(2, receivedEntries.size());
        assertEquals(firstEntry.getId(), receivedEntries.get(0).getId());
        assertEquals(secondEntry.getId(), receivedEntries.get(1).getId());
    }

    @Test
    public void testCreateAndGetWorker() {
        UUID expectedId = UUID.randomUUID();
        String expectedQueue = TEST_QUEUE;
        String expectedHostname = EXAMPLE_HOSTNAME;
        boolean expectedOnline = true;
        int expectedThreads = 5;
        Date expectedLastHeartbeatAt = new Date(END_OF_THE_WORLD);
        Date expectedCreatedAt = new Date(MILLENNIUM);
        JobWorkerInfo createInfo = new JobWorkerInfo()
                .setId(expectedId)
                .setQueue(expectedQueue)
                .setHostname(expectedHostname)
                .setOnline(expectedOnline)
                .setThreads(expectedThreads)
                .setLastHeartbeatAt(expectedLastHeartbeatAt)
                .setCreatedAt(expectedCreatedAt);
        storage.createWorker(createInfo);
        JobWorkerInfo retrievedInfo = storage.getWorker(expectedId);
        assertNotNull(retrievedInfo);
        assertEquals(expectedId, retrievedInfo.getId());
        assertEquals(expectedQueue, retrievedInfo.getQueue());
        assertEquals(expectedHostname, retrievedInfo.getHostname());
        assertEquals(expectedOnline, retrievedInfo.isOnline());
        assertEquals(expectedThreads, retrievedInfo.getThreads());
        assertEquals(expectedLastHeartbeatAt, retrievedInfo.getLastHeartbeatAt());
        assertEquals(expectedCreatedAt, retrievedInfo.getCreatedAt());
    }

    @Test
    public void testCreateWorkerWithMissingOptionalFields() {
        JobWorkerInfo info = new JobWorkerInfo()
                .setQueue(TEST_QUEUE)
                .setHostname(EXAMPLE_HOSTNAME)
                .setOnline(true);
        storage.createWorker(info);
        assertNotNull(info.getId());
        assertNotNull(info.getLastHeartbeatAt());
        assertNotNull(info.getCreatedAt());
        assertNotNull(storage.getWorker(info.getId()));
    }

    @Test
    public void testCreateWorkerWithMissingRequiredFields() {
        JobWorkerInfo infoWithoutQueue = new JobWorkerInfo().setId(UUID.randomUUID()).setHostname(EXAMPLE_HOSTNAME);
        assertThrows(IllegalArgumentException.class, () -> storage.createWorker(infoWithoutQueue));
        assertNull(storage.getWorker(infoWithoutQueue.getId()));
        JobWorkerInfo infoWithoutHostname = new JobWorkerInfo().setId(UUID.randomUUID()).setQueue(TEST_QUEUE);
        assertThrows(IllegalArgumentException.class, () -> storage.createWorker(infoWithoutHostname));
        assertNull(storage.getWorker(infoWithoutHostname.getId()));
    }

    @Test
    public void testQueryWorkers() {
        JobWorkerInfo info = new JobWorkerInfo()
                .setQueue(TEST_QUEUE)
                .setHostname(EXAMPLE_HOSTNAME);
        storage.createWorker(info);
        List<JobWorkerInfo> workers = storage.queryWorkers();
        assertTrue(workers.size() >= 1);
        assertEquals(1, workers.stream().filter(w -> w.getId().equals(info.getId())).count());
    }

    @Test
    public void testSetWorkerOnline() {
        JobWorkerInfo info = new JobWorkerInfo()
                .setQueue(TEST_QUEUE)
                .setOnline(true)
                .setLastHeartbeatAt(new Date(MILLENNIUM))
                .setHostname(EXAMPLE_HOSTNAME);
        storage.createWorker(info);
        JobWorkerInfo infoAfterCreate = storage.getWorker(info.getId());
        assertTrue(infoAfterCreate.isOnline());
        storage.setWorkerOnline(info.getId(), false);
        JobWorkerInfo infoAfterSetOffline = storage.getWorker(info.getId());
        assertFalse(infoAfterSetOffline.isOnline());
        assertNotEquals(infoAfterCreate.getLastHeartbeatAt(), infoAfterSetOffline.getLastHeartbeatAt());
    }

    @Test
    public void testCreateAndGetRecurringJob() {
        RecurringJobInfo info = new RecurringJobInfo()
                .setType(NoOpJob.class.getName())
                .setQueue(TEST_QUEUE)
                .setCron(new CronInterval("*/10 * * * *"));
        assertTrue(storage.createRecurringJob(info));
        assertNotNull(storage.getRecurringJob(info.getId()));
    }

    @Test
    public void testNonExistantRecurringJob() {
        assertNull(storage.getRecurringJob(UUID.randomUUID()));
    }

    @Test
    public void testRecurringJobValidation() {
        RecurringJobInfo info = new RecurringJobInfo();
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> storage.createRecurringJob(info));
        assertEquals("Recurring Job type is required", exception.getMessage());

        info.setType(NoOpJob.class.getName());
        exception = assertThrows(IllegalArgumentException.class, () -> storage.createRecurringJob(info));
        assertEquals("Recurring Job cron is required", exception.getMessage());
    }

    @Test
    public void testUpdateRecurringJob() {
        RecurringJobInfo info = storage.queryRecurringJobs(new RecurringJobQuery()).get(0);
        assertNotNull(info);
        UUID newJobId = UUID.randomUUID();
        Date newExecutionAt = new Date(1676912249000L);
        storage.updateRecurringJob(info.getId(), newJobId, newExecutionAt);
        info = storage.getRecurringJob(info.getId());
        assertNotNull(info);
        assertEquals(newJobId, info.getLastJobId());
        assertEquals(newExecutionAt, info.getLastExecutionAt());
    }

    @Test
    public void testDeleteRecurringJob() {
        RecurringJobInfo info = new RecurringJobInfo().setQueue(TEST_QUEUE).setType(NoOpJob.class.getName()).setPayload(NOOP_PAYLOAD).setCron(new CronInterval("*/5 * * * *"));
        assertTrue(storage.createRecurringJob(info));
        assertNotNull(info.getId());
        assertNotNull(storage.getRecurringJob(info.getId()));
        storage.deleteRecurringJob(info.getId());
        assertNull(storage.getRecurringJob(info.getId()));
    }

    @Test
    public void testQueryRecurringJobSince() {
        RecurringJobInfo past = new RecurringJobInfo().setQueue(TEST_QUEUE).setType(NoOpJob.class.getName()).setPayload(NOOP_PAYLOAD).setCron(new CronInterval("*/5 * * * *")).setLastExecutionAt(Date.from(Instant.now().minus(1, ChronoUnit.DAYS)));
        RecurringJobInfo future = new RecurringJobInfo().setQueue(TEST_QUEUE).setType(NoOpJob.class.getName()).setPayload(NOOP_PAYLOAD).setCron(new CronInterval("*/10 */5 * * *")).setLastExecutionAt(Date.from(Instant.now().plus(1, ChronoUnit.DAYS)));
        assertTrue(storage.createRecurringJob(past));
        assertTrue(storage.createRecurringJob(future));
        int totalCount = storage.queryRecurringJobs(new RecurringJobQuery()).size();
        assertEquals(totalCount - 1, storage.queryRecurringJobs(new RecurringJobQuery().setSinceLastExecution(new Date())).size());
    }

}
