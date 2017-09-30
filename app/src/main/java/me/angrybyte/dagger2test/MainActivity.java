package me.angrybyte.dagger2test;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
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

    private static final String TAG = MainActivity.class.getSimpleName();

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
    // </editor-fold>

    private Observable<SlowComponent> mSlowSingleton;
    private Observable<QuickComponent> mQuickSingleton;

    private Disposable mSlowComponentDisposable;
    private Disposable mQuickComponentDisposable;
    private CompositeDisposable mAllDisposables;

    @Override
    @SuppressLint("SetTextI18n")
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // initialize Rx stuff
        mAllDisposables = new CompositeDisposable();
        mSlowSingleton = Dagger2TestApplication.getInstance(this).getSlowComponentObservable();
        mQuickSingleton = Dagger2TestApplication.getInstance(this).getQuickComponentObservable();

        // wait for both instances to be created
        final Disposable initDisposable = Observable
                .zip(mSlowSingleton, mQuickSingleton, Pair::new)
                .distinct()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(pair -> {
                    // components are loaded now for the first time
                    Log.d(TAG, "Graph available: " + pair);
                    final String formatted = "Generated!\nSlow: %s\nQuick: %s";
                    mSlowInstanceButton.setEnabled(true);
                    mQuickInstanceButton.setVisibility(View.VISIBLE);
                    mInstanceDescription.setText(String.format(formatted, Utils.toStringShort(pair.first, this), Utils.toStringShort(pair.second, this)));
                    mInstanceDescription.setTextColor(Color.WHITE);
                });
        mAllDisposables.add(initDisposable);
    }

    @Override
    @SuppressLint("SetTextI18n")
    public void onClick(@NonNull final View v) {
        switch (v.getId()) {
            case R.id.button_slow_instance: {
                // disable the button until the component loads
                v.setEnabled(false);

                // load the component, manage its disposable
                Utils.dispose(mSlowComponentDisposable);
                mSlowComponentDisposable = mSlowSingleton
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(slowComponentInstance -> {
                            // slow component is either loaded for the first time or we just got an event
                            // from the behavior subject containing the last available instance
                            v.setEnabled(true);
                            Utils.dispose(mSlowComponentDisposable);
                            mInstanceDescription.setText("SlowComponent = " + Utils.toStringShort(slowComponentInstance, this));
                            mInstanceDescription.setTextColor(Utils.randomColor(140));
                        });
                mAllDisposables.add(mSlowComponentDisposable);
                break;
            }
            case R.id.button_quick_instance: {
                // hide the button until the component loads
                v.setVisibility(View.GONE);

                // load the component, manage its disposable
                Utils.dispose(mQuickComponentDisposable);
                mQuickComponentDisposable = mQuickSingleton.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(quickComponent -> {
                    v.setVisibility(View.VISIBLE);
                    Utils.dispose(mQuickComponentDisposable);
                    mInstanceDescription.setText("QuickComponent = " + Utils.toStringShort(quickComponent, this));
                    mInstanceDescription.setTextColor(Utils.randomColor(100));
                });
                mAllDisposables.add(mQuickComponentDisposable);
                break;
            }
            default: {
                super.onClick(v);
                break;
            }
        }
    }

    @Override
    protected void onBlockingDestroy() {
        super.onBlockingDestroy();

        // dispose all callbacks
        Utils.dispose(mAllDisposables);
        mAllDisposables = new CompositeDisposable();

        // remove components
        if (!isChangingConfigurations()) {
            // going down forever, clean up application
            Dagger2TestApplication.getInstance(this).cleanup();
        }
    }

}
