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

import com.nesscomputing.logging.Log;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

/**
 * Helper methods to deal with AMQP stuff.
 */
final class AmqpUtils
{
    private static final Log LOG = Log.findLog();

    private AmqpUtils()
    {
    }


    /**
     * Close a session.
     *
     * @param session
     */
    public static void closeQuietly(final Channel channel)
    {
        if (channel != null && channel.isOpen()) {
            try {
                channel.close();
            }
            catch (IOException ioe) {
                LOG.warnDebug(ioe, "While closing session");
            }
        }
    }

    /**
     * Close a connection.
     *
     * @param connection
     */
    public static void closeQuietly(final Connection connection)
    {
        if (connection != null && connection.isOpen()) {
            try {
                connection.close();
            }
            catch (IOException ioe) {
                LOG.warnDebug(ioe, "While closing connection");
            }
        }
    }
}
