package com.a5idoo.library.update.app;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.codyy.download.Downloader;
import com.codyy.download.entity.DownloadEntity;
import com.codyy.download.service.DownloadStatus;
import com.codyy.download.service.SimpleDownloadListener;

import java.io.File;


public class UpdateDialogActivity extends AppCompatActivity {
    public static final String ARG_TITLE = "title";
    public static final String ARG_CONTENT = "content";
    public static final String ARG_URL = "ARG_URL";
    public static final String ARG_FILE_NAME = "ARG_FILE_NAME";
    public static final String ARG_AUTHORITY = "authority";
    public static final String ARG_APPLICATION_ID = "APPLICATION_ID";
    public static final String ARG_CANCELABLE = "ARG_CANCELABLE";
    TextView mTvDialogTitle;
    TextView mTvDialogContent;
    Button mBtnDialogSure;
    private String mContent = "";
    private String mTitle = "";

    private void downloadUpdate() {
        if (isMobileConnected(this)) {
            new AlertDialog.Builder(this).setTitle((R.string.update_dialog_tip)).setMessage((R.string.update_dialog_mobile_type)).setPositiveButton(R.string.update_dialog_confirm, (dialog, which) -> {
                Downloader.getInstance(UpdateDialogActivity.this).setHoneyCombDownload(true);
                startDownloadApp();
            }).setNegativeButton(R.string.update_dialog_soon, (dialog, which) -> {
                Downloader.getInstance(UpdateDialogActivity.this).setHoneyCombDownload(false);
                finish();
            }).create().show();
        } else {
            startDownloadApp();
        }
    }

    public static boolean isMobileConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm != null && cm.getActiveNetworkInfo() != null
                && cm.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_MOBILE;
    }

    private void startDownloadApp() {
        mBtnDialogSure.setEnabled(false);
        DownloadEntity entity = new DownloadEntity();
        entity.setId(getIntent().getStringExtra(ARG_URL));
        entity.setUrl(getIntent().getStringExtra(ARG_URL));
        Downloader.getInstance(this).download(entity);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Downloader.getInstance(this).receiveDownloadStatus(getIntent().getStringExtra(ARG_URL), new SimpleDownloadListener() {

            @Override
            public void onProgress(DownloadStatus status) {
                mBtnDialogSure.setText(getString(R.string.update_dialog_downloading, (int) status.getPercentNumber()));
            }

            @Override
            public void onComplete() {
                mBtnDialogSure.setEnabled(true);
                mBtnDialogSure.setText(R.string.update_dialog_install);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    boolean haveInstallPermission = getPackageManager().canRequestPackageInstalls();
                    if (haveInstallPermission) {
                        installApp();
                    } else {
                        new AlertDialog.Builder(UpdateDialogActivity.this)
                                .setMessage(R.string.update_dialog_open_permission_settings)
                                .setPositiveButton(R.string.update_dialog_confirm, (dialog, which) -> startUnknownAppSources())
                                .setNegativeButton(R.string.update_dialog_cancel, (dialog, which) -> {
                                })
                                .show();
                    }
                } else {
                    installApp();
                }
            }

            @Override
            public void onError(Exception e) {
                super.onError(e);
                mBtnDialogSure.setEnabled(true);
                mBtnDialogSure.setText(R.string.update_dialog_retry);
//                Toast.makeText(UpdateDialogActivity.this, "下载失败");
            }

            @Override
            public void onFailure(int code) {
                super.onFailure(code);
                mBtnDialogSure.setEnabled(true);
                mBtnDialogSure.setText(R.string.update_dialog_retry);
//                ToastUtil.show(UpdateDialogActivity.this, "下载失败");
            }
        });
    }


    @TargetApi(Build.VERSION_CODES.O)
    private void startUnknownAppSources() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:" + getIntent().getStringExtra(ARG_APPLICATION_ID)));
        startActivityForResult(intent, 10086);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 10086) {
            installApp();//再次执行安装流程，包含权限判等
        }
    }

    private void installApp() {
        File files = new File(Downloader.getInstance(this).getDownloadRecord(getIntent().getStringExtra(ARG_URL)).getSavePath());
        getOpenFileIntent(UpdateDialogActivity.this, files, getIntent().getStringExtra(ARG_AUTHORITY));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog_update);
        mTvDialogTitle=findViewById(R.id.tv_dialog_title);
        mTvDialogContent=findViewById(R.id.tv_dialog_content);
        mBtnDialogSure = findViewById(R.id.btn_dialog_sure);
        mBtnDialogSure.setEnabled(Downloader.isBound());
        Downloader.getInstance(this).setOnConnectedListener(() -> mBtnDialogSure.setEnabled(true));
        Downloader.init(UpdateDialogActivity.this, true);
        if (null != getIntent()) {
            mTitle = getIntent().getStringExtra(ARG_TITLE);
            mContent = getIntent().getStringExtra(ARG_CONTENT);
        }
        mTvDialogTitle.setText(mTitle);
        mTvDialogContent.setText(mContent);
        mBtnDialogSure.setText(R.string.update_dialog_update);
        mBtnDialogSure.setOnClickListener(v -> {
            if (AntiShakeUtils.isInvalidClick(v)) {
                return;
            }
            if (mBtnDialogSure.isEnabled() && getString(R.string.update_dialog_install).equals(mBtnDialogSure.getText().toString())) {
                installApp();
            } else {
                if (!getPermission()) {
                    checkPermission();
                } else {
                    downloadUpdate();
                }
            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 2018: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    downloadUpdate();
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private boolean getPermission() {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void checkPermission() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this).setCancelable(false).setMessage("您未授予下载权限,将无法下载文件,是否授予权限?").setTitle("提示").setPositiveButton(R.string.update_dialog_confirm, (dialog, which) -> ActivityCompat.requestPermissions(UpdateDialogActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        2018)).setNegativeButton(R.string.update_dialog_cancel, (dialog, which) -> {

                }).create().show();
            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        2018);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Downloader.getInstance(this).deleteAll();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            onBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 增加了默认的返回finish事件
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBack();
                return true;
            default:
        }
        return super.onOptionsItemSelected(item);

    }

    private void onBack() {
        if (getIntent().getBooleanExtra(ARG_CANCELABLE, false)) {
            finish();
        }
    }

    public void getOpenFileIntent(Context context, File file, String authority) {
        Intent intent = new Intent();
        //设置intent的Action属性
        intent.setAction(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //设置intent的data和Type属性。
        if (TextUtils.isEmpty(authority)) return;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setDataAndType(FileProvider.getUriForFile(context, authority, file), MimeTypeMap.getSingleton().getMimeTypeFromExtension(getFileExtension(file)));
        } else {
            intent.setDataAndType(Uri.fromFile(file), MimeTypeMap.getSingleton().getMimeTypeFromExtension(getFileExtension(file)));
        }
        PackageManager manager = context.getPackageManager();
        if (manager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null) {
            context.startActivity(intent);
        } else {
            Toast.makeText(context, R.string.update_dialog_can_not_open_file_mime_type, Toast.LENGTH_SHORT).show();
            Intent intent1 = new Intent();
            intent1.setAction(Intent.ACTION_MAIN);
            intent1.addCategory(Intent.CATEGORY_APP_MARKET);
            if (manager.resolveActivity(intent1, PackageManager.MATCH_DEFAULT_ONLY) != null) {
                context.startActivity(intent1);
            }
        }
    }

    /**
     * 获取全路径中的文件拓展名
     *
     * @param file 文件
     * @return 文件拓展名
     */
    private String getFileExtension(File file) {
        if (file == null) return null;
        return getFileExtension(file.getPath());
    }

    /**
     * 获取全路径中的文件拓展名
     *
     * @param filePath 文件路径
     * @return 文件拓展名
     */
    private String getFileExtension(String filePath) {
        if (isSpace(filePath)) return filePath;
        int lastPoi = filePath.lastIndexOf('.');
        int lastSep = filePath.lastIndexOf(File.separator);
        if (lastPoi == -1 || lastSep >= lastPoi) return "";
        return filePath.substring(lastPoi + 1).toLowerCase();
    }

    /**
     * 判断字符串是否为null或全为空格
     *
     * @param s 待校验字符串
     * @return {@code true}: null或全空格<br> {@code false}: 不为null且不全空格
     */
    private boolean isSpace(String s) {
        return (s == null || s.trim().length() == 0);
    }

}
