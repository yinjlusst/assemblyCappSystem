package com.yjl.assemblycappsystem.bean;


import org.springframework.stereotype.Indexed;

import java.io.Serializable;

//@Document(collection = "comment")
//使用注解添加复合索引
//@CompoundIndex(def = "{'userid':1,'nickname':-1}")
public class Comment implements Serializable {//如果类上未使用@Document注解，类名相等与mongodb中的集合名字相等（首字母大写）

   /* private String id;//如果属性上未使用@Id注解，属性名需要为id
    private String articleId;//文章Id
    private String content;//评论内容
    @Indexed//使用注解添加单值索引
    private String userId;//评论人ID
    private String nickname;//评论人昵称
    private String createDateTime;//评论日期
    private String likeNum;//点赞数
    private String replyNum;//回复数
    private String state;//状态 0不可见1为可见
    private String parentId;//上级ID，如果为0表示文章的顶级评论

    public String getId() {
        return id;
    }

    public void setId(String _id) {
        this.id = id;
    }

    public String getArticleId() {
        return articleId;
    }

    public void setArticleId(String articleId) {
        this.articleId = articleId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getCreateDateTime() {
        return createDateTime;
    }

    public void setCreateDateTime(String createDateTime) {
        this.createDateTime = createDateTime;
    }

    public String getLikeNum() {
        return likeNum;
    }

    public void setLikeNum(String likeNum) {
        this.likeNum = likeNum;
    }

    public String getReplyNum() {
        return replyNum;
    }

    public void setReplyNum(String replyNum) {
        this.replyNum = replyNum;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }*/
}
