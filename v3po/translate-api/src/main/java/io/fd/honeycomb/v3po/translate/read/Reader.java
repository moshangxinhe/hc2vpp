/*
 * Copyright (c) 2016 Cisco and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.fd.honeycomb.v3po.translate.read;

import com.google.common.annotations.Beta;
import com.google.common.base.Optional;
import io.fd.honeycomb.v3po.translate.SubtreeManager;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Base reader, responsible for translation between DataObjects and any other side
 *
 * @param <D> Specific DataObject derived type, that is handled by this reader
 */
@Beta
public interface Reader<D extends DataObject> extends SubtreeManager<D> {

    // TODO make async

    /**
     * Reads data identified by id
     *
     * @param id unique identifier of subtree to be read. The subtree must contain managed data object type. For
     *           identifiers pointing below node managed by this reader, it's reader's responsibility to filter out the
     *           right node or to delegate the read to a child reader.
     * @param ctx Read context
     *
     * @return List of DataObjects identified by id. If the ID points to a single node, it will be wrapped in a list
     * @throws ReadFailedException if read was unsuccessful
     */
    @Nonnull
    Optional<? extends DataObject> read(@Nonnull final InstanceIdentifier<? extends DataObject> id,
                                        @Nonnull ReadContext ctx) throws ReadFailedException;

}