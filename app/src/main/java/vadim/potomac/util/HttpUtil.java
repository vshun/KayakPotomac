package vadim.potomac.util;

import java.io.IOException;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;


import android.content.Context;
import android.net.ConnectivityManager;

public class HttpUtil {
	   public static InputStream readFromURL (String urlString) throws IOException {
		   URLConnection conn = new URL(urlString ).openConnection();
		   conn.connect();
		   return conn.getInputStream();
	    }

		public static boolean checkInternetConnection(Context context) {
			ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			// test for connection
			assert cm != null;
			return ((cm.getActiveNetworkInfo() != null)
					&& cm.getActiveNetworkInfo().isAvailable()
					&& cm.getActiveNetworkInfo().isConnected());
		}
}
