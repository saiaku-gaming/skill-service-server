package com.valhallagame.wardrobeserviceserver.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.valhallagame.common.rabbitmq.RabbitMQRouting;

@Configuration
public class RabbitMQConfig {
	// Wardrobe configs
	@Bean
	public DirectExchange wardrobeExchange() {
		return new DirectExchange(RabbitMQRouting.Exchange.WARDROBE.name());
	}

	@Bean
	public Jackson2JsonMessageConverter jacksonConverter() {
		return new Jackson2JsonMessageConverter();
	}

	@Bean
	public SimpleRabbitListenerContainerFactory containerFactory() {
		SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
		factory.setMessageConverter(jacksonConverter());
		return factory;
	}
	
	@Bean
	public Queue wardrobeCharacterDelete() {
		return new Queue("wardrobeCharacterDeleteQueue");
	}
	
	@Bean
	public Binding bindingCharacterDeleted(DirectExchange characterExchange, Queue wardrobeCharacterDeleteQueue) {
		return BindingBuilder.bind(wardrobeCharacterDeleteQueue).to(characterExchange)
				.with(RabbitMQRouting.Character.DELETE);
	}

}