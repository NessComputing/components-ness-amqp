/**
 * Copyright (C) 2012 Ness Computing, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nesscomputing.amqp;

import java.io.IOException;

import com.google.common.base.Charsets;
import com.rabbitmq.client.AMQP.BasicProperties;

public class StringPublisherCallback implements PublisherCallback<String>
{
    private final BasicProperties props;

    StringPublisherCallback()
    {
        this.props = new BasicProperties.Builder()
        .contentType("text/plain")
        .deliveryMode(1)
        .priority(0)
        .build();
    }

    @Override
    public PublisherData publish(final String data) throws IOException
    {
        if (data != null) {
            return new PublisherData() {
                @Override
                public BasicProperties getProperties() {
                    return props;
                }

                @Override
                public byte [] getData() throws IOException {
                    return data.getBytes(Charsets.UTF_8);
                }
            };
        }

        return null;
    }
}
