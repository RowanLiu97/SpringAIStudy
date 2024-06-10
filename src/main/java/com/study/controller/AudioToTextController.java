package com.study.controller;

import jakarta.annotation.Resource;
import org.springframework.ai.openai.OpenAiAudioSpeechModel;
import org.springframework.ai.openai.OpenAiAudioSpeechOptions;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.ai.openai.OpenAiAudioTranscriptionOptions;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.ai.openai.audio.speech.SpeechPrompt;
import org.springframework.ai.openai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.core.io.FileSystemResource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;

@RestController
@RequestMapping("audioAi")
public class AudioToTextController {
    @Resource
    private OpenAiAudioTranscriptionModel openAiAudioTranscriptionModel;

    @Resource
    private OpenAiAudioSpeechModel openAiAudioSpeechModel;


    @GetMapping("audioToText")
    public String audioToText() {

        FileSystemResource fileSystemResource = new FileSystemResource("src/main/resources/happySample.wav");
        AudioTranscriptionPrompt prompt = new AudioTranscriptionPrompt(fileSystemResource, OpenAiAudioTranscriptionOptions.builder()
                .withLanguage("zh")
                .withTemperature(0f)
                .withModel("whisper-1")
                .withResponseFormat(OpenAiAudioApi.TranscriptResponseFormat.TEXT)
                .build());

        // 存在问题 "message":"Could not parse multipart form， curl是没问题的
        return openAiAudioTranscriptionModel.call(prompt).getResult().getOutput();
    }

    @GetMapping("TextToAudio")
    public String TextToAudio(@RequestParam("msg") String msg) {
        SpeechPrompt speechPrompt = new SpeechPrompt(msg, OpenAiAudioSpeechOptions.builder()
                .withModel(OpenAiAudioApi.TtsModel.TTS_1.value)
                .withResponseFormat(OpenAiAudioApi.SpeechRequest.AudioResponseFormat.MP3)
                .withSpeed(1.0f)
                .build());
        // 语音文件的二进制
        byte[] bytes = openAiAudioSpeechModel.call(speechPrompt).getResult().getOutput();
        // 将语音文件保存到本地
        save2File("src/main/resources/output.mp3", bytes);
        return "OK";
    }

    public static boolean save2File(String fname, byte[] msg){
        OutputStream fos = null;
        try{
            File file = new File(fname);
            File parent = file.getParentFile();
            boolean bool;
            if ((!parent.exists()) &&
                    (!parent.mkdirs())) {
                return false;
            }
            fos = new FileOutputStream(file);
            fos.write(msg);
            fos.flush();
            return true;
        }catch (FileNotFoundException e){
            return false;
        }catch (IOException e){
            File parent;
            return false;
        }
        finally{
            if (fos != null) {
                try{
                    fos.close();
                }catch (IOException e) {}
            }
        }
    }

}
