package com.microservice.orderservice.service;

import com.microservice.orderservice.dto.InventoryResponse;
import com.microservice.orderservice.dto.OrderLineItemsDto;
import com.microservice.orderservice.dto.OrderRequest;
import com.microservice.orderservice.model.Order;
import com.microservice.orderservice.model.OrderLineItems;
import com.microservice.orderservice.repository.OrderRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;

    private final WebClient.Builder webClientBuilder;


    public void placeOrder(OrderRequest orderRequest){
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());

       List<OrderLineItems> orderLineItemsList = orderRequest.getOrderLineItemsDtoList()
                .stream()
                .map(orderLineItemsDto -> mapToDto(orderLineItemsDto)).toList();
       order.setOrderLineItemsList(orderLineItemsList);

       List<String> skuCode = order.getOrderLineItemsList().stream().map(OrderLineItems::getSkuCode).toList();

       //call inventory service to check product is in stock
       InventoryResponse[] inventoryResponsesArray = webClientBuilder.build().get()
                .uri("http://inventory-service/api/inventory",uriBuilder -> uriBuilder.queryParam("skuCode").build())
                        .retrieve()
                                .bodyToMono(InventoryResponse[].class)
                                        .block();


        boolean allProductsInStock = Arrays.stream(inventoryResponsesArray).allMatch(InventoryResponse::isInStock);

       if(allProductsInStock){
           orderRepository.save(order);
       }else{
           throw new IllegalArgumentException("Product is not in stock please try again later");
       }


    }

    private OrderLineItems mapToDto(OrderLineItemsDto orderLineItemsDto) {

        OrderLineItems orderLineItems = new OrderLineItems();
        orderLineItems.setPrice(orderLineItemsDto.getPrice());
        orderLineItems.setQuantity(orderLineItemsDto.getQuantity());
        orderLineItems.setSkuCode(orderLineItemsDto.getSkuCode());
        return orderLineItems;
    }
}
