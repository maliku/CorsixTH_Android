package uk.co.armedpineapple.corsixth;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import com.dreamdance.th.R;

public class Network {
	public static boolean HasNetworkConnection(Context ctx) {
		ConnectivityManager cm = (ConnectivityManager) ctx
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnected()) {
			return true;
		}
		return false;
	}
}
