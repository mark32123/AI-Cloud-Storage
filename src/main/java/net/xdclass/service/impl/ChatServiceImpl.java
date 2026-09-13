package net.xdclass.service.impl;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import lombok.extern.slf4j.Slf4j;
import net.xdclass.service.ChatService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;

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
public class ChatServiceImpl implements ChatService {

    @Value("${ai.key}")
    private String aiKey;


    @Override
    public GenerationResult callWithMessage(String input) throws Exception {
        //初始化大模型生成器
        Generation gen = new Generation();

        //构建系统角色消息
        Message systemMsg = Message.builder()
                .role(Role.SYSTEM.getValue())
                .content("你是小滴课堂AI机器人，名字叫隔壁老王老王")
                .build();

        //构建用户消息
        Message userMsg = Message.builder()
                .role(Role.USER.getValue())
                .content(input)
                .build();

        //构建请求参数，配置模型的其他关键参数和对话上下文
        GenerationParam param = GenerationParam.builder()
                .model(Generation.Models.QWEN_TURBO)
                .messages(Arrays.asList(systemMsg, userMsg)) //上下文
                .resultFormat(GenerationParam.ResultFormat.MESSAGE) //配置返回格式
                .temperature(0.3f) //配置随机性的温度
                .apiKey(aiKey)
                .build();

        //执行模型调用并且返回结果
        return gen.call(param);
    }
}