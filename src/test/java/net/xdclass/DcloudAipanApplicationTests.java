package net.xdclass;

import com.alibaba.dashscope.aigc.generation.GenerationOutput;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.xdclass.service.ChatService;
import net.xdclass.util.JsonUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest
@Slf4j
class DcloudAipanApplicationTests {


    @Autowired
    private ChatService chatService;

    @Test
    @SneakyThrows
    public void testAI() {
        GenerationResult generationResult = chatService.callWithMessage("你是谁，帮我写个Java的冒泡排序");
        GenerationOutput output = generationResult.getOutput();
        String content = output.getChoices().get(0).getMessage().getContent();
        System.out.println(content);
        System.exit(0);

    }


    @Test
    public void test1() {
        //Flux<String> just = Flux.just("1", "2", "3");
//		just.subscribe(System.out::println);

        //Flux.range(1, 10).subscribe(System.out::println);
        //Flux.range(1, 10).filter(n->n%2==0).subscribe(System.out::println);

        //Mono
        //Mono.just("hello").subscribe(System.out::println);

    }

    /**
     * 使用Reactor的Flux从不同数据源创建响应式流
     * 本方法展示两种创建Flux的常用方式：
     * 1. 从List集合创建Flux流
     * 2. 从数组创建Flux流
     * 无参数
     * 无返回值
     */

    @Test
    public void test2() {
        // 从List集合创建Flux并订阅消费
        List<String> list = Arrays.asList("Java", "Python");
        Flux<String> fluxFromList = Flux.fromIterable(list);
        fluxFromList.subscribe(System.out::println);

        // 从数组创建Flux并订阅消费
        String[] array = {"Apple", "Banana"};
        Flux<String> fluxFromArray = Flux.fromArray(array);
        fluxFromArray.subscribe(System.out::println);
    }


    @Test
    public void testWebClient(){
        Map<String,Object> map = new HashMap<>();
        map.put("url","https://www.runoob.com/python3/python3-function.html");
        map.put("summary_type","简洁点");
        map.put("language","中文");

        String json = JsonUtil.obj2Json(map);

        log.info("json:{}",json);

        //创建webClient
        WebClient webClient = WebClient.builder()
                .baseUrl("http://localhost:8000")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.TEXT_EVENT_STREAM_VALUE)
                .build();

        //发送post请求
        Flux<String> response = webClient.post()
                .uri("/api/document/stream")
                .bodyValue(json)
                .retrieve()
                .bodyToFlux(String.class);

        response.subscribe(chunk->{
            log.info("chunk:{}",chunk);
        },error->{
            log.error("error:{}",error);

        },()->{
            log.info("complete请求完成");
        });

        //等待响应完成，可以设置比较长的时间
        response.blockLast(Duration.ofSeconds(60));

    }


}
