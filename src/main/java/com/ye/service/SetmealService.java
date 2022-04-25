package com.ye.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ye.common.R;
import com.ye.dto.SetmealDto;
import com.ye.entity.Setmeal;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {

    /**
     * 新增套餐 同时需要保存套餐和菜品的关联关系
     * @param setmealDto
     */
    void saveWithDish(SetmealDto setmealDto);

    /**
     * 删除套餐，同时需要删除套餐和菜品关联数据
     * @param ids
     */
    void removeWithDish(List<Long> ids);


    R<SetmealDto> findSetmeaById(Long id);

    R<String> UpdateSetmeal(SetmealDto setmealDto);

    //批量停售起售
    R<String> updateBatchStatus(int status, List<Long> id);
}
