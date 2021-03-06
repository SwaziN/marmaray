/*
 * Copyright (c) 2018 Uber Technologies, Inc.
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions
 * of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */
package com.uber.marmaray.common.schema.cassandra;

import com.google.common.base.Optional;
import com.uber.marmaray.utilities.StringTypes;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.NotEmpty;

import java.util.List;

@Slf4j
public class CassandraMetadataSchemaManager extends CassandraSchemaManager {

    public CassandraMetadataSchemaManager(@NonNull final CassandraSchema schema,
                                      @NonNull final List<String> partitionKeys,
                                      @NonNull final List<ClusterKey> clusteringKeys,
                                      @NonNull final Optional<Long> ttl) {
        super(schema, partitionKeys, clusteringKeys, ttl, Optional.absent(), Optional.absent(), false);
    }

    /**
     * @return
     * Returns insert statement to add row
     */
    public String generateInsertStmt(@NotEmpty final String key, @NonNull final String value) {
        final String ttlStr = this.ttl.isPresent() ? "USING TTL " + this.ttl.get().toString() : StringTypes.EMPTY;

        return String.format("INSERT INTO %s.%s ( %s ) VALUES ( %s ) %s",
                this.schema.getKeySpace(),
                this.schema.getTableName(),
                key,
                value,
                ttlStr);
    }

    /**
     * @return
     * Delete command to remove a job
     */
    public String generateDeleteJob(@NotEmpty final String key) {
        return String.format("DELETE FROM %s.%s WHERE job='%s'",
                this.schema.getKeySpace(),
                this.schema.getTableName(),
                key);
    }

    /**
     * @return
     * Cassandra command to delete oldest checkpoint
     */
    public String generateDeleteOldestCheckpoint(@NotEmpty final String key,
                                         @NotEmpty final Optional<String> oldestTimestamp) {
        return String.format("DELETE FROM %s.%s WHERE job='%s' and time_stamp='%s'",
                this.schema.getKeySpace(),
                this.schema.getTableName(),
                key,
                oldestTimestamp.get());
    }

    /**
     * @return
     * Returns Select Cassandra command to query metadata manager
     */
    public String generateSelectJob(@NotEmpty final String key, @NotEmpty final int limit) {
        return String.format("SELECT * FROM %s.%s WHERE job='%s' LIMIT %s;",
                this.schema.getKeySpace(),
                this.schema.getTableName(),
                key,
                limit);
    }

    /**
     * @return
     * Returns drop table cassandra command
     * this resets our metadata manager
     */
    public String generateDropTable() {
        return String.format("DROP TABLE %s.%s",
                this.schema.getKeySpace(),
                this.schema.getTableName());
    }
}
