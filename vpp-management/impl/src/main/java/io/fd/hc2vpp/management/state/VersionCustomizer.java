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

package io.fd.hc2vpp.management.state;

import com.google.common.primitives.UnsignedInts;
import io.fd.hc2vpp.common.translate.util.ByteDataTranslator;
import io.fd.hc2vpp.common.translate.util.FutureJVppCustomizer;
import io.fd.hc2vpp.common.translate.util.JvppReplyConsumer;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.fd.honeycomb.translate.spi.read.ReaderCustomizer;
import io.fd.jvpp.core.dto.ShowVersion;
import io.fd.jvpp.core.dto.ShowVersionReply;
import io.fd.jvpp.core.future.FutureJVppCore;
import io.fd.jvpp.dto.ControlPing;
import io.fd.jvpp.dto.ControlPingReply;
import io.fd.jvpp.dto.JVppReply;
import java.util.concurrent.CompletionStage;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.fd.io.hc2vpp.yang.vpp.management.rev170315.VppStateBuilder;
import org.opendaylight.yang.gen.v1.http.fd.io.hc2vpp.yang.vpp.management.rev170315.vpp.state.Version;
import org.opendaylight.yang.gen.v1.http.fd.io.hc2vpp.yang.vpp.management.rev170315.vpp.state.VersionBuilder;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public final class VersionCustomizer
        extends FutureJVppCustomizer
        implements ReaderCustomizer<Version, VersionBuilder>, ByteDataTranslator, JvppReplyConsumer {

    public VersionCustomizer(@Nonnull final FutureJVppCore futureJVppCore) {
        super(futureJVppCore);
    }

    @Override
    public void merge(@Nonnull final Builder<? extends DataObject> parentBuilder, @Nonnull final Version readValue) {
        ((VppStateBuilder) parentBuilder).setVersion(readValue);
    }

    @Nonnull
    @Override
    public VersionBuilder getBuilder(@Nonnull InstanceIdentifier<Version> id) {
        return new VersionBuilder();
    }

    @Override
    public void readCurrentAttributes(@Nonnull final InstanceIdentifier<Version> id, @Nonnull final VersionBuilder builder,
                                      @Nonnull final ReadContext context) throws ReadFailedException {

        // Execute with timeout
        final CompletionStage<ShowVersionReply> showVersionFuture = getFutureJVpp().showVersion(new ShowVersion());
        final ShowVersionReply reply = getReplyForRead(showVersionFuture.toCompletableFuture(), id);

        builder.setBranch(reply.version);
        builder.setName(reply.program);
        builder.setBuildDate(reply.buildDate);
        builder.setBuildDirectory(reply.buildDirectory);
        builder.setPid(getPid(id));
    }

    private Long getPid(@Nonnull final InstanceIdentifier<Version> id) throws ReadFailedException {
        final CompletionStage<JVppReply<ControlPing>> request = getFutureJVpp().send(new ControlPing());
        final ControlPingReply reply = (ControlPingReply)getReplyForRead(request.toCompletableFuture(), id);
        return UnsignedInts.toLong(reply.vpePid);
    }


}
