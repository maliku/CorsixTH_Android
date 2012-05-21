package uk.co.armedpineapple.corsixth.wizard;

import android.app.Activity;
import android.util.Pair;
import com.mobclick.android.MobclickAgent;
import uk.co.armedpineapple.corsixth.Configuration;
import com.dreamdance.th.R;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.LinearLayout.LayoutParams;
import uk.co.armedpineapple.corsixth.Utility;

public class DisplayWizard extends WizardView {

	RadioGroup displayRadioGroup;
	RadioButton defaultResolutionRadio;
	RadioButton nativeResolutionRadio;
	RadioButton customResolutionRadio;

	int customWidth;
	int customHeight;
	Context ctx;

	public DisplayWizard(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		ctx = context;
	}

	public DisplayWizard(Context context, AttributeSet attrs) {
		super(context, attrs);
		ctx = context;
	}

	public DisplayWizard(Context context) {
		super(context);
		ctx = context;
	}

	@Override
	void saveConfiguration(Configuration config) {

		if (defaultResolutionRadio.isChecked()) {
			config.setResolutionMode(Configuration.RESOLUTION_DEFAULT);
            MobclickAgent.onEvent(ctx, "Display", "default");
		} else if (nativeResolutionRadio.isChecked()) {
			config.setResolutionMode(Configuration.RESOLUTION_NATIVE);
            MobclickAgent.onEvent(ctx, "Display", "native");
		} else if (customResolutionRadio.isChecked()) {
			config.setResolutionMode(Configuration.RESOLUTION_CUSTOM);
			config.setDisplayHeight(customHeight);
			config.setDisplayWidth(customWidth);
            MobclickAgent.onEvent(ctx, "Display", "custom");
		}
	}

	@Override
	void loadConfiguration(Configuration config) {

		displayRadioGroup = ((RadioGroup) findViewById(R.id.displayRadioGroup));
		defaultResolutionRadio = ((RadioButton) findViewById(R.id.defaultResolutionRadio));
		nativeResolutionRadio = ((RadioButton) findViewById(R.id.nativeResolutionRadio));
		customResolutionRadio = ((RadioButton) findViewById(R.id.customResolutionRadio));
        Pair<Integer, Integer> resolution = Utility.getResolution((Activity)ctx);
        nativeResolutionRadio.setText(ctx.getString(R.string.native_resolution) +
                "(" + resolution.first + "x" + resolution.second + ")");

		switch (config.getResolutionMode()) {
		case Configuration.RESOLUTION_DEFAULT:
			defaultResolutionRadio.setChecked(true);

			break;
		case Configuration.RESOLUTION_NATIVE:
			nativeResolutionRadio.setChecked(true);
			break;
		case Configuration.RESOLUTION_CUSTOM:
			customResolutionRadio.setChecked(true);
			customWidth = config.getDisplayWidth();
			customHeight = config.getDisplayHeight();
			break;
		}

		final LinearLayout linLayout = new LinearLayout(ctx);
		final EditText heightBox = new EditText(ctx);
		final EditText widthBox = new EditText(ctx);
		linLayout.setOrientation(LinearLayout.HORIZONTAL);
		heightBox.setHint(ctx.getString(R.string.height));
		widthBox.setHint(ctx.getString(R.string.width));
		heightBox.setInputType(InputType.TYPE_CLASS_NUMBER);
		widthBox.setInputType(InputType.TYPE_CLASS_NUMBER);

		android.widget.LinearLayout.LayoutParams heightParams = new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1f);

		android.widget.LinearLayout.LayoutParams widthParams = new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1f);

		heightParams.setMargins(2, 2, 2, 2);
		widthParams.setMargins(2, 2, 2, 2);

		heightBox.setLayoutParams(heightParams);
		widthBox.setLayoutParams(widthParams);

		linLayout.addView(widthBox);
		linLayout.addView(heightBox);

		final DisplayMetrics dm = new DisplayMetrics();
		((WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE))
				.getDefaultDisplay().getMetrics(dm);

		Builder builder = new Builder(ctx);
		builder.setMessage(ctx.getString(R.string.enter_resolution));
		builder.setNeutralButton(ctx.getText(R.string.ok), new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (!widthBox.getText().toString().trim().equals("")) {
					int givenWidth = Integer.valueOf(widthBox.getText()
							.toString());
					if (givenWidth > 0) {
						customWidth = Math.min(dm.widthPixels, givenWidth);
					} else {
						customWidth = 640;
					}

				} else {
					customWidth = 640;
				}
				if (!heightBox.getText().toString().trim().equals("")) {
					int givenHeight = Integer.valueOf(heightBox.getText()
							.toString());
					if (givenHeight > 0) {
						customHeight = Math.min(dm.heightPixels, givenHeight);
					} else {
						givenHeight = 480;
					}

				} else {
					customHeight = 480;
				}
				customResolutionRadio.setText("Custom (" + customWidth + "x"
						+ customHeight + ")");
			}
		});

		builder.setView(linLayout);

		final AlertDialog d = builder.create();

		customResolutionRadio.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				d.show();
			}
		});

	}
}
