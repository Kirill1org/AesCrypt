package bonch.dev.aescrypt;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import java.security.SecureRandom;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        initCryptParams();

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

    private String getNameFromPath(String pathFile) {
        String[] pathFileArray = pathFile.split("/");
        String[] pathWithExtention = pathFileArray[pathFileArray.length - 1].split("\\.");
        return pathWithExtention[0];
    }


    private void encodeFile(final Intent data, final String fileName) {
        String path = getApplicationContext().getExternalFilesDir("") + "/" + "encoded_" + fileName + ".CTR";
        File file = new File(path);

        Disposable disposable = Observable.create(new ObservableOnSubscribe<byte[]>() {
            @Override
            public void subscribe(ObservableEmitter<byte[]> e) throws Exception {

                byte[] buffer = new byte[1024 * 8];
                int len;
                InputStream iStream = getContentResolver().openInputStream(data.getData());

                e.onNext(iv);
                while ((len = iStream.read(buffer)) != -1) {
                    Log.e("READING THREAD:", Thread.currentThread().getName());
                    e.onNext((aesCrypt.encrypt(iv, buffer, 0, len)));

                }
                e.onComplete();
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .doOnNext(new Consumer<byte[]>() {
                    @Override
                    public void accept(byte[] bytes) throws Exception {

                        FileOutputStream output = new FileOutputStream(file, true);
                        output.write(bytes);
                        Log.e("WRITE THREAD:", Thread.currentThread().getName());

                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<byte[]>() {
                               @Override
                               public void accept(byte[] bytes) throws Exception {

                               }
                           },
                        new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {

                            }
                        },
                        new Action() {
                            @Override
                            public void run() throws Exception {

                            }
                        });

    }

    private void decodeFile(final Intent data, final String fileName) {
        String path = getApplicationContext().getExternalFilesDir("") + "/" + "decoded_" + fileName + ".jpg";
        File file = new File(path);

        Disposable disposable = Observable.create(new ObservableOnSubscribe<byte[]>() {
            @Override
            public void subscribe(ObservableEmitter<byte[]> e) throws Exception {
                byte[] buffer = new byte[1024 * 8];
                InputStream iStream = getContentResolver().openInputStream(data.getData());
                byte[] ownIv = new byte[16];

                iStream.read(ownIv);

                int len;
                while ((len = iStream.read(buffer)) != -1) {
                    Log.e("READING THREAD:", Thread.currentThread().getName());
                    e.onNext((aesCrypt.decrypt(ownIv, buffer, 0, len)));

                }
                e.onComplete();
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .doOnNext(new Consumer<byte[]>() {
                    @Override
                    public void accept(byte[] bytes) throws Exception {
                        FileOutputStream output = new FileOutputStream(file, true);
                        output.write(bytes);
                        Log.e("WRITING THREAD:", Thread.currentThread().getName());

                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<byte[]>() {
                               @Override
                               public void accept(byte[] bytes) throws Exception {

                               }
                           },
                        new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {

                            }
                        },
                        new Action() {
                            @Override
                            public void run() throws Exception {
                                Toast.makeText(MainActivity.this, "Successfully created:" + fileName, Toast.LENGTH_SHORT).show();
                            }
                        });
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

        aesCrypt = new AESCrypt(getRandomKey(), AesTypes.CTR.getParam());
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

