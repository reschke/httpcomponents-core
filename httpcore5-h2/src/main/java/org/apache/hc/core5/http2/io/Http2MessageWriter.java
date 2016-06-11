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

package org.apache.hc.core5.http2.io;

import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.MessageHead;
import org.apache.hc.core5.util.ByteArrayBuffer;

/**
 * Abstract message writer intended to serialize HTTP message head to an arbitrary
 * data sink.
 *
 * @since 4.0
 */
public interface Http2MessageWriter<T extends MessageHead> {

    /**
     * Serializes an instance of {@link MessageHead} to the given output buffer.
     *
     * @param message HTTP message head
     * @param buffer output buffer
     * @throws HttpException in case of HTTP protocol violation
     */
    void write(T message, ByteArrayBuffer buffer) throws HttpException;

}