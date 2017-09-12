package me.angrbyte.dagger2test;

import android.annotation.SuppressLint;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Random;

import io.reactivex.android.schedulers.AndroidSchedulers;
import me.angrybyte.sillyandroid.parsable.Annotations.Clickable;
import me.angrybyte.sillyandroid.parsable.Annotations.FindView;
import me.angrybyte.sillyandroid.parsable.Annotations.Layout;
import me.angrybyte.sillyandroid.parsable.components.ParsableActivity;

@Layout(R.layout.activity_main)
public class MainActivity extends ParsableActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    @Clickable
    @FindView(R.id.button_generate)
    private Button mGenerateButton;

    @FindView(R.id.instance_description_outer)
    private TextView mOuterInstanceDesc;

    @Override
    @SuppressLint("SetTextI18n")
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.button_generate: {
                ((Dagger2TestApplication) getApplicationContext())
                        .getSlowComponent()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(slowComponent -> {
                            mOuterInstanceDesc.setText("onNext(): SlowComponent = " + slowComponent.toString());
                            mOuterInstanceDesc.setTextColor(new Random().nextInt());
                        });
                break;
            }
            default: {
                super.onClick(v);
                break;
            }
        }
    }

}
