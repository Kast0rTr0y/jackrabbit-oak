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

package org.apache.jackrabbit.oak.run.osgi

import org.apache.felix.connect.launch.PojoServiceRegistry
import org.apache.jackrabbit.oak.plugins.segment.SegmentNodeStoreService
import org.apache.jackrabbit.oak.plugins.segment.SegmentStore
import org.apache.jackrabbit.oak.spi.blob.BlobStore
import org.apache.jackrabbit.oak.spi.blob.MemoryBlobStore
import org.apache.jackrabbit.oak.spi.state.NodeStore
import org.junit.Test

class NodeStoreConfigTest extends  AbstractRepositoryFactoryTest{
    private PojoServiceRegistry registry
    @Test
    void testNodeStoreWithBlobStore_OAK_1676(){
        registry = repositoryFactory.initializeServiceRegistry(config)
        registry.registerService(BlobStore.class.name, new MemoryBlobStore(), null)

        createConfig([
                'org.apache.jackrabbit.oak.plugins.segment.SegmentNodeStoreService' : [:]
        ])

        SegmentNodeStoreService ns = getServiceWithWait(NodeStore.class)
        //NodeStore is of type SegmentNodeStoreService
        SegmentStore segStore = ns.getSegmentStore()
        assert segStore.blobStore == null , "BlobStore should not be picked up unless 'customBlobStore " +
                "is set to true"
    }

    @Override
    protected PojoServiceRegistry getRegistry() {
        return registry
    }
}
