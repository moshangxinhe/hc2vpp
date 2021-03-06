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

package io.fd.hc2vpp.l3.write.ipv6;

import static com.google.common.base.Preconditions.checkState;

import io.fd.hc2vpp.common.translate.util.AddressTranslator;
import io.fd.hc2vpp.common.translate.util.ByteDataTranslator;
import io.fd.hc2vpp.common.translate.util.FutureJVppCustomizer;
import io.fd.hc2vpp.common.translate.util.JvppReplyConsumer;
import io.fd.hc2vpp.common.translate.util.NamingContext;
import io.fd.hc2vpp.l3.utils.ip.write.IpWriter;
import io.fd.honeycomb.translate.MappingContext;
import io.fd.honeycomb.translate.spi.write.ListWriterCustomizer;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.fd.jvpp.core.dto.IpNeighborAddDel;
import io.fd.jvpp.core.future.FutureJVppCore;
import io.fd.jvpp.core.types.IpNeighborFlags;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev180220.interfaces.Interface;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.ip.rev180222.interfaces._interface.ipv6.Neighbor;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.ip.rev180222.interfaces._interface.ipv6.NeighborKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Ipv6NeighbourCustomizer extends FutureJVppCustomizer
        implements ListWriterCustomizer<Neighbor, NeighborKey>, ByteDataTranslator, AddressTranslator, IpWriter,
        JvppReplyConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(Ipv6NeighbourCustomizer.class);
    final NamingContext interfaceContext;

    public Ipv6NeighbourCustomizer(final FutureJVppCore futureJVppCore, final NamingContext interfaceContext) {
        super(futureJVppCore);
        this.interfaceContext = interfaceContext;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Neighbor> id, @Nonnull Neighbor dataAfter,
                                       @Nonnull WriteContext writeContext)
            throws WriteFailedException {

        LOG.debug("Processing request for Neighbour write");
        String interfaceName = id.firstKeyOf(Interface.class).getName();
        MappingContext mappingContext = writeContext.getMappingContext();

        checkState(interfaceContext.containsIndex(interfaceName, mappingContext),
                "Mapping does not contains mapping for provider interface name ".concat(interfaceName));

        LOG.debug("Parent interface index found");
        addDelNeighbourAndReply(id, true, interfaceContext.getIndex(interfaceName, mappingContext), dataAfter);
        LOG.debug("Neighbour successfully written");
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Neighbor> id, @Nonnull Neighbor dataBefore,
                                        @Nonnull WriteContext writeContext)
            throws WriteFailedException {

        LOG.debug("Processing request for Neighbour delete");
        String interfaceName = id.firstKeyOf(Interface.class).getName();
        MappingContext mappingContext = writeContext.getMappingContext();

        checkState(interfaceContext.containsIndex(interfaceName, mappingContext),
                "Mapping does not contains mapping for provider interface name %s", interfaceName);

        LOG.debug("Parent interface[{}] index found", interfaceName);
        addDelNeighbourAndReply(id, false, interfaceContext.getIndex(interfaceName, mappingContext), dataBefore);
        LOG.debug("Neighbour {} successfully deleted", id);
    }

    private void addDelNeighbourAndReply(InstanceIdentifier<Neighbor> id, boolean add, int parentInterfaceIndex,
                                         Neighbor data) throws WriteFailedException {
        addDelNeighbour(id, () -> {
            IpNeighborAddDel request = preBindRequest(add);
            request.neighbor.flags = new IpNeighborFlags();
            request.neighbor.flags.add(IpNeighborFlags.IpNeighborFlagsOptions.IP_API_NEIGHBOR_FLAG_STATIC);
            request.neighbor.macAddress = parseMacAddress(data.getLinkLayerAddress().getValue());
            request.neighbor.ipAddress = ipv6AddressToAddress(data.getIp());
            request.neighbor.swIfIndex = parentInterfaceIndex;
            return request;
        }, getFutureJVpp());
    }
}
