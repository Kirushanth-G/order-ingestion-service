package com.example.order_ingestion.mappers;

import com.example.order_ingestion.dtos.UnifiedOrderDTO;
import com.example.order_ingestion.entities.OrderEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "sequenceNumber", ignore = true)
    @Mapping(target = "receivedTime", ignore = true)
    @Mapping(target = "processedTime", ignore = true)
    @Mapping(target = "streamOffset", ignore = true)
    OrderEvent toEntity(UnifiedOrderDTO unifiedOrder);
}
