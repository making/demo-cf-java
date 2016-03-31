package com.example;

import am.ik.voicetext4j.Speaker;

import java.util.concurrent.ExecutionException;

/**
 * Created by makit on 西暦16/03/31.
 */
public class Hoge {
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        Speaker.SHOW.ready().speak("こんにちは").get();
    }
}
