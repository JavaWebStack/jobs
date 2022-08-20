package org.javawebstack.jobs.storage.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

@Getter
@Setter
public class JobLogData {

    UUID id;
    UUID eventId;
    String level;
    Date timestamp;
    String message;

}
