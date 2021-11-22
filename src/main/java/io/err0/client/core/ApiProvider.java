package io.err0.client.core;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.UUID;

public interface ApiProvider {

    /**
     * Ensure the policy is set-up for generating error codes.
     * @param policy
     */
    void ensurePolicyIsSetUp(final ApplicationPolicy policy);

    /**
     * Free up any resources used by this api provider.
     */
    void close();

    /**
     * Replace this API providers valid error number cache with
     * current valid project error numbers.
     * @param policy
     */
    void cacheAllValidErrorNumbers(final ApplicationPolicy policy);

    /**
     * Check that the error code is valid, e.g. not invented by a developer.
     * @param errorCode
     * @return
     */
    boolean validErrorNumber(final ApplicationPolicy policy, final long errorCode);

    /**
     * Return the next error number in sequence.
     * @return
     */
    void cacheErrorNumberBatch(ApplicationPolicy policy, long number);

    long nextErrorNumber(ApplicationPolicy policy);

    enum InsertType {
        NEW, EXISTING
    }

    class ForInsert {
        public ForInsert(final String errorCode, final long errorOrdinal, final JsonObject metaData, final InsertType insertType) {
            this.errorCode = errorCode;
            this.errorOrdinal = errorOrdinal;
            this.metaData = metaData;
            this.insertType = insertType;
        }
        public final String errorCode;
        public final long errorOrdinal;
        public final JsonObject metaData;
        public final InsertType insertType;
    }

    /**
     * Bulk insert the meta-data about this error in the database.
     */
    void bulkInsertMetaData(final ApplicationPolicy policy, final UUID run_uuid, final String errorPrefix, final ArrayList<ForInsert> forBulkInsert);

    /**
     * For some modes of operation, set the next error number based on error numbers already seen.  This is
     * used in the full-offline mode via the --unit-test-provider command line argument.
     * @param nextErrorNumber
     */
    void setNextErrorNumber(final ApplicationPolicy policy, final long nextErrorNumber);

    /**
     * Create a version of the app in the realm.
     * @param policy
     * @param gitHash
     * @param gitTag
     * @param runState 'insert' or 'analyse', see database sql run_state_enum
     * @return
     */
    UUID createRun(final ApplicationPolicy policy, final JsonObject appGitMetadata, final JsonObject runGitMetadata, final String runState);

    /**
     * Update the version's meta-data, for example with a git hash with '-dirty' appended and a NULL git tag.
     * @param policy
     * @param run_uuid
     * @param gitHash
     * @param gitTag
     */
    void updateRun(final ApplicationPolicy policy, final UUID run_uuid, final JsonObject gitMetadata, final JsonObject runMetadata);

    /**
     * Import previous state, if applicable
     */
    void importPreviousState(final ApplicationPolicy policy, final GlobalState globalState);

    void finaliseRun(final ApplicationPolicy policy, UUID run_uuid);
}
