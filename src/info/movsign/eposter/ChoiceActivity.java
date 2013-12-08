package info.movsign.eposter;

import java.io.InputStream;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageView;

import com.artifex.mupdfdemo.MuPDFCore;
import com.artifex.mupdfdemo.MuPDFPageAdapter;
import com.artifex.mupdfdemo.MuPDFReaderView;
import com.artifex.mupdfdemo.MuPDFReflowAdapter;
import com.artifex.mupdfdemo.OutlineActivityData;

public class ChoiceActivity extends Activity {
	private MuPDFCore core;
	private MuPDFReaderView mDocView;
	private boolean mReflow = false;

	private MuPDFCore openFile(String path) {
		System.out.println("Trying to open " + path);
		try {
			core = new MuPDFCore(this, path);
			// New file: drop the old outline data
			OutlineActivityData.set(null);
		} catch (Exception e) {
			System.out.println(e);
			return null;
		}
		return core;
	}

	private MuPDFCore openBuffer(byte buffer[]) {
		System.out.println("Trying to open byte buffer");
		try {
			core = new MuPDFCore(this, buffer);
			// New file: drop the old outline data
			OutlineActivityData.set(null);
		} catch (Exception e) {
			System.out.println(e);
			return null;
		}
		return core;
	}

	private void insertLog(Uri uri) {
		ContentValues values = new ContentValues();
		values.put("uri", uri.toString());
		getContentResolver().insert(
				Uri.parse("content://info.movsign.eposter.touchcountprovider"),
				values);
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		getWindow().addFlags(
				WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
						| WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
						| WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
		super.onCreate(savedInstanceState);

		if (core == null) {
			core = (MuPDFCore) getLastNonConfigurationInstance();
		}
		if (core == null) {
			Intent intent = getIntent();
			byte buffer[] = null;
			if (Intent.ACTION_VIEW.equals(intent.getAction())) {
				Uri uri = intent.getData();
				if (uri.toString().startsWith("content://")) {
					// Handle view requests from the Transformer Prime's file
					// manager
					// Hopefully other file managers will use this same scheme,
					// if not
					// using explicit paths.
					Cursor cursor = getContentResolver().query(uri,
							new String[] { "_data" }, null, null, null);
					if (cursor.moveToFirst()) {
						String str = cursor.getString(0);
						String reason = null;
						if (str == null) {
							try {
								InputStream is = getContentResolver()
										.openInputStream(uri);
								int len = is.available();
								buffer = new byte[len];
								is.read(buffer, 0, len);
								is.close();
							} catch (java.lang.OutOfMemoryError e) {
								System.out
										.println("Out of memory during buffer reading");
								reason = e.toString();
							} catch (Exception e) {
								reason = e.toString();
							}
							if (reason != null) {
								buffer = null;
								Resources res = getResources();
								setTitle(String
										.format(res
												.getString(R.string.cannot_open_document_Reason),
												reason));
								return;
							}
						} else {
							uri = Uri.parse(str);
						}
					}
				}
				if (buffer != null) {
					core = openBuffer(buffer);
				} else {
					core = openFile(Uri.decode(uri.getEncodedPath()));
				}
				if (core.countPages() == 0)
					core = null;

				insertLog(uri);
			}
			if (core != null && core.needsPassword()) {
				return;
			}
		}
		if (core == null) {
			return;
		}

		createUI(savedInstanceState);
	}

	public void createUI(Bundle savedInstanceState) {
		if (core == null)
			return;

		setContentView(R.layout.choice);
		mDocView = (MuPDFReaderView) findViewById(R.id.choiceview);
		// Now create the UI.
		// First create the document view
		// mDocView = new MuPDFReaderView(this);
		mDocView.setAdapter(new MuPDFPageAdapter(this, core));

		reflowModeSet(false);

		// Stick the document view and the buttons overlay into a parent view
		// RelativeLayout layout = new RelativeLayout(this);
		// layout.addView(mDocView);
		// layout.setBackgroundResource(R.drawable.tiled_background);
		// layout.setBackgroundResource(R.color.canvas);
		// setContentView(layout);
		ImageView imageView = (ImageView) findViewById(R.id.home);
		imageView.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				Intent intent = new Intent(ChoiceActivity.this,
						HomeActivity.class);
				startActivity(intent);
				ChoiceActivity.this.finish();
			}
		});
	}

	public Object onRetainNonConfigurationInstance() {
		MuPDFCore mycore = core;
		core = null;
		return mycore;
	}

	private void reflowModeSet(boolean reflow) {
		mReflow = reflow;
		mDocView.setAdapter(mReflow ? new MuPDFReflowAdapter(this, core)
				: new MuPDFPageAdapter(this, core));
		mDocView.refresh(mReflow);
	}

	public void onDestroy() {
		if (core != null)
			core.onDestroy();
		core = null;
		super.onDestroy();
	}

	@Override
	protected void onStart() {
		if (core != null) {
			core.startAlerts();
		}

		super.onStart();
		ImageView ivLeft = (ImageView) findViewById(R.id.swipe_left);
		ImageView ivRight = (ImageView) findViewById(R.id.swipe_right);

		if (core != null && core.countPages() != 1) {
			// ivLeft.setVisibility(View.VISIBLE);
			// AnimatorSet setLeft = (AnimatorSet)
			// AnimatorInflater.loadAnimator(this,
			// R.animator.blink);
			// setLeft.setTarget(ivLeft);
			// setLeft.start();

			ivRight.setVisibility(View.VISIBLE);
			AnimatorSet setRight = (AnimatorSet) AnimatorInflater.loadAnimator(
					this, R.animator.blink);
			setRight.setTarget(ivRight);
			setRight.start();
		} else {
			ivLeft.setVisibility(View.INVISIBLE);
			ivRight.setVisibility(View.INVISIBLE);
		}
	}

	@Override
	protected void onStop() {
		if (core != null) {
			core.stopAlerts();
		}

		super.onStop();
	}
}
