package com.ye.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ye.common.R;
import com.ye.entity.Orders;

public interface OrdersService extends IService<Orders> {

    void submit(Orders orders);

    R<Page> findUserPage(Long page, Long pageSize);

    R<Page> findPage(Long page, Long pageSize, Long number, String beginTime, String endTime);

    R<String> updateStatus(Integer status, Long id);
}
