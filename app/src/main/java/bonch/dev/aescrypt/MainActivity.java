package bonch.dev.aescrypt;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.security.SecureRandom;
import java.util.Arrays;


public class MainActivity extends AppCompatActivity {

    private AESCrypt aesCrypt;
    private byte[] iv;

    private Button encodeBtn;
    private Button decodeBtn;


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
            case Constants.ENCODE_REQUEST: {
                if (resultCode == RESULT_OK) {

                    encodeFile(data, getNameFromPath(data.getData().getPath()));
                }
                break;
            }
            case Constants.DECODE_REQUEST: {
                if (resultCode == RESULT_OK) {

                    decodeFile(data, getNameFromPath(data.getData().getPath()));
                }
                break;
            }

        }
    }

    private String getNameFromPath(String pathFile) {
        String[] pathFileArray = pathFile.split("/");
        return pathFileArray[pathFileArray.length - 1];
    }


    private void encodeFile(final Intent data, final String fileName) {

        HandlerThread handlerThread = new HandlerThread("HandlerThread");
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        final Handler handler1 = new Handler(looper) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                Bundle bundle = msg.getData();
                String fileName = bundle.getString(Constants.HANDLER_MESSAGE_KEY);
                Toast.makeText(MainActivity.this, fileName + " is successfuly created", Toast.LENGTH_LONG).show();
            }
        };


        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                Message messageHandler = handler1.obtainMessage();
                Bundle bundle = new Bundle();
                bundle.putString(Constants.HANDLER_MESSAGE_KEY, fileName);
                messageHandler.setData(bundle);
                handler1.sendMessage(messageHandler);


                String path = getApplicationContext().getExternalFilesDir("") + "/" + "encoded_" + fileName;

                File file = new File(path);
                if (!file.exists()) {
                    try {
                        file.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                        throw new RuntimeException("Creation file error");
                    }
                }

                try (InputStream iStream = getContentResolver().openInputStream(data.getData());
                     FileOutputStream output = new FileOutputStream(file.getAbsolutePath(), true)) {

                    encodeChunkByChunk(iStream, output);

                } catch (IOException e) {
                    e.printStackTrace();
                    throw new RuntimeException("Input and output encode file error");
                }


            }

        };

        handler1.post(runnable);


    }

    private void encodeChunkByChunk(InputStream iStream, FileOutputStream output) {
        byte[] buffer = new byte[1024 * 8];

        int len;
        try {
            while ((len = iStream.read(buffer)) != -1) {
                Log.e("LENGTH", String.valueOf(len));
                Log.e("ENCRYPT_LENGTN", String.valueOf(aesCrypt.encrypt(buffer, iv).length));
                output.write(aesCrypt.encrypt(Arrays.copyOfRange(buffer, 0, len), iv));
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Encode chunk by chunk error");
        }

    }

    private void decodeChunkByChunk(InputStream iStream, FileOutputStream output) {
        byte[] buffer_decode = new byte[(1024 * 8) + 16];

        int len;
        try {
            while ((len = iStream.read(buffer_decode)) != -1) {
                Log.e("LENGTH", String.valueOf(len));
                Log.e("ENCRYPT_LENGTN", String.valueOf(aesCrypt.decrypt(buffer_decode, iv).length));
                output.write(aesCrypt.decrypt(Arrays.copyOfRange(buffer_decode, 0, len), iv));
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Decode chunk by chunk error");
        }

    }

    private void decodeFile(final Intent data, final String fileName) {

        final Handler handler = new Handler(getApplicationContext().getMainLooper()) {
            @Override
            public void handleMessage(Message inputMessage) {
                Bundle bundle = inputMessage.getData();
                String fileName = bundle.getString(Constants.HANDLER_MESSAGE_KEY);
                Toast.makeText(MainActivity.this, fileName + " is successfully created", Toast.LENGTH_SHORT).show();

            }
        };


        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                Message messageHandler = handler.obtainMessage();
                Bundle bundle = new Bundle();
                bundle.putString(Constants.HANDLER_MESSAGE_KEY, fileName);
                messageHandler.setData(bundle);
                handler.sendMessage(messageHandler);

                String path = getApplicationContext().getExternalFilesDir("") + "/" + "decoded_" + fileName;

                File file = new File(path);
                if (!file.exists()) {
                    try {
                        file.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                        throw new RuntimeException("Creation file error");
                    }
                }

                try (InputStream iStream = getContentResolver().openInputStream(data.getData());
                     FileOutputStream output = new FileOutputStream(file.getAbsolutePath(), true)) {

                    decodeChunkByChunk(iStream, output);

                } catch (IOException e) {
                    e.printStackTrace();
                    throw new RuntimeException("Input and output decode file error");
                }


            }
        };

        Thread thread = new Thread(runnable);
        thread.start();

    }

    private void pickFile(View view) {

        int requestCode = 0;

        if (view.getId() == encodeBtn.getId()) requestCode = Constants.ENCODE_REQUEST;
        else if (view.getId() == decodeBtn.getId()) requestCode = Constants.DECODE_REQUEST;

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

        aesCrypt = new AESCrypt(getRandomKey(), Constants.AES_CBC_TYPE);
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

