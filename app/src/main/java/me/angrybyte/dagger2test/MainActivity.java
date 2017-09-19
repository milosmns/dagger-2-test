package me.angrybyte.dagger2test;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import me.angrybyte.dagger2test.components.QuickComponent;
import me.angrybyte.dagger2test.components.SlowComponent;
import me.angrybyte.sillyandroid.parsable.Annotations.Clickable;
import me.angrybyte.sillyandroid.parsable.Annotations.FindView;
import me.angrybyte.sillyandroid.parsable.Annotations.Layout;
import me.angrybyte.sillyandroid.parsable.components.ParsableActivity;

@Layout(R.layout.activity_main)
public class MainActivity extends ParsableActivity {

    // <editor-fold desc="Views">
    @Clickable
    @SuppressWarnings("unused")
    @FindView(R.id.button_slow_instance)
    private Button mSlowInstanceButton;

    @Clickable
    @SuppressWarnings("unused")
    @FindView(R.id.button_quick_instance)
    private Button mQuickInstanceButton;

    @FindView(R.id.instance_description_outer)
    private TextView mInstanceDescription;
    // </editor-fold>

    private Disposable mInitFinalizer;
    private CompositeDisposable mClickDisposables;
    private Observable<SlowComponent> mSlowSingleton;
    private Observable<QuickComponent> mQuickSingleton;

    @Override
    @SuppressLint("SetTextI18n")
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mClickDisposables = new CompositeDisposable();
        mSlowSingleton = ((Dagger2TestApplication) getApplicationContext()).getSlowComponentObservable();
        mQuickSingleton = ((Dagger2TestApplication) getApplicationContext()).getQuickComponentObservable();

        // disable the buttons until the components are loaded
        mSlowInstanceButton.setEnabled(false);
        mQuickInstanceButton.setVisibility(View.GONE);

        final Observable<Pair<SlowComponent, QuickComponent>> zippedObservable = Observable.zip(mSlowSingleton, mQuickSingleton, Pair::new);
        final Single<Pair<SlowComponent, QuickComponent>> initializer = Single.fromObservable(zippedObservable);
        mInitFinalizer = initializer.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(pair -> {
            // components are loaded for the first time
            mSlowInstanceButton.setEnabled(true);
            mQuickInstanceButton.setVisibility(View.VISIBLE);
            dispose(mInitFinalizer);
            mInstanceDescription.setText("Initialized! Slow: " + pair.first.toString() + ", Quick: " + pair.second.toString());
            mInstanceDescription.setTextColor(Color.WHITE);
        });
    }

    @Override
    @SuppressLint("SetTextI18n")
    public void onClick(@NonNull final View v) {
        switch (v.getId()) {
            case R.id.button_slow_instance: {
                final Disposable slowDisposable = mSlowSingleton
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(slowComponent -> {
                            // slow component is either loaded for the first time or we just got an event
                            // from the behavior subject containing the last available instance
                            v.setEnabled(true);
                            dispose(mInitFinalizer, mClickDisposables);
                            mInstanceDescription.setText("SlowComponent = " + getCleanToString(slowComponent));
                            mInstanceDescription.setTextColor(randomColor(140));
                        });
                v.setEnabled(false);
                mClickDisposables.add(slowDisposable);
                break;
            }
            case R.id.button_quick_instance: {
                final Disposable quickDisposable = mQuickSingleton
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(quickComponent -> {
                            // quick component is either loaded for the first time or we just got an event
                            // from the behavior subject containing the last available instance
                            v.setVisibility(View.VISIBLE);
                            dispose(mInitFinalizer, mClickDisposables);
                            mInstanceDescription.setText("QuickComponent = " + getCleanToString(quickComponent));
                            mInstanceDescription.setTextColor(randomColor(100));
                        });
                v.setVisibility(View.GONE);
                mClickDisposables.add(quickDisposable);
                break;
            }
            default: {
                super.onClick(v);
                break;
            }
        }
    }

    @ColorInt
    private int randomColor(@IntRange(from = 0, to = 255) final int smallestBrightness) {
        final int randomR = (int) Math.floor((double) smallestBrightness + (Math.random() * (255d - smallestBrightness)));
        final int randomG = (int) Math.floor((double) smallestBrightness + (Math.random() * (255d - smallestBrightness)));
        final int randomB = (int) Math.floor((double) smallestBrightness + (Math.random() * (255d - smallestBrightness)));
        return Color.argb(255, randomR, randomG, randomB);
    }

    @NonNull
    private String getCleanToString(@Nullable final Object object) {
        return String.valueOf(object).replace(getPackageName() + ".", "");
    }

    private void dispose(@Nullable final Disposable... disposables) {
        if (disposables == null || disposables.length == 0) {
            return;
        }
        for (@Nullable final Disposable disposable : disposables) {
            if (disposable != null && !disposable.isDisposed()) {
                disposable.dispose();
            }
        }
    }

    @Override
    protected void onBlockingDestroy() {
        super.onBlockingDestroy();
        dispose(mInitFinalizer, mClickDisposables);
    }

}
