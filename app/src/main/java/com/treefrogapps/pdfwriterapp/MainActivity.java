package com.treefrogapps.pdfwriterapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import crl.android.pdfwriter.PDFDocument;
import crl.android.pdfwriter.PDFWriter;
import crl.android.pdfwriter.PaperSize;
import crl.android.pdfwriter.StandardFonts;
import crl.android.pdfwriter.XObjectImage;


public class MainActivity extends AppCompatActivity {


    private EditText editText;
    private Button button;
    private Button button2;
    private TextView textView;
    private TextView fileNameTV;

    private String fileName;
    private String outputFolder = "Exported_PDFs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText = (EditText) findViewById(R.id.editText);
        button = (Button) findViewById(R.id.button);
        button2 = (Button) findViewById(R.id.button2);
        textView = (TextView) findViewById(R.id.textView);
        fileNameTV = (TextView) findViewById(R.id.fileNameTV);

    }


    public void createPDF(View view) {

        if (!editText.getText().toString().equals("")) {

            formattedTextA4(editText.getText().toString(), 14, PaperSize.A4_HEIGHT - 60);

        } else {

            Toast.makeText(getApplicationContext(), "Well type something!", Toast.LENGTH_SHORT).show();
        }
    }

    private class TextParams {

        private String textToOutput;
        private int pointSize;
        private int initialDistanceFromTop;

        public TextParams(String textToOutput, int pointSize, int initialDistanceFromTop) {

            this.textToOutput = textToOutput;
            this.pointSize = pointSize;
            this.initialDistanceFromTop = initialDistanceFromTop;
        }

    }

    public void formattedTextA4(String textToOutput, int pointSize, int initialDistanceFromTop) {

        TextParams textParams = new TextParams(textToOutput, pointSize, initialDistanceFromTop);
        new MyAsyncTask().execute(textParams);
    }


    private class MyAsyncTask extends AsyncTask<TextParams, Void, String> {
        @Override
        protected void onPreExecute() {

            button.setClickable(false);
            button2.setClickable(false);
            button2.setTextColor(Color.GRAY);
        }

        @Override
        protected String doInBackground(TextParams[] textParams) {

            String textToOutput = textParams[0].textToOutput;
            int pointSize = textParams[0].pointSize;
            int initialDistanceFromTop = textParams[0].initialDistanceFromTop;

            PDFWriter pdfWriter = new PDFWriter(PaperSize.A4_WIDTH, PaperSize.A4_HEIGHT);
            pdfWriter.addRawContent("0 0 0 rg\n");

            insertBitmap(pdfWriter);

            // 30pt left margin and 40pt right margin each side of page = 595 - 60 = 535 page width
            float paragraphWidth = 535f;
            // initial distance from top
            int distanceFromTop = initialDistanceFromTop;
            int leftMargin = 35;
            // Split textArray
            String[] textArray = textToOutput.split(" ");
            String words = "";
            String tempWords = "";
            Paint paint = new Paint();
            paint.setTextSize(pointSize);

            if (paint.measureText(textToOutput) < paragraphWidth) {

                pdfWriter.setFont(StandardFonts.SUBTYPE, StandardFonts.HELVETICA);
                pdfWriter.addText(leftMargin, distanceFromTop, pointSize, textToOutput);

            } else {

                for (int i = 0; i < textArray.length; i++) {

                    textArray[i] = textArray[i].trim();

                    tempWords = tempWords + textArray[i] + " ";
                    float wordLength = paint.measureText(tempWords);

                    if (wordLength < paragraphWidth && i == textArray.length - 1) {

                        pdfWriter.setFont(StandardFonts.SUBTYPE, StandardFonts.HELVETICA);
                        pdfWriter.addText(leftMargin, distanceFromTop, pointSize, words);

                        words = textArray[i] + " ";
                        tempWords = textArray[i] + " ";
                        distanceFromTop -= 18;

                        if (distanceFromTop < 40) {

                            distanceFromTop = PaperSize.A4_HEIGHT - 60;
                            pdfWriter.newPage();
                        }

                    } else if (wordLength >= paragraphWidth) {

                        pdfWriter.setFont(StandardFonts.SUBTYPE, StandardFonts.HELVETICA);
                        pdfWriter.addText(leftMargin, distanceFromTop, pointSize, words);

                        words = textArray[i] + " ";
                        tempWords = textArray[i] + " ";
                        distanceFromTop -= 18;

                        if (distanceFromTop < 40) {

                            distanceFromTop = PaperSize.A4_HEIGHT - 60;
                            pdfWriter.newPage();
                            insertBitmap(pdfWriter);
                        }

                    } else {
                        words = words + textArray[i] + " ";
                    }
                }
            }
            String textOutput = pdfWriter.asString();

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            String date = sdf.format(new Date());

            outputToFile("test_" + date + ".pdf", textOutput, "ISO-8859-1");

            return textOutput;
        }

        @Override
        protected void onPostExecute(String textOutput) {

            textView.setText(textOutput);
            fileNameTV.setText(Environment.getExternalStorageDirectory() + "/" + outputFolder + "/" + fileName);
            Toast.makeText(getApplicationContext(), "File Saved", Toast.LENGTH_SHORT).show();
            button.setClickable(true);
            button2.setClickable(true);
            button2.setTextColor((Color.BLACK));
        }
    }

    public void insertBitmap(PDFWriter pdfWriter) {

        int fromLeft = 0;
        int fromBottom = PaperSize.A4_HEIGHT - 25;

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        pdfWriter.addImageKeepRatio(fromLeft, fromBottom, 25, 25, bitmap);

        fromLeft = PaperSize.A4_WIDTH - 25;
        fromBottom = PaperSize.A4_HEIGHT - 25;
        pdfWriter.addImageKeepRatio(fromLeft, fromBottom, 25, 25, bitmap);

        fromLeft = PaperSize.A4_WIDTH - 25;
        fromBottom = 0;
        pdfWriter.addImageKeepRatio(fromLeft, fromBottom, 25, 25, bitmap);

        fromLeft = 0;
        fromBottom = 0;
        pdfWriter.addImageKeepRatio(fromLeft, fromBottom, 25, 25, bitmap);


    }

    private void outputToFile(String fileName, String textOutput, String fileEncoding) {

        this.fileName = fileName;

        File folder = new File(Environment.getExternalStorageDirectory(), outputFolder);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        File outputFile = new File(Environment.getExternalStorageDirectory() + "/" + outputFolder + "/" + fileName);

        Log.e("Storage Location,", Environment.getExternalStorageDirectory() + "/" + outputFolder + "/" + fileName);

        try {
            FileOutputStream pdfFile = new FileOutputStream(outputFile);
            pdfFile.write(textOutput.getBytes(fileEncoding));
            pdfFile.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void emailPDF(View view) {

        if (fileName != null) {

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("message/rfc822");
            intent.putExtra(Intent.EXTRA_SUBJECT, "projected quote " + fileName);
            intent.putExtra(Intent.EXTRA_TEXT, "Your quote attached");
            intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + Environment.getExternalStorageDirectory() + "/" + outputFolder + "/" + fileName));
            try {
                startActivity(Intent.createChooser(intent, "Send Quote ..."));
            } catch (android.content.ActivityNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(getApplicationContext(), "File doesn't exist", Toast.LENGTH_SHORT).show();
        }
    }

}
