package com.laboratory600.peng.http;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

/********************
 *
 * Place:HITWH,laboratory600
 * Author:Peng       Version:1.0        Date: 2015/5/1
 * Description:Take photographs and post them to servlet
 *
 *
 *
 *
 * *****************/

public class MotorPostActivity extends Activity {

    private ImageView imageView;
    String URL = "http://172.31.138.70:8080/myServlet/servlet/MyServlet";
    String file_str = Environment.getExternalStorageDirectory().getPath();
    SharedPreferences mySharedPreferences;
    File mars_file = new File(file_str + "/motorcamera");
    File file_go;
    MainMyHandler mhandler;
    BitmapFactory.Options options = new BitmapFactory.Options();
    int plus = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_airdoor);
        Button button2 = (Button) findViewById(R.id.button2);
        Button button1 = (Button) findViewById(R.id.button);
        imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);//像素太高只能关掉硬件加速
        /**提取sharedPreferences储存数据*/
        mySharedPreferences = getSharedPreferences("URL", Activity.MODE_PRIVATE);
        String burl = mySharedPreferences.getString("MotorURL", "");
        if (!burl.equals("")) {
            URL = burl;
        }

       /**确保存在可存储空间并创建存相片的文件mars_file*/
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            if ((!mars_file.exists())&&(!mars_file.mkdirs())) {

                    new AlertDialog.Builder(this)
                            .setTitle("提示")  //创建失败时提示退出
                            .setMessage("无法创建文件")
                            .setPositiveButton("退出", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    android.os.Process.killProcess(android.os.Process.myPid());
                                }
                            })
                            .show();


            }else{
            /**调节相片显示大小**/
            options.outWidth = 4096;
            options.outHeight = 4096;
            /**检测文件mars_file中已有照片的最大下标**/
            File[] a = mars_file.listFiles();
            String fileName;
            int b = a.length;
            int s ;
            int nameLength;
            int nameChar;
            int max;
            if (b != 0) {

                for (File anA : a) {
                    fileName = anA.getName();
                    nameLength = fileName.length();
                    max = 0;
                    if (fileName.substring(0, 4).equals("file") &&
                            fileName.substring(nameLength - 4, nameLength).equals(".jpg")) {
                        for (s = nameLength - 5; s > 3; s--) {
                            nameChar = fileName.charAt(s);
                            if (47 < nameChar && nameChar < 58) {
                                max += (nameChar - 48) * StrictMath.pow(10, s - 4);
                            } else {
                                break;
                            }
                        }
                    }
                    if (max > plus) {
                        plus = max;
                    }
                }
                /**初始化图片显示*/
                file_go = new File(file_str + "/motorcamera/file" + String.valueOf(plus) + ".jpg");
                try {
                    Bitmap bitmap = getDiskbitmap(file_go, options);
                    imageView.setImageBitmap(bitmap);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }



                /**拍照*/
                button1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    plus++;
                    file_go = new File(file_str + "/motorcamera/file" + String.valueOf(plus) + ".jpg");
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file_go));
                    startActivityForResult(intent, 0x1);

                }


            });
            /**上传照片*/
            button2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    file_go = new File(file_str + "/motorcamera/file" + String.valueOf(plus) + ".jpg");
                    if (file_go.exists()) {
                        Toast.makeText(MotorPostActivity.this, "上传中", Toast.LENGTH_LONG).show();
                        FileImageUpload fileImageUpload = new FileImageUpload(file_go, URL);
                        new Thread(fileImageUpload).start();


                    } else {
                        Toast.makeText(MotorPostActivity.this, "请拍照", Toast.LENGTH_LONG).show();
                    }
                }
            });
           }
        } else {
            Toast.makeText(MotorPostActivity.this, "请先装好sd卡", Toast.LENGTH_LONG).show();
        }
        mhandler = new MainMyHandler(MotorPostActivity.this);
    }

    /**
     * 拍照成功则更新显示的照片
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == 0x1) && (resultCode == RESULT_OK)) {
            try {
                Bitmap bitmap = getDiskbitmap(file_go, options);
                imageView.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            plus--; //没拍照则不加
        }
    }
    static class MainMyHandler extends Handler {
        WeakReference<MotorPostActivity> mActivity;

        MainMyHandler(MotorPostActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MotorPostActivity theActivity = mActivity.get();
            switch (msg.what) {
                case 0:
                    Toast.makeText(theActivity, "发送异常", Toast.LENGTH_SHORT).show();
                    break;
                case 2:
                    Toast.makeText(theActivity, (String) msg.obj, Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    /**
     * 通过文件流创建Bitmap
     */
    public Bitmap getDiskbitmap(File file, BitmapFactory.Options options) throws FileNotFoundException {
        Bitmap bitmap = null;
        if (file.exists()) {
            InputStream inputStream = new FileInputStream(file);
            bitmap = BitmapFactory.decodeStream(inputStream, null, options);
        }
        return bitmap;
    }

    /**
     * 上传线程
     */
    public class FileImageUpload implements Runnable {
        private static final String TAG = "uploadFile";
        private static final int TIME_OUT = 10 * 10000000; //超时时间
        private static final String CHARSET = "utf-8"; //设置编码

        File file;
        String RequestURL;

        public FileImageUpload(File file, String RequestURL) {
            this.file = file;
            this.RequestURL = RequestURL;
        }

        @Override
        public void run() {
            String BOUNDARY = UUID.randomUUID().toString(); //边界标识 随机生成
            String PREFIX = "--", LINE_END = "\r\n";
            String CONTENT_TYPE = "multipart/form-data"; //内容类型
            HttpURLConnection conn = null;
            try {
                URL url = new URL(RequestURL);
                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(TIME_OUT);
                conn.setConnectTimeout(TIME_OUT);
                conn.setDoInput(true); //允许输入流
                conn.setDoOutput(true); //允许输出流
                conn.setUseCaches(false); //不允许使用缓存
                conn.setRequestMethod("POST"); //请求方式
                conn.setRequestProperty("Charset", CHARSET);
                //设置编码
                conn.setRequestProperty("connection", "keep-alive");
                conn.setRequestProperty("Content-Type", CONTENT_TYPE + ";boundary=" + BOUNDARY);
                if (file != null) {
                    OutputStream outputSteam = conn.getOutputStream();
                    DataOutputStream dos = new DataOutputStream(outputSteam);
                    StringBuffer sb = new StringBuffer();
                    sb.append(PREFIX);
                    sb.append(BOUNDARY);
                    sb.append(LINE_END);
                    /**
                     *
                     * name里面的值为服务器端需要key 只有这个key 才可以得到对应的文件
                     *
                     */
                    sb.append("Content-Disposition: form-data; name=\"img\"; filename=\"" + file.getName() + "\"" + LINE_END);
                    sb.append("Content-Type: application/octet-stream; charset=" + CHARSET + LINE_END);
                    sb.append(LINE_END);
                    dos.write(sb.toString().getBytes());
                    InputStream is = new FileInputStream(file);
                    byte[] bytes = new byte[1024];
                    int len;
                    while ((len = is.read(bytes)) != -1) {   //read不能保证bytes被写满，但会返回读取字节数
                        dos.write(bytes, 0, len);
                    }
                    is.close();
                    dos.write(LINE_END.getBytes());
                    byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINE_END).getBytes();
                    dos.write(end_data);
                    dos.flush();

                    /**获取服务器结果**/
                    InputStreamReader in = new InputStreamReader(conn.getInputStream(), "UTF-8");//读取字节
                    BufferedReader bufferedReader = new BufferedReader(in);
                    StringBuilder buffer = new StringBuilder();//缓冲
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        buffer.append(line);
                    }
                    mhandler.obtainMessage(2, buffer.toString()).sendToTarget();
                    in.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
                mhandler.obtainMessage(0).sendToTarget();
            }finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }

        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_settings:
                final EditText view = new EditText(this);
                view.setText(URL);
                new AlertDialog.Builder(MotorPostActivity.this)
                        .setTitle("设置URL")
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setView(view)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                URL = String.valueOf(view.getText());
                                mySharedPreferences.edit().putString("AirURL", URL).apply();
                            }
                        })
                        .setNegativeButton("取消", null)
                        .show();
                break;

        }
        return super.onOptionsItemSelected(item);
    }
}