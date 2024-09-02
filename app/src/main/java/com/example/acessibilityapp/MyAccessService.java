package com.example.acessibilityapp;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.util.List;

public class MyAccessService extends AccessibilityService {

    private static MyAccessService instance;
    private boolean hasReturned = false;
    public boolean chromeclicked = false;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        if (accessibilityEvent.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            CharSequence packageNameChar = accessibilityEvent.getPackageName();
            if (packageNameChar != null) {
                String packageName = accessibilityEvent.getPackageName().toString();
                if (isHomeScreen(packageName) && !chromeclicked) {
                    Log.i("ACCESSIBILITY", "Home screen is active");
                    clickChrome();
                }
            }
        }
    }

    @Override
    public void onInterrupt() {

    }
    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        instance = this;

        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED | AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        info.flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS;
        setServiceInfo(info);

    }

    public static MyAccessService getInstance() {
        return instance;
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    public void GoToHome(){
        performGlobalAction(GLOBAL_ACTION_HOME);
    }

    public void resetHomeAction(){
        hasReturned = false;
    }

    public void resetChromeAction(){
        chromeclicked = false;
    }

    private boolean isHomeScreen(String packN){
        return packN.equals("com.android.launcher") ||
                packN.equals("com.google.android.apps.nexuslauncher") ||
                packN.equals("com.sec.android.app.launcher");
    }

    private void clickChrome(){
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode != null){
            List<AccessibilityNodeInfo> chromeIcons = rootNode.findAccessibilityNodeInfosByText("Chrome");
            if (chromeIcons != null && !chromeIcons.isEmpty()){
                for (AccessibilityNodeInfo node : chromeIcons) {
                    if (node.isClickable()){
                        node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        Log.i("ACCESS", "Clicked on Chrome");
                        chromeclicked = true;
                        return;
                    }
                }
            }
        }
    }

}
