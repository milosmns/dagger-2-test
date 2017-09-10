package me.angrbyte.dagger2test;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import me.angrybyte.sillyandroid.parsable.Annotations.Clickable;
import me.angrybyte.sillyandroid.parsable.Annotations.FindView;
import me.angrybyte.sillyandroid.parsable.Annotations.Layout;
import me.angrybyte.sillyandroid.parsable.components.ParsableActivity;

@Layout(R.layout.activity_main)
public class MainActivity extends ParsableActivity {

    @Clickable
    @FindView(R.id.button_generate)
    private Button mGenerateButton;

    @FindView(R.id.instance_description_outer)
    private TextView mOuterInstanceDesc;

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.button_generate: {
                toastShort("Demo works!");
                break;
            }
            default: {
                super.onClick(v);
                break;
            }
        }
    }

}
