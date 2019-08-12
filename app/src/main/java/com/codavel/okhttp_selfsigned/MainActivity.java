package com.codavel.okhttp_selfsigned;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.io.IOException;
import java.security.cert.CertificateException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import static org.apache.http.conn.ssl.SSLSocketFactory.SSL;

public class MainActivity extends AppCompatActivity {

    private static final String URL = "http-multiplexing.codavel.com/img.jpg";
    private static final String UNSAFE_URL = "http-multiplexing.codavel.com:8443/img.jpg";
    private static final String HTTPS = "https://";


    /**
     * Create insecure instance of OkHttpClient. This will disable the validation of the Host Name
     * Verifier and allows all SSL certificates.
     *
     * @return instance of OkHttpClient
     */
    private OkHttpClient getUnsafeOkHttpClient() {
        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {

                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain,
                                                       String authType) throws
                                CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain,
                                                       String authType) throws
                                CertificateException {
                        }
                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance(SSL);
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);

            // Bypass hostname verification
            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });

            return builder.build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.regular).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                safeTransfer();
            }
        });

        findViewById(R.id.multiplex).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unsafeTransfer();
            }
        });
    }

    void safeTransfer() {
        Log.d("info", "safeTransfer");

        final OkHttpClient client = new OkHttpClient();

        Request get = new Request.Builder()
                .url(HTTPS + URL)
                .build();

        client.newCall(get).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    ResponseBody responseBody = response.body();
                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected code " + response);
                    }
                    Log.d("info", "response code:" + response.code() + ", content length:" + response.body().contentLength());
                    response.code();
                    response.body().contentLength();
                    responseBody.string();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                client.connectionPool().evictAll();
            }
        });
    }

    void unsafeTransfer() {
        Log.d("info", "unsafeTransfer");

        final OkHttpClient client = getUnsafeOkHttpClient();

        Request get = new Request.Builder()
                .url(HTTPS + UNSAFE_URL)
                .build();

        client.newCall(get).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    ResponseBody responseBody = response.body();
                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected code " + response);
                    }

                    Log.d("info", "response code:" + response.code() + ", content length:" + response.body().contentLength());
                    response.code();
                    response.body().contentLength();
                    responseBody.string();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                client.connectionPool().evictAll();
            }
        });
    }
}
