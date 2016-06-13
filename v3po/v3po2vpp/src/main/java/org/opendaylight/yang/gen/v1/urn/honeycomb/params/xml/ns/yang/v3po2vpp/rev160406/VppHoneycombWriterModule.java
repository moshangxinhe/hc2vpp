package org.opendaylight.yang.gen.v1.urn.honeycomb.params.xml.ns.yang.v3po2vpp.rev160406;

import io.fd.honeycomb.v3po.translate.impl.write.CompositeChildWriter;
import io.fd.honeycomb.v3po.translate.impl.write.CompositeListWriter;
import io.fd.honeycomb.v3po.translate.impl.write.CompositeRootWriter;
import io.fd.honeycomb.v3po.translate.util.RWUtils;
import io.fd.honeycomb.v3po.translate.util.write.CloseableWriter;
import io.fd.honeycomb.v3po.translate.util.write.NoopWriterCustomizer;
import io.fd.honeycomb.v3po.translate.util.write.ReflexiveChildWriterCustomizer;
import io.fd.honeycomb.v3po.translate.v3po.vpp.BridgeDomainCustomizer;
import io.fd.honeycomb.v3po.translate.v3po.vpp.L2FibEntryCustomizer;
import io.fd.honeycomb.v3po.translate.write.ChildWriter;
import java.util.ArrayList;
import java.util.List;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.v3po.rev150105.Vpp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.v3po.rev150105.l2.fib.attributes.L2FibTable;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.v3po.rev150105.l2.fib.attributes.l2.fib.table.L2FibEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.v3po.rev150105.l2.fib.attributes.l2.fib.table.L2FibEntryKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.v3po.rev150105.vpp.BridgeDomains;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.v3po.rev150105.vpp.bridge.domains.BridgeDomain;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.v3po.rev150105.vpp.bridge.domains.BridgeDomainKey;
import org.opendaylight.yangtools.yang.binding.ChildOf;

public class VppHoneycombWriterModule extends org.opendaylight.yang.gen.v1.urn.honeycomb.params.xml.ns.yang.v3po2vpp.rev160406.AbstractVppHoneycombWriterModule {
    public VppHoneycombWriterModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public VppHoneycombWriterModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.yang.gen.v1.urn.honeycomb.params.xml.ns.yang.v3po2vpp.rev160406.VppHoneycombWriterModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        final CompositeListWriter<BridgeDomain, BridgeDomainKey> bridgeDomainWriter = new CompositeListWriter<>(
            BridgeDomain.class,
                RWUtils.singletonChildWriterList(l2FibTableWriter()),
            new BridgeDomainCustomizer(getVppJvppWriterDependency(), getBridgeDomainContextVppDependency()));

        final ChildWriter<BridgeDomains> bridgeDomainsWriter = new CompositeChildWriter<>(
            BridgeDomains.class,
            RWUtils.singletonChildWriterList(bridgeDomainWriter),
            new ReflexiveChildWriterCustomizer<>());

        final List<ChildWriter<? extends ChildOf<Vpp>>> childWriters = new ArrayList<>();
        childWriters.add(bridgeDomainsWriter);

        return new CloseableWriter<>(new CompositeRootWriter<>(
            Vpp.class,
            childWriters,
            new NoopWriterCustomizer<>()));
    }

    private ChildWriter l2FibTableWriter() {
        final CompositeListWriter<L2FibEntry, L2FibEntryKey> l2FibEntryWriter = new CompositeListWriter<>(L2FibEntry.class,
                new L2FibEntryCustomizer(getVppJvppWriterDependency(),
                        getBridgeDomainContextVppDependency(), getInterfaceContextVppDependency()));

        final ChildWriter<L2FibTable> l2FibTableWriter = new CompositeChildWriter<>(
                L2FibTable.class,
                RWUtils.singletonChildWriterList(l2FibEntryWriter),
                new ReflexiveChildWriterCustomizer<>());

        return l2FibTableWriter;
    }



}
