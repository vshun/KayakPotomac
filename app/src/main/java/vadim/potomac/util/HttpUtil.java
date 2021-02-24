package vadim.potomac.util;

import java.io.BufferedInputStream;
import java.io.IOException;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


import android.content.Context;
import android.net.ConnectivityManager;

public class HttpUtil {
	   public static InputStream readFromURL (String urlString) throws IOException {
		   URLConnection conn = new URL(urlString ).openConnection();
		   conn.connect();

		   return conn.getInputStream();
	    }

	    public static String readFromURLNew (String urlString) throws IOException {
			OkHttpClient client = new OkHttpClient();

			Request request = new Request.Builder().url(urlString).build();

			try (Response response = client.newCall(request).execute()) {

				return response.body().string();
			}
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
