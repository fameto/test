package com.example.udfreaderapp;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

// Imports for isofilereader
import com.palantir.isofilereader.GenericInternalIsoFile;
import com.palantir.isofilereader.IsoFileReader;
import com.palantir.isofilereader.UdfFormatException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int PICK_FILE_REQUEST_CODE = 100;
    private static final String TAG = "MainActivity";
    private Button selectFileButton;
    private RecyclerView filesRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        selectFileButton = findViewById(R.id.button_select_file);
        filesRecyclerView = findViewById(R.id.recyclerview_files);

        // Setup RecyclerView
        filesRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        selectFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFilePicker();
            }
        });
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*"); // UDF does not have a standard MIME type, so we allow all files.
        startActivityForResult(intent, PICK_FILE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FILE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                if (uri != null) {
                    // Process the selected UDF file
                    processUdfFile(uri);
                }
            }
        }
    }

    private void processUdfFile(Uri uri) {
        // File operations can be slow, so this should ideally be done on a background thread.
        // For simplicity, we are doing it on the main thread here.

        File tempFile = null;
        try {
            // Copy the content from the URI to a temporary file in the cache directory
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                throw new IOException("Unable to open input stream for URI");
            }

            tempFile = new File(getCacheDir(), "temp_udf.udf");
            try (OutputStream outputStream = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[4 * 1024]; // 4K buffer
                int read;
                while ((read = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, read);
                }
                outputStream.flush();
            }
            inputStream.close();

            // Now, use isofilereader to parse the temporary file
            try (IsoFileReader isoFileReader = new IsoFileReader(tempFile)) {
                // Get all files in a flat list
                List<GenericInternalIsoFile> fileList = isoFileReader.convertTreeFilesToFlatList(isoFileReader.getAllFiles());

                // Update the RecyclerView with the file list
                FileAdapter adapter = new FileAdapter(fileList);
                filesRecyclerView.setAdapter(adapter);

                Toast.makeText(this, "Found " + fileList.size() + " files.", Toast.LENGTH_SHORT).show();

            } catch (UdfFormatException e) {
                Log.e(TAG, "Error parsing UDF file", e);
                Toast.makeText(this, "Error: Not a valid UDF file.", Toast.LENGTH_LONG).show();
            }

        } catch (IOException e) {
            Log.e(TAG, "File processing failed", e);
            Toast.makeText(this, "Error: Failed to read the file.", Toast.LENGTH_LONG).show();
        } finally {
            // Clean up the temporary file
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }
}
