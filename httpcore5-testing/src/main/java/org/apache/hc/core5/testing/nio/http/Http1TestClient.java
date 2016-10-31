/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package org.apache.hc.core5.testing.nio.http;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.nio.channels.SelectionKey;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hc.core5.concurrent.BasicFuture;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.ExceptionListener;
import org.apache.hc.core5.http.config.ConnectionConfig;
import org.apache.hc.core5.http.config.H1Config;
import org.apache.hc.core5.http.impl.DefaultConnectionReuseStrategy;
import org.apache.hc.core5.http.impl.HttpProcessors;
import org.apache.hc.core5.http.impl.nio.bootstrap.AsyncRequester;
import org.apache.hc.core5.http.impl.nio.bootstrap.ClientEndpoint;
import org.apache.hc.core5.http.impl.nio.bootstrap.ClientEndpointImpl;
import org.apache.hc.core5.http.nio.command.ShutdownCommand;
import org.apache.hc.core5.http.nio.command.ShutdownType;
import org.apache.hc.core5.http.protocol.HttpProcessor;
import org.apache.hc.core5.reactor.IOReactorConfig;
import org.apache.hc.core5.reactor.IOSession;
import org.apache.hc.core5.reactor.IOSessionCallback;
import org.apache.hc.core5.reactor.SessionRequest;
import org.apache.hc.core5.reactor.SessionRequestCallback;

public class Http1TestClient extends AsyncRequester {

    public Http1TestClient(final IOReactorConfig ioReactorConfig) throws IOException {
        super(ioReactorConfig, new ExceptionListener() {

            private final Log log = LogFactory.getLog(Http1TestClient.class);

            @Override
            public void onError(final Exception ex) {
                log.error(ex.getMessage(), ex);
            }

        }, new IOSessionCallback() {
            @Override
            public void execute(final IOSession session) throws IOException {
                session.getCommandQueue().addFirst(new ShutdownCommand(ShutdownType.GRACEFUL));
                session.setEvent(SelectionKey.OP_WRITE);
            }
        });
    }

    public Http1TestClient() throws IOException {
        this(IOReactorConfig.DEFAULT);
    }

    public void start(
            final HttpProcessor httpProcessor,
            final H1Config h1Config,
            final ConnectionConfig connectionConfig) throws IOException {
        execute(new InternalClientHttp1EventHandlerFactory(
                httpProcessor,
                h1Config,
                connectionConfig,
                DefaultConnectionReuseStrategy.INSTANCE));
    }

    public void start(final H1Config h1Config, final ConnectionConfig connectionConfig) throws IOException {
        start(HttpProcessors.client(), h1Config, connectionConfig);
    }

    public void start(final H1Config h1Config) throws IOException {
        start(h1Config, ConnectionConfig.DEFAULT);
    }

    public void start() throws IOException {
        start(H1Config.DEFAULT);
    }

    @Override
    public SessionRequest requestSession(
            final InetSocketAddress address,
            final long timeout,
            final TimeUnit timeUnit,
            final SessionRequestCallback callback) throws InterruptedException {
        return super.requestSession(address, timeout, timeUnit, callback);
    }

    public Future<ClientEndpoint> connect(
            final InetSocketAddress address,
            final long timeout,
            final TimeUnit timeUnit,
            final FutureCallback<ClientEndpoint> callback) throws InterruptedException {
        final BasicFuture<ClientEndpoint> future = new BasicFuture<>(callback);
        requestSession(address, timeout, timeUnit, new SessionRequestCallback() {

            @Override
            public void completed(final SessionRequest request) {
                final IOSession session = request.getSession();
                future.completed(new ClientEndpointImpl(session));
            }

            @Override
            public void failed(final SessionRequest request) {
                future.failed(request.getException());
            }

            @Override
            public void timeout(final SessionRequest request) {
                future.failed(new SocketTimeoutException("Connect timeout"));
            }

            @Override
            public void cancelled(final SessionRequest request) {
                future.cancel();
            }
        });
        return future;
    }

    public Future<ClientEndpoint> connect(
            final InetSocketAddress address,
            final long timeout,
            final TimeUnit timeUnit) throws InterruptedException {
        return connect(address, timeout, timeUnit, null);
    }

    public Future<ClientEndpoint> connect(
            final String hostname,
            final int port,
            final long timeout,
            final TimeUnit timeUnit) throws InterruptedException {
        return connect(new InetSocketAddress(hostname, port), timeout, timeUnit);
    }

}
