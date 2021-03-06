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

package io.fd.hc2vpp.common.test.read;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.fd.hc2vpp.common.test.util.FutureProducer;
import io.fd.honeycomb.translate.util.read.cache.EntityDumpExecutor;
import io.fd.jvpp.VppInvocationException;
import io.fd.jvpp.core.future.FutureJVppCore;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Generic test for implementation of {@link EntityDumpExecutor}
 *
 * @param <T> implementation of {@link EntityDumpExecutor}
 */
public abstract class JvppDumpExecutorTest<T extends EntityDumpExecutor<?, ?>> implements FutureProducer {

    @Mock
    protected FutureJVppCore api;

    private T executor;

    @Before
    public void setUpParent() {
        MockitoAnnotations.initMocks(this);
        this.executor = initExecutor();
    }

    protected abstract T initExecutor();

    protected T getExecutor() {
        return this.executor;
    }

    /**
     * Return pre-stubed mock that will throw {@link TimeoutException},
     * while performing desired method
     *
     * @return pre-stubed mock that will throw {@link TimeoutException}
     * @throws InterruptedException when the operation is interrupted
     * @throws ExecutionException when execution fails
     * @throws TimeoutException when timeout occurs
     */
    protected FutureJVppCore doThrowTimeoutExceptionWhen()
            throws InterruptedException, ExecutionException, TimeoutException {

        CompletableFuture failedFuture = mock(CompletableFuture.class);
        when(failedFuture.toCompletableFuture()).thenReturn(failedFuture);
        when(failedFuture.get(anyLong(), eq(TimeUnit.SECONDS)))
                .thenThrow(new TimeoutException("Exception invoked by " + JvppDumpExecutorTest.class.getName()));

        return doReturn(failedFuture).when(api);
    }

    /**
     * Return pre-stubed mock that will throw {@link VppInvocationException},
     * while performing desired method
     *
     * @return pre-stubed mock that will throw {@link VppInvocationException}
     */
    protected FutureJVppCore doThrowFailExceptionWhen() {
        return doReturn(failedFuture()).when(api);
    }

    /**
     * Return pre-stubed mock that will return specified result
     * while performing desired method
     *
     * @param replyDump type of reply dump
     * @return pre-stubed mock that will return specified result
     */
    protected <U> FutureJVppCore doReturnResponseWhen(U replyDump) {
        return doReturn(future(replyDump)).when(api);
    }
}
