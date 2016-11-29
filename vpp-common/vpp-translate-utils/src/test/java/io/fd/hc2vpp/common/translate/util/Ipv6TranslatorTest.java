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

package io.fd.hc2vpp.common.translate.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6AddressNoZone;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Prefix;

public class Ipv6TranslatorTest implements Ipv6Translator {

    @Test
    public void testIpv6NoZone() {
        final Ipv6AddressNoZone ipv6Addr = new Ipv6AddressNoZone("3ffe:1900:4545:3:200:f8ff:fe21:67cf");
        byte[] bytes = ipv6AddressNoZoneToArray(ipv6Addr);
        assertEquals((byte) 63, bytes[0]);
        final Ipv6AddressNoZone ivp6AddressNoZone = arrayToIpv6AddressNoZone(bytes);
        assertEquals(ipv6Addr, ivp6AddressNoZone);
    }

    @Test
    public void testIpv6AddressPrefixToArray() {
        byte[] ip = ipv6AddressPrefixToArray(new Ipv6Prefix("3ffe:1900:4545:3:200:f8ff:fe21:67cf/48"));

        assertEquals("3ffe:1900:4545:3:200:f8ff:fe21:67cf", arrayToIpv6AddressNoZone(ip).getValue());
    }

    @Test
    public void testIpv4AddressPrefixToArray() {
        byte[] ip = ipv6AddressPrefixToArray(new Ipv6Prefix("2001:0db8:0a0b:12f0:0000:0000:0000:0001/128"));

        assertEquals("2001:db8:a0b:12f0::1", arrayToIpv6AddressNoZone(ip).getValue());
    }

    @Test
    public void testExtractPrefix() {
        assertEquals(48, extractPrefix(new Ipv6Prefix("3ffe:1900:4545:3:200:f8ff:fe21:67cf/48")));
    }
}