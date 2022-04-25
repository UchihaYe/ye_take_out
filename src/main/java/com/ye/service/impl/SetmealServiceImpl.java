package com.ye.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ye.common.CustomException;
import com.ye.common.R;
import com.ye.dto.SetmealDto;
import com.ye.entity.Category;
import com.ye.entity.Setmeal;

import com.ye.entity.SetmealDish;
import com.ye.mapper.SetmealMapper;
import com.ye.service.CategoryService;
import com.ye.service.SetmealDishService;
import com.ye.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private CategoryService categoryService;

    @Override
    @Transactional
    public void saveWithDish(SetmealDto setmealDto) {
        //保存套餐的基本信息，操作setmeal执行insert操作
        this.save(setmealDto);

        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes = setmealDishes.stream().map((item)->{
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());


        //保存套餐和菜品的关联关系，操作setmeal_dish，执行insert操作
        setmealDishService.saveBatch(setmealDishes);
    }

    /**
     * 删除套餐，同时需要删除套餐和菜品关联数据
     * @param ids
     */
    @Override
    @Transactional
    public void removeWithDish(List<Long> ids) {
        //查询套餐状态，确定是否可以删除
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.in(Setmeal::getId,ids);
        queryWrapper.eq(Setmeal::getStatus,1);

        //如果不能删除，抛出一个业务异常
        int count = this.count(queryWrapper);
        if (count > 0){
            throw new CustomException("套餐正在售卖中，不能删除");
        }

        //如果可以删除，先删除套餐表中的数据
        this.removeByIds(ids);

        //删除关系表中的数据
        LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(SetmealDish::getSetmealId,ids);
        setmealDishService.remove(lambdaQueryWrapper);
    }

    @Override
//    更新套餐信息
    public R<String> UpdateSetmeal(SetmealDto setmealDto) {
//        一、更新套餐
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDto,setmeal);
        boolean isSuccess = this.updateById(setmeal);

//        二、更新套餐的菜品信息
//        1.先删除再添加
//        先获取套餐的id，然后在套餐菜品表中，根据套餐的id进行删除
        Long id = setmeal.getId();
//        创建条件构造器
        LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
//        id不为空时，条件：套餐的菜品信息数据库setmealId=传过来的id
        lambdaQueryWrapper.eq(id!=null,SetmealDish::getSetmealId,id);
//        执行删除
        setmealDishService.remove(lambdaQueryWrapper);
//        2.添加菜品信息
//        获取前端传过来的对象
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
//        遍历出来然后给这个集合中的对象分别添加各自的id
        for (SetmealDish setmealDish : setmealDishes) {
            setmealDish.setSetmealId(id);
        }
        setmealDishService.saveBatch(setmealDishes);
        if (isSuccess){
            return R.success("成功");
        }
        return R.error("失败");
    }


    @Override
//    批量停售起售
    public R<String> updateBatchStatus(int status, List<Long> id) {
//        创建一个list，装Setmeal对象的，后面批量更新会用到
        List<Setmeal> list = new ArrayList();
//        将id遍历出来，然后赋值给setmeal对象中的属性
        for (Long aLong : id) {
            Setmeal setmeal = new Setmeal();
            setmeal.setId(aLong);
            setmeal.setStatus(status);
            list.add(setmeal);
        }
        if (list!=null){
//            然后进行批量更新
            boolean b = this.updateBatchById(list);
            if (b){
                return R.success("成功");
            }
        }
        return R.error("失败");
    }

    @Override
    public R<SetmealDto> findSetmeaById(Long id) {
//        一、查询套餐
//        先创建好要返回的对象
        SetmealDto setmealDto = new SetmealDto();
//        根据id查询setmeal表。
        Setmeal setmeal = this.getById(id);
//        将setmeal属性复制给setmealDto
        BeanUtils.copyProperties(setmeal,setmealDto);

//        二、查询套餐的分类
        Long categoryId = setmeal.getCategoryId();
        Category byId = categoryService.getById(categoryId);
        setmealDto.setCategoryName(byId.getName());

//        三、查询套餐的菜品
        Long id1 = setmeal.getId();
        LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(SetmealDish::getSetmealId,id1);
        List<SetmealDish> list = setmealDishService.list(lambdaQueryWrapper);
        if (list!=null){
            setmealDto.setSetmealDishes(list);
            return R.success(setmealDto);
        }

        return R.error("不存在");
    }
}
