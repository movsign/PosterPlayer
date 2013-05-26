package jp.pizzafactory.sumida.eposter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.PointF;
import android.support.v4.view.PagerAdapter;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
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
	private ArrayList<File> mList;

	/**
	 * コンストラクタ.
	 * 
	 * @param context
	 *            {@link Context}
	 * @param files
	 */
	public ImagePagerAdapter(Context context, File[] files) {
		mContext = context;
		mList = new ArrayList<File>();
		for (File f : files) {
			mList.add(f);
		}
	}

	/**
	 * アイテムを追加する.
	 * 
	 * @param id
	 *            ID
	 */
	public void add(File id) {
		mList.add(id);
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {

		// リストから取得
		File id = mList.get(position);
		Bitmap bitmap = null;
		try {
			MuPDFCore core = new MuPDFCore(mContext, id.getAbsolutePath());
			core.countPages();
			PointF rect = core.getPageSize(0);
			Display display = ((WindowManager) mContext
					.getSystemService(Context.WINDOW_SERVICE))
					.getDefaultDisplay();
			Point p = new Point();
			display.getSize(p);

			bitmap = core.drawPage(0, (int) rect.x, (int) rect.y, 0, 0, p.x * 15 / 10, p.y * 15 / 10);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// View を生成
		ImageView imageView = new ImageView(mContext);
		imageView.setImageBitmap(bitmap);

		// コンテナに追加
		container.addView(imageView);

		return imageView;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		// コンテナから View を削除
		container.removeView((View) object);
	}

	public File getId(int index) {
		return mList.get(index);
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