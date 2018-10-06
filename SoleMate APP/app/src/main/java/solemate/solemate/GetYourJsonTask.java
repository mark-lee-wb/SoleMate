////package solemate.solemate;
////
////import android.app.Activity;
////import android.content.Context;
////import android.content.Intent;
////import android.os.AsyncTask;
////import android.os.Handler;
////
////import org.json.JSONArray;
////import org.json.JSONException;
////import org.json.JSONObject;
////
////
//public class GetYourJsonTask extends AsyncTask<ApiConnector,Long,JSONObject>
////{
////    int id = 0;
////    private Context context;
////    private AsyncListener asyncInterface;
////    GetYourJsonTask
////
////    protected JSONObject doInBackground(ApiConnector... params) {
////
////        // it is executed on Background thread
////        System.out.println("test" + params[0].GetYourJson());
////        try {
////            id = Integer.parseInt(params[0].GetYourJson().getString("entry_id"));
////        } catch (JSONException e) {
////            e.printStackTrace();
////        }
////        return params[0].GetYourJson();
////    }
////    @Override
////    protected void onPostExecute(JSONObject jsonArray) {
////        //TODO: Do what you want with your json here
////        new Handler().postDelayed(new Runnable() {
////            public void run() {
////                new GetYourJsonTask().execute(new ApiConnector());
////            }
////        }, 1000);
////
////        System.out.println("afterback" + id);
////
////        Intent intent = new Intent(context, MapsActivity.class);
////        context.startActivity(intent);
////        ((Activity)context).finish();
////    }
////}
