package uk.co.armedpineapple.corsixth;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import com.dreamdance.th.R;
import com.mobclick.android.MobclickAgent;

public class PrefsActivity extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.prefs);
	}

    @Override
    protected void onResume() {
        super.onResume();    //To change body of overridden methods use File | Settings | File Templates.
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();    //To change body of overridden methods use File | Settings | File Templates.
        MobclickAgent.onPause(this);
    }
}
