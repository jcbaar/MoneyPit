package com.development.jaba.moneypit;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;


public class AboutActivity extends BaseActivity {

    @Bind(R.id.version) TextView mVersion;
    @Bind(R.id.body) TextView mBody;

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_about;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);

        Context context = getApplicationContext(); // or activity.getApplicationContext()
        PackageManager packageManager = context.getPackageManager();
        String packageName = context.getPackageName();

        String myVersionName = "";
        try {
            myVersionName = getString(R.string.version) + " " + packageManager.getPackageInfo(packageName, 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("AboutActivity", e.getMessage());
        }

        mVersion.setText(myVersionName);
        mBody.setMovementMethod(LinkMovementMethod.getInstance());
        mBody.setText(Html.fromHtml(getString(R.string.about_body)));
    }
}
