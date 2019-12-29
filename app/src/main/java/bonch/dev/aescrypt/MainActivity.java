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
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.security.SecureRandom;


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

                    try (InputStream iStream = getContentResolver().openInputStream(data.getData())) {

                        encodeFile(getBytes(iStream), getNameFromPath(data.getData().getPath()));

                    } catch (IOException e) {
                        e.printStackTrace();
                        throw new RuntimeException("OnActivityResult");
                    }

                }
                break;
            }
            case Constants.DECODE_REQUEST: {
                if (resultCode == RESULT_OK) {

                    try (InputStream inputStream = getContentResolver().openInputStream(data.getData())) {

                        disabledBtns();
                        decodeFile(getBytes(inputStream), getNameFromPath(data.getData().getPath()));

                    } catch (IOException e) {
                        e.printStackTrace();
                        throw new RuntimeException("OnActivityResult");
                    }

                }
                break;
            }

        }
    }

    private String getNameFromPath(String pathFile) {
        String[] pathFileArray = pathFile.split("/");
        return pathFileArray[pathFileArray.length - 1];
    }


    private void encodeFile(final byte[] inputFile, final String fileName) {

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

                writeToFile(aesCrypt.encrypt(inputFile, iv), fileName);


            }

        };

        handler1.post(runnable);


    }

    private void decodeFile(final byte[] encodedFile, final String fileName) {

        final Handler handler = new Handler(getApplicationContext().getMainLooper()) {
            @Override
            public void handleMessage(Message inputMessage) {
                Bundle bundle = inputMessage.getData();
                String fileName = bundle.getString(Constants.HANDLER_MESSAGE_KEY);
                Toast.makeText(MainActivity.this, fileName + " is successfuly created", Toast.LENGTH_SHORT).show();
                enabledBtns();

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

                writeToFile(aesCrypt.decrypt(encodedFile, iv), fileName);

            }

        };

        Thread thread = new Thread(runnable);
        thread.start();

    }

    private void writeToFile(byte[] array, String nameFile) {


        try (FileOutputStream stream = new FileOutputStream(getApplicationContext().getExternalFilesDir("") + "/" + nameFile)) {
            String path = getApplicationContext().getExternalFilesDir("") + "/" + nameFile;

            File file = new File(path);
            if (!file.exists()) {
                file.createNewFile();
            }

            stream.write(array);
            stream.flush();


        } catch (IOException e1) {
            e1.printStackTrace();
            throw new RuntimeException("WriteToFile");
        }
    }

    private byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
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

    private void disabledBtns() {
        encodeBtn.setEnabled(false);
        decodeBtn.setEnabled(false);

    }

    private void enabledBtns() {
        encodeBtn.setEnabled(true);
        decodeBtn.setEnabled(true);

    }

}
