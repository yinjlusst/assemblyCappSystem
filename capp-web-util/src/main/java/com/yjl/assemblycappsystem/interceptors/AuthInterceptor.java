package com.yjl.assemblycappsystem.interceptors;

import com.alibaba.fastjson.JSON;
import com.yjl.assemblycappsystem.annotations.LoginRequired;
import com.yjl.assemblycappsystem.util.CookieUtil;

import com.yjl.assemblycappsystem.util.HttpclientUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@Component//@Component注解将这个类注入bean
public class AuthInterceptor extends HandlerInterceptorAdapter {

    /**
     * 登录授权的拦截器的业务逻辑
     * 1.查看当前请求是否需要登录，不需要登录直接放行
     * 2.需要登录的获取token，进行校验，校验成功的更新cookie，存入用户信息，放行
     * 3.校验不成功的跳转到认证中心
     *
     * @param request
     * @param response
     * @param handle
     * @return
     * @throws Exception
     */
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handle) throws Exception {
        //通过反射判断当前方法有没有注解@LoginRequired


        /*
         * HandlerMethod介绍
         * HandlerMethod 封装方法定义相关的信息,如类,方法,参数等.
         * Spring MVC应用启动时会搜集并分析每个Web控制器方法
         * HandlerMethod在  访问请求方法  的时候可以方便的访问到方法、也就是获取当前请求的
         * 方法参数、方法上的注解、所属类等并且对方法参数封装处理，
         * 也可以方便的访问到方法参数的注解等信息。
         */
        HandlerMethod hm = (HandlerMethod) handle;
        //getMethodAnnotation可以获取方法上的注解
        LoginRequired methodAnnotation = hm.getMethodAnnotation(LoginRequired.class);
        //如果有的话就直接放行
        if (methodAnnotation != null) {
            //没有必要拦截
            return true;
        }

        //没有的话需要拦截进行身份验证
        String token = "";

        //获取cookie中可能有的那个token，cookie
        String oldToken = CookieUtil.getCookieValue(request, "oldToken", true);
        if (StringUtils.isNotBlank(oldToken)) {
            token = oldToken;
        }

        //获取可能拼接在url后面的token
        String newToken = request.getParameter("token");
        if (StringUtils.isNotBlank(newToken)) {
            token = newToken;
        }


        //在校验之前，由于jwt的解析需要用到ip，所以要获取到请求用户的ip
        String ip = request.getHeader("x-forwarded-for");
        if (StringUtils.isBlank(ip)) {
            ip = request.getRemoteAddr();//从request中获取ip
        }
        if (StringUtils.isBlank(ip)) {
            //请求出现问题或者为非法请求,返回error页面
            response.sendRedirect("http://passport.capp.com:7000/passport/error");
            return false;
        }
        if (ip.equals("0:0:0:0:0:0:0:1")){
            ip = "127.0.0.1";
        }


        if (StringUtils.isBlank(token)) {
            //没有获取到token
            //登录失败

            //跳转到认证中心
            try {
                goToPassportCenter(request, response);
            } catch (Exception e) {
                e.printStackTrace();
                response.sendRedirect("http://passport.capp.com:7000/passport/error");
            }
            return false;
        }


        // 这里需要调用passport-web的controller层中的verify方法校验token
        // 使用的是jwt校验，而不是访问数据库，所以
        // 使用httpclient发送http请求到web层
        Map<String, String> verifyInfoMapper = new HashMap<>();
        String verifySuccess = "fail";
        if (StringUtils.isNotBlank(token)) {
            //验证方法返回的结果为json,包含 是否成功 和 用户信息
            String verifySuccessJson = HttpclientUtil.doGet("http://passport.capp.com:7000/passport/verify?token=" + token + "&ip=" + ip);
            verifyInfoMapper = JSON.parseObject(verifySuccessJson, Map.class);
            verifySuccess = verifyInfoMapper.get("status");
        }

        if (verifySuccess.equals("success")) {
            //登录成功
            //更新cookie,存入用户信息，放行
            request.setAttribute("employeeId", verifyInfoMapper.get("employeeId"));
            request.setAttribute("username", verifyInfoMapper.get("username"));
            request.setAttribute("id",verifyInfoMapper.get("id"));
            if (StringUtils.isNotBlank(token)) {
                CookieUtil.setCookie(request, response, "oldToken", token, 60 * 60 * 2, true);
            }

            return true;

        } else {
            //登录失败，返回passport认证中心验证
            //跳转到认证中心
            try {
                goToPassportCenter(request, response);
            } catch (Exception e) {
                e.printStackTrace();
                response.sendRedirect("http://passport.capp.com:7000/passport/error");
            }
            return false;
        }
    }

    private void goToPassportCenter(HttpServletRequest request, HttpServletResponse response) throws Exception {
        StringBuffer returnUrl = request.getRequestURL();
        if (StringUtils.isBlank(returnUrl)) {
            //returnUrl默认为首页，这边先随便写一个http://127.0.0.1:7001/index
            returnUrl = new StringBuffer("http://passport.capp.com:7001/manage/index");
        }
        response.sendRedirect("http://passport.capp.com:7000/passport/index?returnUrl=" + returnUrl);
    }
}
