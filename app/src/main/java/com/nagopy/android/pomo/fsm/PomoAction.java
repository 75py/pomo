package com.nagopy.android.pomo.fsm;


public interface PomoAction {
    void init();

    void resetTimerCount();

    void startWorkTimer();

    void startShortBreakTimer();

    void startLongBreakTimer();

    void stopTimer();

    void layout_IDLE();

    void layout_RUNNING();

    void layout_PAUSE();

    void layout_FINISHED();

    void layout_SHORT_BREAKING();

    void layout_LONG_BREAKING();

    void throwIllegalStateException();

    void showAds();

    void hideAds();

    void showNavigation();

    void hideNavigation();
}
