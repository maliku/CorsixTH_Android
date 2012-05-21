package uk.co.armedpineapple.corsixth.wizard;

import java.io.File;

import com.dreamdance.th.R;
import com.mobclick.android.MobclickAgent;
import uk.co.armedpineapple.corsixth.*;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

/**
 * Author: dk
 * Date: 12-5-14
 */
public class ExtractDataWizard  extends WizardView {
    static final String DEFAULT_PATH_ALIAS = "/mnt/sdcard/th";
    RadioGroup originalFilesRadioGroup;
    RadioButton automaticRadio;
    RadioButton cacheRadio;
    RadioButton manualRadio;

    String customLocation;

    Context ctx;
    String cachePath;
    String defaultPath;

    public ExtractDataWizard(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        ctx = context;
        init();
    }

    public ExtractDataWizard(Context context, AttributeSet attrs) {
        super(context, attrs);
        ctx = context;
        init();
    }

    public ExtractDataWizard(Context context) {
        super(context);
        ctx = context;
        init();
    }

    private void init() {
        String externalStoragePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        defaultPath = externalStoragePath + "/th";
        cachePath = ctx.getExternalFilesDir(null).getAbsolutePath();
        cachePath += "/th";
    }

    @Override
    void saveConfiguration(Configuration config) {
        String gamePath = null;

        if (automaticRadio.isChecked()) {
            gamePath = defaultPath;
        } else if (manualRadio.isChecked()) {
            gamePath = customLocation;
        } else {
            gamePath = cachePath;
        }
        config.setOriginalFilesPath(gamePath);
        MobclickAgent.onEvent(ctx, "GamePath", gamePath);
    }

    @Override
    void loadConfiguration(Configuration config) {

        originalFilesRadioGroup = ((RadioGroup) findViewById(R.id.originalFilesRadioGroup));
        automaticRadio = ((RadioButton) findViewById(R.id.automaticRadio));
        manualRadio = ((RadioButton) findViewById(R.id.manualRadio));
        cacheRadio = ((RadioButton) findViewById(R.id.cacheRadio));
        automaticRadio.setText(automaticRadio.getText() + "(" + defaultPath + ")");

        final EditText editTextBox = new EditText(ctx);
        editTextBox.setText(defaultPath);
        Builder builder = new Builder(ctx);
        builder.setMessage("Theme Hospital Game Files location");
        builder.setNeutralButton(ctx.getText(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                customLocation = editTextBox.getText().toString();
                manualRadio.setText(manualRadio.getText() + "(" + customLocation + ")");
            }
        });

        builder.setView(editTextBox);
        builder.setCancelable(false);

        final AlertDialog d = builder.create();

        manualRadio.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                d.show();
            }
        });

        String configFilePath = config.getOriginalFilesPath();
        if (configFilePath.equals(defaultPath)
                || DEFAULT_PATH_ALIAS.equals(defaultPath)) {
            automaticRadio.setChecked(true);
        } else if (configFilePath.equals(cachePath)) {
            cacheRadio.setChecked(true);
        } else {
            manualRadio.setChecked(true);
            // DK: may be default path in xml not equals to 'defaultPath', such as "/sdcard/th" and "/mnt/sdcard/th".
            // So give the customLocation a initialize value.
            customLocation = configFilePath;
        }
    }
}
