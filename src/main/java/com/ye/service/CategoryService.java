package com.ye.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ye.entity.Category;


public interface CategoryService extends IService<Category> {
    public void remove(Long id);
}
