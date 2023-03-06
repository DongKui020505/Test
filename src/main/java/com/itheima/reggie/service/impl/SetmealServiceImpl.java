package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.mapper.SetmealMapper;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private CategoryService categoryService;

    @Override
    @Transactional
    public void saveWithDish(SetmealDto setmealDto) {
        this.save(setmealDto);
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes.forEach(setmealDish -> {
            setmealDish.setSetmealId(setmealDto.getId());
        });
        setmealDishService.saveBatch(setmealDishes);
    }

    @Override
    public SetmealDto getByIdWithDish(Long id) {
        Setmeal setmeal = this.getById(id);
        SetmealDto setmealDto = new SetmealDto();
        BeanUtils.copyProperties(setmeal, setmealDto);
        // 获取菜品信息
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId, setmealDto.getId());
        List<SetmealDish> setmealDishes = setmealDishService.list(queryWrapper);
        setmealDto.setSetmealDishes(setmealDishes);
        // 获取分类名称
        Category category = categoryService.getById(setmealDto.getCategoryId());
        if (category != null) {
            setmealDto.setCategoryName(category.getName());
        }
        return setmealDto;
    }

    @Override
    @Transactional
    public void updateWithDish(SetmealDto setmealDto) {
        // 修改套餐基本信息
        this.updateById(setmealDto);
        // 删除原来的菜品信息
        Long setmealId = setmealDto.getId();
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId, setmealId);
        setmealDishService.remove(queryWrapper);
        // 添加新的菜品信息
        List<SetmealDish> dishList = setmealDto.getSetmealDishes();
        dishList.forEach(dish -> {
            dish.setSetmealId(setmealDto.getId());
        });
        setmealDishService.saveBatch(dishList);
    }

    @Override
    @Transactional
    public void removeWithDish(List<Long> ids) {
        // 查询套餐状态，是否可删除
        LambdaQueryWrapper<Setmeal> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.in(Setmeal::getId, ids);
        queryWrapper1.eq(Setmeal::getStatus, 1);
        long count = this.count(queryWrapper1);
        // 如果不能，则抛出异常
        if (count > 0) {
            throw new CustomException("套餐正在售卖中，无法删除！");
        }
        // 如果能，先删除套餐
        this.removeBatchByIds(ids);
        // 再删除套餐对应的套餐菜品关系
        LambdaQueryWrapper<SetmealDish> queryWrapper2 = new LambdaQueryWrapper<>();
        queryWrapper2.in(SetmealDish::getSetmealId, ids);
        setmealDishService.remove(queryWrapper2);
    }

    @Override
    @Transactional
    public void start(List<Long> ids) {
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Setmeal::getId, ids);
        queryWrapper.eq(Setmeal::getStatus, 1);
        long count = this.count(queryWrapper);
        if (count > 0) {
            throw new CustomException("套餐正在启售中！");
        }
        LambdaUpdateWrapper<Setmeal> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(Setmeal::getStatus, 1);
        updateWrapper.in(Setmeal::getId, ids);
        this.update(updateWrapper);
    }

    @Override
    public void stop(List<Long> ids) {
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Setmeal::getId, ids);
        queryWrapper.eq(Setmeal::getStatus, 0);
        long count = this.count(queryWrapper);
        if (count > 0) {
            throw new CustomException("套餐正在停售中！");
        }
        LambdaUpdateWrapper<Setmeal> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(Setmeal::getStatus, 0);
        updateWrapper.in(Setmeal::getId, ids);
        this.update(updateWrapper);
    }
}
