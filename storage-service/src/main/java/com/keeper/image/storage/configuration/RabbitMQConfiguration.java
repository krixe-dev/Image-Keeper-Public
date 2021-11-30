package com.keeper.image.storage.configuration;

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

@Configuration
public class RabbitMQConfiguration {

    public static final String IMAGE_QUEUE_NAME = "new-image-queue";
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

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory listenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter());
        return factory;
    }
}
