package net.xdclass.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.xdclass.config.WebClientConfig;
import net.xdclass.service.StreamService;
import net.xdclass.util.JsonData;
import net.xdclass.util.JsonUtil;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;

/**
 * 小滴课堂,愿景：让技术不再难学
 *
 * @Description
 * @Author 二当家小D
 * @Remark 有问题直接联系我，源码-笔记-技术交流群,官网 https://xdclass.net
 * @Version 1.0
 **/
@Service
@Slf4j
//简化@Autowired
@RequiredArgsConstructor
public class StreamServiceImpl implements StreamService {

    private final WebClient webClient;

    private final WebClientConfig webClientConfig;


    @Override
    public Flux<String> handleChatStream(String token, String message) {
        Map<String,Object> body = new HashMap<>();
        body.put("message",message);

        return sendRequest(webClientConfig.getChatStreamPath(),body,token);
    }


    private Flux<String> sendRequest(String path,  Map<String,Object> body,String token){

        try {
            String requestBodyJson = JsonUtil.obj2Json(body);

            WebClient.RequestBodySpec requestBodySpec = webClient.post()
                    .uri(path)
                    .contentType(MediaType.APPLICATION_JSON);

            if(token != null){
                requestBodySpec.header("token",token);
            }
            return requestBodySpec.
                    bodyValue(requestBodyJson)
                    .retrieve()
                    .bodyToFlux(String.class)
                    .doOnError(error->{
                        log.error("error:{}",error);
                    })
                    .doOnComplete(()->{
                        log.info("complete请求完成");
                    });
        }catch (Exception e){
            log.error("error:{}",e);
            return Flux.just("错误请求，请重试");
        }

    }


}