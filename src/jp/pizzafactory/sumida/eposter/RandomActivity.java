package jp.pizzafactory.sumida.eposter;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.view.ViewPager;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;

public class RandomActivity extends Activity {
	private static final long TIMER_PERIOD = 5000;
	private ViewPager mPager;
	private ImagePagerAdapter adapter;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		getWindow().addFlags(
				WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
						| WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
						| WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
		super.onCreate(savedInstanceState);

		setContentView(R.layout.random);
		mPager = (ViewPager) findViewById(R.id.viewpager);

		getLoaderManager().initLoader(0, null, callbacks);

		mPager.setOnTouchListener(new OnTouchListener() {

			public boolean onTouch(View v, MotionEvent event) {
				choicePoster();
				RandomActivity.this.finish();
				return false;
			}});

		ImageButton imageButton = (ImageButton) findViewById(R.id.home);
		imageButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				Intent intent = new Intent(RandomActivity.this,
						HomeActivity.class);
				startActivity(intent);
				RandomActivity.this.finish();
			}
		});
	}

	Handler handler = new Handler();
	Timer timer;
	@Override
	public void onStart() {
		super.onStart();
		timer = new Timer();
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				handler.post(new Runnable() {
					public void run() {
						mPager.setCurrentItem(mPager.getCurrentItem()
								+ (int) (Math.random() * 5) - 2);
					}
				});
			}
		}, TIMER_PERIOD, TIMER_PERIOD);
	}

	@Override
	public void onStop() {
		super.onStop();
		timer.cancel();
		timer.purge();
	}

	void choicePoster() {
		Intent intent = new Intent(RandomActivity.this, ChoiceActivity.class);
		int index = mPager.getCurrentItem();
		Long id = adapter.getId(index);
		intent.putExtra("pageid", id);

		startActivity(intent);
		finish();
	}

	/** CursorLoader のコールバック. */
	private LoaderCallbacks<Cursor> callbacks = new LoaderCallbacks<Cursor>() {

		public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
			Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
			return new CursorLoader(getApplicationContext(), uri, null, null,
					null, null);
		}

		public void onLoaderReset(Loader<Cursor> loader) {
		}

		public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
			// Cursor から id を取得して PagerAdapter に入れる
			adapter = new ImagePagerAdapter(
					RandomActivity.this);
			c.moveToFirst();
			do {
				long id = c
						.getLong(c
								.getColumnIndexOrThrow(MediaStore.Images.ImageColumns._ID));
				adapter.add(id);
			} while (c.moveToNext());
			// ViewPager にセット
			mPager.setAdapter(adapter);
		}
	};

}
