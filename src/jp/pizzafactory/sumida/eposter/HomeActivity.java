package jp.pizzafactory.sumida.eposter;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.artifex.mupdfdemo.MuPDFCore;
import com.jess.ui.TwoWayGridView;

public class HomeActivity extends Activity {
	private static List<Thumbnail> list;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		getWindow().addFlags(
				WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
						| WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
						| WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home);

		TwoWayGridView view = (TwoWayGridView) findViewById(R.id.thumbnail_grid);
		if (list == null) {
			list = load();
		}

		PDFAdapter adapter = new PDFAdapter(getApplicationContext(),
				R.layout.pdf_thumbnail, list);
		view.setAdapter(adapter);

	}

	@Override
	public void onStart() {
		super.onStart();
		AnimatorSet setLeft = (AnimatorSet) AnimatorInflater.loadAnimator(this,
				R.animator.blink);
		ImageView ivLeft = (ImageView) findViewById(R.id.swipe_left);
		setLeft.setTarget(ivLeft);
		setLeft.start();

		AnimatorSet setRight = (AnimatorSet) AnimatorInflater.loadAnimator(this,
				R.animator.blink);
		ImageView ivRight = (ImageView) findViewById(R.id.swipe_right);
		setRight.setTarget(ivRight);
		setRight.start();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		list = null;
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
					Intent intent = new Intent(getApplicationContext(),
							ChoiceActivity.class);
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

	private List<Thumbnail> load() {
		ArrayList<Thumbnail> list = new ArrayList<Thumbnail>();
		File dir = new File(Environment.getExternalStorageDirectory(),
				"Download");
		File[] pdfs = dir.listFiles(new FilenameFilter() {
			private static final String EXTENT = ".pdf";

			public boolean accept(File dir, String filename) {
				return filename.endsWith(EXTENT);
			}
		});

		for (File pdf : pdfs) {

			try {
				MuPDFCore core = new MuPDFCore(getApplicationContext(),
						pdf.getAbsolutePath());
				core.countPages();
				PointF pf = core.getPageSize(0);
				Bitmap bitmap = core.drawPage(0, (int) pf.x, (int) pf.y, 0, 0,
						(int) pf.x, (int) pf.y);
				float scalex = 228.0f / pf.x;
				float scaley = 320.0f / pf.y;
				Matrix matrix = new Matrix();
				if (scalex > scaley) {
					matrix.postScale(scaley, scaley);
				} else {
					matrix.postScale(scalex, scalex);
				}
				Bitmap thumb = Bitmap.createBitmap(bitmap, 0, 0, (int) pf.x,
						(int) pf.y, matrix, false);
				list.add(new Thumbnail(thumb, Uri.fromFile(pdf)));
				core.onDestroy();
			} catch (Exception e) {
			}
		}
		return list;
	}
}