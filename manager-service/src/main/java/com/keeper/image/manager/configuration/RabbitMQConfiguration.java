package com.keeper.image.manager.configuration;

import lombok.Getter;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for connection with rabbitmq
 */
@Configuration
@Getter
public class RabbitMQConfiguration {

    // name of queue on which this service will publish information about new images
    public static final String IMAGE_QUEUE_NAME = "new-image-queue";
    // name of queue from which this service will obtain information about image being processed
    public static final String METADATA_QUEUE_NAME = "new-metadata-queue";

    // New image messages queue configuration
    @Bean
    public DirectExchange imageDirectExchange() {
        return new DirectExchange(IMAGE_QUEUE_NAME);
    }

    @Bean
    public Queue imageQueue() {
        return new Queue(IMAGE_QUEUE_NAME);
    }

    @Bean
    public Binding imageBinding() {
        return BindingBuilder.bind(imageQueue()).to(imageDirectExchange()).with(IMAGE_QUEUE_NAME);
    }
    // ##

    // New metadata messages queue configuration
    @Bean
    public DirectExchange metadataDirectExchange() {
        return new DirectExchange(METADATA_QUEUE_NAME);
    }

    @Bean
    public Queue metadataQueue() {
        return new Queue(METADATA_QUEUE_NAME);
    }

    @Bean
    public Binding metadataBinding() {
        return BindingBuilder.bind(metadataQueue()).to(metadataDirectExchange()).with(METADATA_QUEUE_NAME);
    }
    // ##

    /**
     * Setting up new message conversion for queue communication -communication with JSON data format
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * Creating listener configuration for handling incoming messages
     */
    @Bean
    public SimpleRabbitListenerContainerFactory listenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter());
        return factory;
    }

}
