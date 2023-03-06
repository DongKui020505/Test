package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.ShoppingCart;
import com.itheima.reggie.service.ShoppingCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/shoppingCart")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart) {
        // 设置用户ID，指定是谁的购物车
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);
        // 查询当前菜品是否在购物车中
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, userId);
        if (shoppingCart.getDishId() != null) {
            queryWrapper.eq(ShoppingCart::getDishId, shoppingCart.getDishId());
        } else {
            queryWrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        }
        ShoppingCart cart = shoppingCartService.getOne(queryWrapper);
        if (cart != null) { // 如果存在，则数量加一
            cart.setNumber(cart.getNumber() + 1);
            shoppingCartService.updateById(cart);
        } else {    // 如果不存在，则添加到数据库中，数量默认为一
            shoppingCart.setNumber(1);
            shoppingCartService.save(shoppingCart);
            cart = shoppingCart;
        }
        return R.success(cart);
    }

    @PostMapping("/sub")
    public R<ShoppingCart> sub(@RequestBody ShoppingCart shoppingCart) {
        // 设置用户ID，指定是谁的购物车
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);
        // 查询当前菜品是否在购物车中
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, userId);
        if (shoppingCart.getDishId() != null) {
            queryWrapper.eq(ShoppingCart::getDishId, shoppingCart.getDishId());
        } else {
            queryWrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        }
        ShoppingCart cart = shoppingCartService.getOne(queryWrapper);
        if (cart != null) {
            cart.setNumber(cart.getNumber() - 1);
            if (cart.getNumber() == 0) {
                shoppingCartService.removeById(cart.getId());
            } else {
                shoppingCartService.updateById(cart);
            }
            return R.success(cart);
        }
        return R.error("购物车菜品不存在！");
    }

    @GetMapping("/list")
    public R<List<ShoppingCart>> list() {
        Long userId = BaseContext.getCurrentId();
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, userId);
        List<ShoppingCart> shoppingCarts = shoppingCartService.list(queryWrapper);
        return R.success(shoppingCarts);
    }

    @DeleteMapping("/clean")
    public R<String> clean() {
        Long userId = BaseContext.getCurrentId();
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, userId);
        shoppingCartService.remove(queryWrapper);
        return R.success("清空成功！");
    }
}
