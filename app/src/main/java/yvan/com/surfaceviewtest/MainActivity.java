package yvan.com.surfaceviewtest;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private ImageView img_control;
    private ImageView img_change_select;
    private LuckyPan luckyPan;
    private int mChoice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initEvent();

    }

    private void initView() {
        img_change_select = (ImageView) findViewById(R.id.img_change_select);
        luckyPan = (LuckyPan) findViewById(R.id.sv_lucky_pan);
        img_control = (ImageView) findViewById(R.id.img_control);
    }

    private void initEvent() {
        img_control.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                control_toggle();
            }
        });

        img_change_select.setOnTouchListener(new onDoubleClick());
    }

    private void control_toggle() {
        if (!luckyPan.isStart()) {
            img_control.setImageResource(R.drawable.center_stop);

            luckyPan.luckyStart();
        } else if (!luckyPan.isShouldEnd()) {
            img_control.setImageResource(R.drawable.center_start);
            Log.i("choice", mChoice + "");
            luckyPan.luckyEnd(mChoice);
        }
    }


    class onDoubleClick implements View.OnTouchListener {

        private int count;
        private long firClick;
        private long secClick;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (MotionEvent.ACTION_DOWN == event.getAction()) {
                count++;
                if (count == 1) {
                    firClick = System.currentTimeMillis();

                } else if (count == 2) {
                    secClick = System.currentTimeMillis();
                    if (secClick - firClick < 1000) {
                        //双击事件
                        mChoice++;
                        mChoice %= 6;
                        Toast.makeText(MainActivity.this, "" + mChoice, Toast.LENGTH_SHORT).show();

                    }
                    count = 0;
                    firClick = 0;
                    secClick = 0;

                }
            }
            return true;
        }

    }
}
