package net.xdclass.service;

import com.alibaba.dashscope.aigc.generation.GenerationResult;

public interface ChatService {

    /**
     * 调用大模型接口
     * @param input
     * @return
     * @throws Exception
     */
    GenerationResult callWithMessage(String input)throws Exception;

}
