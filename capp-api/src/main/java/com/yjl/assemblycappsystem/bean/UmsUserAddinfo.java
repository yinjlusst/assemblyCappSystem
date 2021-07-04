package com.yjl.assemblycappsystem.bean;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Transient;
import java.io.Serializable;


public class UmsUserAddinfo implements Serializable {

    private String id;
    private String qq;
    private String weibo;
    private String introduce;
    private Integer userId;
    @Transient
    private String headPortraitsUrl;

    public String getHeadPortraitsUrl() {
        return headPortraitsUrl;
    }

    public void setHeadPortraitsUrl(String headPortraitsUrl) {
        this.headPortraitsUrl = headPortraitsUrl;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getQQ() {
        return qq;
    }

    public void setQQ(String qq) {
        this.qq = qq;
    }

    public String getWeibo() {
        return weibo;
    }

    public void setWeibo(String weibo) {
        this.weibo = weibo;
    }

    public String getIntroduce() {
        return introduce;
    }

    public void setIntroduce(String introduce) {
        this.introduce = introduce;
    }
}
