package com.heima.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.heima.reggie.common.R;
import com.heima.reggie.dto.DishDto;
import com.heima.reggie.entity.Category;
import com.heima.reggie.entity.Dish;
import com.heima.reggie.entity.DishFlavor;
import com.heima.reggie.service.CategoryService;
import com.heima.reggie.service.DishFlavorService;
import com.heima.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {
    @Autowired
    private DishService dishService;

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private CategoryService categoryService;

    /**
     * 新增菜品
     * @param dishDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){
        log.info(dishDto.toString());

        dishService.saveWithFlavor(dishDto);
        return R.success("新增菜品成功");
    }

    /**
     * 菜品批量停售起售
     * @param status
     * @param ids
     * @return
     */
    @PostMapping("/status/{status}")
    public R<String> updateStatus(@PathVariable int status,String[] ids){
        log.info("id {} {}", status, ids);
        for (String id: ids) {
            //添加条件构造器
            LambdaUpdateWrapper<Dish> updateWrapper = new LambdaUpdateWrapper<>();
            //根据id更新Dish表中的对应的菜品状态
            updateWrapper.set(Dish::getStatus,status).in(Dish::getId, id);
            dishService.update(updateWrapper);
        }
        return R.success("菜品信息修改成功");

    }
//    @PostMapping("/status/{status}")
//    public R<String> updateStatus(@PathVariable int status,@RequestParam Long[] ids){
//        log.info("id {} {}",status, ids);
//        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.in(Dish::getId,ids);
//        List<Dish> list = dishService.list(queryWrapper);
//        list.stream().map((item) ->{
//            item.setStatus(status);
//            return item;
//        }).collect(Collectors.toList());
//        dishService.updateBatchById(list);
//        return R.success("菜品信息修改成功");
//    }

    /**
     * 菜品信息分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String  name){
        log.info("page = {},pageSize = {}, name = {}",page,pageSize,name);
        Page pageInfo = new Page<>(page, pageSize);
        Page<DishDto> dishDtoPage = new Page<>();

        //添加条件构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        //添加过滤条件 name不空
        queryWrapper.like(StringUtils.isNotEmpty(name),Dish::getName,name);
        //添加排序条件
        queryWrapper.orderByDesc(Dish::getUpdateTime);
        //执行查询
        dishService.page(pageInfo,queryWrapper);

        //对象拷贝 pageInfo拷贝到dishDtoPage
        BeanUtils.copyProperties(pageInfo,dishDtoPage,"records");

        List<Dish> records = pageInfo.getRecords();
        List<DishDto> list = records.stream().map((item) ->{
            DishDto dishDto = new DishDto();

            BeanUtils.copyProperties(item, dishDto);
            Long categoryId = item.getCategoryId();//分类id
            //根据id查询分类对象
            Category category = categoryService.getById(categoryId);

            if (category != null){
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }


            return dishDto;
        }).collect(Collectors.toList());

        dishDtoPage.setRecords(list);
        return R.success(dishDtoPage);

    }

    /**
     * 根据id查询菜品信息和对应的口味信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto> getById(@PathVariable Long id){
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        return R.success(dishDto);
    }

    /**
     * 修改菜品
     * @param dishDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto){
        log.info(dishDto.toString());

        dishService.updateWithFlavor(dishDto);
        return R.success("修改菜品成功");
    }

    /**
     * 根据条件查询对应的菜品数据
     * @param dish
     * @return
     */
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish){

        //构造查询条件
        LambdaQueryWrapper<Dish> queryWrapper =  new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId() != null,Dish::getCategoryId,dish.getCategoryId());
        //添加条件，查询状态为1（起售状态）的菜品
        queryWrapper.eq(Dish::getStatus,1);
        //添加排序条件
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        List<Dish> list = dishService.list(queryWrapper);


        List<DishDto> dishDtoList = list.stream().map((item) ->{
                    DishDto dishDto = new DishDto();

                    BeanUtils.copyProperties(item, dishDto);
                    Long categoryId = item.getCategoryId();//分类id
                    //根据id查询分类对象
                    Category category = categoryService.getById(categoryId);

                    if (category != null){
                        String categoryName = category.getName();
                        dishDto.setCategoryName(categoryName);
                    }
                    //菜品id
                    Long dishId = item.getId();
                    LambdaQueryWrapper<DishFlavor> queryWrapper1 = new LambdaQueryWrapper<>();
                    queryWrapper1.eq(DishFlavor::getDishId,dishId);
                    //sql:select * from dish_flavor where dish_id = ?
                    List<DishFlavor> dishFlavorList = dishFlavorService.list(queryWrapper1);
                    dishDto.setFlavors(dishFlavorList);

            return dishDto;
        }).collect(Collectors.toList());
        return R.success(dishDtoList);

    }

    /**
     * 菜品信息批量删除
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids){
        log.info("id {}", ids);
        dishService.deleteWithFlavor(ids);
        return R.success("菜品信息删除成功");
    }

}
