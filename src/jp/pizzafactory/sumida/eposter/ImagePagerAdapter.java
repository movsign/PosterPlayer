package jp.pizzafactory.sumida.eposter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PointF;
import android.support.v4.view.PagerAdapter;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import com.artifex.mupdfdemo.MuPDFCore;

/**
 * 画像を表示する PagerAdapter.
 */
public class ImagePagerAdapter extends PagerAdapter {

	/** コンテキスト. */
	private Context mContext;

	/** File のリスト. */
	private ArrayList<Poster> mList;

	private class Poster {
		File file;
		ImageView imageView;

		Poster(File file, ImageView bitmap) {
			this.file = file;
			this.imageView = bitmap;
		}
	}
	/**
	 * コンストラクタ.
	 * 
	 * @param context
	 *            {@link Context}
	 * @param files
	 */
	public ImagePagerAdapter(Context context, File[] files) {
		mContext = context;
		mList = new ArrayList<Poster>();
		for (File f : files) {
			add(f);
		}
	}

	/**
	 * アイテムを追加する.
	 * 
	 * @param id
	 *            ID
	 */
	public void add(File id) {
		mList.add(new Poster(id, null));
	}
	
	public void add(Poster p) {
		mList.add(p);
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {

		if (position > mList.size()) {
			return null;
		}
		// リストから取得
		Poster poster = mList.get(position);
		if (poster.imageView == null) {
			Bitmap bitmap = null;
			try {
				Display display = ((WindowManager) mContext
						.getSystemService(Context.WINDOW_SERVICE))
						.getDefaultDisplay();
				Point p = new Point();
				display.getSize(p);
	
				MuPDFCore core = new MuPDFCore(mContext, poster.file.getAbsolutePath());
				core.countPages();
				PointF rect = core.getPageSize(0);
				Bitmap pdfBitmap = core.drawPage(0, (int) rect.x, (int) rect.y, 0, 0, (int)rect.x, (int)rect.y);
				core.onDestroy();
				float scalex = (float)p.x / rect.x;
				float scaley = (float)p.y / rect.y;
				Matrix matrix = new Matrix();
				if (scalex > scaley) {
					matrix.postScale(scaley, scaley);
				} else {
					matrix.postScale(scalex, scalex);
				}
				bitmap = Bitmap.createBitmap(pdfBitmap, 0, 0, (int)rect.x, (int)rect.y, matrix, false);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// View を生成
			poster.imageView = new ImageView(mContext);
			poster.imageView.setImageBitmap(bitmap);
		}


		// コンテナに追加
		container.addView(poster.imageView);

		return poster.imageView;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		// コンテナから View を削除
		container.removeView((View) object);
	}

	public File getId(int index) {
		return mList.get(index).file;
	}

	@Override
	public int getCount() {
		return mList.size();
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view.equals(object);
	}
}