package info.movsign.eposter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.artifex.mupdfdemo.MuPDFCore;
import com.jess.ui.TwoWayGridView;

public class HomeActivity extends Activity {
	public class PosterMetrics {
		int width;
		int height;
	}

	private static List<Thumbnail> list;
	private DisplayMetrics metrics;
	private PosterMetrics posterMetrics = new PosterMetrics();

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		getWindow().addFlags(
				WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
						| WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
						| WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home);

		metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		int width = metrics.widthPixels;
		posterMetrics.width = width * 3 / 10; /* means (w / 3) * 0.9 */
		int posterWidthSpacing = width / 30;
		int height = metrics.heightPixels;
		posterMetrics.height = height * 21 / 80; /* means ((h * 7 / 8) / 3) * 0.9 */
		int posterHeightSpacing = height * 7 / 240; /*
													 * means ((h * 7 / 8) / 3) *
													 * 0.1
													 */
		System.out.println(metrics.heightPixels);
		TwoWayGridView view = (TwoWayGridView) findViewById(R.id.thumbnail_grid);
		view.setHorizontalSpacing(posterWidthSpacing);
		view.setVerticalSpacing(posterHeightSpacing);
		view.setColumnWidth(posterMetrics.width);
		view.setRowHeight(posterMetrics.height);

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

		AnimatorSet setRight = (AnimatorSet) AnimatorInflater.loadAnimator(
				this, R.animator.blink);
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

	private Cursor getPdfCursor() {
		ContentResolver cr = getContentResolver();
		Uri uri = MediaStore.Files.getContentUri("external");

		// every column, although that is huge waste, you probably need
		// BaseColumns.DATA (the path) only.
		String[] projection = { MediaStore.Files.FileColumns.DATA };
		String sortOrder = null; // unordered

		String selectionMimeType = MediaStore.Files.FileColumns.MIME_TYPE
				+ "=?";
		String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
				"pdf");
		String[] selectionArgsPdf = new String[] { mimeType };
		Cursor allPdfFiles = cr.query(uri, projection, selectionMimeType,
				selectionArgsPdf, sortOrder);
		allPdfFiles.moveToFirst();
		return allPdfFiles;
	}

	private List<HomeActivity.Thumbnail> load() {
		list = new ArrayList<HomeActivity.Thumbnail>();
		Cursor allPdfFiles = getPdfCursor();
		while (allPdfFiles.moveToNext()) {
			String fileName = allPdfFiles.getString(0);

			try {
				MuPDFCore core = new MuPDFCore(getApplicationContext(),
						fileName);
				core.countPages();
				Point p = new Point(posterMetrics.width, posterMetrics.height);
				PointF rect = core.getPageSize(0);
				if (rect.y / rect.x < 1.2) {
					p.y = (int) (p.x * rect.y / rect.x);
				}
				Bitmap thumb = core.drawPage(0, p.x, p.y, 0, 0, p.x, p.y);
				list.add(new Thumbnail(thumb, Uri.fromFile(new File(fileName))));
				core.onDestroy();
			} catch (Exception e) {
			}
		}
		return list;
	}
}
