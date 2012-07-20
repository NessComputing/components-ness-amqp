package com.nesscomputing.amqp.rabbitmq;

import com.nesscomputing.amqp.AbstractTestQueueFactory;
import com.nesscomputing.amqp.AmqpProvider;
import com.nesscomputing.amqp.RabbitMQProvider;

public class RabbitMQTestQueueFactory extends AbstractTestQueueFactory
{
    private AmqpProvider provider = new RabbitMQProvider();

    @Override
    protected AmqpProvider getProvider()
    {
        return provider;
    }
}
