package com.nesscomputing.amqp;

public interface AmqpProvider
{
    void startup() throws Exception;

    void shutdown() throws Exception;

    String getUri();
}

