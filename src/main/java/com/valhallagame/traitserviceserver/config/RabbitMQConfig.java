package com.valhallagame.traitserviceserver.config;

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
	// Trait configs
	@Bean
	public DirectExchange characterExchange() {
		return new DirectExchange(RabbitMQRouting.Exchange.CHARACTER.name());
	}

	@Bean
	public DirectExchange featExchange() {
		return new DirectExchange(RabbitMQRouting.Exchange.FEAT.name());
	}

	@Bean
	public Queue traitFeatAddQueue() {
		return new Queue("traitFeatAddQueue");
	}

	@Bean
	public Binding bindingFeatAdd(DirectExchange featExchange, Queue traitFeatAddQueue) {
		return BindingBuilder.bind(traitFeatAddQueue).to(featExchange).with(RabbitMQRouting.Feat.ADD);
	}

	@Bean
	public Queue traitCharacterDeleteQueue() {
		return new Queue("traitCharacterDeleteQueue");
	}

	@Bean
	public Binding bindingCharacterDeleted(DirectExchange characterExchange, Queue traitCharacterDeleteQueue) {
		return BindingBuilder.bind(traitCharacterDeleteQueue).to(characterExchange)
				.with(RabbitMQRouting.Character.DELETE);
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
}
