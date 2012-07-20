package com.nesscomputing.amqp.qpid;

import com.nesscomputing.amqp.AbstractTestExchangeFactory;
import com.nesscomputing.amqp.AmqpProvider;
import com.nesscomputing.amqp.QPidProvider;

public class TestExchangeFactoryQPid extends AbstractTestExchangeFactory
{
    private AmqpProvider provider = new QPidProvider();

    @Override
    protected AmqpProvider getProvider()
    {
        return provider;
    }
}
