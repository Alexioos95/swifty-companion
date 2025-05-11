package com.apayen.swifty_companion;

import android.app.Application;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentTransaction;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MainActivity extends AppCompatActivity
{
	//////////////////////////////
	// Creation
	//////////////////////////////
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		EdgeToEdge.enable(this);
		setContentView(R.layout.activity_main);
		ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
			Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
			v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
			return insets;
		});
		// Set Action's bar
		setSupportActionBar(findViewById(R.id.my_toolbar));
		this.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
		this.getWindow().setStatusBarColor(Color.parseColor("#131313"));
		// Load app if no savedInstance
		if (savedInstanceState == null)
		{
			SharedPreferences prefs = this.getSharedPreferences("AppPrefs", Application.MODE_PRIVATE);
			String token = prefs.getString("access_token", "");
			String refresh = prefs.getString("refresh_token", "");

			if (token.isEmpty())
			{
				System.out.println("----- FIRST CONNECTION");
				// First connection
				getSupportFragmentManager().beginTransaction()
						.replace(R.id.fragment_container, new LoginFragment())
						.commit();
			}
			else
			{
				// Check token's validity
				OkHttpClient client = new OkHttpClient();
				Request request = new Request.Builder()
						.url("https://api.intra.42.fr/oauth/token/info")
						.header("Authorization", "Bearer " + token)
						.build();
				client.newCall(request).enqueue(new Callback() {
					@Override
					public void onFailure(@NonNull Call call, @NonNull IOException e)
					{
						// Show error
						runOnUiThread(() -> Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
						// Reset data
						SharedPreferences prefs = MainActivity.this.getSharedPreferences("AppPrefs", Application.MODE_PRIVATE);
						SharedPreferences.Editor editor = prefs.edit();
						editor.putString("access_token", "");
						editor.apply();
						// Load LoginFragment
						getSupportFragmentManager().beginTransaction()
								.replace(R.id.fragment_container, new LoginFragment())
								.commit();
					}
					@Override
					public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException
					{
						if (response.isSuccessful())
						{
							System.out.println("----- VALID TOKEN");
							// Go to SearchFragment
							getSupportFragmentManager().beginTransaction()
									.replace(R.id.fragment_container, new SearchFragment())
									.commit();
						}
						else
						{
							System.out.println("----- INVALID TOKEN");
							// Try to refresh token
							OkHttpClient client = new OkHttpClient();
							RequestBody requestBody = new FormBody.Builder()
									.add("refresh_token", refresh)
									.add("client_id", BuildConfig.APP_UID)
									.add("client_secret", BuildConfig.APP_SECRET)
									.add("grant_type", "refresh_token")
									.build();
							Request request = new Request.Builder()
									.url("https://api.intra.42.fr/oauth/token")
									.post(requestBody)
									.build();
							client.newCall(request).enqueue(new Callback() {
								@Override
								public void onFailure(@NonNull Call call, @NonNull IOException e)
								{
									// Show error
									runOnUiThread(() -> Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
									// Reset data
									SharedPreferences prefs = MainActivity.this.getSharedPreferences("AppPrefs", Application.MODE_PRIVATE);
									SharedPreferences.Editor editor = prefs.edit();
									editor.putString("access_token", "");
									editor.apply();
									// Load LoginFragment
									getSupportFragmentManager().beginTransaction()
											.replace(R.id.fragment_container, new LoginFragment())
											.commit();
								}
								@Override
								public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException
								{
									System.out.println("----- REFRESHED TOKEN");
									if (response.isSuccessful())
									{
										ResponseBody body = response.body();
										if (body != null)
										{
											try
											{
												// Update data
												JSONObject obj = new JSONObject(body.string());
												SharedPreferences prefs = MainActivity.this.getSharedPreferences("AppPrefs", Application.MODE_PRIVATE);
												SharedPreferences.Editor editor = prefs.edit();
												editor.putString("access_token", obj.getString("access_token"));
												editor.putString("refresh_token", obj.getString("refresh_token"));
												editor.apply();
												// Load SearchFragment
												FragmentTransaction transaction = MainActivity.this.getSupportFragmentManager().beginTransaction();
												transaction.replace(R.id.fragment_container, new SearchFragment());
												transaction.commit();
											}
											catch (JSONException e)
											{
												// Show error
												runOnUiThread(() -> Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
												// Reset data
												SharedPreferences prefs = MainActivity.this.getSharedPreferences("AppPrefs", Application.MODE_PRIVATE);
												SharedPreferences.Editor editor = prefs.edit();
												editor.putString("access_token", "");
												editor.apply();
												// Load LoginFragment
												getSupportFragmentManager().beginTransaction()
														.replace(R.id.fragment_container, new LoginFragment())
														.commit();
											}
										}
									}
									else
									{
										// Show error
										runOnUiThread(() -> Toast.makeText(MainActivity.this, "Error " + response.code(), Toast.LENGTH_SHORT).show());
										// Reset data
										SharedPreferences prefs = MainActivity.this.getSharedPreferences("AppPrefs", Application.MODE_PRIVATE);
										SharedPreferences.Editor editor = prefs.edit();
										editor.putString("access_token", "");
										editor.apply();
										// Load LoginFragment
										getSupportFragmentManager().beginTransaction()
												.replace(R.id.fragment_container, new LoginFragment())
												.commit();
									}
								}
							});
						}
					}
				});
			}
		}
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.menu_actionbar, menu);
		return (true);
	}
}