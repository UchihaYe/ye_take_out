package com.ye.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ye.common.BaseContext;
import com.ye.common.R;
import com.ye.entity.ShoppingCart;
import com.ye.mapper.ShoppingCartMapper;
import com.ye.service.ShoppingCartService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart> implements ShoppingCartService {
/*    @Override
    public R<String> subShopping(ShoppingCart shoppingCart) {
        if (shoppingCart.getDishId()!=null&&shoppingCart.getDishFlavor()==null){
            return R.error("减少失败");
        }
        LambdaQueryWrapper<ShoppingCart> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(shoppingCart.getDishId()!=null,ShoppingCart::getDishId,shoppingCart.getDishId());
        queryWrapper.eq(shoppingCart.getSetmealId()!=null,ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        queryWrapper.eq(shoppingCart.getDishFlavor()!=null,ShoppingCart::getDishFlavor,shoppingCart.getDishFlavor());
        ShoppingCart shoppingCartOne = getOne(queryWrapper);
        Integer number = shoppingCartOne.getNumber();
        if (number>1){
            shoppingCartOne.setNumber(number-1);
            updateById(shoppingCartOne);
        }
        else {
            removeById(shoppingCartOne.getId());
        }
        return R.success("减少成功");
    }*/

 /*   @Override
//    菜单数量减一
    public R<ShoppingCart> subShopping(ShoppingCart shoppingCart) {
        //查询当前用户id
        Long currentId = BaseContext.getCurrentId();
        shoppingCart.setUserId(currentId);

        //查询要减少的是不是菜品
        Long dishId = shoppingCart.getDishId();

        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.eq(ShoppingCart::getUserId,currentId);

        if (dishId != null){
            queryWrapper.eq(ShoppingCart::getDishId,dishId);
        }else {
            queryWrapper.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        }

        //判断购物车中是否已经有该菜品或者套餐，如果已经有
        ShoppingCart cartServiceOne = getOne(queryWrapper);

        if (cartServiceOne != null && cartServiceOne.getNumber() != 0){
            Integer number = cartServiceOne.getNumber();
            if (number - 1 == 0){
                cartServiceOne.setNumber(number - 1);
                shoppingCart.setCreateTime(LocalDateTime.now());
                remove(queryWrapper);
            }else {
                cartServiceOne.setNumber(number -1);
                shoppingCart.setCreateTime(LocalDateTime.now());
                updateById(cartServiceOne);
            }
        }
        ShoppingCart cartServiceOne1 = getOne(queryWrapper);
        return R.success(cartServiceOne1);
    }*/
}
