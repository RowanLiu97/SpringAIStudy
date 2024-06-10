package com.study.controller;

import com.alibaba.fastjson2.JSON;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("chat")
public class ChatController {
    @Resource
    private OpenAiChatModel openAiChatModel;

    // 历史消息列表
    static List<Message> historyMessage = new ArrayList<>();

    // 历史消息列表的最大长度
    static int maxLen = 3;


    /**
     * 最简单的调用OpenAPI方式
     * @param msg
     * @return
     */
    @GetMapping("simple")
    public String chat(@RequestParam(value = "msg") String msg) {
        log.info("USER：{}", msg);
        String callResult = openAiChatModel.call(msg);
        log.info("AI：{}", callResult);
        return callResult;
    }

    /**
     * 使用Prompt类去调用OpenAPI
     * @param msg
     * @return
     */
    @GetMapping("usePrompt")
    public ChatResponse usePrompt(@RequestParam(value = "msg") String msg){
        Prompt prompt1 = new Prompt(msg);
        ChatResponse chatResponse = openAiChatModel.call(prompt1);
        log.info("AI：{}", JSON.toJSONString(chatResponse));
        return chatResponse;
    }

    /**
     * 使用带OpenAiChatOptions类的Prompt去调用OpenAPI
     * @param msg
     * @return
     */
    @GetMapping("useOptions")
    public ChatResponse userOptions(@RequestParam(value = "msg") String msg){
        Prompt prompt1 = new Prompt(msg, OpenAiChatOptions.builder()
                // 可选参数再代码中配置，会以代码中配置为准，yml文件中的配置会覆盖掉
                .withModel("gpt-3.5-turbo") //gtp的版本
                .withTemperature(0.6F)  // 随机性,控制模型的输出结果的随机性，取值范围是0-1，值越大，输出结果的随机性越高。
                .build());
        ChatResponse chatResponse = openAiChatModel.call(prompt1);
        log.info("AI：{}", JSON.toJSONString(chatResponse));
        return chatResponse;
    }

    /**
     * 流的方式返回
     * @param msg
     * @return
     */
    @GetMapping("useStream")
    public Object useStream(@RequestParam(value = "msg") String msg){
        Prompt prompt = new Prompt(msg, OpenAiChatOptions.builder()
                .withTemperature(0.6F)
                .build());
        Flux<ChatResponse> stream = openAiChatModel.stream(prompt);
        stream.toStream().forEach(chatResponse -> {
            log.info(chatResponse.getResult().getOutput().getContent());
        });
        return stream.collectList();
    }

    @GetMapping("memoryChat")
    public String memoryChat(@RequestParam(value = "msg") String msg){

        // 用户输入的文本是UserMessage
        historyMessage.add(new UserMessage(msg));
        // 发给AI前对历史消息对列的长度进行检查
        if(historyMessage.size() > maxLen){
            log.info("历史消息列表长度超过最大长度，截取前：{}", JSON.toJSONString(historyMessage));
            historyMessage = historyMessage.subList(historyMessage.size()-maxLen,historyMessage.size());
            log.info("历史消息列表长度超过最大长度，截取后：{}", JSON.toJSONString(historyMessage));
        }
        Prompt prompt = new Prompt(historyMessage, OpenAiChatOptions.builder()
                .withTemperature(0.6F)
                .withModel(OpenAiApi.ChatModel.GPT_4_O)
                .build());
        // 获取AssistantMessage
        ChatResponse chatResponse = openAiChatModel.call(prompt);
        AssistantMessage assistantMessage = chatResponse.getResult().getOutput();
        // 将AI回复的消息放到历史消息列表中
        historyMessage.add(assistantMessage);
        return assistantMessage.getContent();
    }

}
