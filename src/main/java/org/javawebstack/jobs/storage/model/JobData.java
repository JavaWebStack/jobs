package org.javawebstack.jobs.storage.model;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class JobData {

    UUID id;
    String type;
    String data;

}
