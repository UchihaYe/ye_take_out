package com.ye.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ye.dto.DishDto;
import com.ye.entity.DishFlavor;
import com.ye.mapper.DishFlavorMapper;
import com.ye.service.DishFlavorService;
import org.springframework.stereotype.Service;

@Service
public class DishFlavorServiceImpl extends ServiceImpl<DishFlavorMapper, DishFlavor> implements DishFlavorService {

}
