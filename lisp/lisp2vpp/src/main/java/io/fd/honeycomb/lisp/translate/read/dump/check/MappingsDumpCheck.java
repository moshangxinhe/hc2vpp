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

package io.fd.honeycomb.lisp.translate.read.dump.check;


import io.fd.honeycomb.translate.util.read.cache.EntityDumpNonEmptyCheck;
import io.fd.honeycomb.translate.util.read.cache.exceptions.check.DumpCheckFailedException;
import io.fd.honeycomb.translate.util.read.cache.exceptions.check.i.DumpEmptyException;
import org.openvpp.jvpp.core.dto.LispEidTableDetailsReplyDump;

/**
 * Common dump check for both local and remote mappings
 */
public class MappingsDumpCheck implements EntityDumpNonEmptyCheck<LispEidTableDetailsReplyDump> {

    @Override
    public void assertNotEmpty(final LispEidTableDetailsReplyDump data) throws DumpCheckFailedException {
        if (data == null) {
            throw new DumpEmptyException("Mappings dump is null");
        }

        if (data.lispEidTableDetails == null) {
            throw new DumpEmptyException("Mappings dump is empty");
        }
    }
}