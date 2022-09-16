package com.foryou.billingapi.api.service.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.foryou.billingapi.api.dto.request.PaymentRequestMessage;
import com.foryou.billingapi.api.dto.response.PaymentResponseMessage;
import com.foryou.billingapi.api.service.PaymentService;
import com.foryou.billingapi.global.Constants;
import com.siot.IamportRestClient.exception.IamportResponseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumer {

    private final ObjectMapper objMapper;
    private final PaymentService paymentService;
    private final KafkaPaymentResultProducer producer;

    @KafkaListener(
            topics = Constants.KAFKA_TOPIC_PARTY
            , groupId = Constants.KAFKA_GROPU_ID_PAYMENT
    )
    public void listen(
            @Header(KafkaHeaders.ACKNOWLEDGMENT) Acknowledgment ack
            , @Header(KafkaHeaders.RECEIVED_TOPIC) String topic
            , @Header(KafkaHeaders.GROUP_ID) String groupId
            , @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition
            , @Header(KafkaHeaders.OFFSET) long offset
            , @Header(KafkaHeaders.RECEIVED_TIMESTAMP) long ts
            , String msg) {
        try {
            PaymentRequestMessage request = objMapper.readValue(msg, PaymentRequestMessage.class);
            log.info("message: {}, topic: {}, groupId: {}, partition: {}, offset: {}, time: {}", request, topic, groupId, partition, offset, ts);

            PaymentResponseMessage resultMessage = createResultMessage(request, paymentService.doPayAgain(request));
            producer.sendMessage(resultMessage);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
        } catch (IamportResponseException e) {
            log.error(e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        ack.acknowledge();
    }

    private PaymentResponseMessage createResultMessage(PaymentRequestMessage request, boolean isSuccess) {
        return PaymentResponseMessage.builder()
                .memberId(request.getMemberId())
                .partyNo(request.getPartyNo())
                .paymentNo(request.getPaymentNo())
                .success(isSuccess)
                .build();
    }
}