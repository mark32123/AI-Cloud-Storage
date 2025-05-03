package net.xdclass.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Web客户端配置类，用于配置HTTP客户端相关参数
 *
 * <p>通过@ConfigurationProperties绑定配置文件中的stream前缀配置项</p>
 *
 * @Description Web客户端全局配置（基础URL、流式聊天路径、超时配置、连接池配置）
 * @Author 二当家小D
 * @Remark 有问题直接联系我，源码-笔记-技术交流群,官网 https://xdclass.net
 * @Version 1.0
 **/
@Data
@Configuration
@ConfigurationProperties(prefix = "stream")
public class WebClientConfig {

    /**
     * 服务基础地址（配置项：stream.base-url）
     * <p>示例：http://api.example.com</p>
     */
    private String baseUrl;

    /**
     * 流式聊天接口路径（配置项：stream.chat-stream-path）
     * <p>示例：/api/v1/chat/stream</p>
     */
    private String chatStreamPath;

    /**
     * 超时配置组（配置项：stream.timeout.*）
     */
    private Timeout timeout = new Timeout();

    /**
     * 连接池配置组（配置项：stream.pool.*）
     */
    private Pool pool = new Pool();

    /**
     * 超时配置嵌套类
     * <p>所有时间单位遵循Spring Boot的Duration格式（如：5s、2m）</p>
     */
    @Data
    public static class Timeout {
        /**
         * 连接超时时间（配置项：stream.timeout.connect）
         */
        private Duration connect;

        /**
         * 响应等待超时时间（配置项：stream.timeout.response）
         */
        private Duration response;

        /**
         * 读取超时时间（配置项：stream.timeout.read）
         */
        private Duration read;

        /**
         * 写入超时时间（配置项：stream.timeout.write）
         */
        private Duration write;
    }

    /**
     * 连接池配置嵌套类
     * <p>用于配置HTTP连接池参数</p>
     */
    @Data
    public static class Pool {
        /**
         * 最大连接数（配置项：stream.pool.max-connections）
         */
        private int maxConnections;

        /**
         * 最大空闲时间（配置项：stream.pool.max-idle-time）
         */
        private Duration maxIdleTime;

        /**
         * 连接最大存活时间（配置项：stream.pool.max-life-time）
         */
        private Duration maxLifeTime;

        /**
         * 等待获取连接超时时间（配置项：stream.pool.pending-acquire-timeout）
         */
        private Duration pendingAcquireTimeout;

        /**
         * 后台清理间隔时间（配置项：stream.pool.evict-in-background）
         */
        private Duration evictInBackground;
    }


    @Bean
    public WebClient webClient(){
        // 创建连接池
        ConnectionProvider provider = ConnectionProvider.builder("stream-connection-pool")
                .maxConnections(pool.getMaxConnections())
                .maxIdleTime(pool.getMaxIdleTime())
                .maxLifeTime(pool.getMaxLifeTime())
                .pendingAcquireTimeout(pool.getPendingAcquireTimeout())
                .evictInBackground(pool.getEvictInBackground())
                .build();

        // 配置 HttpClient
        HttpClient httpClient = HttpClient.create(provider)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) timeout.getConnect().toMillis())
                .responseTimeout(timeout.getResponse())
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(timeout.getRead().getSeconds(), TimeUnit.SECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(timeout.getWrite().getSeconds(), TimeUnit.SECONDS))
                );


        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.TEXT_EVENT_STREAM_VALUE)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();

    }


}
