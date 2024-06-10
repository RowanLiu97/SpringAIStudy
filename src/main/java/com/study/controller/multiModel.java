package com.study.controller;

import org.springframework.ai.chat.messages.Media;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("multiModel")
public class multiModel {

    @Autowired
    private ChatModel chatModel;

    @GetMapping("demoForImg")
    public String multiModelDemoForImg(String msg, String imgUrl) throws IOException {

        var userMessage = new UserMessage(
                msg, // content
                List.of(new Media(MimeTypeUtils.IMAGE_PNG, imgUrl))); // media


        ChatResponse response = chatModel.call(new Prompt(userMessage, OpenAiChatOptions.builder()
                .withModel(OpenAiApi.ChatModel.GPT_4_O.getValue())
                .build()));
        return response.getResult().getOutput().getContent();
    }
}
