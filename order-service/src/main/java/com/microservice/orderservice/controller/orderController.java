package com.microservice.orderservice.controller;

import com.microservice.orderservice.dto.OrderRequest;
import com.microservice.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class orderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus
    public String placeOrder(@RequestBody OrderRequest orderRequest){
         orderService.placeOrder(orderRequest);
         return "order placed successfully";

    }
}
