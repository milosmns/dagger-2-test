package me.angrybyte.dagger2test;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import io.reactivex.Observable;
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
    @FindView(R.id.instance_description_outer)
    private TextView mInstanceDescription;

    @Clickable
    @SuppressWarnings("unused")
    @FindView(R.id.button_slow_instance)
    private Button mSlowInstanceButton;

    @Clickable
    @SuppressWarnings("unused")
    @FindView(R.id.button_quick_instance)
    private Button mQuickInstanceButton;

    @Clickable
    @SuppressWarnings("unused")
    @FindView(R.id.button_generate)
    private Button mGenerateGraphButton;
    // </editor-fold>

    private Observable<SlowComponent> mSlowSingleton;
    private Observable<QuickComponent> mQuickSingleton;

    private Disposable mGenerateDisposable;
    private Disposable mSlowComponentDisposable;
    private Disposable mQuickComponentDisposable;
    private CompositeDisposable mAllDisposables;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAllDisposables = new CompositeDisposable();
        mSlowSingleton = ((Dagger2TestApplication) getApplicationContext()).getSlowComponentObservable();
        mQuickSingleton = ((Dagger2TestApplication) getApplicationContext()).getQuickComponentObservable();
    }

    /**
     * Generates a new dependency graph.
     */
    @SuppressLint("SetTextI18n")
    private void reloadGraph() {
        // disable the buttons until the components are loaded
        mSlowInstanceButton.setEnabled(false);
        mQuickInstanceButton.setVisibility(View.GONE);
        mGenerateGraphButton.setVisibility(View.GONE);

        if (mGenerateDisposable == null || mGenerateDisposable.isDisposed()) {
            mGenerateDisposable = Observable
                    .zip(mSlowSingleton, mQuickSingleton, Pair::new)
                    .distinct()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::onNewGraphAvailable);
        }
        mAllDisposables.add(mGenerateDisposable);
    }

    @Override
    protected void onStart() {
        super.onStart();
        reloadGraph();
    }

    @Override
    protected void onStop() {
        super.onStop();
        dispose(mAllDisposables);
        mAllDisposables = new CompositeDisposable();
    }

    @Override
    @SuppressLint("SetTextI18n")
    public void onClick(@NonNull final View v) {
        switch (v.getId()) {
            case R.id.button_slow_instance: {
                v.setEnabled(false);
                dispose(mSlowComponentDisposable);
                mSlowComponentDisposable = mSlowSingleton
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(slowComponentInstance -> {
                            // slow component is either loaded for the first time or we just got an event
                            // from the behavior subject containing the last available instance
                            v.setEnabled(true);
                            dispose(mSlowComponentDisposable);
                            mInstanceDescription.setText("SlowComponent = " + getCleanToString(slowComponentInstance));
                            mInstanceDescription.setTextColor(randomColor(140));
                        });
                mAllDisposables.add(mSlowComponentDisposable);
                break;
            }
            case R.id.button_quick_instance: {
                v.setVisibility(View.GONE);
                dispose(mQuickComponentDisposable);
                mQuickComponentDisposable = mQuickSingleton.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(quickComponent -> {
                    // quick component is either loaded for the first time or we just got an event
                    // from the behavior subject containing the last available instance
                    v.setVisibility(View.VISIBLE);
                    dispose(mQuickComponentDisposable);
                    mInstanceDescription.setText("QuickComponent = " + getCleanToString(quickComponent));
                    mInstanceDescription.setTextColor(randomColor(100));
                });
                mAllDisposables.add(mQuickComponentDisposable);
                break;
            }
            case R.id.button_generate: {
                ((Dagger2TestApplication) getApplicationContext()).buildAll();
                reloadGraph();
                break;
            }
            default: {
                super.onClick(v);
                break;
            }
        }
    }

    private void onNewGraphAvailable(@NonNull final Pair<SlowComponent, QuickComponent> graph) {
        Log.d(getClass().getSimpleName(), "onNewGraphAvailable: " + graph);
        // components are loaded for the first time
        mSlowInstanceButton.setEnabled(true);
        mQuickInstanceButton.setVisibility(View.VISIBLE);
        mGenerateGraphButton.setVisibility(View.VISIBLE);
        mInstanceDescription.setText("Generated!\nSlow: " + getCleanToString(graph.first) + "\nQuick: " + getCleanToString(graph.second));
        mInstanceDescription.setTextColor(Color.WHITE);
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
        if (object instanceof SlowComponent) {
            return ((SlowComponent) object).getWebClient().toString();
        } else if (object instanceof QuickComponent) {
            return ((QuickComponent) object).getWebClient().toString();
        } else {
            return String.valueOf(object).replace(getPackageName() + ".", "");
        }
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
        dispose(mAllDisposables);
    }

}
