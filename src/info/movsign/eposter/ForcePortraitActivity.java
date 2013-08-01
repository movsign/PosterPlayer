package info.movsign.eposter;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;

public class ForcePortraitActivity extends Activity {
	Handler handler;

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		handler = new Handler();
	}

	@Override
	public void onStart() {
		super.onStart();
		handler.postDelayed(new Runnable () {
			public void run() {
				ForcePortraitActivity.this.finish();
			}}, 3000);
	}
}
