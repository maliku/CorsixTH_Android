package uk.co.armedpineapple.corsixth;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Properties;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

public class HttpDownloadTask extends AsyncTask<String, Integer, AsyncTaskResult<File>> {

	public static final int RES_OK = 0;
	public static final int RES_ERR_UNKNOW = 1;
	public static final int RES_ERR_TIMEOUT = 2;
	public static final int RES_ERR_CONNECT = 3;
	public static final int RES_ERR_SOCKET = 4;
	public static final int RES_ERR_SDCARD = 5;
	public static final int RES_ERR_IO = 6;
	
	public static String DOWNLOAD_DIR = Utility.getDownloadDir();
	
	private String mUrlStr;
    String mDownloadTo;

    public HttpDownloadTask(String downloadTo) {
        mDownloadTo = downloadTo;
    }

	@Override
	protected AsyncTaskResult<File> doInBackground(String... params) {

		int res = RES_ERR_UNKNOW;
		
		try {
            String urlStr = params[0];
            URL url = new URL(urlStr);
            Properties metaData = getMetaData(urlStr);

            mUrlStr = urlStr;

            long start = 0;

            if (metaData == null) {
                metaData = initMetaData(url);
            } else {
                File f = new File(getOutPathFromURL(urlStr));
                if (f.exists()) {
                    start = f.length();

                    long totalSize = Long.parseLong(metaData
                            .getProperty(CONTENT_LENGTH));

                    if (start == totalSize) {
                        // Download completed, return RES_OK immediately
                        return new AsyncTaskResult<File>(f);
                    }
                }
            }

            File file = httpDownload(url, start, metaData);
            return new AsyncTaskResult<File>(file);
        } catch (SocketTimeoutException e) {
			Log.e(TAG, "Connect/read Time out");
            return new AsyncTaskResult<File>(e);
		} catch (ConnectException e) {
			Log.e(TAG, "Can't connect download server");
            return new AsyncTaskResult<File>(e);
		} catch (SocketException e) {
			Log.e(TAG, "socket exception");
            return new AsyncTaskResult<File>(e);
		} catch (IOException e) {
			Log.e(TAG, "IO Error.");
            return new AsyncTaskResult<File>(e);
		} catch (Exception e) {
            return new AsyncTaskResult<File>(e);
        }
	}
	
	private static final int TIMEOUT_MS = 5000;
	
	public static final String CONTENT_LENGTH = "Content-Length";
	public static final String CONTENT_RANGE = "Content-Range";
	public static final String ETAG = "Etag";
	public static final String LAST_MODIFIED = "Last-Modified";
	public static final String RANGE = "Range";
	public static final String IF_RANGE = "If-Range";
	private static final String META_URL = "Url";
	
	
	private static final String TAG = "HttpDownloaderTask";
	private static final int BUF_SIZE = 8192;

	private File httpDownload(URL mUrl, long start, Properties metaData) throws IOException, SocketTimeoutException {
		HttpURLConnection urlConnection = (HttpURLConnection) mUrl
				.openConnection();

		urlConnection.setRequestMethod("GET");
		urlConnection.setDoOutput(true);
		urlConnection.setConnectTimeout(TIMEOUT_MS);
		urlConnection.setReadTimeout(TIMEOUT_MS);

		urlConnection.setRequestProperty(RANGE, "bytes=" + start + "-");
		urlConnection.setRequestProperty(IF_RANGE, metaData.getProperty(ETAG));

		urlConnection.connect();
		
		int respCode = urlConnection.getResponseCode();
		
		// TODO: handle 404 error
		
		debug(TAG, "HTTP Response code: " + urlConnection.getResponseCode());

		debug(TAG, CONTENT_LENGTH + ": " + urlConnection.getHeaderField(CONTENT_LENGTH));
		debug(TAG, CONTENT_RANGE + ": " + urlConnection.getHeaderField(CONTENT_RANGE));

		debug(TAG, ETAG + ":" + urlConnection.getHeaderField(ETAG));
		debug(TAG, LAST_MODIFIED + ": " + urlConnection.getHeaderField(LAST_MODIFIED));
		
		if (start > 0 && respCode != 206) {

			Log.d(TAG, "Etag changed! Re-download the entire file.");

			start = 0;

			// update meta file
			String contentLength = urlConnection.getHeaderField(CONTENT_LENGTH);
			if (null != contentLength) {
				metaData.setProperty(CONTENT_LENGTH, contentLength);
			}
			
			String lastModified = urlConnection.getHeaderField(LAST_MODIFIED);
			if (null != lastModified) {
				metaData.setProperty(LAST_MODIFIED, lastModified);
			}
			
			String etag = urlConnection.getHeaderField(ETAG);
			if (null != etag) {
				metaData.setProperty(ETAG, etag);
			}
			
			saveMetaFile(metaData, getMetaPathFromURL(mUrl.toString()));
		}

		File file = new File(getOutPathFromURL(mUrl.toString()));

		boolean append = false;
		if (start > 0) {
			append = true;
		}
		FileOutputStream fileOutput = new FileOutputStream(file, append);

		InputStream inputStream = urlConnection.getInputStream();
		
		long totalSize = urlConnection.getContentLength();
		if (start > 0) {
			String s = metaData.getProperty(CONTENT_LENGTH);
			if (s != null)
				totalSize = Long.parseLong(s);
		}
		long downloadedSize = start;

		byte[] buffer = new byte[BUF_SIZE];
		int readIn = 0;
		
		while (! isCancelled()) {
			readIn = inputStream.read(buffer);
			if (readIn <= 0) {
				// download completed
				break;
			}

			fileOutput.write(buffer, 0, readIn);
			downloadedSize += readIn;

			int p = (int)(downloadedSize * 100.0 / totalSize);
			publishProgress(p);
		}
		
		fileOutput.close();
		inputStream.close();
		
		urlConnection.disconnect();
        return file;
	}

	public static final String getFileNameFromURL(String url) {
		int idx = url.lastIndexOf('/');
		return url.substring(idx+1);
	}
	
	public static final String getOutPathFromURL(String url) {
		return DOWNLOAD_DIR + "/" + getFileNameFromURL(url);
	}

	public static final String getMetaPathFromURL(String url) {
		return getOutPathFromURL(url) + ".meta";
	}


	private void saveMetaFile(Properties metaData, String metaFilePath) throws IOException {
		FileOutputStream fos = new FileOutputStream(metaFilePath);
		metaData.store(fos, "Meta file for downloading from " + metaData.getProperty(META_URL));
		fos.close();

		Log.d(TAG, "Meta file has been saved - " + metaData.getProperty(META_URL));
	}


	private static void debug(String tag, String s) {
		if (s == null) s = "null";
		Log.d(tag, s);
	}
	
	public static int getDownloadedPercent(String url) {
		File mf = new File(getMetaPathFromURL(url));
		File f = new File(getOutPathFromURL(url));

		if (mf.exists()) {
			try {
				Properties p = new Properties();
				InputStream is = new FileInputStream(mf);
				p.load(is);
				is.close();

				if (p.getProperty(META_URL) != null
						&& p.getProperty(CONTENT_LENGTH) != null
						&& p.getProperty(LAST_MODIFIED) != null
						&& p.getProperty(ETAG) != null) {
					if (f.exists()) {
						long totalSize = Long.parseLong(p
								.getProperty(CONTENT_LENGTH));

						if (f.exists()) {
							long downloadedSize = f.length();

							if (downloadedSize <= totalSize)
								return (int) (downloadedSize * 100 / totalSize);
						}
					}
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			Log.d(TAG, "Meta file or downloaded file is broken!");
		}

		// clear files
		if (mf.exists())
			mf.delete();
		if (f.exists())
			f.delete();

		return -1;
	}

	private Properties getMetaData(String url) throws IOException {
		File mf = new File(getMetaPathFromURL(url));
		File f = new File(getOutPathFromURL(url));

		if (mf.exists()) {
			try {
				Properties metaData = new Properties();
				InputStream is = new FileInputStream(mf);
				metaData.load(is);
				is.close();

				if (metaData.getProperty(META_URL) != null
						&& metaData.getProperty(CONTENT_LENGTH) != null
						&& metaData.getProperty(LAST_MODIFIED) != null
						&& metaData.getProperty(ETAG) != null) {
					if (f.exists()) {
						long totalSize = Long.parseLong(metaData
								.getProperty(CONTENT_LENGTH));

						if (f.exists()) {
							long downloadedSize = f.length();
							
							if (downloadedSize <= totalSize) {
								// get valid meta data
								return metaData;
							}
						}
					}
				}
			} catch (IOException e) {
			}

			Log.d(TAG, "Meta file or downloaded file is broken!");
		}

		// clear files
		if (mf.exists())
			mf.delete();
		if (f.exists())
			f.delete();
		
		return null;
	}
	
	private Properties initMetaData(URL url) throws IOException {
		Log.d(TAG, "init meta data for url" + url);
		
		HttpURLConnection urlConnection = (HttpURLConnection) url
				.openConnection();

		urlConnection.setRequestMethod("GET");
		urlConnection.setConnectTimeout(TIMEOUT_MS);
		urlConnection.setReadTimeout(TIMEOUT_MS);
		
		urlConnection.connect();

		// TODO: handle 404 error
		
		Properties metaData = new Properties();

		debug(TAG, "HTTP Response code: " + urlConnection.getResponseCode());
		
		metaData.setProperty(META_URL, url.toString());

		String content_len = urlConnection.getHeaderField(CONTENT_LENGTH);
		if (null != content_len) {
			metaData.setProperty(CONTENT_LENGTH, content_len);
		}
		String last_modified = urlConnection.getHeaderField(LAST_MODIFIED);
		if (null != last_modified) {
			metaData.setProperty(LAST_MODIFIED, last_modified);
		}
		String etag = urlConnection.getHeaderField(ETAG);
		if (null != etag) {
			metaData.setProperty(ETAG, etag);
		}

		urlConnection.disconnect();

		saveMetaFile(metaData, getMetaPathFromURL(url.toString()));

		return metaData;
	}
}
