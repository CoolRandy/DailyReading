package com.example.randy.dailyreading.util;

/**
 * Created by randy on 2016/2/18.
 * 音乐播放状态管理
 */
public class MusicPlayManager {

    private static boolean isPlaying;
    private static boolean isPaused;
    private static boolean isStoped;

    public static boolean isStoped() {
        return isStoped;
    }

    public static void setIsStoped(boolean isStoped) {
        MusicPlayManager.isStoped = isStoped;
    }

    public static boolean isPlaying() {
        return isPlaying;
    }

    public static void setIsPlaying(boolean isPlaying) {
        MusicPlayManager.isPlaying = isPlaying;
    }

    public static boolean isPaused() {
        return isPaused;
    }

    public static void setIsPaused(boolean isPaused) {
        MusicPlayManager.isPaused = isPaused;
    }
}
