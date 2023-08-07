package com.heima.reggie.controller;

import com.heima.reggie.service.OrderDetailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequestMapping("/orderDetail")
@RestController
public class OrderDetailController {

    @Autowired
    private OrderDetailService orderDetailService;
}
