package com.pixelrpg.game.intro;

import com.badlogic.gdx.graphics.Pixmap;

public interface IntroPlaybackService {
    IntroPlaybackService NO_OP = new IntroPlaybackService() {
        @Override
        public boolean isAvailable() {
            return false;
        }

        @Override
        public boolean start(Runnable onComplete) {
            return false;
        }

        @Override
        public boolean isReady() {
            return false;
        }

        @Override
        public int getFrameWidth() {
            return 0;
        }

        @Override
        public int getFrameHeight() {
            return 0;
        }

        @Override
        public boolean uploadCurrentFrame(Pixmap pixmap) {
            return false;
        }

        @Override
        public float getPlaybackPositionSeconds() {
            return 0f;
        }

        @Override
        public float getDurationSeconds() {
            return 0f;
        }

        @Override
        public void skip() {
        }

        @Override
        public void dispose() {
        }
    };

    boolean isAvailable();

    boolean start(Runnable onComplete);

    boolean isReady();

    int getFrameWidth();

    int getFrameHeight();

    boolean uploadCurrentFrame(Pixmap pixmap);

    float getPlaybackPositionSeconds();

    float getDurationSeconds();

    void skip();

    void dispose();
}
