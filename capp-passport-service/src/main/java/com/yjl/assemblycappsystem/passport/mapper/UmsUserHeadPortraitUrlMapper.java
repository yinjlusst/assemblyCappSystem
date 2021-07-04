package com.yjl.assemblycappsystem.passport.mapper;

import com.yjl.assemblycappsystem.bean.UmsUserHeadPortraitUrl;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface UmsUserHeadPortraitUrlMapper extends MongoRepository<UmsUserHeadPortraitUrl,String> {
    UmsUserHeadPortraitUrl findByUserId(Integer userId);
}
