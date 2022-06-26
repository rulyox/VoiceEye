package zyon.voiceeye;

import android.content.Context;
import android.speech.tts.TextToSpeech;

import java.util.Locale;

public class TTS {

    private TextToSpeech tts;

    public TTS(Context context) {

        tts = new TextToSpeech(context, status -> { if(status != TextToSpeech.ERROR) tts.setLanguage(Locale.ENGLISH); });

    }

    public void setLanguage(String language) {

        switch(language) {

            case "eng":
                tts.setLanguage(Locale.ENGLISH);
                break;

            case "kor":
                tts.setLanguage(Locale.KOREAN);
                break;

        }

    }

    public void speak(String text) {

        if(text != null) tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);

    }

    public void stop() {

        tts.stop();

    }

}
