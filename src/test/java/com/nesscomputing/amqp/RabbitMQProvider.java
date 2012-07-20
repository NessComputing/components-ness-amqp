package com.nesscomputing.amqp;

/**
 * Expects a local rabbit mq server running on the host that runs the tests.
 */
public class RabbitMQProvider implements AmqpProvider
{
    @Override
    public void startup() throws Exception
    {
    }

    @Override
    public void shutdown() throws Exception
    {
    }

    @Override
    public String getUri()
    {
        return "amqp://localhost";
    }
}

