package com.demo.guessthestartest;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ActionMenuView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {
    private Button button1;
    private Button button2;
    private Button button3;
    private Button button4;
    private ImageView imageView;
    private String[] allStars;
    private String rightStar;
    private String url = "http://www.posh24.se/kandisar/";
    private int countChoice =4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button1 = findViewById(R.id.button1);
        button2 = findViewById(R.id.button2);
        button3 = findViewById(R.id.button3);
        button4 = findViewById(R.id.button4);
        imageView = findViewById(R.id.imageView);
       getContent();
       init();
    }

    private void getContent(){
        DownloadPage downloadList = new DownloadPage();
        String page = null;
        try {
            page = downloadList.execute(url).get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        Pattern pattern = Pattern.compile("div class=\"channelListEntry\">(.*?)<div class=\"col-xs-12 col-sm-6 col-md-4\">");
        Matcher matcher = pattern.matcher(page);
        while (matcher.find()) {
            page = matcher.group(1);
        }
        allStars = page.split("div class=\"channelListEntry\">");
    }

    private void init() {
        ArrayList<Integer> randomNumbers = new ArrayList<>();
        for (int i = 0; i < countChoice; i++) {
            int random = (int) (Math.random() * allStars.length);
            while (randomNumbers.contains(random)){
                random = (int) (Math.random() * allStars.length);
            }
            randomNumbers.add(random);
        }
        String[] choiceStars = new String[countChoice];
        for (int i = 0; i < choiceStars.length; i++) {
            choiceStars[i]=allStars[randomNumbers.get(i)];
        }
        String urlImg = null;
        Pattern patternImg = Pattern.compile("<img src=\"(.*?)\" alt");
        Matcher matcherImg = patternImg.matcher(choiceStars[0]);
        while (matcherImg.find()) {
            urlImg = matcherImg.group(1);
        }
        DownloadDraw downloadDraw = new DownloadDraw();
        Bitmap bitmap = null;
        try {
            bitmap =  downloadDraw.execute(urlImg).get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        imageView.setImageBitmap(bitmap);
        String[] names = new String[countChoice];
        for (int i = 0; i < names.length ; i++) {
            Pattern patternName = Pattern.compile("alt=\"(.*?)\"/>");
            Matcher matcherName = patternName.matcher(choiceStars[i]);
            while (matcherName.find()) {
                names[i] = matcherName.group(1);
            }
        }
        rightStar = names[0];
        ArrayList<Integer> numbersButton= new ArrayList<>();
        for (int i = 0; i < countChoice; i++) {
            int random = (int) (Math.random() * countChoice);
            while (numbersButton.contains(random)) {
                random = (int) (Math.random() * countChoice);
            }
            numbersButton.add(random);
        }
        button1.setText(names[numbersButton.get(0)]);
        button2.setText(names[numbersButton.get(1)]);
        button3.setText(names[numbersButton.get(2)]);
        button4.setText(names[numbersButton.get(3)]);
    }

    public void onClickButton(View view) {
        Button button = (Button) view;
        String answer = button.getText().toString();
        if(answer.equals(rightStar)){
            Toast.makeText(getApplicationContext(), R.string.right, Toast.LENGTH_SHORT).show();
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    init();
                }
            }, 3000);

        }
        else {
            Toast.makeText(getApplicationContext(), R.string.lie, Toast.LENGTH_SHORT).show();
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    init();
                }
            }, 3000);
        }
    }


    private static class DownloadPage extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            URL url = null;
            HttpURLConnection urlConnection = null;
            StringBuilder builder = new StringBuilder();

            try {
                url = new URL(strings[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                String s = reader.readLine();
                while (s != null) {
                    builder.append(s);
                    s = reader.readLine();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            return builder.toString();
        }
    }

    private static class DownloadDraw extends AsyncTask <String, Void, Bitmap>{
        @Override
        protected Bitmap doInBackground(String... strings) {
            URL url = null;
            HttpURLConnection httpURLConnection = null;
            Bitmap bitmap=null;
            try {
                url = new URL(strings[0]);
                httpURLConnection=(HttpURLConnection) url.openConnection();
                InputStream in = httpURLConnection.getInputStream();
                bitmap = BitmapFactory.decodeStream(in);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (httpURLConnection!=null) {
                    httpURLConnection.disconnect();
                }
            }
            return bitmap;
        }
    }
}
