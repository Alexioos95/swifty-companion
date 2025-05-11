package com.apayen.swifty_companion;

import static androidx.core.content.ContextCompat.getSystemService;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import android.graphics.Typeface;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class SearchFragment extends Fragment
{
	//////////////////////////////
	// Variables
	//////////////////////////////
	String token;
	String refresh;
	EditText search;
	ImageView photo;
	TextView login;
	TextView name;
	TextView location;
	TextView phone;
	TextView email;
	TextView wallet;
	TextView eval_points;
	TextView level;
	LinearLayout footer;
	private List<String> projectNamesList = new ArrayList<>();
	private List<String> projectNotesList = new ArrayList<>();
	private List<Integer> projectValidatedList = new ArrayList<>();
	private List<String> skillNames = new ArrayList<>();
	private List<String> skillLevels = new ArrayList<>();
	//////////////////////////////
	// Creation
	//////////////////////////////
	@Override
	public void onCreate(Bundle savedInstanceState)
	{ super.onCreate(savedInstanceState); }
	@Override
	public void onViewCreated(@NonNull View view, Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);
		// Set action's bar
		requireActivity().addMenuProvider(new MenuProvider() {
			@Override
			public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater)
			{ menuInflater.inflate(R.menu.menu_logout_actionbar, menu); }
			@Override
			public boolean onMenuItemSelected(@NonNull MenuItem menuItem)
			{
				if (menuItem.getItemId() == R.id.action_logout)
					logoutStud();
				return (false);
			}
		}, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.fragment_search, container, false);
		// Init variables
		SharedPreferences prefs = requireActivity().getSharedPreferences("AppPrefs", Application.MODE_PRIVATE);
		token = prefs.getString("access_token", "");
		refresh = prefs.getString("refresh_token", "");
		search = view.findViewById(R.id.searchBar);
		photo = view.findViewById(R.id.stud_photo);
		login = view.findViewById(R.id.stud_login);
		name = view.findViewById(R.id.stud_name);
		location = view.findViewById(R.id.stud_location);
		phone = view.findViewById(R.id.stud_phone);
		email = view.findViewById(R.id.stud_email);
		wallet = view.findViewById(R.id.stud_wallet);
		eval_points = view.findViewById(R.id.stud_eval_points);
		level = view.findViewById(R.id.stud_level);
		footer = view.findViewById(R.id.footer_container);
		// Load values
		if (savedInstanceState != null)
			loadPrevData(savedInstanceState);
		else
			loadMeData();
		// Event Listener for student search
		search.setOnEditorActionListener((v, actionId, event) -> {
			if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE)
			{
				String login = search.getText().toString().trim();
				InputMethodManager imm = (InputMethodManager)requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);

				loadStudData(login);
				search.setText("");
				search.clearFocus();
				if (imm != null)
					imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
				return (true);
			}
			return (false);
		});
		// Event Listener to disable keyboard
		view.findViewById(R.id.scrollview_container).setOnTouchListener((v, event) -> {
			if (event.getAction() == MotionEvent.ACTION_UP)
			{
				InputMethodManager imm = (InputMethodManager)requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);

				if (imm != null)
					imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
				v.performClick();
			}
			return (true);
		});
		return (view);
	}
	//////////////////////////////
	// Saved Instance
	//////////////////////////////
	@Override
	public void onSaveInstanceState(@NonNull Bundle outState)
	{
		super.onSaveInstanceState(outState);
		outState.putString("search", search.getText().toString());
		outState.putString("login", login.getText().toString());
		outState.putString("name", name.getText().toString());
		outState.putString("location", location.getText().toString());
		outState.putString("phone", phone.getText().toString());
		outState.putString("email", email.getText().toString());
		outState.putString("wallet", wallet.getText().toString());
		outState.putString("eval_points", eval_points.getText().toString());
		outState.putString("level", level.getText().toString());
		outState.putStringArrayList("projectNames", new ArrayList<>(projectNamesList));
		outState.putStringArrayList("projectNotes", new ArrayList<>(projectNotesList));
		outState.putIntegerArrayList("projectValidated", new ArrayList<>(projectValidatedList));
		outState.putStringArrayList("skillNames", new ArrayList<>(skillNames));
		outState.putStringArrayList("skillLevels", new ArrayList<>(skillLevels));
	}
	private void loadPrevData(Bundle savedInstanceState)
	{
		SharedPreferences sharedPreferences = requireContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
		String studPhotoMedium = sharedPreferences.getString("studPhotoMedium", "");

		Glide.with(requireContext())
				.load(studPhotoMedium)
				.listener(new RequestListener<>() {
					@Override
					public boolean onLoadFailed(@Nullable GlideException e, Object model, @NonNull Target<Drawable> target, boolean isFirstResource)
					{ return (false); }
					@Override
					public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model, Target<Drawable> target, @NonNull DataSource dataSource, boolean isFirstResource) {
						photo.setBackgroundResource(R.drawable.shape_circle);
						photo.setClipToOutline(true);
						return (false);
					}
				})
				.into(photo);
		search.setText(savedInstanceState.getString("search", ""));
		login.setText(savedInstanceState.getString("login", "undefined"));
		name.setText(savedInstanceState.getString("name", "undefined"));
		location.setText(savedInstanceState.getString("location", "Unavailable"));
		phone.setText(savedInstanceState.getString("phone", "Phone is hidden"));
		email.setText(savedInstanceState.getString("email", "undefined"));
		wallet.setText(savedInstanceState.getString("wallet", "undefined"));
		eval_points.setText(savedInstanceState.getString("eval_points", "undefined"));
		level.setText(savedInstanceState.getString("level", "undefined"));
		projectNamesList = savedInstanceState.getStringArrayList("projectNames");
		projectNotesList = savedInstanceState.getStringArrayList("projectNotes");
		projectValidatedList = savedInstanceState.getIntegerArrayList("projectValidated");
		skillNames = savedInstanceState.getStringArrayList("skillNames");
		skillLevels = savedInstanceState.getStringArrayList("skillLevels");
		recoverFooter();
	}
	private void recoverFooter()
	{
		for (int i = 0; i < skillNames.size(); i++)
		{
			ConstraintLayout layout = new ConstraintLayout(requireContext());
			layout.setLayoutParams(new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT));
			// Name of skill
			TextView textName = new TextView(requireContext());
			textName.setId(View.generateViewId());
			textName.setText(skillNames.get(i));
			textName.setTextSize(14);
			ConstraintLayout.LayoutParams textNameParams = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
			textNameParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
			textNameParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
			layout.addView(textName, textNameParams);
			// Level of skill
			TextView textNote = new TextView(requireContext());
			textNote.setText(skillLevels.get(i));
			textNote.setTextSize(16);
			textNote.setTypeface(null, Typeface.BOLD);
			ConstraintLayout.LayoutParams textNoteParams = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
			textNoteParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
			textNoteParams.topToTop = textName.getId();
			layout.addView(textNote, textNoteParams);
			// Add to footer
			requireActivity().runOnUiThread(() -> footer.addView(layout));
		}
		for (int i = 0; i < projectNamesList.size(); i++)
		{
			ConstraintLayout constraintLayout = new ConstraintLayout(requireContext());
			constraintLayout.setLayoutParams(new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT));
			// Name of project
			TextView textName = new TextView(requireContext());
			textName.setId(View.generateViewId());
			textName.setText(projectNamesList.get(i));
			textName.setTextSize(14);
			ConstraintLayout.LayoutParams textNameParams = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
			textNameParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
			textNameParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
			constraintLayout.addView(textName, textNameParams);
			// Note of project
			TextView textNote = new TextView(requireContext());
			textNote.setText(projectNotesList.get(i));
			textNote.setTextSize(16);
			textNote.setTypeface(null, Typeface.BOLD);
			if (projectValidatedList.get(i) == 1)
				textNote.setTextColor(ContextCompat.getColor(requireContext(), R.color.green));
			else
				textNote.setTextColor(ContextCompat.getColor(requireContext(), R.color.red));
			ConstraintLayout.LayoutParams textNoteParams = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
			textNoteParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
			textNoteParams.topToTop = textName.getId();
			constraintLayout.addView(textNote, textNoteParams);
			// Add to footer
			requireActivity().runOnUiThread(() -> footer.addView(constraintLayout));
		}
	}
	//////////////////////////////
	// Saved data
	//////////////////////////////
	private void loadMeData()
	{
		// Get ME infos
		OkHttpClient client = new OkHttpClient();
		Request request = new Request.Builder()
				.url("https://api.intra.42.fr/v2/me")
				.header("Authorization", "Bearer " + token)
				.build();
		client.newCall(request).enqueue(new Callback() {
			@Override
			public void onFailure(@NonNull Call call, @NonNull IOException e)
			{ requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()); }
			@Override
			public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException
			{
				if (response.isSuccessful())
				{
					ResponseBody body = response.body();

					if (body != null)
					{
						String data = body.string();
						fillData(data);
					}
				}
				else
				{
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
							requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
							logoutStud();
						}
						@Override
						public void onResponse(@NonNull Call call, @NonNull Response response)
						{
							if (response.isSuccessful())
								loadMeData();
							else
							{
								requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), "Error " + response.code(), Toast.LENGTH_SHORT).show());
								logoutStud();
							}
						}
					});
				}
			}
		});
	}
	private void loadStudData(String login)
	{
		// Get <login> infos
		OkHttpClient client = new OkHttpClient();
		Request request = new Request.Builder()
				.url("https://api.intra.42.fr/v2/users/" + login)
				.header("Authorization", "Bearer " + token)
				.build();
		client.newCall(request).enqueue(new Callback() {
			@Override
			public void onFailure(@NonNull Call call, @NonNull IOException e)
			{ requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()); }
			@Override
			public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException
			{
				if (response.isSuccessful())
				{
					ResponseBody body = response.body();

					if (body != null)
					{
						String data = body.string();
						fillData(data);
					}
				}
				else
				{
					int statusCode = response.code();

					if (statusCode == 404 || statusCode == 500)
						requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), "Error " + statusCode, Toast.LENGTH_SHORT).show());
					else
					{
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
								requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
								logoutStud();
							}
							@Override
							public void onResponse(@NonNull Call call, @NonNull Response response)
							{
								if (response.isSuccessful())
									loadStudData(login);
								else
								{
									requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), "Error " + response.code(), Toast.LENGTH_SHORT).show());
									logoutStud();
								}
							}
						});
					}
				}
			}
		});
	}
	//////////////////////////////
	// Handle data
	//////////////////////////////
	private void fillData(String data)
	{
		try
		{
			// Get data
			JSONObject obj = new JSONObject(data);
			JSONObject image = obj.getJSONObject("image");
			JSONObject versions = image.getJSONObject("versions");
			String studPhotoMedium = versions.getString("medium");
			String studLogin = obj.getString("login");
			String studName = obj.getString("displayname");
			String studLocation = obj.getString("location").equals("null") ? "Unavailable" : obj.getString("location");
			String studPhone = obj.getString("phone").equals("hidden") ? "Phone number hidden" : obj.getString("phone");
			String studEmail = obj.getString("email");
			String studWallet = obj.getString("wallet") + " ₳";
			String studEvalPoints = obj.getString("correction_point");
			JSONArray cursusArray = obj.getJSONArray("cursus_users");
			JSONObject cursusObj = cursusArray.getJSONObject(1);
			JSONArray skillsArray = cursusObj.getJSONArray("skills");
			String studLevel = "Level " + cursusObj.getString("level");
			JSONArray projectsArray = obj.getJSONArray("projects_users");

			// Load photo
			requireActivity().runOnUiThread(() -> Glide.with(requireContext())
					.load(studPhotoMedium)
					.listener(new RequestListener<>() {
						@Override
						public boolean onLoadFailed(@Nullable GlideException e, Object model, @NonNull Target<Drawable> target, boolean isFirstResource) {
							return (false);
						}
						@Override
						public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model, Target<Drawable> target, @NonNull DataSource dataSource, boolean isFirstResource) {
							SharedPreferences sharedPreferences = requireContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
							SharedPreferences.Editor editor = sharedPreferences.edit();
							editor.putString("studPhotoMedium", studPhotoMedium);
							editor.apply();
							photo.setBackgroundResource(R.drawable.shape_circle);
							photo.setClipToOutline(true);
							return (false);
						}
					})
					.into(photo));
			// Load texts
			requireActivity().runOnUiThread(() -> {
				login.setText(studLogin);
				name.setText(studName);
				location.setText(studLocation);
				phone.setText(studPhone);
				email.setText(studEmail);
				wallet.setText(studWallet);
				eval_points.setText(studEvalPoints);
				level.setText(studLevel);
			});
			// Empty footer and lists
			requireActivity().runOnUiThread(() -> footer.removeAllViews());
			skillNames.clear();
			skillLevels.clear();
			projectNamesList.clear();
			projectNotesList.clear();
			projectValidatedList.clear();
			// Load skills
			for (int i = 0; i < skillsArray.length(); i++)
			{
				// Get values
				JSONObject skill = skillsArray.getJSONObject(i);
				String skillName = skill.getString("name");
				double skillLevel = skill.getDouble("level");
				ConstraintLayout layout = new ConstraintLayout(requireContext());
				layout.setLayoutParams(new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT));
				// Name of skill
				TextView textName = new TextView(requireContext());
				textName.setId(View.generateViewId());
				textName.setText(skillName);
				textName.setTextSize(14);
				ConstraintLayout.LayoutParams textNameParams = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
				textNameParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
				textNameParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
				layout.addView(textName, textNameParams);
				// Level of skill
				TextView textNote = new TextView(requireContext());
				textNote.setText(String.valueOf(skillLevel));
				textNote.setTextSize(16);
				textNote.setTypeface(null, Typeface.BOLD);
				ConstraintLayout.LayoutParams textNoteParams = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
				textNoteParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
				textNoteParams.topToTop = textName.getId();
				layout.addView(textNote, textNoteParams);
				// Add to footer
				requireActivity().runOnUiThread(() -> footer.addView(layout));
				// Add to lists
				skillNames.add(skillName);
				skillLevels.add(String.valueOf(skillLevel));
			}
			// Load projects
			for (int i = 0; i < projectsArray.length(); i++)
			{
				try
				{
					// Get values
					JSONObject itemObject = projectsArray.getJSONObject(i);
					String projectStatus = itemObject.getString("status");

					if (!projectStatus.equals("in_progress"))
					{
						// Get values
						JSONObject itemSubObject = itemObject.getJSONObject("project");
						String projectName = itemSubObject.getString("name");
						String projectNote = itemObject.getString("final_mark");
						boolean projectValidatedTmp = itemObject.getBoolean("validated?");
						int projectValidated = !projectValidatedTmp ? 0 : 1;
						ConstraintLayout constraintLayout = new ConstraintLayout(requireContext());
						constraintLayout.setLayoutParams(new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT));
						// Name of project
						TextView textName = new TextView(requireContext());
						textName.setId(View.generateViewId());
						textName.setText(projectName);
						textName.setTextSize(14);
						ConstraintLayout.LayoutParams textNameParams = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
						textNameParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
						textNameParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
						constraintLayout.addView(textName, textNameParams);
						// Note of project
						TextView textNote = new TextView(requireContext());
						textNote.setText(projectNote);
						textNote.setTextSize(16);
						textNote.setTypeface(null, Typeface.BOLD);
						if (projectValidated == 1)
							textNote.setTextColor(ContextCompat.getColor(requireContext(), R.color.green));
						else
							textNote.setTextColor(ContextCompat.getColor(requireContext(), R.color.red));
						ConstraintLayout.LayoutParams textNoteParams = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
						textNoteParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
						textNoteParams.topToTop = textName.getId();
						constraintLayout.addView(textNote, textNoteParams);
						// Add to footer
						requireActivity().runOnUiThread(() -> footer.addView(constraintLayout));
						// Add to lists
						projectNamesList.add(projectName);
						projectNotesList.add(projectNote);
						projectValidatedList.add(projectValidated);
					}
				}
				catch (JSONException e)
				{ requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()); }
			}
		}
		catch (JSONException e)
		{ requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()); }
	}
	//////////////////////////////
	// Logout
	//////////////////////////////
	private void logoutStud()
	{
		// Reset data
		SharedPreferences prefs = requireActivity().getSharedPreferences("AppPrefs", Application.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString("access_token", "");
		editor.apply();
		// Load LoginFragment
		FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
		transaction.replace(R.id.fragment_container, new LoginFragment());
		transaction.commit();
	}
}