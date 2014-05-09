package com.strongloop.android.loopback.test.helpers;

import android.app.Instrumentation;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.test.mock.MockContext;
import android.test.mock.MockPackageManager;

public class TestContext extends MockContext {
    private final Instrumentation instrumentation;

    private String packageVersionName = "a-version-name";
    private int packageVersionCode = 1;

    public TestContext(Instrumentation instrumentation) {
        this.instrumentation = instrumentation;
    }

    public void setPackageVersionName(String value) {
        packageVersionName = value;
    }

    public void setPackageVersionCode(int value) {
        packageVersionCode = value;
    }

    @Override
    public String getPackageName() {
        return "a-package-name";
    }

    @Override
    public PackageManager getPackageManager() {
        return new TestPackageManager();
    }

    @Override
    public SharedPreferences getSharedPreferences(String name, int mode) {
        return getRealContext().getSharedPreferences(name, mode);
    }

    public void clearSharedPreferences(String name, int mode) {
        getRealContext()
                .getSharedPreferences(name, mode)
                .edit().clear().commit();
    }

    private Context getRealContext() {
        return instrumentation.getContext();
    }

    class TestPackageManager extends MockPackageManager {
        @Override
        public PackageInfo getPackageInfo(String packageName, int flags)
                throws NameNotFoundException {

            if (packageName != "a-package-name")
                throw new NameNotFoundException();

            PackageInfo info = new PackageInfo();
            info.versionName = packageVersionName;
            info.versionCode = packageVersionCode;
            return info;
        }
    }

}
