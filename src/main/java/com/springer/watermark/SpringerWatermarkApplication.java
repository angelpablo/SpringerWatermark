package com.springer.watermark;


import java.util.HashMap;
import java.util.Map;

import javax.jms.ConnectionFactory;

import org.apache.activemq.broker.BrokerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

import com.springer.watermark.business.WatermarkProcessor;
import com.springer.watermark.business.WatermarkProcessorImpl;

@SpringBootApplication
public class SpringerWatermarkApplication {

	private static final Logger log = LoggerFactory.getLogger(SpringerWatermarkApplication.class);
	@Bean(initMethod = "start", destroyMethod = "stop")
	public BrokerService broker() throws Exception {
	    final BrokerService broker = new BrokerService();
	    broker.addConnector("tcp://localhost:61616");
	    broker.addConnector("vm://localhost");
	    broker.setPersistent(false);
	    return broker;
	}
	
	@Bean 
	public WatermarkProcessor watermarkProcessor()
	{
		WatermarkProcessor processor = new WatermarkProcessorImpl();
		return processor;
	}
	
    @Bean
    public JmsListenerContainerFactory<?> myFactory(ConnectionFactory connectionFactory,
                                                    DefaultJmsListenerContainerFactoryConfigurer configurer) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        // This provides all boot's default to this factory, including the message converter
        configurer.configure(factory, connectionFactory);
        // You could still override some of Boot's default if necessary.
        return factory;
    }

    @Bean // Serialize message content to json using TextMessage
    public MessageConverter jacksonJmsMessageConverter() {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");
        return converter;
    }
	
	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(SpringerWatermarkApplication.class, args);
		
	}
}
