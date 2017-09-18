package me.angrbyte.dagger2test;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import me.angrbyte.dagger2test.components.SlowComponent;
import me.angrybyte.sillyandroid.parsable.Annotations.Clickable;
import me.angrybyte.sillyandroid.parsable.Annotations.FindView;
import me.angrybyte.sillyandroid.parsable.Annotations.Layout;
import me.angrybyte.sillyandroid.parsable.components.ParsableActivity;

@Layout(R.layout.activity_main)
public class MainActivity extends ParsableActivity {

    // <editor-fold desc="Views">
    @Clickable
    @SuppressWarnings("unused")
    @FindView(R.id.button_generate)
    private Button mGenerateButton;

    @FindView(R.id.instance_description_outer)
    private TextView mOuterInstanceDesc;
    // </editor-fold>

    private Disposable mInitFinalizer;
    private CompositeDisposable mClickDisposables;
    private Observable<SlowComponent> mSingletonComponent;

    @Override
    @SuppressLint("SetTextI18n")
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mClickDisposables = new CompositeDisposable();
        mSingletonComponent = ((Dagger2TestApplication) getApplicationContext()).getSingletonInitializer();

        // disable the 'generate' button until the slow component is loaded
        mGenerateButton.setEnabled(false);
        mInitFinalizer = mSingletonComponent.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(slowComponent -> {
            // slow component is loaded for the first time
            mGenerateButton.setEnabled(true);
            dispose(mInitFinalizer);
            mOuterInstanceDesc.setText("Initialized! Instance: " + slowComponent.toString());
            mOuterInstanceDesc.setTextColor(Color.WHITE);
        });
    }

    @Override
    @SuppressLint("SetTextI18n")
    public void onClick(@NonNull final View v) {
        switch (v.getId()) {
            case R.id.button_generate: {
                final Disposable disposable = mSingletonComponent
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(slowComponent -> {
                            // slow component is either loaded for the first time or we just got an event
                            // from the behavior subject containing the last available instance
                            v.setEnabled(true);
                            dispose(mInitFinalizer, mClickDisposables);
                            mOuterInstanceDesc.setText("onNext(): SlowComponent = " + slowComponent.toString());
                            mOuterInstanceDesc.setTextColor(randomColor(130));
                        });
                v.setEnabled(false);
                mClickDisposables.add(disposable);
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
