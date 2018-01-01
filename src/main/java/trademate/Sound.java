/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package trademate;

import javafx.scene.media.AudioClip;

import viewtify.Viewtify;

/**
 * @version 2018/01/01 19:43:29
 */
public enum Sound {

    なし, 追加, 注目, クリア, 来た, 完了, 削除, 下, エラー, 失敗, ゴール, 行くよ, スタート, 成功, 何か不安定みたい, 上, がーん, ばばーん, じゃじゃーん, 始めるよ, ブレークポイント, やったー, うわーん;

    /**
     * 
     */
    private Sound() {

    }

    public void play() {
        if (this != なし) {
            Viewtify.inUI(() -> {
                new AudioClip(ClassLoader.getSystemResource("sound/" + name() + ".m4a").toExternalForm()).play();
            });
        }
    }
}
