package uk.co.armedpineapple.corsixth.wizard;

import com.mobclick.android.MobclickAgent;
import uk.co.armedpineapple.corsixth.Configuration;
import com.dreamdance.th.R;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.Spinner;

public class LanguageWizard extends WizardView {

	Spinner languageSpinner;
	Context ctx;
	String[] langValuesArray;
	String[] langArray;

	public LanguageWizard(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		ctx = context;
	}

	public LanguageWizard(Context context, AttributeSet attrs) {
		super(context, attrs);
		ctx = context;
	}

	public LanguageWizard(Context context) {
		super(context);
		ctx = context;
	}

	@Override
	void saveConfiguration(Configuration config) {
		config.setLanguage(langValuesArray[languageSpinner
				.getSelectedItemPosition()]);
        MobclickAgent.onEvent(ctx, "Language",
                langValuesArray[languageSpinner.getSelectedItemPosition()]);
	}

	@Override
	void loadConfiguration(Configuration config) {
		languageSpinner = ((Spinner) findViewById(R.id.languageSpinner));
		langValuesArray = ctx.getResources().getStringArray(
				R.array.languages_values);
		langArray = ctx.getResources().getStringArray(R.array.languages);

		languageSpinner.setSelection(0);

        String systemLanguage = ctx.getString(R.string.language);
        for (int i = 0; i < langValuesArray.length; i++) {
            if (langValuesArray[i].equals(systemLanguage)) {
                languageSpinner.setSelection(i);
                break;
            }
        }

		// Look for the language in the values array
		for (int i = 0; i < langValuesArray.length; i++) {
			if (langValuesArray[i].equals(config.getLanguage())) {
				languageSpinner.setSelection(i);
				break;
			}
		}
	}

}
