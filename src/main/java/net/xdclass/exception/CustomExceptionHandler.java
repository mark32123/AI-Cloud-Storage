package net.xdclass.exception;

import lombok.extern.slf4j.Slf4j;
import net.xdclass.util.JsonData;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * 小滴课堂,愿景：让技术不再难学
 *
 * @Description
 * @Author 二当家小D
 * @Remark 有问题直接联系我，源码-笔记-技术交流群,官网 https://xdclass.net
 * @Version 1.0
 **/
@ControllerAdvice
@Slf4j
public class CustomExceptionHandler {

    @ExceptionHandler(value = MethodArgumentTypeMismatchException.class)
    @ResponseBody
    public JsonData handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException e) {
        log.error("[参数类型不匹配异常] 参数名: {}, 期望类型: {}, 实际值: {}", 
            e.getName(), e.getRequiredType().getSimpleName(), e.getValue());
        return JsonData.buildError("参数类型错误: " + e.getName() + " 应该是 " + e.getRequiredType().getSimpleName() + " 类型");
    }

    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public JsonData handler(Exception e){

        if(e instanceof BizException bizException){
            log.error("[业务异常]{}",e);
            return JsonData.buildCodeAndMsg(bizException.getCode(),bizException.getMsg());
        }else {
            log.error("[系统异常]{}",e);
            return JsonData.buildError("系统异常");
        }

    }

}