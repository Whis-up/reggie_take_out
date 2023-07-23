package com.heima.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.heima.reggie.entity.SetmealDish;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.transaction.annotation.Transactional;

@Mapper
public interface SetmealDishMapper extends BaseMapper<SetmealDish> {
}
