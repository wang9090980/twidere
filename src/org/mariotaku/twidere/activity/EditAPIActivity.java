/*
 *				Twidere - Twitter client for Android
 * 
 * Copyright (C) 2012 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.activity;

import static android.text.TextUtils.isEmpty;
import static org.mariotaku.twidere.util.ParseUtils.parseString;
import static org.mariotaku.twidere.util.Utils.isValidUrl;
import static org.mariotaku.twidere.util.Utils.trim;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.provider.TweetStore.Accounts;

import twitter4j.TwitterConstants;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

public class EditAPIActivity extends BaseDialogActivity implements TwitterConstants, OnCheckedChangeListener,
		OnClickListener {

	private EditText mEditRestBaseURL, mEditSigningRESTBaseURL, mEditOAuthBaseURL, mEditSigningOAuthBaseURL;
	private EditText mEditConsumerKey, mEditConsumerSecret;
	private RadioGroup mEditAuthType;
	private RadioButton mButtonOAuth, mButtonxAuth, mButtonBasic, mButtonTwipOMode;
	private Button mSaveButton;
	private TextView mAdvancedAPIConfigLabel;
	private View mAdvancedAPIConfigContainer;

	private String mRestBaseURL, mSigningRestBaseURL, mOAuthBaseURL, mSigningOAuthBaseURL;
	private String mConsumerKey, mConsumerSecret;
	private int mAuthType;

	@Override
	public void onCheckedChanged(final RadioGroup group, final int checkedId) {
		switch (checkedId) {
			case R.id.oauth: {
				mAuthType = Accounts.AUTH_TYPE_OAUTH;
				break;
			}
			case R.id.xauth: {
				mAuthType = Accounts.AUTH_TYPE_XAUTH;
				break;
			}
			case R.id.basic: {
				mAuthType = Accounts.AUTH_TYPE_BASIC;
				break;
			}
			case R.id.twip_o: {
				mAuthType = Accounts.AUTH_TYPE_TWIP_O_MODE;
				break;
			}
		}
		final boolean is_oauth = mAuthType == Accounts.AUTH_TYPE_OAUTH || mAuthType == Accounts.AUTH_TYPE_XAUTH;
		mAdvancedAPIConfigContainer.setVisibility(is_oauth ? View.VISIBLE : View.GONE);
	}

	@Override
	public void onClick(final View v) {
		final SharedPreferences pref = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		final String consumer_key = pref.getString(PREFERENCE_KEY_CONSUMER_KEY, TWITTER_CONSUMER_KEY_2);
		final String consumer_secret = pref.getString(PREFERENCE_KEY_CONSUMER_SECRET, TWITTER_CONSUMER_SECRET_2);
		switch (v.getId()) {
			case R.id.save: {
				saveEditedText();
				if (checkUrlErrors()) return;
				final Bundle extras = new Bundle();
				extras.putString(Accounts.REST_BASE_URL, isEmpty(mRestBaseURL) ? DEFAULT_REST_BASE_URL : mRestBaseURL);
				extras.putString(Accounts.OAUTH_BASE_URL, isEmpty(mOAuthBaseURL) ? DEFAULT_OAUTH_BASE_URL
						: mOAuthBaseURL);
				extras.putString(Accounts.SIGNING_REST_BASE_URL,
						isEmpty(mSigningRestBaseURL) ? DEFAULT_SIGNING_REST_BASE_URL : mSigningRestBaseURL);
				extras.putString(Accounts.SIGNING_OAUTH_BASE_URL,
						isEmpty(mSigningOAuthBaseURL) ? DEFAULT_SIGNING_OAUTH_BASE_URL : mSigningOAuthBaseURL);
				extras.putString(Accounts.CONSUMER_KEY, isEmpty(mConsumerKey) ? consumer_key : mConsumerKey);
				extras.putString(Accounts.CONSUMER_SECRET, isEmpty(mConsumerSecret) ? consumer_secret : mConsumerSecret);
				extras.putInt(Accounts.AUTH_TYPE, mAuthType);
				setResult(RESULT_OK, new Intent().putExtras(extras));
				finish();
				break;
			}
			case R.id.advanced_api_config_label: {
				final View stub_view = findViewById(R.id.stub_advanced_api_config);
				final View inflated_view = findViewById(R.id.advanced_api_config);
				if (stub_view != null) {
					stub_view.setVisibility(View.VISIBLE);
					mAdvancedAPIConfigLabel.setCompoundDrawablesWithIntrinsicBounds(R.drawable.expander_open_holo, 0,
							0, 0);
					mEditSigningRESTBaseURL = (EditText) findViewById(R.id.signing_rest_base_url);
					mEditOAuthBaseURL = (EditText) findViewById(R.id.oauth_base_url);
					mEditSigningOAuthBaseURL = (EditText) findViewById(R.id.signing_oauth_base_url);
					mEditConsumerKey = (EditText) findViewById(R.id.consumer_key);
					mEditConsumerSecret = (EditText) findViewById(R.id.consumer_secret);

					mEditSigningRESTBaseURL.setText(isEmpty(mSigningRestBaseURL) ? DEFAULT_SIGNING_REST_BASE_URL
							: mSigningRestBaseURL);
					mEditOAuthBaseURL.setText(isEmpty(mOAuthBaseURL) ? DEFAULT_OAUTH_BASE_URL : mOAuthBaseURL);
					mEditSigningOAuthBaseURL.setText(isEmpty(mSigningOAuthBaseURL) ? DEFAULT_SIGNING_OAUTH_BASE_URL
							: mSigningOAuthBaseURL);
					mEditConsumerKey.setText(isEmpty(mConsumerKey) ? consumer_key : mConsumerKey);
					mEditConsumerSecret.setText(isEmpty(mConsumerSecret) ? consumer_secret : mConsumerSecret);
				} else if (inflated_view != null) {
					final boolean is_visible = inflated_view.getVisibility() == View.VISIBLE;
					final int compound_res = is_visible ? R.drawable.expander_close_holo
							: R.drawable.expander_open_holo;
					mAdvancedAPIConfigLabel.setCompoundDrawablesWithIntrinsicBounds(compound_res, 0, 0, 0);
					inflated_view.setVisibility(is_visible ? View.GONE : View.VISIBLE);
				}
				break;
			}
		}
	}

	@Override
	public void onContentChanged() {
		super.onContentChanged();
		mEditRestBaseURL = (EditText) findViewById(R.id.rest_base_url);
		mEditAuthType = (RadioGroup) findViewById(R.id.auth_type);
		mButtonOAuth = (RadioButton) findViewById(R.id.oauth);
		mButtonxAuth = (RadioButton) findViewById(R.id.xauth);
		mButtonBasic = (RadioButton) findViewById(R.id.basic);
		mButtonTwipOMode = (RadioButton) findViewById(R.id.twip_o);
		mAdvancedAPIConfigContainer = findViewById(R.id.advanced_api_config_container);
		mAdvancedAPIConfigLabel = (TextView) findViewById(R.id.advanced_api_config_label);
		mSaveButton = (Button) findViewById(R.id.save);
	}

	@Override
	public void onSaveInstanceState(final Bundle outState) {
		saveEditedText();
		outState.putString(Accounts.REST_BASE_URL, mRestBaseURL);
		outState.putString(Accounts.SIGNING_REST_BASE_URL, mSigningRestBaseURL);
		outState.putString(Accounts.OAUTH_BASE_URL, mOAuthBaseURL);
		outState.putString(Accounts.SIGNING_OAUTH_BASE_URL, mSigningOAuthBaseURL);
		outState.putInt(Accounts.AUTH_TYPE, mAuthType);
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_api);
		final Bundle extras = getIntent().getExtras();
		if (savedInstanceState != null) {
			mRestBaseURL = savedInstanceState.getString(Accounts.REST_BASE_URL);
			mSigningRestBaseURL = savedInstanceState.getString(Accounts.SIGNING_REST_BASE_URL);
			mOAuthBaseURL = savedInstanceState.getString(Accounts.OAUTH_BASE_URL);
			mSigningOAuthBaseURL = savedInstanceState.getString(Accounts.SIGNING_OAUTH_BASE_URL);
			mConsumerKey = trim(savedInstanceState.getString(Accounts.CONSUMER_KEY));
			mConsumerSecret = trim(savedInstanceState.getString(Accounts.CONSUMER_SECRET));
			mAuthType = savedInstanceState.getInt(Accounts.AUTH_TYPE);
		} else if (extras != null) {
			mRestBaseURL = extras.getString(Accounts.REST_BASE_URL);
			mSigningRestBaseURL = extras.getString(Accounts.SIGNING_REST_BASE_URL);
			mOAuthBaseURL = extras.getString(Accounts.OAUTH_BASE_URL);
			mSigningOAuthBaseURL = extras.getString(Accounts.SIGNING_OAUTH_BASE_URL);
			mConsumerKey = trim(extras.getString(Accounts.CONSUMER_KEY));
			mConsumerSecret = trim(extras.getString(Accounts.CONSUMER_SECRET));
			mAuthType = extras.getInt(Accounts.AUTH_TYPE);
		}

		mEditAuthType.setOnCheckedChangeListener(this);
		mAdvancedAPIConfigLabel.setOnClickListener(this);
		mSaveButton.setOnClickListener(this);
		mEditRestBaseURL.setText(isEmpty(mRestBaseURL) ? DEFAULT_REST_BASE_URL : mRestBaseURL);

		mButtonOAuth.setChecked(mAuthType == Accounts.AUTH_TYPE_OAUTH);
		mButtonxAuth.setChecked(mAuthType == Accounts.AUTH_TYPE_XAUTH);
		mButtonBasic.setChecked(mAuthType == Accounts.AUTH_TYPE_BASIC);
		mButtonTwipOMode.setChecked(mAuthType == Accounts.AUTH_TYPE_TWIP_O_MODE);
	}

	private boolean checkUrlErrors() {
		boolean urlHasErrors = false;
		if (mEditRestBaseURL != null && !isValidUrl(mEditRestBaseURL.getText())) {
			mEditRestBaseURL.setError(getString(R.string.wrong_url_format));
			urlHasErrors = true;
		}
		if (mEditSigningRESTBaseURL != null && !isValidUrl(mEditSigningRESTBaseURL.getText())) {
			mEditSigningRESTBaseURL.setError(getString(R.string.wrong_url_format));
			urlHasErrors = true;
		}
		if (mEditOAuthBaseURL != null && !isValidUrl(mEditOAuthBaseURL.getText())) {
			mEditOAuthBaseURL.setError(getString(R.string.wrong_url_format));
			urlHasErrors = true;
		}
		if (mEditSigningOAuthBaseURL != null && !isValidUrl(mEditSigningOAuthBaseURL.getText())) {
			mEditSigningOAuthBaseURL.setError(getString(R.string.wrong_url_format));
			urlHasErrors = true;
		}
		return urlHasErrors;
	}

	private void saveEditedText() {
		if (mEditRestBaseURL != null) {
			mRestBaseURL = parseString(mEditRestBaseURL.getText());
		}
		if (mEditSigningRESTBaseURL != null) {
			mSigningRestBaseURL = parseString(mEditSigningRESTBaseURL.getText());
		}
		if (mEditOAuthBaseURL != null) {
			mOAuthBaseURL = parseString(mEditOAuthBaseURL.getText());
		}
		if (mEditSigningOAuthBaseURL != null) {
			mSigningOAuthBaseURL = parseString(mEditSigningOAuthBaseURL.getText());
		}
		if (mEditConsumerKey != null) {
			mConsumerKey = parseString(mEditConsumerKey.getText());
		}
		if (mEditConsumerSecret != null) {
			mConsumerSecret = parseString(mEditConsumerSecret.getText());
		}
	}
}
