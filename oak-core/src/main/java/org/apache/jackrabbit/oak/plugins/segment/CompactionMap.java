/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.jackrabbit.oak.plugins.segment;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * A {@code CompactionMap} is a composite of multiple {@link PartialCompactionMap}
 * instances. Operations performed on this map are delegated back to the individual
 * maps.
 */
public class CompactionMap {

    /**
     * An empty map.
     */
    public static final CompactionMap EMPTY =
            new CompactionMap(Collections.<PartialCompactionMap>emptyList());

    private final List<PartialCompactionMap> maps;

    private CompactionMap(@Nonnull List<PartialCompactionMap> maps) {
        this.maps = maps;
    }

    /**
     * Checks whether the record with the given {@code before} identifier was
     * compacted to a new record with the given {@code after} identifier.
     *
     * @param before before record identifier
     * @param after  after record identifier
     * @return whether {@code before} was compacted to {@code after}
     */
    public boolean wasCompactedTo(@Nonnull RecordId before, @Nonnull RecordId after) {
        for (PartialCompactionMap map : maps) {
            if (map.wasCompactedTo(before, after)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks whether content in the segment with the given identifier was
     * compacted to new segments.
     *
     * @param id segment identifier
     * @return whether the identified segment was compacted
     */
    public boolean wasCompacted(@Nonnull UUID id) {
        for (PartialCompactionMap map : maps) {
            if (map.wasCompacted(id)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Retrieve the record id {@code before} maps to or {@code null}
     * if no such id exists.
     * @param before before record id
     * @return after record id or {@code null}
     */
    @CheckForNull
    public RecordId get(@Nonnull RecordId before) {
        for (PartialCompactionMap map : maps) {
            RecordId after = map.get(before);
            if (after != null) {
                return after;
            }
        }
        return null;
    }

    /**
     * Remove all keys from this map where {@code keys.contains(key.asUUID())}.
     * @param uuids  uuids of the keys to remove
     */
    public void remove(@Nonnull Set<UUID> uuids) {
        List<PartialCompactionMap> remove = newArrayList();
        for (PartialCompactionMap map : maps) {
            map.remove(uuids);
            if (map.getSegmentCount() == 0) {
                remove.add(map);
            }
        }
        maps.removeAll(remove);
    }

    /**
     * Create a new {@code CompactionMap} containing all maps
     * of this instances and additional the passed {@code map}.
     * @param map
     * @return a new {@code CompactionMap} instance
     */
    @Nonnull
    public CompactionMap cons(@Nonnull PartialCompactionMap map) {
        List<PartialCompactionMap> maps = newArrayList(map);
        maps.addAll(this.maps);
        return new CompactionMap(maps);
    }

    /**
     * Java's lacking libraries...
     * @param longs
     * @return sum of the passed {@code longs}
     */
    public static long sum(long[] longs) {
        long sum = 0;
        for (long x : longs) {
            sum += x;
        }
        return sum;
    }

    /**
     * The depth of the compaction map is the number of partial compaction maps
     * this map consists of.
     *
     * @return the depth of this compaction map
     * @see #cons(PartialCompactionMap)
     */
    public int getDepth() {
        return maps.size();
    }

    /**
     * The weight of the compaction map is its  memory consumption bytes
     * @return Estimated weight of the compaction map
     */
    public long[] getEstimatedWeights() {
        long[] weights = new long[maps.size()];
        int c = 0;
        for (PartialCompactionMap map : maps) {
            weights[c++] = map.getEstimatedWeight();
        }
        return weights;
    }

    /**
     * Number of segments referenced by the keys in this map. The returned value might only
     * be based on the compressed part of the individual maps.
     * @return  number of segments
     */
    public long[] getSegmentCounts() {
        long[] counts = new long[maps.size()];
        int c = 0;
        for (PartialCompactionMap map : maps) {
            counts[c++] = map.getSegmentCount();
        }
        return counts;
    }

    /**
     * Number of records referenced by the keys in this map. The returned value might only
     * be based on the compressed part of the  individual maps.
     * @return  number of records
     */
    public long[] getRecordCounts() {
        long[] counts = new long[maps.size()];
        int c = 0;
        for (PartialCompactionMap map : maps) {
            counts[c++] = map.getRecordCount();
        }
        return counts;
    }

}
