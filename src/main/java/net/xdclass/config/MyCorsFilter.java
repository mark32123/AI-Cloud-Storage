package net.xdclass.config;

/**
 * 小滴课堂,愿景：让技术不再难学
 *
 * @Description
 * @Author 二当家小D
 * @Remark 有问题直接联系我，源码-笔记-技术交流群,官网 https://xdclass.net
 * @Version 1.0
 **/
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class MyCorsFilter {

    @Bean
    public CorsFilter corsFilter() {
        //创建 CORS 配置对象
        CorsConfiguration config = new CorsConfiguration();
        //支持域
        config.addAllowedOriginPattern("*");
        //是否发送 Cookie
        config.setAllowCredentials(true);
        //支持请求方式
        config.addAllowedMethod("*");
        //允许的原始请求头部信息
        config.addAllowedHeader("*");
        //暴露的头部信息
        config.addExposedHeader("*");
        //添加地址映射
        UrlBasedCorsConfigurationSource corsConfigurationSource = new UrlBasedCorsConfigurationSource();
        corsConfigurationSource.registerCorsConfiguration("/**", config);
        //返回 CorsFilter 对象
        return new CorsFilter(corsConfigurationSource);
    }
}