package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.mapper.DishMapper;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private CategoryService categoryService;

    @Override
    public DishDto getByIdWithFlavor(Long id) {
        // 查询基本信息
        Dish dish = this.getById(id);
        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish, dishDto);
        // 添加菜品分类
        Category category = categoryService.getById(dish.getId());
        if (category != null) {
            dishDto.setCategoryName(category.getName());
        }
        // 添加菜品口味
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, id);
        List<DishFlavor> flavors = dishFlavorService.list(queryWrapper);
        dishDto.setFlavors(flavors);
        return dishDto;
    }

    @Override
    public void saveWithFlavor(DishDto dishDto) {
        // 保存菜品基本信息到菜品表
        this.save(dishDto);
        // 给菜品口味添加dishId
        Long dishId = dishDto.getId();
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors.forEach(flavor->{
            flavor.setDishId(dishId);
        });
        // 保存菜品口味信息到口味表
        dishFlavorService.saveBatch(flavors);
    }

    @Override
    @Transactional
    public void updateWithFlavor(DishDto dishDto) {
        // 修改菜品基本信息
        this.updateById(dishDto);
        // 删除原来口味信息
        Long dishId = dishDto.getId();
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, dishId);
        dishFlavorService.remove(queryWrapper);
        // 添加新的口味信息
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors.forEach(flavor->{
            flavor.setDishId(dishId);
        });
        dishFlavorService.saveBatch(flavors);
    }

    @Override
    @Transactional
    public void removeWithFlavor(List<Long> ids) {
        // 查询菜品状态是否可删除
        LambdaQueryWrapper<Dish> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.in(Dish::getId, ids);
        queryWrapper1.eq(Dish::getStatus, 1);
        long count = this.count(queryWrapper1);
        // 如果不能删除，抛出异常
        if (count > 0) {
            throw new CustomException("菜品正在售卖中，无法删除！");
        }
        // 如果可以删除，先删除菜品
        this.removeBatchByIds(ids);
        // 再删除菜品对应口味
        LambdaQueryWrapper<DishFlavor> queryWrapper2 = new LambdaQueryWrapper<>();
        queryWrapper2.in(DishFlavor::getDishId, ids);
        dishFlavorService.remove(queryWrapper2);
    }

    @Override
    @Transactional
    public void start(List<Long> ids) {
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Dish::getId, ids);
        queryWrapper.eq(Dish::getStatus, 1);
        long count = this.count(queryWrapper);
        if (count > 0) {
            throw new CustomException("菜品正在启售中！");
        }
        LambdaUpdateWrapper<Dish> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(Dish::getId, ids);
        updateWrapper.set(Dish::getStatus, 1);
        this.update(updateWrapper);
    }

    @Override
    @Transactional
    public void stop(List<Long> ids) {
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Dish::getId, ids);
        queryWrapper.eq(Dish::getStatus, 0);
        long count = this.count(queryWrapper);
        if (count > 0) {
            throw new CustomException("菜品正在停售中！");
        }
        LambdaUpdateWrapper<Dish> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(Dish::getId, ids);
        updateWrapper.set(Dish::getStatus, 0);
        this.update(updateWrapper);
    }
}
