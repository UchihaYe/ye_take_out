package com.ye.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ye.common.R;
import com.ye.dto.DishDto;
import com.ye.entity.Dish;

import java.util.List;

public interface DishService extends IService<Dish> {
    //新增菜品，同时插入菜品对应的口味数据，需要操作两张表
    public void saveWithFlavor(DishDto dishDto);

    //根据id查询菜名信息和对应的口味信息
    DishDto getByIdWithFlavor(Long id);

    //更新菜品信息，同时更新口味信息
    void updateWithFlavor(DishDto dishDto);

    //批量删除
    R<String> deleteBatchByIds(List<Long> id);

    //批量停售起售
    R<String> updateDishStatus(Integer status, List<Long> id);
}
