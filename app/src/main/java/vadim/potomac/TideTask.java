package vadim.potomac;

import vadim.playpotomac.R;
import vadim.potomac.model.TideInfo;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import vadim.potomac.util.HttpUtil;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Date;

class TideTask extends AsyncTask<String, Void, TideInfo> {
    private static final String TAG = "PlayPotomac.Tide";

    private final WeakReference<CurrentConditionsFragment> fragmentWeakRef;

    TideTask(CurrentConditionsFragment fragment)  {
        this.fragmentWeakRef = new WeakReference<>(fragment);
    }

    @Override
    protected TideInfo doInBackground(String... params) {
        try {

            // add today date to URL
            // date is in yyyymmdd format
            String timeStamp = new SimpleDateFormat("yyyyMMdd").format(new Date());
            String tideUrl = params[0] + "&begin_date=" + timeStamp + "&end_date=" + timeStamp;
            // for reference service URL- https://tidesandcurrents.noaa.gov/api/datagetter?product=predictions&application=NOS.COOPS.TAC.WL&datum=MLLW&station=8594900&time_zone=lst_ldt&units=english&interval=hilo&format=xml&begin_date=20171111&end_date=20171111

            InputStream stream = HttpUtil.readFromURL(tideUrl);
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(stream, null);

            TideInfo tideInfo = new TideInfo();
            for (int eventType = xpp.getEventType();eventType != XmlPullParser.END_DOCUMENT; eventType = xpp.next()) {
                if((eventType == XmlPullParser.START_TAG)) {
                    // <pr t="2017-11-11 01:21" v="2.967" type="H"/>
                    String name = xpp.getName();
                    if (name.equals("pr")) {
                        String dateTime = xpp.getAttributeValue(null, "t");
                        if (dateTime != null) {
                            String[] el = dateTime.split(" ");
                            if (el.length > 1) {
                                String time = el[1];
                                String type = xpp.getAttributeValue(null, "type");
                                if (time != null && type != null)
                                    tideInfo.addReading(time, type);
                            }
                        }
                    }
                }
            }
            return tideInfo;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return null;
    }

    @SuppressLint("SetTextI18n")
    protected void onPostExecute(TideInfo tideInfo) {
        try {
            super.onPostExecute(tideInfo);
            CurrentConditionsFragment fragment = this.fragmentWeakRef.get();
            if (fragment == null) return; // be cautious if fragment gets dropped
            View rootView = fragment.getView();
            if (tideInfo != null && rootView != null) {
                formatTideInfoToView (tideInfo.getLows(),
                        (TextView) rootView.findViewById(R.id.lowTide));
                formatTideInfoToView (tideInfo.getHighs(),
                        (TextView) rootView.findViewById(R.id.highTide));
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private void formatTideInfoToView (List<String> tideInfo, TextView view) {
        StringBuilder tideBuffer = new StringBuilder();

        if (tideInfo.size() > 0)
            tideBuffer.append(tideInfo.get(0));

        if (tideInfo.size() > 1) {
            tideBuffer.append(" and ");
            tideBuffer.append(tideInfo.get(1));
        }

        view.setText(tideBuffer.toString());
    }
}
