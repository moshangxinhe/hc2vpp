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

package io.fd.hc2vpp.v3po.factory;

import static io.fd.hc2vpp.v3po.factory.InterfacesWriterFactory.IFC_ID;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.fd.hc2vpp.common.translate.util.NamingContext;
import io.fd.hc2vpp.v3po.interfaces.ip.v6.Ipv6AddressCustomizer;
import io.fd.hc2vpp.v3po.interfaces.ip.v6.Ipv6Customizer;
import io.fd.hc2vpp.v3po.interfaces.ip.v6.Ipv6NeighbourCustomizer;
import io.fd.honeycomb.translate.impl.write.GenericListWriter;
import io.fd.honeycomb.translate.impl.write.GenericWriter;
import io.fd.honeycomb.translate.write.WriterFactory;
import io.fd.honeycomb.translate.write.registry.ModifiableWriterRegistryBuilder;
import io.fd.vpp.jvpp.core.future.FutureJVppCore;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.ip.rev140616.Interface1;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.ip.rev140616.interfaces._interface.Ipv6;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.ip.rev140616.interfaces._interface.ipv6.Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.ip.rev140616.interfaces._interface.ipv6.Neighbor;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Created by jsrnicek on 3.1.2017.
 */
public class Ipv6WriterFactory implements WriterFactory {

    @Inject
    private FutureJVppCore jvpp;

    @Inject
    @Named("interface-context")
    private NamingContext ifcNamingContext;

    @Override
    public void init(@Nonnull final ModifiableWriterRegistryBuilder registry) {

        final InstanceIdentifier<Interface1> ifc1AugId = IFC_ID.augmentation(Interface1.class);

        // Ipv6(after interface) =
        final InstanceIdentifier<Ipv6> ipv6Id = ifc1AugId.child(Ipv6.class);
        registry.addAfter(new GenericWriter<>(ipv6Id, new Ipv6Customizer(jvpp)), IFC_ID);

        final InstanceIdentifier<Address>
                ipv6AddressId = ipv6Id.child(Address.class);
        registry.addAfter(new GenericListWriter<>(ipv6AddressId, new Ipv6AddressCustomizer(jvpp, ifcNamingContext)),
                ipv6Id);

        registry.addAfter(new GenericListWriter<>(ipv6Id.child(Neighbor.class),
                new Ipv6NeighbourCustomizer(jvpp, ifcNamingContext)), ipv6AddressId);
    }
}