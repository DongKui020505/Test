package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @PostMapping("/login")
    public R<Employee> login(HttpSession session, @RequestBody Employee employee){
        // 获取密码对应的md5值
        String password = DigestUtils.md5DigestAsHex(employee.getPassword().getBytes());
        // 根据用户名查询
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername, employee.getUsername());
        Employee emp = employeeService.getOne(queryWrapper);
        // 验证身份
        if (emp == null) {
            return R.error("用户不存在！");
        }
        if (!emp.getPassword().equals(password)) {
            return R.error("密码错误！");
        }
        if (emp.getStatus() == 0) {
            return R.error("账号已禁用！");
        }
        session.setAttribute("employee", emp.getId());
        return R.success(emp);
    }

    @PostMapping("/logout")
    public R<String> logout(HttpSession session) {
        session.removeAttribute("employee");
        return R.success("退出成功！");
    }

    @PostMapping
    public R<String> save(@RequestBody Employee employee) {
        // 设置员工初始信息
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
        employeeService.save(employee);
        return R.success("新增员工成功！");
    }

    @GetMapping("/page")
    public R<Page<Employee>> page(Integer page, Integer pageSize, String name) {
        // 分页构造器
        Page<Employee> pageInfo = new Page<>(page, pageSize);
        // 添加查询条件
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotEmpty(name), Employee::getName, name);
        queryWrapper.orderByDesc(Employee::getUpdateTime);
        employeeService.page(pageInfo, queryWrapper);
        return R.success(pageInfo);
    }

    @PutMapping
    public R<String> update(@RequestBody Employee employee) {
        return employeeService.updateById(employee) ? R.success("修改成功！") : R.error("修改失败！");
    }

    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable("id") Long id) {
        Employee employee = employeeService.getById(id);
        return employee != null ? R.success(employee) : R.error("用户不存在！");
    }
}
