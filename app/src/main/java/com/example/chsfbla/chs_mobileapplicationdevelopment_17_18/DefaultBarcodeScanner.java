package com.example.chsfbla.chs_mobileapplicationdevelopment_17_18;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;
/*
This is the BarcodeScanner class. It is a core functionality of the app, as it allows users to check out books
simply by scanning the barcode of a book. It is also used for librarians to check in books. The activity works by
using a separate framework (ZXingScanner), although we also developed a separate, native version that can be found
in the BarcodeScanning folder (not used anymore, however).
 */
public class DefaultBarcodeScanner extends AppCompatActivity implements ZXingScannerView.ResultHandler {
    //This is the attribute that references the view used to display the barcode feed to the user
    private ZXingScannerView scannerView;
    //Specific request code needed for the barcode scanning functionality
    public final int CAMERA_REQUEST_CODE = 8;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        scannerView = new ZXingScannerView(this);
        //Instead of setting the contentView to a specific layout, just set it to the scannerView
        setContentView(scannerView);
        //Permission code that makes sure the app has the camera permission; if not, it requests this permission (this is a standard convention/practice)
        int permissionCheck = ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {android.Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //If we needed to request permission, check if they gave permission or not, and toast appropriately to let the user know
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Camera permission granted. Thank you!", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Camera permission denied. Checkout features will not work.", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        scannerView.setResultHandler(this);
        scannerView.startCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        scannerView.stopCamera();
    }

    @Override
    public void handleResult(Result result) {
        //Pass the result through to an intent, and then pass this intent through as a parameter to the setResult() method
        Intent i = new Intent();
        i.putExtra("Result", result.getText());
        /* The setResult() method, as you might notice, is not specifically defined in the class. This is because it is a
            built-in android method. It takes in an intent, and a result code (in this case, we pass in RESULT_OK because
            the barcode scanning was successful). Finally, close the activity with finish(), another built-in method.
         */
        setResult(RESULT_OK, i);
        finish();
    }
}
