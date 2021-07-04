package com.yjl.assemblycappsystem.passport.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.yjl.assemblycappsystem.bean.UmsUserHeadPortraitUrl;
import com.yjl.assemblycappsystem.passport.mapper.UmsUserHeadPortraitUrlMapper;
import com.yjl.assemblycappsystem.util.ActiveMQUtil;
import com.yjl.assemblycappsystem.util.RedisUtil;
import com.yjl.assemblycappsystem.bean.UmsUserAddinfo;
import com.yjl.assemblycappsystem.bean.UmsUserInfo;
import com.yjl.assemblycappsystem.bean.UmsUserSearchInfo;
import com.yjl.assemblycappsystem.passport.mapper.UmsUserAddinfoMapper;
import com.yjl.assemblycappsystem.passport.mapper.UmsUserInfoMapper;

import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Session;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class UserService implements com.yjl.assemblycappsystem.service.UserService {
    @Autowired
    RedisUtil redisUtil;
    @Autowired
    RedissonClient redissonClient;
    @Autowired
    UmsUserInfoMapper umsUserInfoMapper;
    @Autowired
    UmsUserAddinfoMapper umsUserAddinfoMapper;
    @Autowired
    ActiveMQUtil activeMQUtil;
    @Autowired
    JestClient jestClient;
    @Autowired
    UmsUserHeadPortraitUrlMapper umsUserHeadPortraitUrlMapper;

    /**
     * 用户注册
     * 添加一个新的用户
     * 用户信息（用户名、密码）只与mysql有关
     * 把employeeId和username存入redis
     * @param umsUserInfo
     * @return
     */
    @Override
    public Integer addUser(UmsUserInfo umsUserInfo) {
        int i = umsUserInfoMapper.insertSelective(umsUserInfo);
        if (i <= 0) {
            return -1;
        }
        return i;
    }

    /**
     * 向mongodb中插入用户默认头像
     * @param umsUserInfo
     */
    @Override
    public void addHeadPortraitUrl(UmsUserInfo umsUserInfo,String url) {
        UmsUserHeadPortraitUrl umsUserHeadPortraitUrl = new UmsUserHeadPortraitUrl();
        umsUserHeadPortraitUrl.setHeadPortraitUrl(url);
        umsUserHeadPortraitUrl.setUserId(umsUserInfo.getId());
        umsUserHeadPortraitUrlMapper.insert(umsUserHeadPortraitUrl);
    }

    /**
     * 将用户的username和employeeId加入redis防止重复
     * @param umsUserInfo
     * @return
     */
    @Override
    public String addUsernameAndEmployeeId(UmsUserInfo umsUserInfo){
        Jedis jedis = null;
        //将username和employeeId分别存入redis
        try {
            jedis = redisUtil.getJedis();
            if (jedis != null) {
                jedis.sadd("all:username:set", umsUserInfo.getUsername());
                jedis.sadd("all:employeeId:set", umsUserInfo.getEmployeeId());
            }
            return "success";
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            jedis.close();
        }
        return "fail";

    }


    /**
     * 在ums_user_addinfo表中插入数据
     * 添加用户附加数据
     * 存放在mongodb
     * @param umsUserAddinfo
     * @return
     */
    @Override
    public UmsUserAddinfo addUserAddinfo(UmsUserAddinfo umsUserAddinfo) {
        UmsUserAddinfo insertUmsUserAddinfo = umsUserAddinfoMapper.insert(umsUserAddinfo);
        if (insertUmsUserAddinfo == null) {
            //保存失败
            return null;
        }
        return insertUmsUserAddinfo;
    }


    /**
     * 从ums_user_info表中更新头像
     * 头像信息存储在redis和mongodb中
     */
    @Override
    public UmsUserHeadPortraitUrl renewHeadPortraitsUrl(UmsUserHeadPortraitUrl umsUserHeadPortraitUrl) {
        Jedis jedis = null;

        //查找当前用户的id对应的数据库的id
        UmsUserHeadPortraitUrl umsUserHeadPortraitUrlFind = umsUserHeadPortraitUrlMapper.findByUserId(umsUserHeadPortraitUrl.getUserId());
        //更新mongoDB
        umsUserHeadPortraitUrl.setId(umsUserHeadPortraitUrlFind.getId());
        UmsUserHeadPortraitUrl umsHeadPortraitUrlUpdate = umsUserHeadPortraitUrlMapper.save(umsUserHeadPortraitUrl);

        if (umsHeadPortraitUrlUpdate == null){
            return null;
        }
        //更新redis
        try {
            jedis = redisUtil.getJedis();
            //头像保存2天
            jedis.setex("user:" + umsHeadPortraitUrlUpdate.getUserId() + ":headPortraitsUrl", 60 * 60 * 24 * 2, umsHeadPortraitUrlUpdate.getHeadPortraitUrl());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            jedis.close();
        }

        return umsHeadPortraitUrlUpdate;
    }


    /**
     * 从ums_user_addinfo表中更新信息
     * umsUserAddinfo存储在mongodb中
     * @param umsUserAddinfo
     * @return
     */
    @Override
    public UmsUserAddinfo renewUserAddinfo(UmsUserAddinfo umsUserAddinfo) {
        //从mongodb中获取id
        UmsUserAddinfo umsUserAddinfoFind = umsUserAddinfoMapper.findByUserId(umsUserAddinfo.getUserId());

        //更新
        umsUserAddinfo.setId(umsUserAddinfoFind.getId());
        UmsUserAddinfo umsUserAddinfoUpdate = umsUserAddinfoMapper.save(umsUserAddinfo);

        if (umsUserAddinfoUpdate == null){
            return null;
        }
        return umsUserAddinfoUpdate;

    }

    /**
     * 更新UserInfo表中的信息
     * 直接更新mysql
     * @param umsUserInfo
     */
    @Override
    public void renewUserLastLoginTime(UmsUserInfo umsUserInfo) {

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String nowDate = format.format(Calendar.getInstance().getTime());
        umsUserInfo.setLastLoginDate(nowDate);
        Example example = new Example(UmsUserInfo.class);
        example.createCriteria().andEqualTo("id", umsUserInfo.getId());//建立正则规则，添加条件
        umsUserInfoMapper.updateByExampleSelective(umsUserInfo, example);//第一个参数为修改的部分值组成的对象，其中有些属性为null则表示该项不修改，第二个参数为example

        //新建消息队列发送登录通知邮件
        Connection connection = null;
        Session session = null;
        try {
            //创建session和connection
            //1.建立连接
            connection = activeMQUtil.getConnection();
            connection.start();
            session = connection.createSession(true, 0);
            activeMQUtil.sendText(session,umsUserInfo.getEmail(),"SEND_LOGINNOTE_QUEUE");

        } catch (Exception e) {
            e.printStackTrace();
            try {
                //消息回旋
                session.rollback();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } finally {
            try {
                connection.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }


    /**
     * 使用消息中间件发送消息
     * 更新mysql数据库上次登录时间
     * 发送邮件给用户通知上线
     * 发送短信给用户校验
     * @param idStr
     */
    @Override
    public void renewLoginStatus(String idStr) {
        Connection connection = null;
        Session session = null;
        try {
            //创建session和connection
            //1.建立连接,从连接池获取
            connection = activeMQUtil.getConnection();
            connection.start();
            session = connection.createSession(true, 0);
            activeMQUtil.sendText(session,idStr,"UPDATE_LASTLOGINDATE_QUEUE");


        } catch (Exception e) {
            e.printStackTrace();
            try {
                //消息回旋
                session.rollback();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } finally {
            try {
                connection.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }




    /**
     * 通过id查找头像
     * 如果找到头像返回头像url
     * 如果没找到头像返回默认值defaultHeadPortraitUrl
     * 使用mongodb+redis的存储
     */
    @Override
    public String getHeadPortraitsUrlById(Integer userId, String defaultHeadPortraitsUrl) {
        //先从redis中找，key为user:id:headPortraitUrl
        Jedis jedis = null;
        try {
            jedis = redisUtil.getJedis();
            String headPortraitsUrlFromCache = jedis.get("user:" + userId + ":headPortraitsUrl");
            if (StringUtils.isNotBlank(headPortraitsUrlFromCache)) {
                //redis中找到了
                return headPortraitsUrlFromCache;
            } else {
                //redis中没有，查找mongodb
                UmsUserHeadPortraitUrl umsUserHeadPortraitUrlFind = umsUserHeadPortraitUrlMapper.findByUserId(userId);

                String headPortraitsUrl = umsUserHeadPortraitUrlFind.getHeadPortraitUrl();
                if (StringUtils.isNotBlank(headPortraitsUrl)) {
                    //放入redis
                    jedis.setex("user:" + userId + ":headPortraitsUrl", 60 * 60 * 24, headPortraitsUrl);
                    return headPortraitsUrl;
                } else {
                    jedis.setex("user:" + userId + ":headPortraitsUrl", 60 * 60 * 24, defaultHeadPortraitsUrl);
                    return defaultHeadPortraitsUrl;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            jedis.close();
        }
        return defaultHeadPortraitsUrl;
    }


    /**
     * 通过员工号employeeId模糊查询
     * 查询mysql中的信息
     */
    @Override
    public List<UmsUserSearchInfo> getUserByFuzzyEmployeeId(UmsUserSearchInfo umsUserSearchInfo) {
        List<UmsUserSearchInfo> umsUserSearchInfos = new ArrayList<>();
        //需要先按照用户输入的部分employeeId的值获取到对应的完整username
        UmsUserInfo userInfo1 = new UmsUserInfo();
        userInfo1.setEmployeeId(umsUserSearchInfo.getEmployeeId());
        List<UmsUserInfo> umsUserInfoList = fuzzySelect(userInfo1, "employeeId", userInfo1.getEmployeeId());

        for (UmsUserInfo userInfo : umsUserInfoList) {
            UmsUserSearchInfo umsUserSearchInfo1 = new UmsUserSearchInfo();
            BeanUtils.copyProperties(userInfo,umsUserSearchInfo1);
            umsUserSearchInfos.add(umsUserSearchInfo1);
        }
        return umsUserSearchInfos;
    }



    /**
     *通过用户名username模糊查询
     * 查询mysql中的信息
     */
    @Override
    public List<UmsUserSearchInfo> getUserByFuzzyUsername(UmsUserSearchInfo umsUserSearchInfo) {
        List<UmsUserSearchInfo> umsUserSearchInfos = new ArrayList<>();
        //从ums_user_info表中模糊查询
        UmsUserInfo userInfo1 = new UmsUserInfo();
        userInfo1.setUsername(umsUserSearchInfo.getUsername());
        List<UmsUserInfo> umsUserInfoList = fuzzySelect(userInfo1, "username", userInfo1.getUsername());

        //将UmsUserInfo转换为UmsUserSearchInfo
        for (UmsUserInfo userInfo : umsUserInfoList) {
            UmsUserSearchInfo umsUserSearchInfo1 = new UmsUserSearchInfo();
            BeanUtils.copyProperties(userInfo, umsUserSearchInfo1);
            umsUserSearchInfos.add(umsUserSearchInfo1);
        }
        return umsUserSearchInfos;
    }


    /**
     * 通过某一个条件模糊查询umsUserInfo表
     * 参数2为字段名
     * 参数3为字段所对应的值
     */
    public List<UmsUserInfo> fuzzySelect(UmsUserInfo umsUserInfo,String field,String value){
        Example e = new Example(UmsUserInfo.class);
        e.createCriteria().andLike(field,"%" + value + "%");
        List<UmsUserInfo> umsUserInfoList = umsUserInfoMapper.selectByExample(e);
        return umsUserInfoList;
    }


    /**
     * 按照条件精准查找某一个User
     */
    @Override
    public UmsUserInfo getUser(UmsUserInfo umsUserInfo) {
        UmsUserInfo umsUserInfoFromDB = umsUserInfoMapper.selectOne(umsUserInfo);
        if (umsUserInfoFromDB == null) {
            return null;
        }
        return umsUserInfoFromDB;
    }


    /**
     * 在ums_user_addinfo表中按照用户id查找用户附加数据
     * ums_user_info只使用mongodb存储
     */
    @Override
    public UmsUserAddinfo getUserAddinfoByUserId(Integer userId) {
        //查询mongodb
        UmsUserAddinfo userAddinfoFromDB = umsUserAddinfoMapper.findByUserId(userId);
        if (userAddinfoFromDB == null){
            return null;
        }
        return userAddinfoFromDB;
    }


    /**
     * 查询所有用户,按照关键字（用户名或者工号）
     * 使用es
     * 注意！！！！现在是不会更新的
     * 后面会调用消息队列/脚本等手段定时更新
     * @return
     */
    @Override
    public List<UmsUserSearchInfo> getAllUserFromSearchDB(UmsUserSearchInfo umsUserSearchInfo, Integer page, Integer rowInOnePage){
        //用api执行复杂查询
        List<UmsUserSearchInfo> umsUserSearchInfos = new ArrayList<>();
        String username="";

        //如果不是搜索全部的人，搜索的是部分用户名或者工号，需要先进入redis/sql通过模糊查询匹配到这匹配上的人的完整用户名，再通过用户名去搜索
        if(StringUtils.isNotBlank(umsUserSearchInfo.getEmployeeId())||StringUtils.isNotBlank(umsUserSearchInfo.getUsername())){
            //查询数据库
            UmsUserInfo userInfo = new UmsUserInfo();
            if (StringUtils.isNotBlank(umsUserSearchInfo.getEmployeeId())){
                userInfo.setEmployeeId(umsUserSearchInfo.getUsername());
            }else {
                userInfo.setUsername(umsUserSearchInfo.getUsername());
            }
            UmsUserInfo user = getUser(userInfo);
            if(user == null){
                return umsUserSearchInfos;
            }
            username = user.getUsername();
            umsUserSearchInfo.setUsername(username);
        }


        Integer fromIndex = rowInOnePage*(page-1)+1;



        String dslStr = getSearchDsl(umsUserSearchInfo,fromIndex,rowInOnePage);

        Search search = new Search.Builder(dslStr).addIndex("capp0208").addType("UmsUserInfo").build();
        SearchResult execute = null;
        try{
            execute = jestClient.execute(search);
        }
        catch (Exception ex){
            ex.printStackTrace();
        }

        List<SearchResult.Hit<UmsUserSearchInfo, Void>> hits = execute.getHits(UmsUserSearchInfo.class);
        for (SearchResult.Hit<UmsUserSearchInfo, Void> hit : hits) {
            UmsUserSearchInfo source = hit.source;
            umsUserSearchInfos.add(source);
        }

        return umsUserSearchInfos;
    }




    /**
     * 验证登录
     * 使用redis+mysql
     * @param umsUserInfo 里面包含用户的用户名和密码
     * @return 返回用户的信息
     */
    public UmsUserInfo checkUserNamePasswordByRedis(UmsUserInfo umsUserInfo) {
        Jedis jedis = null;
        RLock lock = redissonClient.getLock("lock");
        //从redis中获取用户信息

        //  在redis中寻找user:用户名+密码:info
        //      如果redis找不到这个key
        //          打开mysql查询
        //              mysql中发现也不对
        //                  密码错误
        //              mysql中发现了user
        //                  将user存入redis
        //      如果redis找到，说明用户名密码正确
        try {
            jedis = redisUtil.getJedis();

            if (jedis != null) {

                String umsInfoStr = jedis.get("user:" + umsUserInfo.getUsername() + umsUserInfo.getPassword() + ":info");

                if (StringUtils.isNotBlank(umsInfoStr)) {
                    //密码正确
                    UmsUserInfo umsUserInfoFromCache = JSON.parseObject(umsInfoStr, UmsUserInfo.class);
                    return umsUserInfoFromCache;
                } else {
                    //缓存中没有查到数据
                    //从sql数据库中查询

                    //设置分布式锁，防止缓存击穿
                    lock.lock();

                    UmsUserInfo umsUserInfoSel = new UmsUserInfo();
                    umsUserInfoSel.setUsername(umsUserInfo.getUsername());
                    umsUserInfoSel.setPassword(umsUserInfo.getPassword());

                    UmsUserInfo umsUserInfoFromDB = umsUserInfoMapper.selectOne(umsUserInfoSel);
                    if (umsUserInfoFromDB != null) {
                        //密码正确
                        //放入redis
                        jedis.setex("user:" + umsUserInfoFromDB.getUsername() + umsUserInfoFromDB.getPassword() + ":info", 60 * 60 * 24, JSON.toJSONString(umsUserInfoFromDB));
                        return umsUserInfoFromDB;
                    } else {
                        //密码错误
                        //放入redis空值，过期时间5s，防止缓存穿透
                        jedis.setex("user:" + umsUserInfo.getUsername() + umsUserInfo.getPassword() + ":info", 5, JSON.toJSONString(""));
                        return null;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            jedis.close();
            lock.unlock();
        }
        return null;
    }

    /**
     * 验证登录
     * 只是用mysql
     * @param umsUserInfo 里面包含用户的用户名和密码
     * @return 返回用户的信息
     */
    @Override
    public UmsUserInfo checkUserNamePassword(UmsUserInfo umsUserInfo) {
        UmsUserInfo umsUserInfoSel = new UmsUserInfo();
        umsUserInfoSel.setUsername(umsUserInfo.getUsername());
        umsUserInfoSel.setPassword(umsUserInfo.getPassword());
        UmsUserInfo umsUserInfoFromDB = umsUserInfoMapper.selectOne(umsUserInfoSel);
        if (umsUserInfoFromDB == null){
            return null;
        }else{
            return umsUserInfoFromDB;
        }
    }

    /**
     * 验证工号是否使用过
     * 使用mysql+redis
     * @param umsUserInfo
     * @return
     */
    @Override
    public String checkEmployeeId(UmsUserInfo umsUserInfo) {
        Jedis jedis = null;

        try {
            jedis = redisUtil.getJedis();
            Set<String> usernames = jedis.smembers("all:employeeId:set");
            if (usernames.contains(umsUserInfo.getUsername())){
                return "fail";
            }
            else {
                return "success";
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            jedis.close();
        }
        return "fail";
    }

    /**
     * 验证用户名是否使用过
     * 使用mysql+redis
     * @param umsUserInfo
     * @return
     */
    @Override
    public String checkUsername(UmsUserInfo umsUserInfo) {
        Jedis jedis = null;
        try {
            jedis = redisUtil.getJedis();
            Set<String> usernames = jedis.smembers("all:username:set");
            if (usernames.contains(umsUserInfo.getUsername())){
                return "fail";
            }
            else {
                return "success";
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            jedis.close();
        }
        return "fail";
    }




    /**
     * 使用redis+mysql获取所有用户
     * @return 获取所有的用户并返回
     */
    @Override
    public List<UmsUserInfo> getAllUser() {
        List<UmsUserInfo> userList = new ArrayList<>();
        //从redis中获取
        Jedis jedis = null;
        try {
            jedis = redisUtil.getJedis();
            if (jedis != null){
                Map<String, String> allUserInfoJson = jedis.hgetAll("allUserInfo");
                if (!allUserInfoJson.isEmpty()){
                    for (Map.Entry<String, String> entry:allUserInfoJson.entrySet()){
                        UmsUserInfo umsUserInfo = JSON.parseObject(entry.getValue(), UmsUserInfo.class);
                        umsUserInfo.setId(Integer.parseInt(entry.getKey()));
                        userList.add(umsUserInfo);
                    }
                }else {
                    //从DB中查询数据
                    userList = umsUserInfoMapper.selectAll();
                    if (userList!= null){
                        //从DB中获取到数据了，添加到redis，结构为hash结构
                        for (UmsUserInfo umsUserInfo : userList) {
                            //没有必要将密码保存
                            umsUserInfo.setPassword("");
                            jedis.hset("allUserInfo",umsUserInfo.getId()+"",JSON.toJSONString(umsUserInfo));
                        }
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            jedis.close();
        }

        return userList;
    }

    /**
     * 获取对应的dsl语句
     * @param umsUserSearchInfo
     * @return
     */
    private String getSearchDsl(UmsUserSearchInfo umsUserSearchInfo,Integer startIndex,Integer rowInOnePage) {

        String username = umsUserSearchInfo.getUsername();

        //jest的dsl工具
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        //bool
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        if (StringUtils.isNotBlank(username)){
            TermQueryBuilder termQueryBuilder = new TermQueryBuilder("username",username);
            boolQueryBuilder.filter(termQueryBuilder);
        }

        //query
        searchSourceBuilder.query(boolQueryBuilder);

        //from
        searchSourceBuilder.from(startIndex);
        //size
        searchSourceBuilder.size(rowInOnePage);

        String dslStr= searchSourceBuilder.toString();
        return dslStr;

    }

    @Override
    public void addUserToken(String token, String employeeNumber) {

    }
}
