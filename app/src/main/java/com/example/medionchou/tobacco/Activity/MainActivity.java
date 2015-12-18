package com.example.medionchou.tobacco.Activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.medionchou.tobacco.Constants.Config;
import com.example.medionchou.tobacco.Constants.States;
import com.example.medionchou.tobacco.LocalService;
import com.example.medionchou.tobacco.LocalServiceConnection;
import com.example.medionchou.tobacco.MD5;
import com.example.medionchou.tobacco.R;
import com.example.medionchou.tobacco.ServiceListener;
import com.example.medionchou.tobacco.SubFragment.ScheduleFragment;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainActivity extends FragmentActivity implements ServiceListener {

    private LocalService mService;
    private LocalServiceConnection mConnection;
    private ConnectionAsynTask asynTask;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = new Intent(this, LocalService.class);
        Bundle extras = getIntent().getExtras();


        if (extras == null)
            startService(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, LocalService.class);

        initObject();
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        asynTask = new ConnectionAsynTask();
        asynTask.execute((Void)null);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mConnection.isBound()) {
            unbindService(mConnection);
        }
        asynTask.stopProgressBar();
    }

    public LocalServiceConnection getLocalServiceConnection() {
        return mConnection;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        switch (item.getItemId()) {
            case R.id.action_settings:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                final LinearLayout ip_portLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.ip_port_layout, null);
                builder.setTitle("設定IP及PORT");
                builder.setView(ip_portLayout);

                builder.setPositiveButton("確認", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences settings = getSharedPreferences("IPFILE", 0);
                        SharedPreferences.Editor editor = settings.edit();
                        EditText ipView = (EditText) ip_portLayout.findViewById(R.id.ip);
                        EditText portView = (EditText) ip_portLayout.findViewById(R.id.port);
                        String ipTest = ipView.getText().toString();
                        String portTest = portView.getText().toString();
                        String ip = "127.0.0.1";
                        Pattern pattern = Pattern.compile("[0-9]{1,3}+\\.[0-9]{1,3}+\\.[0-9]{1,3}+\\.[0-9]{1,3}+");
                        Matcher matcher = pattern.matcher(ipTest);
                        int port = 0;
                        boolean checker;

                        checker = matcher.matches();

                        if (checker) {
                            if (!portTest.equals("")) {
                                if (Integer.valueOf(portTest) <= 65536) {
                                    port = Integer.valueOf(portTest);
                                    checker = true;
                                } else {
                                    checker = false;
                                }
                            }
                        }

                        if (checker) {
                            String[] strip = ipTest.split("\\.");
                            boolean isMatch = true;

                            for (String tmp : strip) {
                                if (Integer.valueOf(tmp) > 255)
                                    isMatch = false;
                            }

                            if (isMatch)
                                ip = ipTest;

                            checker = isMatch;
                        }


                        if (checker) {
                            editor.putString("IP", ip);
                            editor.putInt("PORT", port);
                            editor.apply();
                        } else {
                            AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                            alert.setTitle("警告");
                            alert.setMessage("IP 或 PORT 設定錯誤");
                            alert.show();
                        }
                    }
                });
                builder.show();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }

    private void initObject() {
        mConnection = new LocalServiceConnection();
    }


    private class ConnectionAsynTask extends AsyncTask<Void, Void, Void> {
        String msg = "";
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setTitle(getString(R.string.progress_dialog_waiting));
            progressDialog.setMessage(getString(R.string.logging));
            progressDialog.show();
            progressDialog.setCancelable(false);
        }

        @Override
        protected Void doInBackground(Void[] params) {

            while (!mConnection.isBound());

            mService = mConnection.getService();

            while (mService.getClientState() != States.CONNECT_OK) {
                try {
                    Thread.sleep(2000);
                } catch(InterruptedException e) {

                }
            }

            //progressDialog.cancel();
            Intent intent = new Intent(MainActivity.this, LoggedInActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

            return (Void)null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }

        public void stopProgressBar() {
            progressDialog.cancel();
        }
    }
}
