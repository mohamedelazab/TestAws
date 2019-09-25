package com.azab.testaws;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClient;
import com.amazonaws.services.rekognition.model.Attribute;
import com.amazonaws.services.rekognition.model.DetectFacesRequest;
import com.amazonaws.services.rekognition.model.DetectFacesResult;
import com.amazonaws.services.rekognition.model.Image;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class MainActivity extends AppCompatActivity {

    Button btnCapture;
    TextView tvResult;
    ImageView imageView;
    Image image;
    static AmazonRekognition client = null;
    private static final int CAMERA_REQUEST =1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnCapture =findViewById(R.id.btnCapture);
        imageView =findViewById(R.id.seleted_img);
        tvResult =findViewById(R.id.tv_analyze);

        new CredRetriever().execute();

        btnCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cameraIntent =new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent,CAMERA_REQUEST);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.JPEG,100,stream);

            ByteBuffer imageBytes = ByteBuffer.wrap(stream.toByteArray());
            image = new Image();
            image.withBytes(imageBytes);

            ImageView imageView = findViewById(R.id.seleted_img);
            imageView.setImageBitmap(imageBitmap);

            DetectFacesRequest request = new DetectFacesRequest()
                    .withAttributes(Attribute.ALL.toString())
                    .withImage(image);

            new GetFaceResult().execute(request);
        }
    }

    class CredRetriever extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            AWSCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                    getApplicationContext(),"879351739195",
                    "us-east-2:7f634362-c604-42bb-8d94-99fd2a80d3bd",// Identity pool ID
                    "arn:aws:iam::879351739195:role/Cognito_identity1Unauth_Role",
                    "arn:aws:iam::879351739195:role/Cognito_identity1Auth_Role",
                    Regions.US_EAST_2 // Region
            );
            Log.i("TEST",credentialsProvider.getCredentials().toString());
            client = new AmazonRekognitionClient(credentialsProvider);
            return null;
        }
    }

    class GetFaceResult extends AsyncTask<DetectFacesRequest, Void, String> {
        @Override
        protected String doInBackground(DetectFacesRequest... requests) {
            DetectFacesResult result = client.detectFaces(requests[0]);
            result.getFaceDetails();

            return result.toString();
        }

        @Override
        protected void onPostExecute(String s) {
            Log.e("Result",s);
            tvResult.setText(s);
        }
    }
}
