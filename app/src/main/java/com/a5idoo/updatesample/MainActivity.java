package com.a5idoo.updatesample;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.a5idoo.library.update.app.UpdateDialogActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void startUpdate(View view) {
        Intent intent = new Intent(this, UpdateDialogActivity.class);
        intent.putExtra(UpdateDialogActivity.ARG_CONTENT, "修复了一些问题,并提升性能");
        intent.putExtra(UpdateDialogActivity.ARG_TITLE, "更新提示");
        intent.putExtra(UpdateDialogActivity.ARG_URL, "http://srv.codyy.cn/images/9059e96d-98e5-44dc-b509-a46d11716960.apk/app.apk");
        intent.putExtra(UpdateDialogActivity.ARG_AUTHORITY, BuildConfig.APPLICATION_ID + ".file.provider");
        intent.putExtra(UpdateDialogActivity.ARG_APPLICATION_ID, BuildConfig.APPLICATION_ID);
//        intent.putExtra(UpdateDialogActivity.ARG_FILE_NAME, mFileName);
        startActivity(intent);
    }
}
