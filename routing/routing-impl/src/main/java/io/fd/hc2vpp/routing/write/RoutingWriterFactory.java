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

package io.fd.hc2vpp.routing.write;

import static io.fd.hc2vpp.routing.RoutingConfiguration.ROUTE_CONTEXT;
import static io.fd.hc2vpp.routing.RoutingConfiguration.ROUTE_HOP_CONTEXT;
import static io.fd.hc2vpp.routing.RoutingConfiguration.ROUTING_PROTOCOL_CONTEXT;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.fd.hc2vpp.common.translate.util.MultiNamingContext;
import io.fd.hc2vpp.common.translate.util.NamingContext;
import io.fd.hc2vpp.routing.RoutingConfiguration;
import io.fd.hc2vpp.v3po.vppclassifier.VppClassifierContextManager;
import io.fd.honeycomb.translate.impl.write.GenericWriter;
import io.fd.honeycomb.translate.write.WriterFactory;
import io.fd.honeycomb.translate.write.registry.ModifiableWriterRegistryBuilder;
import io.fd.vpp.jvpp.core.future.FutureJVppCore;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.ipv4.unicast.routing.rev140524.routing.routing.instance.routing.protocols.routing.protocol._static.routes.ipv4.Route;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.routing.rev140524.Routing;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.routing.rev140524.routing.RoutingInstance;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.routing.rev140524.routing.routing.instance.RoutingProtocols;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.routing.rev140524.routing.routing.instance.routing.protocols.RoutingProtocol;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.routing.rev140524.routing.routing.instance.routing.protocols.routing.protocol.StaticRoutes;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.vpp.routing.rev161214.RoutingProtocolVppAttr;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.vpp.routing.rev161214.routing.routing.instance.routing.protocols.routing.protocol.VppProtocolAttributes;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Factory producing writers for routing plugin's data.
 */
public final class RoutingWriterFactory implements WriterFactory, Ipv4WriteRoutingNodes, Ipv6WriteRoutingNodes {

    private static final InstanceIdentifier<Routing> ROOT_CONTAINER_ID = InstanceIdentifier.create(Routing.class);

    @Inject
    private FutureJVppCore vppApi;

    @Inject
    private RoutingConfiguration configuration;

    @Inject
    @Named("interface-context")
    private NamingContext interfaceContext;

    @Inject
    @Named(ROUTING_PROTOCOL_CONTEXT)
    private NamingContext routingProtocolContext;

    @Inject
    @Named(ROUTE_CONTEXT)
    private NamingContext routeContext;

    @Inject
    @Named("classify-table-context")
    private VppClassifierContextManager vppClassifierContextManager;

    @Inject
    @Named(ROUTE_HOP_CONTEXT)
    private MultiNamingContext routHopContext;

    @Override
    public void init(@Nonnull final ModifiableWriterRegistryBuilder registry) {

        registry.subtreeAdd(rootNodeHandledChildren(ROOT_CONTAINER_ID),
                new GenericWriter<>(ROOT_CONTAINER_ID, new RoutingCustomizer()));

        registry.add(new GenericWriter<>(routingInstanceIdentifier(), new RoutingInstanceCustomizer(configuration)));

        registry.subtreeAdd(routingProtocolHandledChildren(),new GenericWriter<>(routingProtocolIdentifier(),
                new RoutingProtocolCustomizer(routingProtocolContext)));

        final InstanceIdentifier<StaticRoutes> staticRoutesInstanceIdentifier = staticRoutesIdentifier();
        final InstanceIdentifier<Route> ipv4RouteIdentifier = ipv4RouteIdentifier(staticRoutesInstanceIdentifier);
        final InstanceIdentifier<org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.ipv6.unicast.routing.rev140525.routing.routing.instance.routing.protocols.routing.protocol._static.routes.ipv6.Route>
                ipv6RouteIdentifier = ipv6RouteIdentifier(staticRoutesInstanceIdentifier);
        registry.subtreeAdd(ipv4RoutingHandledChildren(ipv4RouteSubtree()), new GenericWriter<>(ipv4RouteIdentifier,
                new Ipv4RouteCustomizer(vppApi, interfaceContext, routeContext, routingProtocolContext, routHopContext,
                        vppClassifierContextManager)));
        registry.subtreeAdd(ipv6RoutingHandledChildren(ipv6RouteSubtree()), new GenericWriter<>(ipv6RouteIdentifier,
                new Ipv6RouteCustomizer(vppApi, interfaceContext, routeContext, routingProtocolContext, routHopContext,
                        vppClassifierContextManager)));
    }

    private static ImmutableSet<InstanceIdentifier<?>> routingProtocolHandledChildren() {
        return ImmutableSet
                .of(InstanceIdentifier.create(RoutingProtocol.class).augmentation(RoutingProtocolVppAttr.class).child(VppProtocolAttributes.class));
    }

    private static InstanceIdentifier<RoutingInstance> routingInstanceIdentifier() {
        return ROOT_CONTAINER_ID.child(RoutingInstance.class);
    }

    private static InstanceIdentifier<RoutingProtocol> routingProtocolIdentifier() {
        return routingInstanceIdentifier().child(RoutingProtocols.class).child(RoutingProtocol.class);
    }

    private static InstanceIdentifier<StaticRoutes> staticRoutesIdentifier() {
        return routingProtocolIdentifier().child(StaticRoutes.class);
    }

    private static Set<InstanceIdentifier<?>> rootNodeHandledChildren(final InstanceIdentifier<Routing> parent) {
        return ImmutableSet.of(parent.child(RoutingInstance.class).child(RoutingProtocols.class));
    }
}