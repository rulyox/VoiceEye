package zyon.voiceeye;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import zyon.voiceeye.activity.MainActivity;

public class OCRTask extends AsyncTask<String, Void, String> {

    private Context mContext;

    private ProgressDialog mDialog;

    private TessBaseAPI tessBaseAPI;
    private String locale;

    public OCRTask(Context context) {
        mContext = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        mDialog = new ProgressDialog(mContext);
        mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mDialog.setMessage(mContext.getString(R.string.progressing));
        mDialog.setCancelable(false);
        mDialog.show();

    }

    @Override
    protected String doInBackground(String... params) {

        locale = params[0];

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeFile(params[1], bmOptions);

        prepareTessData();

        try{ tessBaseAPI = new TessBaseAPI(); }
        catch(Exception e) { e.printStackTrace(); }

        String dataPath = mContext.getExternalFilesDir("/").getPath() + "/";
        tessBaseAPI.init(dataPath, locale);
        tessBaseAPI.setImage(bitmap);
        String retStr = "No result";

        try{ retStr = tessBaseAPI.getUTF8Text(); }
        catch(Exception e) { e.printStackTrace(); }

        tessBaseAPI.end();

        return retStr;

    }

    @Override
    protected void onPostExecute(String result) {

        mDialog.dismiss();

        ((MainActivity) mContext).finishOCR(result);

    }

    private void prepareTessData(){

        try{

            File dir = mContext.getExternalFilesDir("/tessdata");

            String pathToDataFile = dir + "/" + locale + ".traineddata";
            if(!(new File(pathToDataFile)).exists()){
                InputStream in = mContext.getAssets().open(locale + ".traineddata");
                OutputStream out = new FileOutputStream(pathToDataFile);
                byte [] buff = new byte[1024];
                int len;
                while(( len = in.read(buff)) > 0){
                    out.write(buff,0,len);
                }
                in.close();
                out.close();
            }

        } catch(Exception e) { e.printStackTrace(); }

    }

}
