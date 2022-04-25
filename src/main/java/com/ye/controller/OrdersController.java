package com.ye.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ye.common.R;
import com.ye.entity.Orders;
import com.ye.service.OrdersService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/order")
public class OrdersController {
    @Autowired
    private OrdersService ordersService;

    /**
     * 用户下单
     * @param orders
     * @return
     */
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders){
        ordersService.submit(orders);
        return R.success("下单成功");
    }

    @GetMapping("/userPage")
//    用户分页查询
    public R<Page> findUserPage(Long page, Long pageSize){
        return ordersService.findUserPage(page,pageSize);
    }

    @GetMapping("/page")
//    后台分页查询订单
    public R<Page> page(Long page, Long pageSize, Long number, String beginTime, String endTime){
        return ordersService.findPage(page,pageSize,number,beginTime,endTime);
    }

    @PutMapping
    public R<String> updateStatus(@RequestBody Orders orders){
        return ordersService.updateStatus(orders.getStatus(),orders.getId());
    }
}
