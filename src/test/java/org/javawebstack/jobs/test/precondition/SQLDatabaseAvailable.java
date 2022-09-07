package org.javawebstack.jobs.test.precondition;

import org.javawebstack.jobs.test.TestProperties;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

public class SQLDatabaseAvailable implements ExecutionCondition {

    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext extensionContext) {
        if(TestProperties.isSQLDatabaseAvailable()) // TODO check database availability
            return ConditionEvaluationResult.enabled(null);
        else
            return ConditionEvaluationResult.disabled("No SQL database connection configured, skipping test!");
    }

}
