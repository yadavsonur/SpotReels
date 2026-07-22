package com.sonu.spotreels.ui.upload;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sonu.spotreels.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SupabaseUploadActivity extends AppCompatActivity {

    private static final String SUPABASE_URL = "https://ukocpwnjbfkwpvxrlhue.supabase.co";
    private static final String BUCKET = "videos";

    private String SUPABASE_ANON_KEY;


    private ImageView ivPreview;
    private Button btnPick, btnUpload;
    private EditText etCaption;
    private View overlay;
    private LinearLayout overlayBox;

    private Uri selectedUri;
    private File tempFile;

    private final OkHttpClient http = new OkHttpClient();
    private final Handler main = new Handler(Looper.getMainLooper());
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final ActivityResultLauncher<Intent> pickLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedUri = result.getData().getData();
                    Glide.with(this).load(selectedUri).into(ivPreview);
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        SUPABASE_ANON_KEY = getString(R.string.supabase_key);


        ivPreview = findViewById(R.id.ivPreview);
        btnPick = findViewById(R.id.btnPick);
        btnUpload = findViewById(R.id.btnUpload);
        etCaption = findViewById(R.id.etCaption);
        overlay = findViewById(R.id.uploadOverlay);
        overlayBox = findViewById(R.id.uploadOverlayBox);

        FirebaseFirestore db = FirebaseFirestore.getInstance();  // ✅ LOCAL VARIABLE

        btnPick.setOnClickListener(v -> pickVideo());

        btnUpload.setOnClickListener(v -> {
            if (selectedUri == null) {
                toast("Select a video first");
                return;
            }

            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                toast("Login required");
                return;
            }

            btnUpload.setEnabled(false);
            showOverlay(true);
            executor.execute(() -> uploadToSupabase(db));
        });
    }

    private void pickVideo() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("video/*");
        pickLauncher.launch(intent);
    }

    private File uriToFile(@NonNull Uri uri) throws Exception {
        ContentResolver cr = getContentResolver();
        String name = getFileName(uri);
        if (name == null) name = UUID.randomUUID() + ".mp4";

        File out = new File(getCacheDir(), name);

        try (InputStream in = cr.openInputStream(uri)) {

            if (in == null) {  // ✅ NULL SAFETY
                throw new Exception("Unable to open selected file");
            }

            try (FileOutputStream fos = new FileOutputStream(out)) {
                byte[] buf = new byte[8192];
                int len;
                while ((len = in.read(buf)) > 0) {
                    fos.write(buf, 0, len);
                }
            }
        }
        return out;
    }

    private String getFileName(Uri uri) {
        try (android.database.Cursor c =
                     getContentResolver().query(uri, null, null, null, null)) {

            if (c != null && c.moveToFirst()) {
                return c.getString(
                        c.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME)
                );
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private void uploadToSupabase(@NonNull FirebaseFirestore db) {
        try {
            tempFile = uriToFile(selectedUri);

            String reelId = UUID.randomUUID().toString();
            String remotePath = "uploads/" + reelId + ".mp4";

            RequestBody body =
                    RequestBody.create(tempFile, MediaType.parse("video/mp4"));

            Request req = new Request.Builder()
                    .url(SUPABASE_URL + "/storage/v1/object/" + BUCKET + "/" + remotePath)
                    .addHeader("Authorization", "Bearer " + SUPABASE_ANON_KEY)
                    .addHeader("apikey", SUPABASE_ANON_KEY)
                    .put(body)
                    .build();

            try (Response res = http.newCall(req).execute()) {

                if (!res.isSuccessful()) {
                    toast("Upload failed: " + res.code());
                    uiReset();
                    return;
                }
            }

            String publicUrl =
                    SUPABASE_URL + "/storage/v1/object/public/" + BUCKET + "/" + remotePath;

            main.post(() ->
                    saveToFirestore(db, reelId, publicUrl,
                            etCaption.getText().toString().trim()));

        } catch (Exception e) {
            toast("Upload error: " + e.getMessage());
            uiReset();
        } finally {
            if (tempFile != null && tempFile.exists()) {   // ✅ DELETE RESULT HANDLED
                boolean deleted = tempFile.delete();
                if (!deleted) tempFile.deleteOnExit();
            }
        }
    }

    private void saveToFirestore(
            @NonNull FirebaseFirestore db,
            @NonNull String reelId,
            @NonNull String url,
            @Nullable String caption
    ) {

        String userId = FirebaseAuth.getInstance().getUid();

        HashMap<String, Object> map = new HashMap<>();
        map.put("id", reelId);
        map.put("videoUrl", url);
        map.put("caption", caption == null ? "" : caption);
        map.put("uploadedBy", userId);
        map.put("timestamp", System.currentTimeMillis());
        map.put("likes", 0);
        map.put("likedBy", new ArrayList<String>());

        db.collection("reels")
                .document(reelId)
                .set(map)
                .addOnSuccessListener(a -> {
                    toast("Uploaded!");
                    uiReset();
                    finish();
                })
                .addOnFailureListener(e -> {
                    toast("Firestore error: " + e.getMessage());
                    uiReset();
                });
    }

    private void showOverlay(boolean show) {
        overlay.setVisibility(show ? View.VISIBLE : View.GONE);
        overlayBox.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void uiReset() {
        main.post(() -> {
            showOverlay(false);
            btnUpload.setEnabled(true);
        });
    }

    private void toast(String msg) {
        main.post(() -> Toast.makeText(this, msg, Toast.LENGTH_LONG).show());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}
