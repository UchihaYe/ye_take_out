package com.ye.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ye.common.BaseContext;
import com.ye.common.CustomException;
import com.ye.common.R;
import com.ye.dto.OrdersDto;
import com.ye.entity.*;
import com.ye.mapper.OrdersMapper;
import com.ye.service.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrdersService {


    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private UserService userService;

    @Autowired
    private AddressBookService addressBookService;

    @Autowired
    private OrderDetailService orderDetailService;

    /**
     * 用户下单
     *
     * @param orders
     */
    @Override
    @Transactional
    public void submit(Orders orders) {
        //获取当前用户id
        Long currentId = BaseContext.getCurrentId();

        //查询当前用户的购物车数据
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, currentId);
        List<ShoppingCart> shoppingCarts = shoppingCartService.list(queryWrapper);

        if (shoppingCarts == null || shoppingCarts.size() == 0) {
            throw new CustomException("购物车为空，不能下单");
        }
        //查询用户数据
        User user = userService.getById(currentId);

        //查询地址数据
        Long addressBookId = orders.getAddressBookId();
        AddressBook addressBook = addressBookService.getById(addressBookId);

        if (addressBook == null) {
            throw new CustomException("用户地址信息有误，不能下单");
        }

        //订单号
        String orderId = String.valueOf(IdWorker.getId());

        AtomicInteger amount = new AtomicInteger(0);

        List<OrderDetail> orderDetails = shoppingCarts.stream().map((item) -> {
            OrderDetail orderDetail = new OrderDetail();

            orderDetail.setOrderId(Long.valueOf(orderId));
            orderDetail.setNumber(item.getNumber());
            orderDetail.setDishFlavor(item.getDishFlavor());
            orderDetail.setDishId(item.getDishId());
            orderDetail.setSetmealId(item.getSetmealId());
            orderDetail.setName(item.getName());
            orderDetail.setImage(item.getImage());
            orderDetail.setAmount(item.getAmount());
            amount.addAndGet(item.getAmount().multiply(new BigDecimal(item.getNumber())).intValue());
            return orderDetail;
        }).collect(Collectors.toList());

        //向订单表插入数据（单条
        orders.setNumber(orderId);
        orders.setOrderTime(LocalDateTime.now());
        orders.setCheckoutTime(LocalDateTime.now());
        orders.setStatus(2);
        orders.setAmount(new BigDecimal(amount.get()));//总金额
        orders.setUserId(currentId);
        orders.setNumber(String.valueOf(orderId));
        orders.setUserName(user.getName());
        orders.setConsignee(addressBook.getConsignee());
        orders.setPhone(addressBook.getPhone());
        orders.setAddress((addressBook.getProvinceName() == null ? "" : addressBook.getProvinceName())
                + (addressBook.getCityName() == null ? "" : addressBook.getCityName())
                + (addressBook.getDistrictName() == null ? "" : addressBook.getDistrictName())
                + (addressBook.getDetail() == null ? "" : addressBook.getDetail()));
        this.save(orders);

        //向订单明细表插入数据（多条
        orderDetailService.saveBatch(orderDetails);

        //清空购物车数据
        shoppingCartService.remove(queryWrapper);
    }

    @Override
    public R<Page> findUserPage(Long page, Long pageSize) {
        /*
         * 需求:分页查询订单(orders)及订单明细(Orders)
         *   然后写了一个OrdersDto  Dto的意思就是数据传输对象 Data Transmission Object
         *   可以看到里面有一个List 就是订单明细
         * */
//        这里通过线程工具类,共享的数据中获取用户的id
        Long currentId = BaseContext.getCurrentId();
//        查询order分页
        Page<Orders> page1 = new Page<>(page, pageSize);
        LambdaQueryWrapper<Orders> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Orders::getUserId, currentId).orderByDesc(Orders::getCheckoutTime);
        Page<Orders> page2 = page(page1, lambdaQueryWrapper);

//        创建一个OrdersDto分页,这个就是我们最终返回的分页对象
        Page<OrdersDto> page3 = new Page<>();
//        将page2的分页数据复制给,page3,因为records不能直接复制,通过遍历的方式.
        BeanUtils.copyProperties(page2, page3, "records");

//        存储records的集合
        List<OrdersDto> orderDetailList = new ArrayList<>();
//        遍历page2
        for (Orders record : page2.getRecords()) {
//            创建一个ordersDto
            OrdersDto ordersDto = new OrdersDto();
//            复制records
            BeanUtils.copyProperties(record, ordersDto);
//            查询订单明细
            LambdaQueryWrapper<OrderDetail> lambdaQueryWrapper1 = new LambdaQueryWrapper<>();
            lambdaQueryWrapper1.eq(OrderDetail::getOrderId, record.getId());
            List<OrderDetail> list = orderDetailService.list(lambdaQueryWrapper1);
//            存储订单明细
            ordersDto.setOrderDetails(list);
            orderDetailList.add(ordersDto);
        }
//        最终将list存储到OrdersDto的records中
        page3.setRecords(orderDetailList);
        System.out.println(page3);
//返回
        return R.success(page3);
    }

    @Override
    public R<Page> findPage(Long page, Long pageSize, Long number, String beginTime, String endTime) {
//        需求分析：
//        前端的api请求参数，分页，number（订单号），beginTime(开始时间),endTime(结束时间) 从该时间到结束时间内的订单。
//        需求:查询指定分页以及，订单号，从时间a到时间b的订单
//        前端要返回的属性有：订单号,订单状态，收货人，联系电话，地址，支付金额，下单时间
        Page<Orders> ordersPage = new Page<>(page, pageSize);

//        创建查询orders的条件构造器
        LambdaQueryWrapper<Orders> ordersLambdaQueryWrapper = new LambdaQueryWrapper<>();
        ordersLambdaQueryWrapper
                .eq(number != null && number != 0, Orders::getNumber, number)
                .between(StringUtils.isNotEmpty(beginTime) && StringUtils.isNotEmpty(endTime), Orders::getOrderTime, beginTime, endTime);

        System.out.println(1);
        Page<Orders> page1 = this.page(ordersPage, ordersLambdaQueryWrapper);

//        查询时间内的
//        lambdaQueryWrapper.eq()
//        this.page();
        return R.success(page1);
    }

    @Override
    public R<String> updateStatus(Integer status, Long id) {
        if (id != null && status != null) {
            Orders orders = new Orders();
            orders.setId(id);
            orders.setStatus(status);
            if (this.updateById(orders)) {
                return R.success("状态修改成功");
            }
            return R.error("修改状态失败");
        }

        return R.error("未知错误");
    }
}
