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
package org.apache.jackrabbit.oak.core;

import java.util.Iterator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.jackrabbit.oak.api.PropertyState;
import org.apache.jackrabbit.oak.api.Root;
import org.apache.jackrabbit.oak.api.Tree;
import org.apache.jackrabbit.oak.api.TreeLocation;
import org.apache.jackrabbit.oak.api.Type;
import org.apache.jackrabbit.oak.spi.state.ChildNodeEntry;
import org.apache.jackrabbit.oak.spi.state.NodeState;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class ReadOnlyTree implements Tree {

    /**
     * Parent of this tree, {@code null} for the root
     */
    private final ReadOnlyTree parent;

    /**
     * Name of this tree
     */
    private final String name;

    /**
     * Underlying node state
     */
    private final NodeState state;

    public ReadOnlyTree(NodeState rootState) {
        this(null, "", rootState);
    }

    public ReadOnlyTree(@Nullable ReadOnlyTree parent, @Nonnull String name, @Nonnull NodeState state) {
        this.parent = parent;
        this.name = checkNotNull(name);
        this.state = checkNotNull(state);
        checkArgument(!name.isEmpty() || parent == null);
    }

    public static ReadOnlyTree createFromRoot(Root root) {
        if (root instanceof RootImpl) {
            return new ReadOnlyTree(((RootImpl) root).getBaseState());
        } else if (root instanceof ReadOnlyRoot) {
            return ((ReadOnlyRoot) root).getTree("/");
        } else {
            throw new IllegalArgumentException("Unsupported Root implementation.");
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isRoot() {
        return parent == null;
    }

    @Override
    public String getPath() {
        if (isRoot()) {
            // shortcut
            return "/";
        }

        StringBuilder sb = new StringBuilder();
        buildPath(sb);
        return sb.toString();
    }

    private void buildPath(StringBuilder sb) {
        if (parent != null) {
            parent.buildPath(sb);
            sb.append('/').append(name);
        }
    }

    @Override
    public Tree getParent() {
        return parent;
    }

    @Override
    public PropertyState getProperty(String name) {
        return state.getProperty(name);
    }

    @Override
    public Status getPropertyStatus(String name) {
        if (hasProperty(name)) {
            return Status.EXISTING;
        } else {
            return null;
        }
    }

    @Override
    public boolean hasProperty(String name) {
        return state.getProperty(name) != null;
    }

    @Override
    public long getPropertyCount() {
        return state.getPropertyCount();
    }

    @Override
    public Iterable<? extends PropertyState> getProperties() {
        return state.getProperties();
    }

    @Override
    public ReadOnlyTree getChild(@Nonnull String name) {
        NodeState child = state.getChildNode(name);
        if (child != null) {
            return new ReadOnlyTree(this, name, child);
        } else {
            return null;
        }
    }

    @Override
    public Status getStatus() {
        return Status.EXISTING;
    }

    @Override
    public TreeLocation getLocation() {
        return new NodeLocation(this);
    }

    @Override
    public boolean hasChild(@Nonnull String name) {
        return state.hasChildNode(name);
    }

    @Override
    public long getChildrenCount() {
        return state.getChildNodeCount();
    }

    /**
     * This implementation does not respect ordered child nodes, but always
     * returns them in some implementation specific order.
     * <p/>
     * TODO: respect orderable children (needed?)
     *
     * @return the children.
     */
    @Override
    public Iterable<Tree> getChildren() {
        return new Iterable<Tree>() {
            @Override
            public Iterator<Tree> iterator() {
                final Iterator<? extends ChildNodeEntry> iterator =
                        state.getChildNodeEntries().iterator();
                return new Iterator<Tree>() {
                    @Override
                    public boolean hasNext() {
                        return iterator.hasNext();
                    }

                    @Override
                    public Tree next() {
                        ChildNodeEntry entry = iterator.next();
                        return new ReadOnlyTree(
                                ReadOnlyTree.this,
                                entry.getName(), entry.getNodeState());
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
    }

    @Override
    public Tree addChild(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setOrderableChildren(boolean enable) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean orderBefore(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setProperty(PropertyState property) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> void setProperty(String name, T value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> void setProperty(String name, T value, Type<T> type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeProperty(String name) {
        throw new UnsupportedOperationException();
    }

    //-------------------------------------------------------< TreeLocation >---

    private final class NodeLocation extends AbstractNodeLocation<ReadOnlyTree> {

        private NodeLocation(ReadOnlyTree tree) {
            super(tree);
        }

        @Override
        protected TreeLocation createNodeLocation(ReadOnlyTree tree) {
            return new NodeLocation(tree);
        }

        @Override
        protected TreeLocation createPropertyLocation(AbstractNodeLocation<ReadOnlyTree> parentLocation, String name) {
            return new PropertyLocation(parentLocation, name);
        }

        @Override
        protected ReadOnlyTree getParentTree() {
            return tree.parent;
        }

        @Override
        protected ReadOnlyTree getChildTree(String name) {
            return tree.getChild(name);
        }

        @Override
        protected PropertyState getPropertyState(String name) {
            return tree.getProperty(name);
        }

        @Override
        public Tree getTree() {
            return tree;
        }

        @Override
        public boolean remove() {
            return false;
        }
    }

    private final class PropertyLocation extends AbstractPropertyLocation<ReadOnlyTree> {

        private PropertyLocation(AbstractNodeLocation<ReadOnlyTree> parentLocation, String name) {
            super(parentLocation, name);
        }

        @Override
        public PropertyState getProperty() {
            return parentLocation.tree.getProperty(name);
        }

        @Override
        public boolean remove() {
            return false;
        }

        @Override
        public boolean set(PropertyState property) {
            return false;
        }
    }
}
