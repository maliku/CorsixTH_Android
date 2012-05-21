package uk.co.armedpineapple.corsixth.wizard;

import java.io.File;

import com.mobclick.android.MobclickAgent;
import uk.co.armedpineapple.corsixth.*;
import com.dreamdance.th.R;
import uk.co.armedpineapple.corsixth.Files.DownloadFileTask;
import uk.co.armedpineapple.corsixth.Files.UnzipTask;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.util.AttributeSet;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class AudioWizard extends WizardView {

	CheckBox audioCheck;
	CheckBox fxCheck;
	CheckBox announcerCheck;
	CheckBox musicCheck;

	Context ctx;

	public AudioWizard(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.ctx = context;
	}

	public AudioWizard(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.ctx = context;
	}

	public AudioWizard(Context context) {
		super(context);
		this.ctx = context;
	}

	@Override
	void saveConfiguration(Configuration config) {

		config.setPlayAnnouncements(announcerCheck.isChecked());
		config.setPlayMusic(musicCheck.isChecked());
		config.setPlaySoundFx(fxCheck.isChecked());

        if (announcerCheck.isChecked()) {
            MobclickAgent.onEvent(ctx, "Audio", "announcer");
        }
        if (musicCheck.isChecked()) {
            MobclickAgent.onEvent(ctx, "Audio", "music");
        }
        if (fxCheck.isChecked()) {
            MobclickAgent.onEvent(ctx, "Audio", "soundFX");
        }
	}

	@Override
	void loadConfiguration(Configuration config) {

		audioCheck = ((CheckBox) findViewById(R.id.audioCheck));
		fxCheck = ((CheckBox) findViewById(R.id.fxCheck));
		announcerCheck = ((CheckBox) findViewById(R.id.announcerCheck));
		musicCheck = ((CheckBox) findViewById(R.id.musicCheck));

		audioCheck.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				fxCheck.setEnabled(isChecked);
				announcerCheck.setEnabled(isChecked);
				musicCheck.setEnabled(isChecked);
			}

		});

		musicCheck.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(final CompoundButton buttonView,
					boolean isChecked) {
				if (isChecked) {
					File timidityConfig = new File(
							"/sdcard/timidity/timidity.cfg");

					if (!(timidityConfig.isFile() && timidityConfig.canRead())) {

						AlertDialog.Builder builder = new AlertDialog.Builder(
								ctx);
						DialogInterface.OnClickListener alertListener = new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								if (which == DialogInterface.BUTTON_POSITIVE) {
									doTimidityDownload();
								} else {
									buttonView.setChecked(false);
								}
							}

						};

						builder.setMessage(
								ctx.getString(R.string.music_download_dialog))
								.setCancelable(true)
								.setNegativeButton(ctx.getString(R.string.cancel), alertListener)
								.setPositiveButton(ctx.getString(R.string.ok), alertListener);

						AlertDialog alert = builder.create();
						alert.show();

					}
				}
			}

		});

		fxCheck.setChecked(config.getPlaySoundFx());
		announcerCheck.setChecked(config.getPlayAnnouncements());
		musicCheck.setChecked(config.getPlayMusic());
		audioCheck.setChecked(config.getPlaySoundFx()
				|| config.getPlayAnnouncements() || config.getPlayMusic());

	}

	public void doTimidityDownload() {
		// Check for external storage
		if (Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState())) {
			// Check for network connection
			if (Network.HasNetworkConnection(ctx)) {
				final File extDir = new File(Utility.getDownloadDir());//ctx.getExternalFilesDir(null);
				final ProgressDialog dialog = new ProgressDialog(ctx);

				dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				dialog.setMessage(ctx.getString(R.string.downloading_timidity));
				dialog.setIndeterminate(false);
				dialog.setMax(100);
				dialog.setCancelable(false);

				final UnzipTask uzt = new Files.UnzipTask("/sdcard/timidity/") {

					@Override
					protected void onPostExecute(AsyncTaskResult<String> result) {
						super.onPostExecute(result);
						dialog.hide();

						if (result.getResult() != null) {
                            Toast.makeText(ctx, ctx.getString(R.string.data_ready), Toast.LENGTH_SHORT).show();
                            MobclickAgent.onEvent(ctx, "UnpackMusicData", "success");
						} else if (result.getError() != null) {
							Exception e = result.getError();
                            MobclickAgent.reportError(ctx, e.toString());
							Toast errorToast = Toast.makeText(ctx,
									R.string.download_timidity_error,
									Toast.LENGTH_LONG);

							errorToast.show();
							musicCheck.setChecked(false);
                            MobclickAgent.onEvent(ctx, "UnpackMusicData", "error");
						}
					}

					@Override
					protected void onPreExecute() {
						super.onPreExecute();
						dialog.setMessage(ctx
								.getString(R.string.extracting_timidity));

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

						Toast errorToast = Toast.makeText(ctx,
								R.string.download_timidity_error,
								Toast.LENGTH_LONG);

						if (result.getError() != null) {
                            MobclickAgent.reportError(ctx, result.getError().toString());
							musicCheck.setChecked(false);
							dialog.hide();
							errorToast.show();
                            MobclickAgent.onEvent(ctx, "GetMusicComponent", "error");
						} else {
							uzt.execute(result.getResult());
                            MobclickAgent.onEvent(ctx, "GetMusicComponent", "success");
						}
					}

					@Override
					protected void onPreExecute() {
						super.onPreExecute();
						dialog.show();
                        MobclickAgent.onEvent(ctx, "GetMusicComponent", "start");
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
                        musicCheck.setChecked(false);
                        MobclickAgent.onEvent(ctx, "GetMusicComponent", "cancel");
                    }
                });
				dft.execute(ctx.getString(R.string.timidity_url));
			} else {
				// Connection error
				Dialog connectionDialog = DialogFactory
						.createNetworkDialog(ctx);
				connectionDialog.show();
				musicCheck.setChecked(false);
			}
		} else {
			// No external storage
			Toast toast = Toast.makeText(ctx, R.string.no_external_storage,
					Toast.LENGTH_LONG);
			toast.show();
			musicCheck.setChecked(false);
		}
	}
}
