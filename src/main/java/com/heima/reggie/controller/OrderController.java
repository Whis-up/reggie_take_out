package com.heima.reggie.controller;

import com.heima.reggie.common.R;
import com.heima.reggie.entity.Orders;
import com.heima.reggie.service.OrderDetailService;
import com.heima.reggie.service.OrderService;
import com.heima.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequestMapping("/order")
@RestController
public class OrderController {

    @Autowired
    private OrderService orderService;



    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders){
        log.info("订单数据 {}",orders);
        orderService.submit(orders);
        return R.success("下单成功");
    }
}
