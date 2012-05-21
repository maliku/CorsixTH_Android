package uk.co.armedpineapple.corsixth.wizard;

import java.io.File;

import com.dreamdance.th.R;
import com.mobclick.android.MobclickAgent;
import uk.co.armedpineapple.corsixth.*;
import uk.co.armedpineapple.corsixth.Files.UnzipTask;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

public class OriginalFilesWizard extends WizardView {

	RadioGroup originalFilesRadioGroup;
	RadioButton automaticRadio;
	RadioButton manualRadio;
	RadioButton downloadDemoRadio;
    RadioButton lastChecked;

	String customLocation;

	Context ctx;

	public OriginalFilesWizard(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		ctx = context;
        lastChecked = automaticRadio;
	}

	public OriginalFilesWizard(Context context, AttributeSet attrs) {
		super(context, attrs);
		ctx = context;
        lastChecked = automaticRadio;
	}

	public OriginalFilesWizard(Context context) {
		super(context);
		ctx = context;
        lastChecked = automaticRadio;
	}

	@Override
	void saveConfiguration(Configuration config) {
		if (automaticRadio.isChecked()) {
			config.setOriginalFilesPath("/sdcard/th");
            MobclickAgent.onEvent(ctx, "GamePath", "/sdcard/th");
		} else if (manualRadio.isChecked() || downloadDemoRadio.isChecked()) {
			config.setOriginalFilesPath(customLocation);
            MobclickAgent.onEvent(ctx, "GamePath", customLocation);
		}
	}

	@Override
	void loadConfiguration(Configuration config) {

		originalFilesRadioGroup = ((RadioGroup) findViewById(R.id.originalFilesRadioGroup));
		automaticRadio = ((RadioButton) findViewById(R.id.automaticRadio));
		manualRadio = ((RadioButton) findViewById(R.id.manualRadio));
		downloadDemoRadio = ((RadioButton) findViewById(R.id.downloadDemoRadio));
        automaticRadio.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                lastChecked = automaticRadio;
            }
        });

		final EditText editTextBox = new EditText(ctx);
		editTextBox.setText("/sdcard/th");
		Builder builder = new Builder(ctx);
		builder.setMessage("Theme Hospital Game Files location");
		builder.setNeutralButton(ctx.getText(R.string.ok), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				customLocation = editTextBox.getText().toString();
				manualRadio.setText("Custom (" + customLocation + ")");
                lastChecked = manualRadio;
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

		if (config.getOriginalFilesPath().equals("/sdcard/th")) {
			automaticRadio.setChecked(true);
            lastChecked = automaticRadio;
		} else {
			manualRadio.setChecked(true);
            lastChecked = manualRadio;
		}

		downloadDemoRadio.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (Environment.MEDIA_MOUNTED.equals(Environment
						.getExternalStorageState())) {
					if (!Utility.checkPathExists("/sdcard/th/DATA")) {

						AlertDialog.Builder builder = new AlertDialog.Builder(
								ctx);
						DialogInterface.OnClickListener alertListener = new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								if (which == DialogInterface.BUTTON_POSITIVE) {

									doDemoDownload();

								} else {
                                    lastChecked.setChecked(true);
                                }
							}

						};

						builder.setMessage(
								getResources().getString(
										R.string.download_demo_dialog))
								.setCancelable(true)
								.setNegativeButton(ctx.getText(R.string.cancel), alertListener)
								.setPositiveButton(ctx.getText(R.string.ok), alertListener);

						AlertDialog alert = builder.create();
						alert.show();
					} else {
						customLocation = "/sdcard/th";
                        Toast.makeText(ctx, ctx.getText(R.string.no_need_download), Toast.LENGTH_SHORT).show();
					}
				}
			}
		});
	}

	void doDemoDownload() {
		// Check that the external storage is mounted.
		if (Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState())) {

			// Check that there is an active network connection
			if (Network.HasNetworkConnection(ctx)) {
				final File extDir = new File(HttpDownloadTask.DOWNLOAD_DIR);
				final ProgressDialog dialog = new ProgressDialog(ctx);

				dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				dialog.setMessage(ctx.getString(R.string.downloading_demo));
				dialog.setIndeterminate(false);
				dialog.setMax(100);
				dialog.setCancelable(false);

				final UnzipTask uzt = new Files.UnzipTask("/sdcard/") {

					@Override
					protected void onPostExecute(AsyncTaskResult<String> result) {
						super.onPostExecute(result);
						dialog.hide();

						if (result.getResult() != null) {
							customLocation = result.getResult() + "th";
							Log.d(getClass().getSimpleName(),
									"Extracted TH: " + customLocation);
                            lastChecked = downloadDemoRadio;
                            MobclickAgent.onEvent(ctx, "UnpackGameData", "success");
                            Toast.makeText(ctx, ctx.getString(R.string.data_ready), Toast.LENGTH_SHORT).show();
						} else if (result.getError() != null) {
							Exception e = result.getError();
                            MobclickAgent.reportError(ctx, e.toString());
							DialogFactory
									.createFromException(
											result.getError(),
											ctx.getString(R.string.download_demo_error),
											ctx, false).show();
							lastChecked.setChecked(true);
                            MobclickAgent.onEvent(ctx, "UnpackGameData", "error");
						}
					}

					@Override
					protected void onPreExecute() {
						super.onPreExecute();
						dialog.setMessage(ctx
								.getString(R.string.extracting_demo));
                        MobclickAgent.onEvent(ctx, "UnpackGameData", "start");
					}

					@Override
					protected void onProgressUpdate(Integer... values) {
						super.onProgressUpdate(values);
						dialog.setProgress(values[0]);
					}

				};

				final HttpDownloadTask dft = new HttpDownloadTask(
						extDir.getAbsolutePath()) {

					@Override
					protected void onPostExecute(AsyncTaskResult<File> result) {
						super.onPostExecute(result);

						if (result.getError() != null) {
                            MobclickAgent.reportError(ctx, result.getError().toString());
							lastChecked.setChecked(true);
							dialog.hide();

							DialogFactory
									.createFromException(
											result.getError(),
											ctx.getString(R.string.download_demo_error),
											ctx, false).show();
                            MobclickAgent.onEvent(ctx, "DownloadGameData", "error");
						} else {
							uzt.execute(result.getResult());
                            MobclickAgent.onEvent(ctx, "DownloadGameData", "success");
						}
					}

					@Override
					protected void onPreExecute() {
						super.onPreExecute();
						dialog.show();
                        MobclickAgent.onEvent(ctx, "DownloadGameData", "start");
					}

					@Override
					protected void onProgressUpdate(Integer... values) {
						super.onProgressUpdate(values);
						dialog.setProgress(values[0]);
					}

				};
                dialog.setButton(ctx.getText(R.string.stop), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dft.cancel(true);
                        dialog.hide();
                        lastChecked.setChecked(true);
                        MobclickAgent.onEvent(ctx, "DownloadGameData", "cancel");
                    }
                });

				dft.execute(ctx.getString(R.string.demo_url));
			} else {
				// Connection error
				Dialog connectionDialog = DialogFactory
						.createNetworkDialog(ctx);
				connectionDialog.show();
				lastChecked.setChecked(true);
			}
		} else {
			// External storage error
			Toast toast = Toast.makeText(ctx, R.string.no_external_storage,
                    Toast.LENGTH_LONG);
			toast.show();
			lastChecked.setChecked(true);
		}
	}
}
