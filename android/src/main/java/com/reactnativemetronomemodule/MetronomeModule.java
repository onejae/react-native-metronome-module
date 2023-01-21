package com.reactnativemetronomemodule;

import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Promise;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.media.SoundPool;
import android.media.AudioAttributes;

import androidx.annotation.NonNull;

public class MetronomeModule extends ReactContextBaseJavaModule implements LifecycleEventListener {

  /** === Private members ================================================== */
  private final ReactApplicationContext reactContext;

  private int bpm = 100;
  private boolean shouldPauseOnLostFocus = true;

  private enum metronomeState {
    PLAYING,
    PAUSED,
    STOPPED
  }

  private metronomeState currentState = metronomeState.STOPPED;

  private SoundPool soundPool;

  private final ScheduledThreadPoolExecutor scheduledExecutor = new ScheduledThreadPoolExecutor(1);
  private ScheduledFuture scheduledFuture;

  private int soundId1;
  private int soundId2;
  private List<Integer> soundIds;


  private final Runnable tok = new Runnable() {
    @Override
    public void run() {
      soundPool.play(1, 1, 1, 1, 0, 1.0f);
    }
  };

  /** === Public constructor =============================================== */
  MetronomeModule(ReactApplicationContext context) {
    super(context);
    this.reactContext = context;
    this.reactContext.addLifecycleEventListener(this);

    initializeSoundPool();
  }

  /** === Private methods ================================================== */
  private int getIntervalMS() {
    return 60000 / bpm;
  }

  private void initializeSoundPool() {
    // Use the new SoundPool builder on newer version of android
    soundIds = new ArrayList<>();

    this.soundPool = new SoundPool.Builder()
        .setMaxStreams(4)
        .setAudioAttributes(new AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build())
        .build();



//    int resId1 = this.reactContext.getResources().getIdentifier("firstbeat",
//        "raw", this.reactContext.getPackageName());
//    int resId2 = this.reactContext.getResources().getIdentifier("secondbeat",
//        "raw",
//        this.reactContext.getPackageName());
//    soundId1 = this.soundPool.load(this.reactContext, resId1, 1);
//    soundId2 = this.soundPool.load(this.reactContext, resId2, 2);
  }

  /** === Host lifecycle hooks ============================================= */
  @Override
  public void onHostResume() {
    // Activity `onResume`
    if (this.currentState == metronomeState.PAUSED)
      start();
  }

  @Override
  public void onHostPause() {
    // Activity `onPause`
    if (this.currentState == metronomeState.PLAYING && this.shouldPauseOnLostFocus) {
      this.stop();
      this.currentState = metronomeState.PAUSED;
    }
  }

  @Override
  public void onHostDestroy() {
    // Activity `onDestroy`
    stop();
  }

  /** === React Methods ==================================================== */
  @ReactMethod
  public void start() {
    if (this.currentState != metronomeState.PLAYING) {
      this.scheduledExecutor.setRemoveOnCancelPolicy(true);
      this.scheduledFuture = scheduledExecutor.scheduleAtFixedRate(this.tok, 0, this.getIntervalMS(),
          TimeUnit.MILLISECONDS);

      this.currentState = metronomeState.PLAYING;
    }
  }

  @ReactMethod
  public void stop() {
    if (this.currentState == metronomeState.PLAYING) {
      this.scheduledFuture.cancel(false);
      this.currentState = metronomeState.STOPPED;
    }
  }

  @ReactMethod
  public void setBPM(int newBPM) {
    this.bpm = newBPM;

    // If currently playing, need to restart to pick up the new BPM
    if (this.currentState == metronomeState.PLAYING) {
      this.stop();
      this.start();
    }
  }

  @ReactMethod
  public void getBPM(Promise promise) {
    promise.resolve(this.bpm);
  }

  @ReactMethod
  public void setShouldPauseOnLostFocus(boolean shouldPause) {
    this.shouldPauseOnLostFocus = shouldPause;
  }

  @ReactMethod
  public void getShouldPauseOnLostFocus(Promise promise) {
    promise.resolve(this.shouldPauseOnLostFocus);
  }

  @ReactMethod
  public void isPlaying(Promise promise) {
    promise.resolve(this.currentState == metronomeState.PLAYING);
  }

  @ReactMethod
  public void isPaused(Promise promise) {
    promise.resolve(this.currentState == metronomeState.PAUSED);
  }

  @ReactMethod
  public void playSound(int idx) {
    if (idx >= 0 && idx < soundIds.size()) {
      int soundId = soundIds.get(idx);
      soundPool.play(soundId, 1, 1, 1, 0, 1);
    }
  }

  @ReactMethod
  public void loadSound(int idx, String resource) {
    int resId = this.reactContext.getResources().getIdentifier(resource,
            "raw", this.reactContext.getPackageName());
    int soundId = this.soundPool.load(this.reactContext, resId, 1);
    soundIds.add(soundId);
  }

  /** === Public methods =================================================== */
  @NonNull
  @Override
  public String getName() {
    return "MetronomeModule";
  }

}
