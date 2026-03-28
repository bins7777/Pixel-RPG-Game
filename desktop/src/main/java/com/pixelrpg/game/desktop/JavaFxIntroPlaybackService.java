package com.pixelrpg.game.desktop;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import com.badlogic.gdx.graphics.Pixmap;
import com.pixelrpg.game.intro.IntroPlaybackService;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;

public final class JavaFxIntroPlaybackService implements IntroPlaybackService {
    private static final AtomicBoolean TOOLKIT_STARTED = new AtomicBoolean(false);
    private static final long FRAME_CAPTURE_INTERVAL_NANOS = 33_333_333L;

    private final Object frameLock = new Object();
    private final String videoAbsolutePath;
    private final int windowWidth;
    private final int windowHeight;
    private final AtomicBoolean playbackActive = new AtomicBoolean(false);
    private final AtomicBoolean finishing = new AtomicBoolean(false);

    private Scene scene;
    private StackPane root;
    private MediaPlayer mediaPlayer;
    private MediaView mediaView;
    private AnimationTimer frameCaptureTimer;
    private WritableImage snapshotImage;
    private int[] frontBuffer;
    private int[] backBuffer;
    private volatile boolean ready = false;
    private volatile int frameWidth = 0;
    private volatile int frameHeight = 0;
    private volatile float playbackPositionSeconds = 0f;
    private volatile float durationSeconds = 0f;
    private long frameRevision = 0L;
    private long consumedRevision = -1L;
    private Runnable completionCallback;

    public JavaFxIntroPlaybackService(String videoAbsolutePath, String windowTitle, int windowWidth, int windowHeight) {
        this.videoAbsolutePath = videoAbsolutePath == null ? "" : videoAbsolutePath.trim();
        this.windowWidth = Math.max(640, windowWidth);
        this.windowHeight = Math.max(360, windowHeight);
    }

    @Override
    public boolean isAvailable() {
        return !videoAbsolutePath.isBlank() && new File(videoAbsolutePath).isFile();
    }

    @Override
    public boolean start(Runnable onComplete) {
        if (!isAvailable() || onComplete == null) {
            return false;
        }
        if (!ensureToolkitStarted()) {
            return false;
        }
        if (!playbackActive.compareAndSet(false, true)) {
            return false;
        }

        completionCallback = onComplete;
        finishing.set(false);
        ready = false;
        frameWidth = windowWidth;
        frameHeight = windowHeight;
        synchronized (frameLock) {
            frontBuffer = null;
            backBuffer = null;
            frameRevision = 0L;
            consumedRevision = -1L;
        }

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                initializePlayback();
            }
        });
        return true;
    }

    @Override
    public boolean isReady() {
        return ready;
    }

    @Override
    public int getFrameWidth() {
        return frameWidth;
    }

    @Override
    public int getFrameHeight() {
        return frameHeight;
    }

    @Override
    public boolean uploadCurrentFrame(Pixmap pixmap) {
        if (pixmap == null || !ready) {
            return false;
        }

        synchronized (frameLock) {
            if (frontBuffer == null || frameRevision == consumedRevision) {
                return false;
            }
            if (pixmap.getWidth() != frameWidth || pixmap.getHeight() != frameHeight) {
                return false;
            }

            ByteBuffer pixels = pixmap.getPixels();
            pixels.clear();
            for (int argb : frontBuffer) {
                pixels.put((byte) ((argb >> 16) & 0xFF));
                pixels.put((byte) ((argb >> 8) & 0xFF));
                pixels.put((byte) (argb & 0xFF));
                pixels.put((byte) ((argb >> 24) & 0xFF));
            }
            pixels.flip();
            consumedRevision = frameRevision;
            return true;
        }
    }

    @Override
    public float getPlaybackPositionSeconds() {
        return playbackPositionSeconds;
    }

    @Override
    public float getDurationSeconds() {
        return durationSeconds;
    }

    @Override
    public void skip() {
        if (!playbackActive.get()) {
            return;
        }
        if (Platform.isFxApplicationThread()) {
            finishPlayback(true);
            return;
        }
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                finishPlayback(true);
            }
        });
    }

    @Override
    public void dispose() {
        if (!TOOLKIT_STARTED.get()) {
            playbackActive.set(false);
            ready = false;
            return;
        }
        if (Platform.isFxApplicationThread()) {
            finishPlayback(false);
            return;
        }
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                finishPlayback(false);
            }
        });
    }

    private boolean ensureToolkitStarted() {
        if (TOOLKIT_STARTED.get()) {
            return true;
        }

        CountDownLatch startupLatch = new CountDownLatch(1);
        if (TOOLKIT_STARTED.compareAndSet(false, true)) {
            try {
                Platform.startup(new Runnable() {
                    @Override
                    public void run() {
                        startupLatch.countDown();
                    }
                });
            } catch (IllegalStateException ignored) {
                startupLatch.countDown();
            } catch (Exception error) {
                TOOLKIT_STARTED.set(false);
                return false;
            }
        } else {
            startupLatch.countDown();
        }

        try {
            startupLatch.await();
            return true;
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private void initializePlayback() {
        try {
            root = new StackPane();
            root.setStyle("-fx-background-color: black;");
            root.setPrefSize(windowWidth, windowHeight);
            root.setMinSize(windowWidth, windowHeight);
            root.setMaxSize(windowWidth, windowHeight);

            scene = new Scene(root, windowWidth, windowHeight, Color.BLACK);

            Media media = new Media(new File(videoAbsolutePath).toURI().toString());
            mediaPlayer = new MediaPlayer(media);
            mediaView = new MediaView(mediaPlayer);
            mediaView.setPreserveRatio(true);
            mediaView.setFitWidth(windowWidth);
            mediaView.setFitHeight(windowHeight);
            root.getChildren().setAll(mediaView);
            root.applyCss();
            root.layout();

            mediaPlayer.setOnReady(new Runnable() {
                @Override
                public void run() {
                    ready = true;
                    frameWidth = windowWidth;
                    frameHeight = windowHeight;
                    durationSeconds = (float) mediaPlayer.getTotalDuration().toSeconds();
                    playbackPositionSeconds = 0f;
                    ensureFrameBuffers(frameWidth, frameHeight);
                    startFrameCaptureLoop();
                    mediaPlayer.play();
                }
            });
            mediaPlayer.setOnEndOfMedia(new Runnable() {
                @Override
                public void run() {
                    finishPlayback(true);
                }
            });
            mediaPlayer.setOnError(new Runnable() {
                @Override
                public void run() {
                    finishPlayback(true);
                }
            });
            media.setOnError(new Runnable() {
                @Override
                public void run() {
                    finishPlayback(true);
                }
            });
        } catch (MediaException error) {
            finishPlayback(true);
        } catch (RuntimeException error) {
            finishPlayback(true);
        }
    }

    private void startFrameCaptureLoop() {
        if (frameCaptureTimer != null) {
            frameCaptureTimer.stop();
        }
        frameCaptureTimer = new AnimationTimer() {
            private long lastCaptureTime = 0L;

            @Override
            public void handle(long now) {
                if (!ready) {
                    return;
                }
                if (lastCaptureTime != 0L && now - lastCaptureTime < FRAME_CAPTURE_INTERVAL_NANOS) {
                    return;
                }
                lastCaptureTime = now;
                if (mediaPlayer != null) {
                    playbackPositionSeconds = (float) mediaPlayer.getCurrentTime().toSeconds();
                    durationSeconds = (float) mediaPlayer.getTotalDuration().toSeconds();
                }
                captureCurrentFrame();
            }
        };
        frameCaptureTimer.start();
    }

    private void captureCurrentFrame() {
        if (root == null) {
            return;
        }

        if (snapshotImage == null
                || (int) snapshotImage.getWidth() != frameWidth
                || (int) snapshotImage.getHeight() != frameHeight) {
            snapshotImage = new WritableImage(frameWidth, frameHeight);
        }

        root.applyCss();
        root.layout();
        WritableImage image = root.snapshot(new SnapshotParameters(), snapshotImage);
        PixelReader pixelReader = image.getPixelReader();
        if (pixelReader == null) {
            return;
        }

        ensureFrameBuffers(frameWidth, frameHeight);
        pixelReader.getPixels(0, 0, frameWidth, frameHeight,
                PixelFormat.getIntArgbInstance(), backBuffer, 0, frameWidth);

        synchronized (frameLock) {
            int[] swap = frontBuffer;
            frontBuffer = backBuffer;
            backBuffer = swap;
            frameRevision++;
        }
    }

    private void ensureFrameBuffers(int width, int height) {
        int size = width * height;
        synchronized (frameLock) {
            if (frontBuffer == null || frontBuffer.length != size) {
                frontBuffer = new int[size];
                backBuffer = new int[size];
                frameRevision = 0L;
                consumedRevision = -1L;
            } else if (backBuffer == null || backBuffer.length != size) {
                backBuffer = new int[size];
            }
        }
    }

    private void finishPlayback(boolean invokeCallback) {
        if (!playbackActive.get() && !finishing.get()) {
            return;
        }
        if (!finishing.compareAndSet(false, true)) {
            return;
        }

        Runnable callback = invokeCallback ? completionCallback : null;
        completionCallback = null;

        if (frameCaptureTimer != null) {
            frameCaptureTimer.stop();
            frameCaptureTimer = null;
        }
        if (mediaPlayer != null) {
            try {
                mediaPlayer.stop();
            } catch (Exception ignored) {
            }
            try {
                mediaPlayer.dispose();
            } catch (Exception ignored) {
            }
            mediaPlayer = null;
        }

        mediaView = null;
        scene = null;
        root = null;
        snapshotImage = null;
        ready = false;
        playbackPositionSeconds = 0f;
        durationSeconds = 0f;
        playbackActive.set(false);
        finishing.set(false);

        synchronized (frameLock) {
            frontBuffer = null;
            backBuffer = null;
            frameRevision = 0L;
            consumedRevision = -1L;
        }

        if (callback != null) {
            callback.run();
        }
    }
}
