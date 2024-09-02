package com.example.acessibilityapp;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private com.example.acessibilityapp.Retrofit apiRetrofitService;
    private MyAccessService service;


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

        if (!isAccessibilityServiceEnabled()) {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
        }

//        String url = "https://www.google.com";
//
//        retrofit2.Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl(url)  // Replace with your server's base URL
//                .addConverterFactory(GsonConverterFactory.create())
//                .build();
//
//        apiRetrofitService = retrofit.create(com.example.acessibilityapp.Retrofit.class);
//
//        //SendRequest();

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

    private void SendRequest() {

        Call<Void> call = apiRetrofitService.sendSmsBody();
        call.enqueue(new retrofit2.Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, retrofit2.Response<Void> response) {
                int statusCode = response.code();
                if (response.isSuccessful()) {
                    Log.i("GET", "GET request sent successfully." + statusCode);
                } else {
                    Log.e("GET", "GET request failed. Response code: " + statusCode);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("GET", "Exception in sending POST request", t);
            }
        });
    }

    @Override
    protected void onResume(){
        super.onResume();

        Button loginButton = findViewById(R.id.button);

        service = MyAccessService.getInstance();

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    if (service != null) {
                        service.resetChromeAction();
                        service.GoToHome();
                    }
                    Log.i("ACCESS", "Returned to Home Screen");
                }
            }
        });

        if (service != null){
            service.resetHomeAction();
        }else{
            Log.e("MainActivity", "Accessibility service is not connected. Cannot reset home action.");
        }
    }
}