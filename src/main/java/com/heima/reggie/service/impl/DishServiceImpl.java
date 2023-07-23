package com.heima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.reggie.common.CustomException;
import com.heima.reggie.dto.DishDto;
import com.heima.reggie.entity.Dish;
import com.heima.reggie.entity.DishFlavor;
import com.heima.reggie.entity.Setmeal;
import com.heima.reggie.entity.SetmealDish;
import com.heima.reggie.mapper.DishMapper;
import com.heima.reggie.service.DishFlavorService;
import com.heima.reggie.service.DishService;
import com.heima.reggie.service.SetmealDishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private DishService dishService;

    @Autowired
    private SetmealDishService setmealDishService;
    /**
     * 新增菜品，同时保存对应的口味数据
     * @param dishDto
     */
    @Transactional
    public void saveWithFlavor(DishDto dishDto) {
        //保存菜品的基本信息到菜品表dish
        this.save(dishDto);

        //新增菜品时，没有对应的菜品id，mp雪花算法在新增后自动生成id。因此flavor口味数据无法封装还未生成的id。
        // 逻辑是先保存基本菜品信息到dish，雪花自动生成dish_id，然后再将获得的id封装到口味表里，最后一并保存

        Long dishId = dishDto.getId();//菜品id

        //菜品口味
        List<DishFlavor> flavors = dishDto.getFlavors();

        //flavors.forEach(dishFlavor -> dishDto.setId(dishId));
        flavors = flavors.stream().map((item) -> {
           item.setDishId(dishId);
           return item;
        }).collect(Collectors.toList());
        //保存菜品口味数据到菜品口味表dish_flavor
        dishFlavorService.saveBatch(flavors);


    }

    /**
     * 根据id查询菜品信息和口味信息
     * @param id
     * @return
     */
    public DishDto getByIdWithFlavor(Long id) {
        //查询菜品基本信息，dish表查询
        Dish dish = this.getById(id);

        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish,dishDto);
        //查询当前菜品对应的口味信息，从dish_flavor表查询
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, dish.getId());
        List<DishFlavor> flavors = dishFlavorService.list(queryWrapper);
        dishDto.setFlavors(flavors);
        return dishDto;
    }

    @Override
    @Transactional
    public void updateWithFlavor(DishDto dishDto) {
        //更新dish表基本信息
        this.updateById(dishDto);

        //清理当前菜品对应口味数据 dish_flavor表的delete操作
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,dishDto.getId());
        dishFlavorService.remove(queryWrapper);

        //添加当前提交的口味数据--dish_flavor表的insert操作
        List<DishFlavor> flavors = dishDto.getFlavors();


        flavors = flavors.stream().map((item) -> {
            item.setDishId(dishDto.getId());
            return item;
        }).collect(Collectors.toList());
        dishFlavorService.saveBatch(flavors);
    }

    @Transactional
    @Override
    public void deleteWithFlavor(List<Long> ids) {

        //构造查询条件
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Dish::getId,ids);
        queryWrapper.eq(Dish::getStatus,1);

        int count1 = dishService.count(queryWrapper);
        if (count1 > 0){
            throw new CustomException("菜品已经起售，不能删除");
        }
        LambdaQueryWrapper<SetmealDish> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.in(SetmealDish::getDishId,ids);
        int count2 = setmealDishService.count(queryWrapper1);

        if (count2 > 0){
            throw new CustomException("菜品被包含在套餐内，不能删除");
        }

        //先删除菜品表里的基本信息
        this.removeByIds(ids);
        //再查询口味表中对应菜品的口味信息
        LambdaQueryWrapper<DishFlavor> dishFlavorWrapper = new LambdaQueryWrapper<>();
        dishFlavorWrapper.in(DishFlavor::getDishId,ids);
        //删除菜品口味表里的数据 dish_flavor
        dishFlavorService.remove(dishFlavorWrapper);



    }

}
