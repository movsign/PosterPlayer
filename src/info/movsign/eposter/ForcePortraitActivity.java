package info.movsign.eposter;

import android.app.Activity;
import android.content.Intent;
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
				Intent intent = new Intent(ForcePortraitActivity.this, HomeActivity.class);
				startActivity(intent);
				ForcePortraitActivity.this.finish();
			}}, 3000);
	}
}
