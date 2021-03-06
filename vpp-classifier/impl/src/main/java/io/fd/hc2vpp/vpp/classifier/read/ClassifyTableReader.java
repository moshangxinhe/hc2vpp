/*
 * Copyright (c) 2017 Cisco and/or its affiliates.
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

package io.fd.hc2vpp.vpp.classifier.read;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Preconditions;
import com.google.common.primitives.UnsignedInts;
import io.fd.hc2vpp.common.translate.util.FutureJVppCustomizer;
import io.fd.hc2vpp.common.translate.util.JvppReplyConsumer;
import io.fd.hc2vpp.common.translate.util.MacTranslator;
import io.fd.hc2vpp.v3po.read.InterfaceDataTranslator;
import io.fd.hc2vpp.vpp.classifier.context.VppClassifierContextManager;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.fd.honeycomb.translate.spi.read.Initialized;
import io.fd.honeycomb.translate.spi.read.InitializingListReaderCustomizer;
import io.fd.jvpp.core.dto.ClassifyTableIds;
import io.fd.jvpp.core.dto.ClassifyTableIdsReply;
import io.fd.jvpp.core.dto.ClassifyTableInfo;
import io.fd.jvpp.core.dto.ClassifyTableInfoReply;
import io.fd.jvpp.core.future.FutureJVppCore;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.fd.io.hc2vpp.yang.vpp.classifier.rev170327.VppClassifier;
import org.opendaylight.yang.gen.v1.http.fd.io.hc2vpp.yang.vpp.classifier.rev170327.VppClassifierStateBuilder;
import org.opendaylight.yang.gen.v1.http.fd.io.hc2vpp.yang.vpp.classifier.rev170327.VppNode;
import org.opendaylight.yang.gen.v1.http.fd.io.hc2vpp.yang.vpp.classifier.rev170327.VppNodeName;
import org.opendaylight.yang.gen.v1.http.fd.io.hc2vpp.yang.vpp.classifier.rev170327.vpp.classifier.state.ClassifyTable;
import org.opendaylight.yang.gen.v1.http.fd.io.hc2vpp.yang.vpp.classifier.rev170327.vpp.classifier.state.ClassifyTableBuilder;
import org.opendaylight.yang.gen.v1.http.fd.io.hc2vpp.yang.vpp.classifier.rev170327.vpp.classifier.state.ClassifyTableKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.HexString;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reader customizer responsible for classify table read.<br> to VPP.<br> Equivalent to invoking {@code vppctl show
 * class table} command.
 */
public class ClassifyTableReader extends FutureJVppCustomizer
        implements InitializingListReaderCustomizer<ClassifyTable, ClassifyTableKey, ClassifyTableBuilder>, VppNodeReader,
        MacTranslator, InterfaceDataTranslator, JvppReplyConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(ClassifyTableReader.class);
    private final VppClassifierContextManager classifyTableContext;

    public ClassifyTableReader(@Nonnull final FutureJVppCore futureJVppCore,
                               @Nonnull final VppClassifierContextManager classifyTableContext) {
        super(futureJVppCore);
        this.classifyTableContext = checkNotNull(classifyTableContext, "classifyTableContext should not be null");
    }


    @Override
    public void merge(@Nonnull final Builder<? extends DataObject> builder,
                      @Nonnull final List<ClassifyTable> readData) {
        ((VppClassifierStateBuilder) builder).setClassifyTable(readData);
    }

    @Nonnull
    @Override
    public ClassifyTableBuilder getBuilder(@Nonnull final InstanceIdentifier<ClassifyTable> id) {
        return new ClassifyTableBuilder();
    }

    @Override
    public void readCurrentAttributes(@Nonnull final InstanceIdentifier<ClassifyTable> id,
                                      @Nonnull final ClassifyTableBuilder builder, @Nonnull final ReadContext ctx)
            throws ReadFailedException {
        LOG.debug("Reading attributes for classify table: {}", id);

        final ClassifyTableKey key = id.firstKeyOf(ClassifyTable.class);
        Preconditions.checkArgument(key != null, "could not find ClassifyTable key in {}", id);
        final ClassifyTableInfo request = new ClassifyTableInfo();

        final String tableName = key.getName();
        if (!classifyTableContext.containsTable(tableName, ctx.getMappingContext())) {
            LOG.debug("Could not find classify table {} in the naming context", tableName);
            return;
        }
        request.tableId = classifyTableContext.getTableIndex(tableName, ctx.getMappingContext());


        final ClassifyTableInfoReply reply =
                getReplyForRead(getFutureJVpp().classifyTableInfo(request).toCompletableFuture(), id);

        // mandatory values:
        builder.setName(tableName);
        builder.withKey(key);
        builder.setNbuckets(UnsignedInts.toLong(reply.nbuckets));
        builder.setSkipNVectors(UnsignedInts.toLong(reply.skipNVectors));

        // optional value read from context
        final Optional<String> tableBaseNode =
                classifyTableContext.getTableBaseNode(tableName, ctx.getMappingContext());
        if (tableBaseNode.isPresent()) {
            builder.setClassifierNode(new VppNodeName(tableBaseNode.get()));
        }

        final Optional<VppNode> node =
            readVppNode(reply.tableId, reply.missNextIndex, classifyTableContext, ctx.getMappingContext());
        if (node.isPresent()) {
            builder.setMissNext(node.get());
        } else {
            builder.setMissNext(new VppNode(new VppNodeName("unknown"))); // TODO(HC2VPP-9): remove this workaround
        }
        builder.setMask(new HexString(printHexBinary(reply.mask)));
        builder.setActiveSessions(UnsignedInts.toLong(reply.activeSessions));

        if (reply.nextTableIndex != ~0) {
            // next table index is present:
            builder.setNextTable(classifyTableContext.getTableName(reply.nextTableIndex, ctx.getMappingContext()));
        }

        if (LOG.isTraceEnabled()) {
            LOG.trace("Attributes for classify table {} successfully read: {}", id, builder.build());
        }
    }

    @Nonnull
    @Override
    public List<ClassifyTableKey> getAllIds(@Nonnull final InstanceIdentifier<ClassifyTable> id,
                                            @Nonnull final ReadContext context) throws ReadFailedException {
        LOG.debug("Reading list of keys for classify tables: {}", id);

        final ClassifyTableIdsReply classifyTableIdsReply =
                getReplyForRead(getFutureJVpp().classifyTableIds(new ClassifyTableIds()).toCompletableFuture(),
                        id);
        if (classifyTableIdsReply.ids != null) {
            return Arrays.stream(classifyTableIdsReply.ids).mapToObj(i -> {
                final String tableName = classifyTableContext.getTableName(i, context.getMappingContext());
                LOG.trace("Classify table with name: {} and index: {} found in VPP", tableName, i);
                return new ClassifyTableKey(tableName);
            }).collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public Initialized<org.opendaylight.yang.gen.v1.http.fd.io.hc2vpp.yang.vpp.classifier.rev170327.vpp.classifier.ClassifyTable> init(
            @Nonnull final InstanceIdentifier<ClassifyTable> id, @Nonnull final ClassifyTable readValue,
            @Nonnull final ReadContext ctx) {
        return Initialized.create(getCfgId(id),
                new org.opendaylight.yang.gen.v1.http.fd.io.hc2vpp.yang.vpp.classifier.rev170327.vpp.classifier.ClassifyTableBuilder(readValue)
                        .setName(readValue.getName())
                        .build());
    }

    static InstanceIdentifier<org.opendaylight.yang.gen.v1.http.fd.io.hc2vpp.yang.vpp.classifier.rev170327.vpp.classifier.ClassifyTable> getCfgId(
            final InstanceIdentifier<ClassifyTable> id) {
        return InstanceIdentifier.create(VppClassifier.class)
                .child(org.opendaylight.yang.gen.v1.http.fd.io.hc2vpp.yang.vpp.classifier.rev170327.vpp.classifier.ClassifyTable.class,
                        new org.opendaylight.yang.gen.v1.http.fd.io.hc2vpp.yang.vpp.classifier.rev170327.vpp.classifier.ClassifyTableKey(id.firstKeyOf(ClassifyTable.class).getName()));
    }
}
