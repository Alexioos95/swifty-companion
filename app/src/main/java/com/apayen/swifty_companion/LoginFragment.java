package com.apayen.swifty_companion;

import android.app.Application;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.Toast;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.FormBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.json.JSONException;
import org.json.JSONObject;

public class LoginFragment extends Fragment
{
	//////////////////////////////
	// Variables
	//////////////////////////////
	WebView webView;
	LinearLayout connectWrapper;
	//////////////////////////////
	// Creation
	//////////////////////////////
	@Override
	public void onCreate(Bundle savedInstanceState)
	{ super.onCreate(savedInstanceState); }
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{ return (inflater.inflate(R.layout.fragment_login, container, false)); }
	public void onViewCreated(@NonNull View view, Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);
		// Init variables
		webView = view.findViewById(R.id.oauthWebView);
		connectWrapper = view.findViewById(R.id.loginButton);
		// EventListener for login
		connectWrapper.setOnClickListener(v -> handleToken());
	}
	//////////////////////////////
	// Redirection
	//////////////////////////////
	private void handleToken()
	{
		WebSettings webSettings = webView.getSettings();

		connectWrapper.setVisibility(View.GONE);
		webSettings.setJavaScriptEnabled(true);
		webView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request)
			{
				Uri uri = request.getUrl();

				if (uri.toString().startsWith("apayen://redirect?error"))
				{
					view.loadUrl("about:blank");
					connectWrapper.postDelayed(() -> connectWrapper.setVisibility(View.VISIBLE), 500);
					return (true);
				}
				else if (uri.toString().startsWith("apayen://redirect"))
				{
					String code = uri.getQueryParameter("code");

					if (code != null)
					{
						// Get token
						OkHttpClient client = new OkHttpClient();
						RequestBody body = new FormBody.Builder()
								.add("code", code)
								.add("client_id", BuildConfig.APP_UID)
								.add("client_secret", BuildConfig.APP_SECRET)
								.add("redirect_uri", BuildConfig.APP_REDIRECT)
								.add("grant_type", "authorization_code")
								.build();
						Request req = new Request.Builder()
								.url("https://api.intra.42.fr/oauth/token")
								.post(body)
								.build();
						client.newCall(req).enqueue(new Callback() {
							@Override
							public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException
							{
								if (response.isSuccessful())
								{
									ResponseBody body = response.body();

									if (body != null)
									{
										try
										{
											// Update data
											String data = body.string();
											JSONObject obj = new JSONObject(data);
											SharedPreferences prefs = requireActivity().getSharedPreferences("AppPrefs", Application.MODE_PRIVATE);
											SharedPreferences.Editor editor = prefs.edit();
											editor.putString("access_token", obj.getString("access_token"));
											editor.putString("refresh_token", obj.getString("refresh_token"));
											editor.apply();
											// Load SearchFragment
											FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
											transaction.replace(R.id.fragment_container, new SearchFragment());
											transaction.commit();
										}
										catch (JSONException e)
										{ requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()); }
									}
								}
								else
									requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Error " + response.code(), Toast.LENGTH_SHORT).show());
							}
							@Override
							public void onFailure(@NonNull Call call, @NonNull IOException e)
							{ requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()); }
						});
					}
					return (true);
				}
				return (super.shouldOverrideUrlLoading(view, request));
			}
		});
		webView.loadUrl(BuildConfig.APP_URL);
	}
}