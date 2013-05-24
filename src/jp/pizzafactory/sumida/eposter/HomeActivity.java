package jp.pizzafactory.sumida.eposter;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.artifex.mupdfdemo.MuPDFCore;

public class HomeActivity extends Activity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		getWindow().addFlags(
				WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
						| WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
						| WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home);
		getActionBar().hide();
		
		ViewPager view = (ViewPager) findViewById(R.id.viewpager);
		view.setOnTouchListener(new OnTouchListener() {

			public boolean onTouch(View view, MotionEvent motionEvent) {
				Intent intent;
				float y = motionEvent.getY();
				if (y < 50.0f) {
					intent = new Intent(HomeActivity.this, RandomActivity.class);
					startActivity(intent);
					return true;
				} else {
					return false;
				}
			}
		});

		PagerAdapter pagerAdapter = new MyPagerAdapter();
		view.setAdapter(pagerAdapter);
	}

	private class PDFAdapter extends ArrayAdapter<Thumbnail> {
		private int resourceId;

		public PDFAdapter(Context context, int resource, List<Thumbnail> objects) {
			super(context, resource, objects);
			resourceId = resource;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			if (convertView == null) {
				LayoutInflater inflater = (LayoutInflater) getContext()
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(resourceId, null);
			}

			ImageView view = (ImageView) convertView;
			final Thumbnail thumbnail = getItem(position);
			view.setImageBitmap(thumbnail.bitmap);
			view.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					Intent intent = new Intent(getApplicationContext(), ChoiceActivity.class);
					intent.setAction(Intent.ACTION_VIEW);
					intent.setData(thumbnail.data);
					startActivity(intent);
				}
			});

			return view;
		}

	}

	class Thumbnail {
		final Bitmap bitmap;
		final Uri data;
		Thumbnail(Bitmap bitmap, Uri data) {
			this.bitmap = bitmap;
			this.data = data;
		}
	}
	private List<Thumbnail> load(int page) {
		ArrayList<Thumbnail> list = new ArrayList<Thumbnail>();
		int i = 0;
        File dir = new File(Environment.getExternalStorageDirectory(), "Download");
        File[] pdfs = dir.listFiles(new FilenameFilter() {
			private static final String EXTENT = ".pdf";
			public boolean accept(File dir, String filename) {
				return filename.endsWith(EXTENT);
			}
		});
        
        for (File pdf : pdfs) {
        	if (i >= (page + 1) * 9) {
        		break;
        	}
        	if (i++ < page * 9) {
        		continue;
        	}

        	try {
				MuPDFCore core = new MuPDFCore(getApplicationContext(), pdf.getAbsolutePath());
				core.countPages();
				Bitmap bitmap = core.drawPage(0, 500, 700, 0, 0, 300, 420);
				list.add(new Thumbnail(bitmap, Uri.fromFile(pdf)));
			} catch (Exception e) {
			}
        }
		return list;
	}
	
	private class MyPagerAdapter extends PagerAdapter {
		@Override
        public Object instantiateItem(ViewGroup container, int position) {
            int[] pages = {R.layout.page1, R.layout.page2, R.layout.page3};
            LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            GridView tv = (GridView)inflater.inflate(pages[position], null);
            List<Thumbnail> list = load(position);
            PDFAdapter adapter = new PDFAdapter(getApplicationContext(), R.layout.pdf_thumbnail, list);
            tv.setAdapter(adapter);
            container.addView(tv);
            return tv;
        }

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			((ViewPager) container).removeView((View) object);
		}

		@Override
		public int getCount() {
			return 3;
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view.equals(object);
		}

	}
}