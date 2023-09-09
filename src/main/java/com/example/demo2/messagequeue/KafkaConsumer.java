package com.example.demo2.messagequeue;

import com.example.demo2.jpa.CatalogEntity;
import com.example.demo2.jpa.CatalogRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaConsumer {
    private final CatalogRepository repository;

    @KafkaListener(topics = "example-catalog-topic") // 해당 토픽에 데이터가 전달되면 아래의 메소드 실행
    public void updateQty(String kafkaMessage){
        log.info("Kafka Message: {}",kafkaMessage);

        Map<Object, Object> map;
        ObjectMapper mapper = new ObjectMapper();
        try{
            map = mapper.readValue(kafkaMessage, new TypeReference<>() {});
        }catch (JsonProcessingException ex) {
            throw new RuntimeException(); // 어드바이스에서 해당하는 예외를 알아서 잡게 하자(커스텀 예외)
        }

        CatalogEntity entity = repository.findByProductId((String) map.get("productId"));
        if(entity != null){
            entity.setStock(entity.getStock() - (Integer)map.get("qty"));
            repository.save(entity);
        }

    }
}