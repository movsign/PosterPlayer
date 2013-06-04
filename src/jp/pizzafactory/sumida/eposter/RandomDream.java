package jp.pizzafactory.sumida.eposter;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.service.dreams.DreamService;
import android.support.v4.view.ViewPager;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageView;

public class RandomDream extends DreamService {
	private static final long TIMER_PERIOD = 3000;
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
	}

	Handler handler = new Handler();
	Timer timer;

	@Override
	public void onDreamingStarted() {
		super.onDreamingStarted();
		timer = new Timer();
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				handler.post(new Runnable() {
					public void run() {
						mPager.setCurrentItem(mPager.getCurrentItem()
								+ (int) (Math.random() * 11) - 5);
					}
				});
			}
		}, TIMER_PERIOD, TIMER_PERIOD);
	}

	@Override
	public void onDreamingStopped() {
		super.onDreamingStopped();
		timer.cancel();
		timer.purge();
	}

	void choicePoster() {
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
