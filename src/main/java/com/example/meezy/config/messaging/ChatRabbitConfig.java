package com.example.meezy.config.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ChatRabbitConfig {

    private final ChatRabbitProperties chatRabbitProperties;

    @Bean
    public FanoutExchange chatExchange(){
        return ExchangeBuilder
                .fanoutExchange(chatRabbitProperties.exchangeName())
                .durable(true) //RabbitMQ 재시작 후에도 Exchange 메타데이터 유지
                .build();
    }

    @Bean
    public Queue chatMessageQueue(){
        return QueueBuilder
                .durable(chatRabbitProperties.queuePrefix() + "." + UUID.randomUUID()) //서버마다 고유한 Queue 이름 생성
                .autoDelete() //연결된 Consumer가 모두 종료되면 Queue 자동 삭제
                .build();
    }

    @Bean
    public Binding chatBinding(FanoutExchange chatExchange, Queue chatMessageQueue){
        return BindingBuilder
                .bind(chatMessageQueue)
                .to(chatExchange);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter(){
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public ConnectionFactory connectionFactory(RabbitProperties rabbitProperties){
        CachingConnectionFactory factory = new CachingConnectionFactory(); //RabbitMQ 와의 TCP 연결을 관리하는 팩토리

        factory.setHost(rabbitProperties.getHost());
        factory.setPort(rabbitProperties.getPort());
        factory.setUsername(rabbitProperties.getUsername());
        factory.setPassword(rabbitProperties.getPassword());
        factory.setCacheMode(CachingConnectionFactory.CacheMode.CHANNEL); //Connection은 하나 유지하고 Channel만 캐싱
        factory.setChannelCacheSize(25); //최대 25개의 Channel을 풀로 유지
        factory.setPublisherConfirmType(CachingConnectionFactory.ConfirmType.CORRELATED); //Producer가 보낸 메시지에 대해 Broker ACK/NACK 수신
        factory.setPublisherReturns(true); //라우팅 실패 시 메시지를 반환받기 위한 설정
        factory.getRabbitConnectionFactory().setAutomaticRecoveryEnabled(true); //네트워크 장애 발생 시 자동 재연결
        factory.getRabbitConnectionFactory().setNetworkRecoveryInterval(5000); //재연결 시도 간격: 5초

        return factory;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         Jackson2JsonMessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);

        template.setMessageConverter(converter);
        template.setExchange(chatRabbitProperties.exchangeName());
        template.setMandatory(true);
        template.setConfirmCallback((correlationData, ack, cause) -> {
            // Broker가 메시지를 정상 수신했는지 콜백으로 통지
            if (!ack) {
                log.error("메시지 전달 실패: {}", cause);
            }
        });

        template.setReturnsCallback(returned ->
                //Exchange 에는 도착했지만 Queue에 못 간 경우 호출
                log.warn("메시지 라우팅 실패: {}", returned.getReplyText())
        );

        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            Jackson2JsonMessageConverter converter) {

        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();

        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(converter);
        factory.setPrefetchCount(10);

        factory.setErrorHandler(t ->
                //메시지 처리 중 예외 발생 시 로깅
                log.error("메시지 가져오기 실패: {}", t.getMessage(), t)
        );

        return factory;
    }
}
