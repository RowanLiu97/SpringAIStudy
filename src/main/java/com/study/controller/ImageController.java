package com.study.controller;

import com.alibaba.fastjson.JSON;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.image.*;
import org.springframework.ai.openai.OpenAiImageModel;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("image")
@Slf4j
public class ImageController {

    @Resource
    private OpenAiImageModel openAiImageModel;

    @GetMapping("simple")
    public Image image(@RequestParam(value = "msg") String msg) {
        ImagePrompt imagePrompt = new ImagePrompt(msg, OpenAiImageOptions.builder()
                .withModel("dall-e-3")
                .withQuality("hd")  // 高清图片
                .withN(1)   // 生成图片数（有些模型是不支持多张）
                .build());

        ImageResponse callResult = openAiImageModel.call(imagePrompt);
        Image output = callResult.getResult().getOutput();
        log.info("output: {}", JSON.toJSONString(output));
        return output;
    }
}
