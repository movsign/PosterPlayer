package jp.pizzafactory.sumida.eposter;

import android.content.Intent;
import android.service.dreams.DreamService;

public class RandomDream extends DreamService {
	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
		setInteractive(false);
		setFullscreen(true);
		Intent i = new Intent(this, RandomActivity.class);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(i);
	}

}
