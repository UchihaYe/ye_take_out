package com.ye.service.impl;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ye.common.R;
import com.ye.dto.DishDto;
import com.ye.entity.Dish;

import com.ye.entity.DishFlavor;
import com.ye.mapper.DishMapper;
import com.ye.service.DishFlavorService;
import com.ye.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishFlavorService dishFlavorService;

    @Transactional
    public void saveWithFlavor(DishDto dishDto){
        //保存菜品的基本信息到菜品表dish
        this.save(dishDto);

        Long dishId = dishDto.getId();

        //菜品口味
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors = flavors.stream().map((item) -> {
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());

        //保存菜品的口味数据到dish_flavor
        dishFlavorService.saveBatch(dishDto.getFlavors());
    }

    //根据id查询菜名信息和对应的口味信息
    @Override
    public DishDto getByIdWithFlavor(Long id) {
        //查询菜品的基本信息从dish
        Dish dish = this.getById(id);

        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish,dishDto);

        //查询当前菜品对应的口味信息，从dish_flavor查询
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,dish.getId());
        List<DishFlavor> flavors = dishFlavorService.list(queryWrapper);
        dishDto.setFlavors(flavors);

        return dishDto;
    }

    @Override
    @Transactional
    public void updateWithFlavor(DishDto dishDto) {
        // 更新dish表基本信息
        this.updateById(dishDto);

        //清理当前菜品对应口味数据---dish_flavor表的delete操作
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,dishDto.getId());

        dishFlavorService.remove(queryWrapper);

        //添加当前提交过来的口味数据——-dish_flavor表的insert操作
        List<DishFlavor> flavors = dishDto.getFlavors();

        flavors = flavors.stream().map((item)->{
            item.setDishId(dishDto.getId());
            return item;
        }).collect(Collectors.toList());

        dishFlavorService.saveBatch(flavors);
    }

    /**
     * 批量删除
     * @param id
     * @return
     */
    @Override
    public R<String> deleteBatchByIds(List<Long> id) {
        if (id!=null){
            if (this.removeByIds(id)) {
                return R.success("删除成功");
            }
            return R.error("删除失败");
        }
        return R.error("空的");
    }

    //批量停售起售
    @Override
    public R<String> updateDishStatus(Integer status, List<Long> ids) {
        if (status!=null && ids!=null){
//            一个存放菜品id和status的list
            List<Dish> dishList = new ArrayList<>();
//            LambdaUpdateWrapper<Dish> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
//            lambdaUpdateWrapper.in(Dish::getStatus,status);
            for (Long id : ids) {
//                遍历前端传过来的id
                Dish dish = new Dish();
//                在循环中创建对象,那样可以保证对象是单例的.
                dish.setId(id);
                dish.setStatus(status);
                dishList.add(dish);
            }
//            批量根据id更新操作
            boolean b = updateBatchById(dishList);
            if (b){
                return R.success("成功");
            }
        }
        return R.error("参数为空，操作失败");
    }
}
