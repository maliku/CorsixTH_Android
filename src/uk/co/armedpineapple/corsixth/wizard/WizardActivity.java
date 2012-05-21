package uk.co.armedpineapple.corsixth.wizard;

import android.app.Activity;
import android.app.Dialog;
import android.content.res.*;
import android.widget.Toast;

import com.dreamdance.th.R;
import com.mobclick.android.MobclickAgent;
import uk.co.armedpineapple.corsixth.*;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;

import android.widget.Button;
import android.widget.ViewFlipper;
import uk.co.armedpineapple.corsixth.Configuration;

public class WizardActivity extends Activity {

	private ViewFlipper flipper;
	private Button previousButton;
	private Button nextButton;
	private WizardButtonClickListener buttonClickListener;
	private Configuration config;
	private SharedPreferences preferences;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        MobclickAgent.onError(this);
        MobclickAgent.update(this);
        checkResolution();

		if (!Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			Log.e(getClass().getSimpleName(), "Can't get storage.");

			// Show dialog and end
			DialogFactory.createExternalStorageDialog(this, true).show();

		} else {
			PreferenceManager.setDefaultValues(this, R.xml.prefs, false);

			preferences = PreferenceManager
					.getDefaultSharedPreferences(getBaseContext());

			if (preferences.getBoolean("wizard_run", false)) {
				Log.d(getClass().getSimpleName(), "Wizard isn't going to run.");
				finish();
				startActivity(new Intent(this, SDLActivity.class));
			} else {
				Log.d(getClass().getSimpleName(), "Wizard is going to run.");
				setContentView(R.layout.wizard);
				flipper = (ViewFlipper) findViewById(R.id.flipper);
				previousButton = (Button) findViewById(R.id.leftbutton);
				nextButton = (Button) findViewById(R.id.rightbutton);

				config = Configuration.loadFromPreferences(this, preferences);

				// Add all the wizard views

				LayoutInflater inflater = getLayoutInflater();
				loadAndAdd(inflater, flipper, (WizardView) inflater.inflate(
						R.layout.wizard_welcome, null));
				loadAndAdd(inflater, flipper,
						(LanguageWizard) inflater.inflate(
								R.layout.wizard_language, null));
				loadAndAdd(inflater, flipper,
						(ExtractDataWizard) inflater.inflate(
								R.layout.wizard_extractdatafiles, null));
				loadAndAdd(inflater, flipper, (DisplayWizard) inflater.inflate(
						R.layout.wizard_display, null));
				loadAndAdd(inflater, flipper, (AudioWizard) inflater.inflate(
						R.layout.wizard_audio, null));
				/*loadAndAdd(inflater, flipper,
						(AdvancedWizard) inflater.inflate(
								R.layout.wizard_advanced, null));*/

				// Setup Buttons
				previousButton.setVisibility(View.GONE);
				buttonClickListener = new WizardButtonClickListener();

				previousButton.setOnClickListener(buttonClickListener);
				nextButton.setOnClickListener(buttonClickListener);
			}
		}
	}

	public WizardView loadAndAdd(LayoutInflater inflater, ViewFlipper flipper,
			WizardView wv) {

		flipper.addView(wv);
		wv.loadConfiguration(config);
		return wv;
	}

	class WizardButtonClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			if (v.equals(previousButton)) {
				flipper.setInAnimation(WizardActivity.this,
						R.animator.wizard_anim_slideinright);
				flipper.setOutAnimation(WizardActivity.this,
						R.animator.wizard_anim_slideoutright);
				flipper.showPrevious();
			} else if (v.equals(nextButton)) {
				((WizardView) flipper.getCurrentView())
						.saveConfiguration(config);

				if (nextButton.getText() == getString(R.string.play)) {
                    config.saveToPreferences(WizardActivity.this, preferences);
                    finish();
                    WizardActivity.this.startActivity(new Intent(
                            WizardActivity.this, SDLActivity.class));
                    /*if (Utility.checkPathExists(config.getOriginalFilesPath() + "/DATA")) {
                        config.saveToPreferences(WizardActivity.this, preferences);
                        finish();
                        WizardActivity.this.startActivity(new Intent(
                                WizardActivity.this, SDLActivity.class));
                    } else {
                        Toast.makeText(WizardActivity.this,
                                WizardActivity.this.getText(R.string.need_game_data),
                                Toast.LENGTH_SHORT).show();
                    }*/
				} else {

					flipper.setInAnimation(WizardActivity.this,
                            R.animator.wizard_anim_slideinleft);
					flipper.setOutAnimation(WizardActivity.this,
							R.animator.wizard_anim_slideoutleft);
					flipper.showNext();
				}
			}

			if (hasNext(flipper)) {
				nextButton.setText(getString(R.string.nextButton));
			} else {
				nextButton.setText(getString(R.string.play));
			}
			if (hasPrevious(flipper)) {
				previousButton.setVisibility(View.VISIBLE);
			} else {
				previousButton.setVisibility(View.GONE);
			}

		}

		public boolean hasNext(ViewFlipper flipper) {
			if (flipper.indexOfChild(flipper.getCurrentView()) == flipper
					.getChildCount() - 1) {
				return false;
			}
			return true;
		}

		public boolean hasPrevious(ViewFlipper flipper) {
			if (flipper.indexOfChild(flipper.getCurrentView()) == 0) {
				return false;
			}
			return true;
		}

	}

    void checkResolution() {
        if (!Utility.isSuitableResolution(this)) {
            Dialog dialog = DialogFactory.createResolutionDialog(this);
            dialog.show();
        }
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
