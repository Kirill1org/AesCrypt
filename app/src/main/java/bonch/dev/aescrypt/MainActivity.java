package bonch.dev.aescrypt;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.security.SecureRandom;
import java.util.Arrays;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;


public class MainActivity extends AppCompatActivity {

    private AESCrypt aesCrypt;
    private byte[] iv;

    private Button encodeBtn;
    private Button decodeBtn;

    public static final int ENCODE_REQUEST = 1;
    public static final int DECODE_REQUEST = 2;
    public static final int ENCODE_MODE = 100;
    public static final int DECODE_MODE = 101;
    public static final String TAG = "STORAGE_PERMISSON";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        initCryptParams();
        isStoragePermissionGranted();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case ENCODE_REQUEST: {
                if (resultCode == RESULT_OK) {
                    encodeFile(data, getNameFromPath(data.getData().getPath()));
                }
                break;
            }
            case DECODE_REQUEST: {
                if (resultCode == RESULT_OK) {
                    decodeFile(data, getNameFromPath(data.getData().getPath()));
                }
                break;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.v(TAG, "Permission: " + permissions[0] + "was " + grantResults[0]);
        } else this.finishAffinity();


    }

    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.e("", "Permission is granted");
                return true;
            } else {
                Log.e(TAG, "Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else {
            Log.e(TAG, "Permission is granted");
            return true;
        }
    }


    private String getNameFromPath(String pathFile) {
        String[] pathFileArray = pathFile.split("/");
        String[] pathWithExtention = pathFileArray[pathFileArray.length - 1].split("\\.");
        return pathWithExtention[0];
    }

    public static String getRealPathFromURI(Context context, Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
        int column_index
                = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }


    private void encodeFile(final Intent data, final String fileName) {

        String path = getApplicationContext().getExternalFilesDir("") + "/" + "encoded_" + fileName + ".ctr";
        File file = new File(path);

        Disposable disposable = Observable.create(new ObservableOnSubscribe<ByteBuffer>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            public void subscribe(ObservableEmitter<ByteBuffer> emitter) throws Exception {
                try (FileChannel fileInputChannel = new FileInputStream(new File(getRealPathFromURI(getApplicationContext(), data.getData()))).getChannel();
                     FileChannel fileOutputChannel = new FileOutputStream(file, true).getChannel()) {

                    ByteBuffer byteBuffer = ByteBuffer.wrap(iv);
                    fileOutputChannel.write(byteBuffer);
                    byteBuffer.clear();
                    byteBuffer = ByteBuffer.allocate(1024 * 8);

                    while ((fileInputChannel.read(byteBuffer)) > 0) {
                        emitter.onNext(byteBuffer);
                        byteBuffer.clear();
                    }
                    emitter.onComplete();
                }
            }
        })
                .subscribeOn(Schedulers.io())
                .doOnNext(new Consumer<ByteBuffer>() {
                    @Override
                    public void accept(ByteBuffer byteBuffer) throws Exception {
                        Log.e("BEFORE_CRYPT", Arrays.toString(byteBuffer.array()));
                        byteBuffer.flip();
                        aesCrypt.encrypt(iv, byteBuffer);
                        Log.e("AFTER_CRYPT", Arrays.toString(byteBuffer.array()));
                    }
                })
                .doOnNext(new Consumer<ByteBuffer>() {
                    @Override
                    public void accept(ByteBuffer byteBuffer) throws Exception {
                        try (FileChannel fileChannel = new FileOutputStream(file, true).getChannel()) {
                            Log.e("BEFORE_WRYTE", Arrays.toString(byteBuffer.array()));
                            byteBuffer.flip();
                            fileChannel.write(byteBuffer);
                        }
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .ignoreElements()
                .subscribe(new Action() {
                               @Override
                               public void run() throws Exception {
                                   Toast.makeText(MainActivity.this, "File successfully created!", Toast.LENGTH_SHORT).show();
                               }
                           },
                        new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                throwable.printStackTrace();
                            }
                        });

    }

    private void decodeFile(final Intent data, final String fileName) {

        String path = getApplicationContext().getExternalFilesDir("") + "/" + "decoded_" + fileName + ".jpg";
        File file = new File(path);
        ByteBuffer byteBufferIv = ByteBuffer.allocate(16);

        Disposable disposable = Observable.create(new ObservableOnSubscribe<ByteBuffer>() {
            public void subscribe(ObservableEmitter<ByteBuffer> emitter) throws Exception {
                ByteBuffer byteBuffer = ByteBuffer.allocate(1024 * 8);
                try (FileChannel fileInputChannel = new FileInputStream(
                        new File(getRealPathFromURI(getApplicationContext(), data.getData()))).getChannel()) {
                    fileInputChannel.read(byteBufferIv);
                    while ((fileInputChannel.read(byteBuffer)) > 0) {
                        emitter.onNext(byteBuffer);
                        byteBuffer.clear();
                    }
                    emitter.onComplete();
                }
            }
        })
                .subscribeOn(Schedulers.io())
                .doOnNext(new Consumer<ByteBuffer>() {
                    @Override
                    public void accept(ByteBuffer byteBuffer) throws Exception {
                        Log.e("BEFORE_CRYPT", Arrays.toString(byteBuffer.array()));
                        byteBuffer.flip();
                        aesCrypt.decrypt(byteBufferIv.array(), byteBuffer);
                        Log.e("AFTER_CRYPT", Arrays.toString(byteBuffer.array()));
                    }
                })
                .doOnNext(new Consumer<ByteBuffer>() {
                    @Override
                    public void accept(ByteBuffer byteBuffer) throws Exception {
                        try (FileChannel fileChannel = new FileOutputStream(file, true).getChannel()) {
                            Log.e("BEFORE_WRYTE", Arrays.toString(byteBuffer.array()));
                            byteBuffer.flip();
                            fileChannel.write(byteBuffer);
                        }
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .ignoreElements()
                .subscribe(new Action() {
                               @Override
                               public void run() throws Exception {
                                   Toast.makeText(MainActivity.this, "File successfully created!", Toast.LENGTH_SHORT).show();
                               }
                           },
                        new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                throwable.printStackTrace();
                            }
                        });

    }

    private void doRxCode(final Intent data, final String fileName, int mode) {
    }

    private void pickFile(View view) {

        int requestCode = 0;

        if (view.getId() == encodeBtn.getId()) requestCode = ENCODE_REQUEST;
        else if (view.getId() == decodeBtn.getId()) requestCode = DECODE_REQUEST;

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("file/*");
        startActivityForResult(intent, requestCode);
    }

    private void initViews() {

        encodeBtn = findViewById(R.id.encode_btn);
        decodeBtn = findViewById(R.id.decode_btn);

        encodeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickFile(view);

            }
        });
        decodeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickFile(view);

            }
        });
    }

    private void initCryptParams() {

        aesCrypt = new AESCrypt(getRandomKey(), AESCrypt.Types.CTR);
        iv = getRandomIV();

    }

    private byte[] getRandomKey() {
        byte[] key = new byte[16];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(key);
        return key;
    }

    private byte[] getRandomIV() {
        byte[] iv = new byte[16];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(iv);
        return iv;
    }
}

