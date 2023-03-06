package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;

import java.util.List;

public interface DishService extends IService<Dish> {

    DishDto getByIdWithFlavor(Long id);

    void saveWithFlavor(DishDto dishDto);

    void updateWithFlavor(DishDto dishDto);

    void removeWithFlavor(List<Long> ids);

    void start(List<Long> ids);

    void stop(List<Long> ids);
}
