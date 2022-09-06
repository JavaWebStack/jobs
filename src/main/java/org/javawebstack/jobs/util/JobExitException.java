package org.javawebstack.jobs.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class JobExitException extends RuntimeException {

    boolean success;
    String message;
    int retry = -1;

}
