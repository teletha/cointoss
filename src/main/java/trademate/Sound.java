/*
 * Copyright (C) 2018 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package trademate;

import java.util.concurrent.TimeUnit;

import javafx.scene.media.AudioClip;

import kiss.Signaling;
import viewtify.Viewtify;

/**
 * @version 2018/03/16 20:13:19
 */
public enum Sound {

    なし, 追加, 注目, クリア, 来た, 完了, 削除, 下, エラー, 失敗, ゴール, 行くよ, スタート, 成功, 何か不安定みたい, 上, がーん, ばばーん, じゃじゃーん, 始めるよ, ブレークポイント, やったー, うわーん;

    private final Signaling<String> ons = new Signaling<>();

    /**
     * 
     */
    private Sound() {
        ons.expose.throttle(1000, TimeUnit.MILLISECONDS)
                .on(Viewtify.UIThread)
                .map(v -> new AudioClip(ClassLoader.getSystemResource("sound/" + name() + ".m4a").toExternalForm()))
                .to(AudioClip::play);
    }

    /**
     * Play this sound.
     */
    public void play() {
        if (this != なし) {
            ons.accept("PLAY");
        }
    }
}
