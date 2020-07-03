package com.hdu.auto.will.alarmclock;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import com.gjiazhe.panoramaimageview.GyroscopeObserver;
import com.gjiazhe.panoramaimageview.PanoramaImageView;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;

public class ScrollingActivity extends AppCompatActivity {

    CollapsingToolbarLayout toolBarLayout;
    Toolbar toolbar;
    private GyroscopeObserver gyroscopeObserver;
    ListAdapter adapter;
    List<Alarm> list = new ArrayList<>();
    Switch alarmSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolBarLayout = findViewById(R.id.toolbar_layout);

        SharedPreferences sharedPreferences = this.getSharedPreferences("share", MODE_PRIVATE);
        boolean isFirstRun = sharedPreferences.getBoolean("isFirstRun", true);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (isFirstRun) {

            Alarm tmp=new Alarm("08:00","Good Morning!",8,0,0,0);

            AlarmDBHandler dbHandler=new AlarmDBHandler(getApplicationContext());
            dbHandler.Open();
            dbHandler.addAlarm(tmp);
            dbHandler.Close();

            AlertDialog alertDialog = new AlertDialog.Builder(ScrollingActivity.this).setMessage(R.string.warning).setPositiveButton("我已知晓", (dialog, which) -> {

            }).create();
            alertDialog.show();

            new MaterialTapTargetPrompt.Builder(ScrollingActivity.this)
                    .setTarget(R.id.fab)
                    .setPrimaryText("Set an Alarm")
                    .setSecondaryText("Tap the button to start setting your first alarm")
                    .setPromptStateChangeListener((prompt, state) -> {
                        if (state == MaterialTapTargetPrompt.STATE_DISMISSED)
                        {
                            new MaterialTapTargetPrompt.Builder(ScrollingActivity.this)
                                    .setTarget(R.id.alarm_cardview)
                                    .setPrimaryText("Activate an Alarm")
                                    .setSecondaryText("Tap the card to activated your alarm, long-press to edit or delete the alarm")
                                    .show();
                        }
                    })
                    .show();
            editor.putBoolean("isFirstRun", false);
            editor.apply();
        }


        gyroscopeObserver = new GyroscopeObserver();
        gyroscopeObserver.setMaxRotateRadian(Math.PI / 9);
        PanoramaImageView panoramaImageView = findViewById(R.id.panorama_image_view);
        panoramaImageView.setGyroscopeObserver(gyroscopeObserver);

        DisplayMetrics dm = getResources().getDisplayMetrics();
        final int screenWitch = dm.widthPixels;

        int statusBarHeight = -1;
        try {
            @SuppressLint("PrivateApi") Class<?> clazz = Class.forName("com.android.internal.R$dimen");
            Object object = clazz.newInstance();
            int height = Integer.parseInt(Objects.requireNonNull(clazz.getField("status_bar_height")
                    .get(object)).toString());
            statusBarHeight = getResources().getDimensionPixelSize(height);
        } catch (Exception e) {
            e.printStackTrace();
        }
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(screenWitch, statusBarHeight + (int) (240 * ScrollingActivity.this.getResources().getDisplayMetrics().density + 0.5f));
        panoramaImageView.setLayoutParams(params);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(ScrollingActivity.this, AlarmSettingsActivity.class);
            startActivity(intent);
        });

        adapter = new ListAdapter(list);
        RecyclerView recyclerView = findViewById(R.id.recycler_view_main);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(adapter);

        adapter.addChildClickViewIds(R.id.alarm_cardview);
        adapter.setOnItemChildClickListener((adapter, view, position) -> {
            if (view.getId() == R.id.alarm_cardview) {
                alarmSwitch = (Switch) adapter.getViewByPosition(position, R.id.alarm_switch);
                Alarm curAlarm = (Alarm) adapter.getItem(position);
                String formatTime = curAlarm.getHour() + "：" + curAlarm.getMinute();
                AlarmDBHandler alarmDBHandler = new AlarmDBHandler(getApplicationContext());
                alarmDBHandler.Open();
                if (!alarmSwitch.isChecked()) {
                    alarmSwitch.setChecked(true);
                    Toast.makeText(ScrollingActivity.this, "Alarm at " + formatTime + " is On!", Toast.LENGTH_SHORT).show();
                    curAlarm.setIsActivated(Alarm.ACTIVATED);
                    alarmDBHandler.updateAlarm(curAlarm);
                    if (curAlarm.getDateCount() == Alarm.ONLY_ONCE)
                        AlarmManagerUtil.setAlarm(ScrollingActivity.this, 0, curAlarm.getHour(), curAlarm.getMinute(), (int) curAlarm.getId(), 0, curAlarm.getContent(), 2);
                    if (curAlarm.getDateCount() == Alarm.EVERYDAY)
                        AlarmManagerUtil.setAlarm(ScrollingActivity.this, 1, curAlarm.getHour(), curAlarm.getMinute(), (int) curAlarm.getId(), 0, curAlarm.getContent(), 2);
                    else {
                        int[] tmp = curAlarm.getWeek();
                        for (int i = 0; i < 7; i++) {
                            if (tmp[i] == 1)
                                AlarmManagerUtil.setAlarm(ScrollingActivity.this, 2, curAlarm.getHour(), curAlarm.getMinute(), (int) curAlarm.getId() + 100 + i, i + 1, curAlarm.getContent(), 2);
                        }
                    }
                } else {
                    alarmSwitch.setChecked(false);
                    Toast.makeText(ScrollingActivity.this, "Alarm at " + formatTime + " is Off!", Toast.LENGTH_SHORT).show();
                    curAlarm.setIsActivated(Alarm.DEACTIVATED);
                    alarmDBHandler.updateAlarm(curAlarm);
                    if (curAlarm.getDateCount() > 0) {
                        int[] tmp = curAlarm.getWeek();
                        for (int i = 0; i < 7; i++) {
                            if (tmp[i] == 1)
                                AlarmManagerUtil.cancelAlarm(ScrollingActivity.this, (int) curAlarm.getId() + 100 + i);
                        }
                    } else {
                        AlarmManagerUtil.cancelAlarm(ScrollingActivity.this, (int) curAlarm.getId());
                    }
                }
                alarmDBHandler.Close();
            }
        });

        adapter.addChildLongClickViewIds(R.id.alarm_cardview);
        adapter.setOnItemChildLongClickListener((adapter, view, position) -> {
            if (view.getId() == R.id.alarm_cardview) {
                Vibrator vibrator = (Vibrator) ScrollingActivity.this.getSystemService(VIBRATOR_SERVICE);
                assert vibrator != null;
                vibrator.vibrate(50);
                new AlertDialog.Builder(ScrollingActivity.this)
                        .setMessage(R.string.log_msg)
                        .setNegativeButton(R.string.log_del, (dialog, which) -> {
                            Alarm curAlarm = (Alarm) adapter.getItem(position);
                            AlarmDBHandler dbHandler = new AlarmDBHandler(getApplicationContext());
                            dbHandler.Open();
                            dbHandler.removeAlarm(curAlarm);
                            dbHandler.Close();
                            refreshRecyclerView();
                        })
                        .setPositiveButton(R.string.log_edit, (dialog, which) -> {
                            Alarm curAlarm = (Alarm) adapter.getItem(position);
                            Intent intent = new Intent(ScrollingActivity.this, AlarmReSettingsActivity.class);
                            intent.putExtra("id", curAlarm.getId());
                            startActivity(intent);
                        })
                        .setNeutralButton(R.string.log_no, (dialogInterface, i) -> {
                        })
                        .create().show();
            }
            return false;
        });
        timer.schedule(task, 0, 1000);

        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_settings) {
                Intent intent = new Intent(ScrollingActivity.this, AboutActivity.class);
                startActivity(intent);
            }
            return true;
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_scrolling, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("DefaultLocale")
    private void refreshTime() {
        Calendar c = Calendar.getInstance();
        toolBarLayout.setTitle(String.format("%02d:%02d:%02d",
                c.get(Calendar.HOUR_OF_DAY),
                c.get(Calendar.MINUTE), c.get(Calendar.SECOND)));
    }

    Timer timer = new Timer();
    TimerTask task = new TimerTask() {
        @Override
        public void run() {
            refreshTime();
        }
    };

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onStop() {
        super.onStop();
    }


    @Override
    protected void onResume() {
        super.onResume();

        gyroscopeObserver.register(this);

        refreshRecyclerView();
    }

    @Override
    protected void onPause() {
        super.onPause();

        gyroscopeObserver.unregister();
    }

    public void refreshRecyclerView() {
        AlarmDBHandler dbHandler = new AlarmDBHandler(getApplicationContext());
        dbHandler.Open();
        if (list.size() > 0)
            list.clear();
        list.addAll(dbHandler.getAllAlarms());
        dbHandler.Close();
        adapter.notifyDataSetChanged();
    }
}