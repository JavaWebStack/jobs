package org.javawebstack.jobs.storage.model;

import lombok.Getter;
import lombok.Setter;
import org.javawebstack.jobs.LogLevel;

import java.util.Date;
import java.util.UUID;

@Getter
@Setter
public class JobLogEntry {

    UUID id;
    UUID eventId;
    LogLevel level;
    String message;
    Date createdAt;

}