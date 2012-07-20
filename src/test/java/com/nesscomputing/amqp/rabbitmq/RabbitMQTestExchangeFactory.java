package com.nesscomputing.amqp.rabbitmq;

import com.nesscomputing.amqp.AbstractTestExchangeFactory;
import com.nesscomputing.amqp.AmqpProvider;
import com.nesscomputing.amqp.RabbitMQProvider;

public class RabbitMQTestExchangeFactory extends AbstractTestExchangeFactory
{
    private AmqpProvider provider = new RabbitMQProvider();

    @Override
    protected AmqpProvider getProvider()
    {
        return provider;
    }
}
