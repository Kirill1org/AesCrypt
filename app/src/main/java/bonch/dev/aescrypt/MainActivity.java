package bonch.dev.aescrypt;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class MainActivity extends AppCompatActivity {

    private AESCryptCBD aesCryptCBD;
    private AESCryptCTR aesCryptCTR;

    private Button encodeBtn;
    private Button decodeBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setClickListeners();
        initCryptoParams();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Constants.ENCODE_REQUEST: {
                if (resultCode == RESULT_OK) {

                    try {

                        disabledBtns();
                        InputStream iStream = getContentResolver().openInputStream(data.getData());
                        encodeFile(getBytes(iStream), getNameFromPath(data.getData().getPath()));


                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
                break;
            }
            case Constants.DECODE_REQUEST: {
                if (resultCode == RESULT_OK) {

                    try {
                        disabledBtns();
                        InputStream iStream = getContentResolver().openInputStream(data.getData());
                        decodeFile(getBytes(iStream), getNameFromPath(data.getData().getPath()));

                    } catch (IOException e) {
                        e.printStackTrace();
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

    private void setClickListeners() {

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


    private void encodeFile(final byte[] inputFile, final String fileName) {

        final Handler handler = new Handler(Looper.myLooper()) {
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


                try {
                    writeToFile(aesCryptCBD.encrypt(inputFile), fileName);
                } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
                    e.printStackTrace();
                }

            }

        };

        Thread thread = new Thread(runnable);
        thread.start();
    }

    private void decodeFile(final byte[] encodedFile, final String fileName) {

        final Handler handler = new Handler(Looper.myLooper()) {
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


                try {
                    writeToFile(aesCryptCBD.decrypt(encodedFile), fileName);
                } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException | UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

            }

        };

        Thread thread = new Thread(runnable);
        thread.start();

    }

    private void writeToFile(byte[] array, String nameFile) {

        try {
            String path = getApplicationContext().getExternalFilesDir("") + "/" + nameFile;

            File file = new File(path);
            if (!file.exists()) {
                file.createNewFile();
            }

            FileOutputStream stream = new FileOutputStream(path);
            stream.write(array);
            stream.flush();
            stream.close();


        } catch (FileNotFoundException e1) {
            Log.e("TAG", e1.getMessage());
        } catch (IOException e) {
            Log.e("TAG", e.getMessage());
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
    }

    private void initCryptoParams() {

        SecureRandom secureRandom = new SecureRandom();

        byte[] key = new byte[16];
        //secureRandom.nextBytes(key);
        key=new byte[]{28, 1, 119, -115, 35, -42, -90, 127, -66, -59, -41, 104, -53, 123, 96, -107};
        Log.e("KEY IS:", Arrays.toString(key));

        byte[] iv = new byte[16];
        //secureRandom.nextBytes(iv);
        iv = new byte[] {32, 103, -65, 89, -109, -100, -6, -40, -107, -18, 54, -38, 26, -112, 122, -89};
        Log.e("IV IS:", Arrays.toString(iv));

        aesCryptCBD = AESCryptCBD.getInstance(key, iv);
        aesCryptCTR = AESCryptCTR.getInstance(key,iv);
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
