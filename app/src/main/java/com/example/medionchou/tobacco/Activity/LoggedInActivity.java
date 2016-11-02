package com.example.medionchou.tobacco.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.*;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import com.example.medionchou.tobacco.Constants.States;
import com.example.medionchou.tobacco.LocalService;
import com.example.medionchou.tobacco.LocalServiceConnection;
import com.example.medionchou.tobacco.MarqueeTextView;
import com.example.medionchou.tobacco.R;
import com.example.medionchou.tobacco.ServiceListener;
import com.example.medionchou.tobacco.SubFragment.BoxFragment;
import com.example.medionchou.tobacco.SubFragment.QualityFragment;
import com.example.medionchou.tobacco.SubFragment.ScheduleFragment;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;



public class LoggedInActivity extends FragmentActivity implements ServiceListener {

    private LocalServiceConnection mConnection;

    private LocalService mService;

    private MarqueeTextView runningTextView;

    private RunningTextThread thread;

    private MyThread myThread;

    public static int currentPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logged_in);
        runningTextView = (MarqueeTextView) findViewById(R.id.running_text_view);
    }


    @Override
    protected void onStart() {
        super.onStart();
        mConnection = new LocalServiceConnection();
        Intent intent = new Intent(this, LocalService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        myThread = new MyThread();
        myThread.start();

        thread = new RunningTextThread();
        thread.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mConnection.isBound()) {
            unbindService(mConnection);
        }
        myThread = null;

        thread.stopThread();
        thread = null;
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        stopService(new Intent(this, LocalService.class));
        Log.v("MyLog", "Destroy called");
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        //Log.v("MyLog", "ResetLogout");
    }

    private void initObject() {
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(new PagerAdapter(getSupportFragmentManager()));

        Log.v("MyLog", "called");
    }


    public LocalServiceConnection getLocalServiceConnection() {
        return mConnection;
    }

    public String getWorkerId() {
        return "fuck";
    }


    private class PagerAdapter extends FragmentPagerAdapter {
        private final int PAGE_COUNT = 3;
        private String[] tabTitles = {"差勤查詢v1.0.2", "品管查詢", "箱數查詢"};

        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return PAGE_COUNT;
        }

        @Override
        public Fragment getItem(int position) {

            Log.v("MyLog", position + " _");
            switch (position) {
                case 0:
                    return new ScheduleFragment();
                case 1:
                    return new QualityFragment();
                case 2:
                    return new BoxFragment();
            }

            return null;
        }


        @Override
        public CharSequence getPageTitle(int position) {
            // Generate title based on item position
            return tabTitles[position];
        }
    }

    private class RunningTextThread extends Thread {

        private boolean stop = false;
        String msg = "";
        String oldMsg = "";

        @Override
        public void run() {
            super.run();
            LocalService mService;
            while (!mConnection.isBound()) ;

            mService = mConnection.getService();

            try {
                while (!stop) {
                    msg = mService.getMsg();
                    if (msg.length() > 0 && !oldMsg.equals(msg)) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                runningTextView.setText(msg);
                            }
                        });
                    } else if (msg.length() > 0) {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Calendar cal = Calendar.getInstance();
                                DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                                String date = dateFormat.format(cal.getTime());
                                runningTextView.setNewText(msg + date);
                            }
                        });

                    }
                    oldMsg = msg;
                    Thread.sleep(1000);
                }
            } catch (Exception e) {
                Log.e("MyLogLogged", e.toString());
                Intent intent = new Intent(LoggedInActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        }

        public void stopThread() {
            stop = true;
        }
    }

    private class MyThread extends Thread {
        @Override
        public void run() {
            super.run();
            try {
                while (!mConnection.isBound()) {
                    Thread.sleep(1000);
                }


                mService = mConnection.getService();

                while (mService.getClientState() != States.CONNECT_OK) {
                    Thread.sleep(1000);
                }


                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
                            viewPager.setAdapter(new PagerAdapter(getSupportFragmentManager()));
                            viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                                @Override
                                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                                }

                                @Override
                                public void onPageSelected(int position) {
                                    currentPage = position;
                                }

                                @Override
                                public void onPageScrollStateChanged(int state) {

                                }
                            });
                        } catch (Exception e) {
                            Log.e("MyLog", e.toString());
                        }
                    }
                });

            } catch (Exception e) {
                Log.e("MyLog", e.toString());
            }
        }
    }

}
