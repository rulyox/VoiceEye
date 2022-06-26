package zyon.voiceeye.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.exifinterface.media.ExifInterface;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import zyon.voiceeye.R;
import zyon.voiceeye.OCRTask;
import zyon.voiceeye.TTS;

public class MainActivity extends AppCompatActivity {

    TTS tts;

    TextView textView;

    String imgPath;
    File imgFile;

    String OCRresult, OCRlocale;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tts = new TTS(this);

        setUI();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            Bitmap imgBmp = BitmapFactory.decodeFile(imgPath);

            try{

                ExifInterface imgExif = new ExifInterface(imgPath);
                int exifOrientation = imgExif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                int exifDegree = exifOrientationToDegrees(exifOrientation);
                imgBmp = rotateBitmap(imgBmp, exifDegree);

                FileOutputStream out = new FileOutputStream(imgPath);
                imgBmp.compress(Bitmap.CompressFormat.JPEG, 100, out);

                new OCRTask( MainActivity.this ).execute(OCRlocale, imgPath);

            } catch(Exception e) { e.printStackTrace(); }

        }

    }

    void setUI(){

        textView = this.findViewById(R.id.textView);

        this.findViewById(R.id.main_button_english).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                tts.setLanguage("eng");
                OCRlocale = "eng";
                takePicture();

            }
        });

        this.findViewById(R.id.main_button_korean).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                tts.setLanguage("kor");
                OCRlocale = "kor";
                takePicture();

            }
        });

        this.findViewById(R.id.main_button_stop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                tts.stop();

            }
        });

        this.findViewById(R.id.main_button_again).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if( OCRlocale.equals("eng") ) {
                    tts.setLanguage("eng");
                    tts.speak(OCRresult);
                } else {
                    tts.setLanguage("kor");
                    tts.speak(OCRresult);
                }

            }
        });

    }

    void takePicture(){

        deleteDirectory(getExternalFilesDir(Environment.DIRECTORY_PICTURES));

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {

            Uri photoURI = FileProvider.getUriForFile(MainActivity.this, getPackageName()+".fileprovider", createImageFile());
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            startActivityForResult(takePictureIntent, 1);

        } catch(IOException e) { e.printStackTrace(); }

    }

    // StartTesseract 에서 호출
    public void finishOCR(String result){

        OCRresult = result;

        textView.setText(OCRresult);

        tts.speak(OCRresult);

    }

    // 사진
    private File createImageFile() throws IOException {

        String imgName = "IMAGE_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imgTempFile = File.createTempFile(imgName, ".jpg", storageDir);

        imgPath = imgTempFile.getAbsolutePath();
        imgFile = new File(imgPath);

        return imgTempFile;

    }
    int exifOrientationToDegrees(int exifOrientation) {

        if(exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) return 90;
        else if(exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) return 180;
        else if(exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) return 270;

        return 0;

    }
    Bitmap rotateBitmap(Bitmap bitmap, int degrees) {

        if(degrees != 0 && bitmap != null) {

            Matrix m = new Matrix();
            m.setRotate(degrees, (float) bitmap.getWidth() / 2, (float) bitmap.getHeight() / 2);

            try {

                Bitmap converted = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
                if(bitmap != converted) {
                    bitmap.recycle();
                    bitmap = converted;
                }

            } catch(OutOfMemoryError e) { e.printStackTrace(); }

        }

        return bitmap;

    }

    // 경로 삭제
    private void deleteDirectory(File fileOrDirectory) {

        if ( fileOrDirectory.isDirectory() ) for ( File child : fileOrDirectory.listFiles() ) deleteDirectory(child);
        fileOrDirectory.delete();

    }

}
