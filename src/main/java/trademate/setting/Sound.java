/*
 * Copyright (C) 2021 cointoss Development Team
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

import kiss.I;
import kiss.Signaling;
import viewtify.Viewtify;

public enum Sound {

    Defeat, Dominating, DoubleKill, EnemyDominating, EnemyDoubleKill, EnemyGodlike, EnemyInhibitorIsRespawned, EnemyKillingSpree, EnemyLegendary, EnemyLegendaryKill, EnemyPentaKill, EnemyQuadraKill, EnemyRampage, EnemyTripleKill, EnemyUnstoppable, Godlike, KillingSpree, Legendary, LegendaryKill, MinionHasSpawned, PentaKill, QuadraKill, Rampage, SummonerWasDisconnected, TripleKill, Unstoppable, Victory, WelcomtoSummonersRift, YourTeamDestoryedInhibitor, うわーん, がーん, じゃじゃーん, だよ, ばばーん, やったー, エラー, クリア, ゴール, スタート, ブレークポイント, 上, 下, 何か不安定みたい, 削除, 失敗, 始めるよ, 完了, 成功, 来た, 注目, 行くよ, 追加, なし;

    private final Signaling<Double> ons = new Signaling<>();

    /**
     * 
     */
    private Sound() {
        ons.expose.throttle(1000, TimeUnit.MILLISECONDS).on(Viewtify.UIThread).to(v -> {
            AudioClip clip = new AudioClip(ClassLoader.getSystemResource("sound/" + name() + ".m4a").toExternalForm());
            clip.setVolume(v / 100);
            clip.play();
        });
    }

    /**
     * Play this sound.
     */
    public void play() {
        double volume = I.make(Notificator.class).masterVolume.exact();

        if (this != なし && volume != 0) {
            ons.accept(volume);
        }
    }
}