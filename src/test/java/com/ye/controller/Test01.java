package com.ye.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ye.entity.Employee;
import com.ye.service.EmployeeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
public class Test01 {
    @Autowired
    private EmployeeService employeeService;

    @Test
    public void page() {

        //构造分页构造器
        IPage<Employee> pageInfo = new Page<>(1,2);


        //执行查询

        employeeService.page(pageInfo, null);


    }
}
