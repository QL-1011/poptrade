package com.poptrade.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.poptrade.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 Mapper，常规 CRUD 由 MyBatis-Plus BaseMapper 自动提供。
 * 复杂 SQL 可在 resources/mapper/UserMapper.xml 中补充。
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
