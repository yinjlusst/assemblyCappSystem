package com.yjl.assemblycappsystem.config;

import com.yjl.assemblycappsystem.interceptors.AuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;


@Configuration
public class WebMvcConfiguration extends WebMvcConfigurerAdapter {
    @Autowired
    AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //配置需要拦截的路径为/**，并排出/error
        registry.addInterceptor(authInterceptor).addPathPatterns("/**").excludePathPatterns("/error");//必须排除掉error请求
        super.addInterceptors(registry);
    }

}
