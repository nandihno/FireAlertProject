package org.nando.firealert.task;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Xml;

import com.google.android.gms.maps.model.LatLng;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.nando.firealert.MapFragment;
import org.nando.firealert.pojo.RssItem;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by fernandoMac on 22/10/13.
 */
public class QldRssTask extends AsyncTask<Object,Void,ArrayList<RssItem>> {

    private Fragment mainFragment;
    ProgressDialog pd = null;

    public QldRssTask(Fragment fragment) {
        mainFragment  = fragment;

    }

    @Override
    protected void onPreExecute() {
        pd = new ProgressDialog(mainFragment.getActivity());
        pd.setTitle("Downloading latest incidents...");
        pd.setMessage("Please wait...");
        pd.setCancelable(false);
        pd.setIndeterminate(true);
        pd.show();
    }


    @Override
    protected ArrayList<RssItem> doInBackground(Object... params) {
        ArrayList<RssItem> rssItems = new ArrayList<RssItem>();
        rssItems.addAll(performRssConnectionTo((String)params[0]));
        rssItems.addAll(performRssConnectionTo((String)params[1]));
        return rssItems;
    }

    private ArrayList<RssItem> performRssConnectionTo(String urlSt) {
        ArrayList<RssItem> rssItems = new ArrayList<RssItem>();
        HttpURLConnection conn = null;
        try {

            URL url = new URL(urlSt);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();
            InputStream stream = conn.getInputStream();
            RssParser parser = new RssParser();
            rssItems = parser.parse(stream);

        } catch (Exception e) {
            conn.disconnect();
            e.printStackTrace();
        } finally {
            if(conn != null) {
                conn.disconnect();
            }
        }
        return rssItems;

    }


    protected void onPostExecute(ArrayList<RssItem> items) {
        pd.dismiss();
        pd = null;
        ((MapFragment)this.mainFragment).displayResults(items);
    }

    private class RssParser {

        private  final String ns = null;

        public ArrayList<RssItem> parse(InputStream inputStream) throws XmlPullParserException,IOException {
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES,false);
                parser.setInput(inputStream, null);
                parser.nextTag();
                return readFeed(parser);
            } finally {
                inputStream.close();
            }
        }

        private ArrayList<RssItem> readFeed(XmlPullParser parser) throws XmlPullParserException,IOException {
            parser.require(XmlPullParser.START_TAG,ns,"rss");

            ArrayList<RssItem> items = new ArrayList<RssItem>();
            while(parser.next() != XmlPullParser.END_DOCUMENT) {
                if(parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }
                String name = parser.getName();

                if(name.equals("channel")) {
                    items = readEntry(parser);
                }
                else {
                    skip(parser);
                }

            }
            return items;
        }

        private ArrayList<RssItem> readEntry(XmlPullParser parser) throws XmlPullParserException,IOException {
            parser.require(XmlPullParser.START_TAG, ns, "channel");
            ArrayList<RssItem> items = new ArrayList<RssItem>();
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }
                String name = parser.getName();

                if (name.equals("item")) {
                    items.add(readItem(parser));
                } else {
                    skip(parser);
                }
            }
            return items;
        }

        private RssItem readItem(XmlPullParser parser) throws XmlPullParserException,IOException {
            parser.require(XmlPullParser.START_TAG,ns,"item");
            RssItem item = new RssItem();
            while(parser.next() != XmlPullParser.END_TAG) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }
                String name = parser.getName();

                if (name.equals("description")) {
                    String bigDescription = readDescription(parser);
                    String [] arr = bigDescription.split("<br />");
                    item.alertLevel = arr[0];
                    item.location = arr[1];
                    item.reportedOn = arr[2];
                    item.currentStatus = arr[3];
                    item.details = arr[4];
                } else if (name.equals("pubDate")) {
                    item.pubDate = readPubDate(parser);
                } else if (name.equals("georss:point")) {
                    item.latLng = readGeo(parser);
                } else {
                    skip(parser);
                }

            }
            return item;
        }



        private String readDescription(XmlPullParser parser) throws XmlPullParserException,IOException {
            parser.require(XmlPullParser.START_TAG, ns, "description");
            String text = readText(parser);
            parser.require(XmlPullParser.END_TAG, ns, "description");
            return text;

        }

        private String readPubDate(XmlPullParser parser) throws XmlPullParserException,IOException {
            parser.require(XmlPullParser.START_TAG, ns, "pubDate");
            String text = readText(parser);
            parser.require(XmlPullParser.END_TAG, ns, "pubDate");
            return text;
        }

        private LatLng readGeo(XmlPullParser parser) throws XmlPullParserException,IOException {
            parser.require(XmlPullParser.START_TAG, ns, "georss:point");
            String text = readText(parser);
            parser.require(XmlPullParser.END_TAG, ns, "georss:point");
            String [] textArr = text.split(" ");
            Double latDbl = new Double(textArr[0]);
            Double lonDbl = new Double(textArr[1]);
            LatLng latLng = new LatLng(latDbl,lonDbl);

            return latLng;
        }

        private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
            String result = "";
            if (parser.next() == XmlPullParser.TEXT) {
                result = parser.getText();
                parser.nextTag();
            }
            return result;
        }

        // Skips tags the parser isn't interested in. Uses depth to handle nested tags. i.e.,
        // if the next tag after a START_TAG isn't a matching END_TAG, it keeps going until it
        // finds the matching END_TAG (as indicated by the value of "depth" being 0).
        private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                throw new IllegalStateException();
            }
            int depth = 1;
            while (depth != 0) {
                switch (parser.next()) {
                    case XmlPullParser.END_TAG:
                        depth--;
                        break;
                    case XmlPullParser.START_TAG:
                        depth++;
                        break;
                }
            }
        }



    }
}
