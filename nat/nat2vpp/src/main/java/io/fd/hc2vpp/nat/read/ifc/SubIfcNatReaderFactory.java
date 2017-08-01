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

package io.fd.hc2vpp.nat.read.ifc;


import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.fd.hc2vpp.common.translate.util.JvppReplyConsumer;
import io.fd.hc2vpp.common.translate.util.NamingContext;
import io.fd.honeycomb.translate.impl.read.GenericInitReader;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.fd.honeycomb.translate.read.ReaderFactory;
import io.fd.honeycomb.translate.read.registry.ModifiableReaderRegistryBuilder;
import io.fd.honeycomb.translate.util.read.cache.DumpCacheManager;
import io.fd.honeycomb.translate.util.read.cache.EntityDumpExecutor;
import io.fd.vpp.jvpp.snat.dto.SnatInterfaceDetailsReplyDump;
import io.fd.vpp.jvpp.snat.dto.SnatInterfaceDump;
import io.fd.vpp.jvpp.snat.future.FutureJVppSnatFacade;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev140508.InterfacesState;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev140508.interfaces.state.Interface;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang._interface.nat.rev170801._interface.nat.attributes.Nat;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang._interface.nat.rev170801._interface.nat.attributes.NatBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang._interface.nat.rev170801._interface.nat.attributes.nat.Inbound;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang._interface.nat.rev170801._interface.nat.attributes.nat.Outbound;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.subinterface.nat.rev170615.NatSubinterfaceStateAugmentation;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.subinterface.nat.rev170615.NatSubinterfaceStateAugmentationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.vpp.vlan.rev170607.SubinterfaceStateAugmentation;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.vpp.vlan.rev170607.interfaces.state._interface.SubInterfaces;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.vpp.vlan.rev170607.interfaces.state._interface.sub.interfaces.SubInterface;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Nat readers registration for sub-interfaces.
 */
public final class SubIfcNatReaderFactory implements ReaderFactory {

    private static final InstanceIdentifier<SubInterface>
            SUB_IFC_ID = InstanceIdentifier.create(InterfacesState.class).child(Interface.class).augmentation(
        SubinterfaceStateAugmentation.class).child(SubInterfaces.class).child(SubInterface.class);
    private static final InstanceIdentifier<NatSubinterfaceStateAugmentation> NAT_SUB_AUG_ID =
        SUB_IFC_ID.augmentation(NatSubinterfaceStateAugmentation.class);
    private static final InstanceIdentifier<Nat> NAT_AUG_CONTAINER_ID = NAT_SUB_AUG_ID.child(Nat.class);

    private final DumpCacheManager<SnatInterfaceDetailsReplyDump, Void> snatIfcDumpMgr;
    private final NamingContext ifcContext;

    @Inject
    public SubIfcNatReaderFactory(final FutureJVppSnatFacade jvppSnat,
                                  @Named("interface-context") final NamingContext ifcContext) {
        this.snatIfcDumpMgr = new DumpCacheManager.DumpCacheManagerBuilder<SnatInterfaceDetailsReplyDump, Void>()
                .withExecutor(new SnatInterfaceExecutor(jvppSnat))
                .acceptOnly(SnatInterfaceDetailsReplyDump.class)
                .build();
        this.ifcContext = ifcContext;
    }

    @Override
    public void init(@Nonnull final ModifiableReaderRegistryBuilder registry) {
        registry.addStructuralReader(NAT_SUB_AUG_ID, NatSubinterfaceStateAugmentationBuilder.class);
        registry.addStructuralReader(NAT_AUG_CONTAINER_ID, NatBuilder.class);

        registry.addAfter(new GenericInitReader<>(NAT_AUG_CONTAINER_ID.child(Inbound.class),
                        new SubInterfaceInboundNatCustomizer(snatIfcDumpMgr, ifcContext)), SUB_IFC_ID);
        registry.addAfter(new GenericInitReader<>(NAT_AUG_CONTAINER_ID.child(Outbound.class),
                        new SubInterfaceOutboundNatCustomizer(snatIfcDumpMgr, ifcContext)), SUB_IFC_ID);
    }

    private static final class SnatInterfaceExecutor implements
            EntityDumpExecutor<SnatInterfaceDetailsReplyDump, Void>,
            JvppReplyConsumer {

        private final FutureJVppSnatFacade jvppSnat;

        SnatInterfaceExecutor(final FutureJVppSnatFacade jvppSnat) {
            this.jvppSnat = jvppSnat;
        }

        @Nonnull
        @Override
        public SnatInterfaceDetailsReplyDump executeDump(final InstanceIdentifier<?> identifier, final Void params)
                throws ReadFailedException {
            return getReplyForRead(
                    jvppSnat.snatInterfaceDump(new SnatInterfaceDump()).toCompletableFuture(), identifier);
        }
    }
}
