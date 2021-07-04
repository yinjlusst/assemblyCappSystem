package com.yjl.assemblycappsystem.passport.mapper;


import com.yjl.assemblycappsystem.bean.UmsUserAddinfo;
import org.springframework.data.mongodb.repository.MongoRepository;
import tk.mybatis.mapper.common.Mapper;

public interface UmsUserAddinfoMapper extends MongoRepository<UmsUserAddinfo,String> {
    UmsUserAddinfo findByUserId(Integer userId);
}