/*
 * Copyright (C) 2019 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package trademate.setting;

import java.util.concurrent.TimeUnit;

import javafx.scene.media.AudioClip;

import kiss.Signaling;
import viewtify.Viewtify;

/**
 * @version 2018/03/16 20:13:19
 */
public enum Sound {

    Defeat, Dominating, DoubleKill, EnemyDominating, EnemyDoubleKill, EnemyGodlike, EnemyInhibitorIsRespawned, EnemyKillingSpree, EnemyLegendary, EnemyLegendaryKill, EnemyPentaKill, EnemyQuadraKill, EnemyRampage, EnemyTripleKill, EnemyUnstoppable, Godlike, KillingSpree, Legendary, LegendaryKill, MinionHasSpawned, PentaKill, QuadraKill, Rampage, SummonerWasDisconnected, TripleKill, Unstoppable, Victory, WelcomtoSummonersRift, YourTeamDestoryedInhibitor, うわーん, がーん, じゃじゃーん, だよ, ばばーん, やったー, エラー, クリア, ゴール, スタート, ブレークポイント, 上, 下, 何か不安定みたい, 削除, 失敗, 始めるよ, 完了, 成功, 来た, 注目, 行くよ, 追加, なし;

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

    // public static void main(String[] args) {
    // String value = Locator.directory("src/main/resources/sound")
    // .walkFile()
    // .map(file -> file.base())
    // .scan(Collectors.joining(", ", "", ";"))
    // .to().v;
    // System.out.println(value);
    // }
}
