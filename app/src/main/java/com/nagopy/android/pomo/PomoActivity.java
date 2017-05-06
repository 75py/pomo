package com.nagopy.android.pomo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.nagopy.android.pomo.fsm.AdAction;
import com.nagopy.android.pomo.fsm.PomoAction;
import com.nagopy.android.pomo.fsm.gen.AdFsm;
import com.nagopy.android.pomo.fsm.gen.PomoFsm;
import com.nagopy.android.pomo.view.TimerView;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class PomoActivity extends AppCompatActivity implements PomoAction, AdAction {

    @BindView(R.id.timer)
    TimerView timer;

    @BindView(R.id.msg)
    TextView msg;

    @BindView(R.id.start)
    Button start;

    @BindView(R.id.pause)
    Button pause;

    @BindView(R.id.restart)
    Button restart;

    @BindView(R.id.finish)
    Button finish;

    @BindView(R.id.shortRest)
    Button shortRest;

    @BindView(R.id.long_rest)
    Button longRest;

    @BindView(R.id.ad)
    AdView adView;

    @Nullable
    Disposable timerDisposable = null;

    @NonNull
    PomoFsm pomoFsm = new PomoFsm(this);

    @NonNull
    AdFsm adFsm = new AdFsm(this);

    UserSettings userSettings;

    Vibrator vibrator;

    int elapsedSeconds = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_pomo);
        ButterKnife.bind(this);

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        userSettings = new UserSettings(this);

        adFsm.init();
        pomoFsm.init();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onStart() {
        super.onStart();
        pomoFsm.appResume();
    }

    @Override
    protected void onStop() {
        pomoFsm.appPause();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        adFsm.destroy();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.activity_pomo, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.start)
    public void onClickStart() {
        pomoFsm.start();
    }

    @OnClick(R.id.pause)
    public void onClickPause() {
        pomoFsm.pause();
    }

    @OnClick(R.id.restart)
    public void onClickRestart() {
        pomoFsm.restart();
    }

    @OnClick(R.id.shortRest)
    public void onClickShortRest() {
        pomoFsm.shortBreak();
    }

    @OnClick(R.id.long_rest)
    public void onClickLongRest() {
        pomoFsm.longBreak();
    }

    @OnClick(R.id.finish)
    public void onClickFinish() {
        pomoFsm.finish();
    }

    void startTimer(final int sec) {
        Timber.v("startTimer");
        if (timerDisposable != null) {
            stopTimer();
        }
        timer.setSeconds(sec, elapsedSeconds);
        timerDisposable = Observable.interval(1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> {
                    elapsedSeconds++;
                    Timber.v("tick %d", elapsedSeconds);
                    timer.updateCurrentSecond(elapsedSeconds);
                    if (elapsedSeconds >= sec) {
                        if (userSettings.useVibration()) {
                            vibrator.vibrate(200);
                        }
                        pomoFsm.finish();
                    }
                });
    }

    @Override
    public void init() {
        Timber.v("init");
    }

    @Override
    public void resetTimerCount() {
        Timber.v("resetTimerCount");
        elapsedSeconds = 0;
    }

    @Override
    public void startWorkTimer() {
        Timber.v("startWorkTimer");
        startTimer(userSettings.getWorkMinutes());
    }

    @Override
    public void startShortBreakTimer() {
        Timber.v("startShortBreakTimer");
        startTimer(userSettings.getShortBreakMinutes());
    }

    @Override
    public void startLongBreakTimer() {
        Timber.v("startLongBreakTimer");
        startTimer(userSettings.getLongBreakMinutes());
    }

    @Override
    public void stopTimer() {
        Timber.v("stopTimer");
        if (timerDisposable != null) {
            timerDisposable.dispose();
            timerDisposable = null;
        }
    }

    @Override
    public void layout_IDLE() {
        Timber.v("layout_IDLE");
        timer.setColors(R.color.work_total, R.color.work_min, R.color.work_sec);
        timer.setSeconds(userSettings.getWorkMinutes(), elapsedSeconds);
        msg.setText("");
        start.setVisibility(View.VISIBLE);
        pause.setVisibility(View.GONE);
        restart.setVisibility(View.GONE);
        shortRest.setVisibility(View.GONE);
        longRest.setVisibility(View.GONE);
        finish.setVisibility(View.GONE);
        showNavigation();
    }

    @Override
    public void layout_RUNNING() {
        Timber.v("layout_RUNNING");
        msg.setText(R.string.msg_start_working);
        start.setVisibility(View.GONE);
        pause.setVisibility(View.VISIBLE);
        restart.setVisibility(View.GONE);
        shortRest.setVisibility(View.GONE);
        longRest.setVisibility(View.GONE);
        finish.setVisibility(View.VISIBLE);
        hideNavigation();
    }

    @Override
    public void layout_PAUSE() {
        Timber.v("layout_PAUSE");
        msg.setText(R.string.msg_paused);
        start.setVisibility(View.GONE);
        pause.setVisibility(View.GONE);
        restart.setVisibility(View.VISIBLE);
        shortRest.setVisibility(View.GONE);
        longRest.setVisibility(View.GONE);
        finish.setVisibility(View.VISIBLE);
    }

    @Override
    public void layout_FINISHED() {
        Timber.v("layout_FINISHED");
        msg.setText(R.string.msg_finished);
        start.setVisibility(View.GONE);
        pause.setVisibility(View.GONE);
        restart.setVisibility(View.GONE);
        shortRest.setVisibility(View.VISIBLE);
        longRest.setVisibility(View.VISIBLE);
        finish.setVisibility(View.GONE);
    }

    @Override
    public void layout_SHORT_BREAKING() {
        Timber.v("layout_SHORT_BREAKING");
        timer.setColors(R.color.rest_total, R.color.rest_min, R.color.rest_sec);
        msg.setText(R.string.msg_short_breaking);
        start.setVisibility(View.GONE);
        pause.setVisibility(View.GONE);
        restart.setVisibility(View.GONE);
        shortRest.setVisibility(View.GONE);
        longRest.setVisibility(View.GONE);
        finish.setVisibility(View.VISIBLE);
    }

    @Override
    public void layout_LONG_BREAKING() {
        Timber.v("layout_LONG_BREAKING");
        timer.setColors(R.color.rest_total, R.color.rest_min, R.color.rest_sec);
        msg.setText(R.string.msg_long_breaking);
        start.setVisibility(View.GONE);
        pause.setVisibility(View.GONE);
        restart.setVisibility(View.GONE);
        shortRest.setVisibility(View.GONE);
        longRest.setVisibility(View.GONE);
        finish.setVisibility(View.VISIBLE);
    }

    @Override
    public void throwIllegalStateException() {
        throw new IllegalStateException();
    }

    @Override
    public void showAds() {
        adFsm.show();
    }

    @Override
    public void hideAds() {
        adFsm.hide();
    }

    @Override
    public void showNavigation() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_VISIBLE
        );
    }

    @Override
    public void hideNavigation() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );
    }

    @Override
    public void adInit() {
        adView.loadAd(new AdRequest.Builder()
                .addTestDevice("4EB260715A6D70807B32DAAC473002C9")
                .build()
        );
    }

    @Override
    public void adResume() {
        adView.resume();
        adView.setVisibility(View.VISIBLE);
    }

    @Override
    public void adPause() {
        adView.pause();
        adView.setVisibility(View.GONE);
    }

    @Override
    public void adDestroy() {
        adView.destroy();
    }
}
