package jp.pizzafactory.sumida.eposter;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.service.dreams.DreamService;
import android.support.v4.view.ViewPager;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageView;

public class RandomDream extends DreamService {
	private ViewPager mPager;
	private ImagePagerAdapter adapter;

	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
		setInteractive(true);
		setFullscreen(true);

		setContentView(R.layout.random);
		mPager = (ViewPager) findViewById(R.id.viewpager);
		adapter = new ImagePagerAdapter(getBaseContext(), load());
		mPager.setAdapter(adapter);

		mPager.setOnTouchListener(new OnTouchListener() {

			public boolean onTouch(View v, MotionEvent event) {
				choicePoster();
				RandomDream.this.finish();
				return false;
			}
		});

		ImageView imageView = (ImageView) findViewById(R.id.home);
		imageView.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				Intent intent = new Intent(RandomDream.this, HomeActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
				RandomDream.this.finish();
			}
		});

		Intent intent = new Intent(RandomDream.this, ForcePortraitActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}

	Handler handler = new Handler();
	Timer timer;

	@Override
	public void onDreamingStarted() {
		super.onDreamingStarted();

		final SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(RandomDream.this);

		int timerSpan = Integer.parseInt(sp.getString("span", "5000"));
		timer = new Timer();
		timer.schedule(new TimerTask() {
			int[] hoursTable = new int[] { R.string.enable0, R.string.enable1,
					R.string.enable2, R.string.enable3, R.string.enable4,
					R.string.enable5, R.string.enable6, R.string.enable7,
					R.string.enable8, R.string.enable9, R.string.enable10,
					R.string.enable11, R.string.enable12, R.string.enable13,
					R.string.enable14, R.string.enable15, R.string.enable16,
					R.string.enable17, R.string.enable18, R.string.enable19,
					R.string.enable20, R.string.enable21, R.string.enable22,
					R.string.enable23 };

			@Override
			public void run() {
				handler.post(new Runnable() {
					public void run() {
						Calendar calendar = Calendar.getInstance();
						String idString = getString(hoursTable[calendar
								.get(Calendar.HOUR_OF_DAY)]);
						int visibility = sp.getBoolean(idString, true) ? View.VISIBLE
								: View.INVISIBLE;
						findViewById(R.id.viewpager).setVisibility(visibility);
						findViewById(R.id.home).setVisibility(visibility);
						if (visibility == View.VISIBLE) {
							mPager.setCurrentItem(mPager.getCurrentItem()
									+ (int) (Math.random() * 7) - 3);
						}
					}
				});
			}
		}, timerSpan, timerSpan);
	}

	@Override
	public void onDreamingStopped() {
		super.onDreamingStopped();
		timer.cancel();
		timer.purge();
	}

	@Override
	public void onDetachedFromWindow() {
		mPager.setAdapter(null);
		adapter = null;
		super.onDetachedFromWindow();
	}

	void choicePoster() {
		timer.cancel();
		timer.purge();

		int index = mPager.getCurrentItem();
		File id = adapter.getId(index);

		Intent intent = new Intent(RandomDream.this, ChoiceActivity.class);
		intent.setAction(Intent.ACTION_VIEW);
		intent.setData(Uri.fromFile(id));
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		startActivity(intent);
		finish();
	}

	private File[] load() {
		File dir = new File(Environment.getExternalStorageDirectory(),
				"Download");
		return dir.listFiles(new FilenameFilter() {
			private static final String EXTENT = ".pdf";

			public boolean accept(File dir, String filename) {
				return filename.endsWith(EXTENT);
			}
		});
	}

}
