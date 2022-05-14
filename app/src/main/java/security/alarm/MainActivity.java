package security.alarm;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.rewarded.RewardedAd;
import java.util.Locale;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

//public class MainActivity extends AppCompatActivity {
public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_LOCATION = 1;
    private int startflag = 0;
    private int stopcount = -1;
    private int stopmax = 1;
    private MediaPlayer bgm;
    private String bgm_name;
    public String mess_disp = ("");
    public String mess_mail = ("");
    // GPS用
    private boolean gpsflag;
    private LocationManager mLocationManager;
    private static int PERMISSION_REQUEST_CODE = 1;
    private static final String[] INITIAL_PERMS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
            };
//    static public Intent intent;
    //  設定
    private boolean auto_alarm_flag;
    private boolean alarm_stop_flag;
    private int alarm_volume;
    private String mailaddr1 = "";
    private String mailaddr2 = "";
    private String mailaddr3 = "";
    private String mailaddr4 = "";
    private String mailaddr5 = "";
    private String mailtitle = "";
    private String mailtext = "";
    //  国設定
    private Locale _local;
    private String _language;
    private String _country;

    // 広告
    private AdView mAdview;

    //  DB関連
    public MyOpenHelper helper;         //DBアクセス
    private int db_isopen = 0;          //DB使用したか
    private int db_level = 0;           //DBユーザーレベル
    private int db_data1 = 0;           //DB予備データ
    private int db_data2 = 0;           //DB
    private int db_data3 = 0;           //DB

    final int LV_MAX = 99;               //ユーザーレベルMAX

    // リワード広告
//    private RewardedVideoAd mRewardedVideoAd;
    /*
    // テストID
    private static final String AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917";
    // テストID(APPは本物でOK)
    private static final String APP_ID = "ca-app-pub-4924620089567925~9620469063";
     */
//    private static final String AD_UNIT_ID = "ca-app-pub-4924620089567925/7856940532";
//    private static final String APP_ID = "ca-app-pub-4924620089567925~9620469063";

    //  アプリ生成時の処理
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //  国設定
        _local = Locale.getDefault();
        _language = _local.getLanguage();
        _country = _local.getCountry();

        //　効果音
        bgm = MediaPlayer.create(this, R.raw.alarm);

        //広告
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        mAdview = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdview.loadAd(adRequest);
        mAdview.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
            }

            @Override
            public void onAdFailedToLoad(LoadAdError adError) {
                // Code to be executed when an ad request fails.
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
            }

            @Override
            public void onAdClicked() {
                // Code to be executed when the user clicks on an ad.
            }

            @Override
            public void onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
            }
        });
//        mAdview = findViewById(R.id.adView);
//        AdRequest adRequest = new AdRequest.Builder().build();
//        mAdview.loadAd(adRequest);

        // リワード広告
        /*
        MobileAds.initialize(this, APP_ID);
        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this);
        mRewardedVideoAd.setRewardedVideoAdListener(this);
        loadRewardedVideoAd();
        */

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.v("LifeCycle", "------------------------------>PERMISSION 1");

            // Check Permissions Now
            mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    //            gpsflag = true;
    //            gpsflag = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            gpsflag = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

    //        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, new LocationListener() {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 1, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
            /* テスト用 */
//                    location.setLatitude(35.71023);
//                    location.setLongitude(139.797603);

                if (_language.equals("ja")) {
                    mess_disp = " 緯度：" + location.getLatitude() + "\n 経度：" + location.getLongitude() + "\n\n";
                    mess_mail = "現在の緯度,経度\n" + "http://maps.apple.com/?q=" + location.getLatitude() + "," + location.getLongitude();
                } else if (_language.equals("zh")) {
                    mess_disp = " 纬度:" + location.getLatitude() + "\n 经度　:" + location.getLongitude() + "\n\n";
                    mess_mail = "当前的纬度，经度\n" + "http://maps.apple.com/?q=" + location.getLatitude() + "," + location.getLongitude();
                } else if (_language.equals("es")) {
                    mess_disp = " latitud:" + location.getLatitude() + "\n longitud:" + location.getLongitude() + "\n\n";
                    mess_mail = "latitud,longitud\n" + "http://maps.apple.com/?q=" + location.getLatitude() + "," + location.getLongitude();
                } else if (_language.equals("pt")) {
                    mess_disp = " latitude:" + location.getLatitude() + "\n longitude:" + location.getLongitude() + "\n\n";
                    mess_mail = "latitude,longitude\n" + "http://maps.apple.com/?q=" + location.getLatitude() + "," + location.getLongitude();
                } else {
                    mess_disp = " latitude:" + location.getLatitude() + "\n longitude:" + location.getLongitude() + "\n\n";
                    mess_mail = "Current latitude,longitude\n" + "http://maps.apple.com/?q=" + location.getLatitude() + "," + location.getLongitude();
                }
                Log.d("GPS", mess_disp);
                mLocationManager.removeUpdates(this);

                TextView v = (TextView) findViewById(R.id.textView);
                v.setText(mess_disp);
                v.setTextColor(Color.WHITE);
                v.setBackgroundTintList(null);
                v.setBackgroundResource(R.drawable.bak_flat);
            }

            @Override
            public void onProviderDisabled(String provider) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }
            });

        }
        else {
            Log.v("LifeCycle", "------------------------------>PERMISSION 0");

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_CODE);

            AlertDialog.Builder ad = new AlertDialog.Builder(this);
            if (_language.equals("ja")) {
                ad.setTitle(" 現在位置の取得について");
                ad.setMessage("\n\n\n 位置情報の取得を許可した場合、次回起動時より現在位置の取得が可能です\n\n\n\n");
                ad.setPositiveButton("ＯＫ", null);
            } else if (_language.equals("zh")) {
                ad.setTitle("对于收购的当前位置");
                ad.setMessage("\n\n\n如果允许获取的位置信息，它是可得到比下次启动时的当前位置\n\n\n\n");
                ad.setPositiveButton("确认", null);
            } else if (_language.equals("es")) {
                ad.setTitle("Para la adquisición de la posición actual");
                ad.setMessage("\n\n\nSi permite que la adquisición de información de posición, está disponible conseguir la posición actual de la próxima puesta en marcha\n\n");
                ad.setPositiveButton("cancelar", null);
            } else if (_language.equals("pt")) {
                ad.setTitle("Para a aquisição da posição actual");
                ad.setMessage("\n\n\nSe você permitir que a aquisição de informações de posição, ele está disponível obter a posição atual do que a próxima inicialização\n\n");
                ad.setPositiveButton("cancelar", null);
            } else {
                ad.setTitle("About acquisition of current position");
                ad.setMessage("\n\n\nWhen acquiring position information is permitted, the current position can be acquired from the next startup\n\n\n");
                ad.setPositiveButton("cancel", null);
            }
            ad.show();
        }


    }

    @Override
    public void onStart() {
    super.onStart();// ATTENTION: This was auto-generated to implement the App Indexing API.
    Log.v("LifeCycle", "------------------------------>onStart");

        //  国設定
//        _local = Resources().getSystem().getConfiguration().locale;

        //DBのロード
        /* データベース */
        helper = new MyOpenHelper(this);
        AppDBInitRoad();

        //  設定関連
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        auto_alarm_flag = sharedPreferences.getBoolean("auto_alarm", false);
        alarm_stop_flag = sharedPreferences.getBoolean("alarm_stop", false);
        mailaddr1 = sharedPreferences.getString("mail_addr1", "");
        mailaddr2 = sharedPreferences.getString("mail_addr2", "");
        mailaddr3 = sharedPreferences.getString("mail_addr3", "");
        mailaddr4 = sharedPreferences.getString("mail_addr4", "");
        mailaddr5 = sharedPreferences.getString("mail_addr5", "");
        mailtitle = sharedPreferences.getString("mail_title", "");
        mailtext = sharedPreferences.getString("mail_text", "");
        String bgm_str = sharedPreferences.getString("alarm_kind", "default");
        String str = sharedPreferences.getString("alarm_value", "0");
        alarm_volume = Integer.parseInt(str);
        //音量調整
        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        // 現在の音量を取得する
        int ringVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        int ringMaxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        // 音量を設定する
        am.setStreamVolume(AudioManager.STREAM_MUSIC, alarm_volume, 0);
//        Toast.makeText(this, str+"現在"+ringVolume+"最大"+ringMaxVolume, Toast.LENGTH_SHORT).show();

        //  アラーム種類が変更？
        if (bgm_name != bgm_str) {
            if (bgm.isPlaying() == true) {
                bgm.stop();
                bgm = null;
                startflag = 0;
            }
            bgm_name = bgm_str;
            if (bgm_str.equals("kind_2") == true) bgm = MediaPlayer.create(this, R.raw.alarm2);
            else if (bgm_str.equals("kind_3") == true) bgm = MediaPlayer.create(this, R.raw.alarm3);
            else if (bgm_str.equals("kind_4") == true) bgm = MediaPlayer.create(this, R.raw.alarm4);
            else if (bgm_str.equals("kind_5") == true) bgm = MediaPlayer.create(this, R.raw.alarm5);
            else if (bgm_str.equals("kind_6") == true) bgm = MediaPlayer.create(this, R.raw.alarm6);
            else if (bgm_str.equals("kind_7") == true) bgm = MediaPlayer.create(this, R.raw.alarm7);
            else bgm = MediaPlayer.create(this, R.raw.alarm);
        }

        //  アラーム自動スタート制御
        if (auto_alarm_flag == false) {
            if (bgm.isPlaying() == false) {
                btnStartDisp();
                startflag = 0;
            }
        } else {
            if (bgm.isPlaying() == false) {
                bgm.setLooping(true);
                bgm.start();
            }
            startflag = 1;
            stopcount = 0;
            btnStopDisp();
        }
        //  アラーム停止制御
        if (alarm_stop_flag == false) stopmax = 1;
        else stopmax = 10;

        //  状態テスト表示
        if (mess_disp.isEmpty() == true)
        {
        //           gpsflag = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (gpsflag == false) {
                TextView v = (TextView) findViewById(R.id.textView);
                if (_language.equals("ja")) {
                    v.setText(" 現在地の取得ができません\n 位置情報の設定を確認して下さい");
                } else if (_language.equals("zh")) {
                    v.setText("你不能得到您的位置\n检查的位置信息的设定");
                } else if (_language.equals("es")) {
                    v.setText("No se puede obtener de su ubicación.\nCompruebe el ajuste de la ubicación.");
                    v.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
                } else if (_language.equals("pt")) {
                    v.setText("Você não pode obter a sua localização.\nVerifique a definição da localização");
                    v.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
                } else {
                    v.setText("You can not get your location.\nCheck the setting of the location");
                    v.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
                }
                v.setTextColor(Color.RED);

            }
            else {
                TextView v = (TextView) findViewById(R.id.textView);
                if (_language.equals("ja")) {
                    v.setText(" 現在地を取得しています\n しばらくお待ち下さい．．．");
                } else if (_language.equals("zh")) {
                    v.setText("你必须得到当前位置\n请稍等片刻 ...");
                } else if (_language.equals("es")) {
                    v.setText("Usted tiene que obtener la ubicación actual. por favor espera ...");
                    v.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
                } else if (_language.equals("pt")) {
                    v.setText("Você tem que obter a localização atual.\nPor favor, espere ...");
                    v.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
                } else {
                    v.setText("You have to get the current location.\nPlease wait ...");
                    v.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
                }
                v.setTextColor(Color.BLACK);
            }
        }
    }

    //  アラーム停止処理（ボタン押下処理）
    public void alarm_stop() {
        bgm.pause();
        startflag = 0;
        if (startflag == 0) {
            btnStartDisp();
        }
    }

    //  アラーム開始と停止処理（ボタン押下処理）
    public void alarm_start_stop() {
        /* 効果音スタートの操作 */
        if (startflag == 0) {
            startflag = 1;
            stopcount = 0;
            //  効果音
            bgm.setLooping(true);
            bgm.start();

            if (startflag == 1) {
                btnStopDisp();
            }
        }
        /* 効果音ストップの操作 */
        else {
            stopcount++;
            if (stopcount >= stopmax) {
                if (alarm_stop_flag == true) {
                    AlertDialog.Builder ad = new AlertDialog.Builder(this);
                    if (_language.equals("ja")) {
                        ad.setTitle("アラーム停止の確認");
                        ad.setMessage("\n\n\nアラームを停止しました\n\n\n\n\n\n");
                        ad.setPositiveButton("ＯＫ", null);
                    } else if (_language.equals("zh")) {
                        ad.setTitle("报警停止的确认");
                        ad.setMessage("\n\n\n它已经停止的报警\n\n\n\n\n\n");
                        ad.setPositiveButton("确认", null);
                    } else if (_language.equals("es")) {
                        ad.setTitle("La confirmación de la parada de alarma");
                        ad.setMessage("\n\n\nSe ha detenido la alarma\n\n\n\n\n\n");
                        ad.setPositiveButton("cancelar", null);
                    } else if (_language.equals("pt")) {
                        ad.setTitle("A confirmação da paragem de alarme");
                        ad.setMessage("\n\n\nSe tiver parado o alarme\n\n\n\n\n\n");
                        ad.setPositiveButton("cancelar", null);
                    } else {
                        ad.setTitle("Confirmation of the alarm stop");
                        ad.setMessage("\n\n\nIt has stopped the alarm\n\n\n\n\n\n");
                        ad.setPositiveButton("cancel", null);
                    }
                    ad.show();
                }
                this.alarm_stop();
            }
        }
    }

    //  ボタン：TIPS
    /* **************************************************
        TIPS　ボタン処理
    ****************************************************/
    public void onTips(View view){
        AlertDialog.Builder guide = new AlertDialog.Builder(this);
        TextView vmessage = new TextView(this);
        int level = 0;
        String pop_message = "";
        String btn_yes = "";
        String btn_no = "";

        if (bgm.isPlaying() == false) {

            //ユーザーレベル算出
            level = db_level;
            level++;
            if (level >= LV_MAX){
                level = LV_MAX;
            }

            if (_language.equals("ja")) {

                pop_message += "\n\n 動画を視聴してポイントをGETしますか？" +
                        "\n\n（ポイントをGETするとアプリ機能が追加します）" +
                        "\n　１回視聴：現在位置をメールに自動セット" +
                        "\n　２回以上視聴：ポイントが増えます" +
                        "\n 　現在のポイント「"+db_level+"」→「"+level+"」"+"\n \n\n\n";

                btn_yes += "視聴";
                btn_no += "中止";
            }
            else{
                pop_message += "\n\n \n" +
                        "Do you want to watch the video and get POINTS ?" +
                        "\n\n\n App function will be added when you get POINTS." +
                        "\nAutomatically set your current location to email."+
                        "\n\n POINTS「"+db_level+"」→「"+level+"」"+"\n \n\n\n";

                btn_yes += "YES";
                btn_no += "N O";
            }

            //メッセージ
            vmessage.setText(pop_message);
            vmessage.setBackgroundColor(Color.DKGRAY);
            vmessage.setTextColor(Color.WHITE);
            //タイトル
            guide.setTitle("TIPS");
            guide.setIcon(R.drawable.present);
            guide.setView(vmessage);

            guide.setPositiveButton(btn_yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    /*
                    if (mRewardedVideoAd.isLoaded()) {
                        mRewardedVideoAd.show();
                    }*/

                    //test_make
                    db_level++;
                }
            });
            guide.setNegativeButton(btn_no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    btnStartDisp();
                }
            });

            guide.create();
            guide.show();
        }
        else{

        }
    }


    //  ボタン：効果音スタート＆ストップ
    public void onStartStop(View view) {
        this.alarm_start_stop();
    }

    //  ボタン：スピーカー
    public void onSpeaker(View view) {
        this.alarm_start_stop();
    }

    private void btnStartDisp() {
        Button btn1 = (Button) findViewById(R.id.btn_startstop);
        btn1.setBackgroundTintList(null);
        btn1.setBackgroundResource(R.drawable.bak_emer);
        btn1.setTextColor(Color.parseColor("white"));

        if (_language.equals("es")) {
            btn1.setText("COMIENZO");
            btn1.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 48);
        } else if (_language.equals("pt")) {
            btn1.setText("COMEÇAR");
            btn1.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 54);
        } else {
            btn1.setText("START");
        }

        ImageButton imgbtn1 = (ImageButton) findViewById(R.id.btn_img_speaker);
        imgbtn1.setImageResource(R.drawable.speaker_0);
        imgbtn1.setBackgroundTintList(null);
        imgbtn1.setBackgroundResource(R.drawable.bak_grad);

        ImageButton imgbtn2 = (ImageButton) findViewById(R.id.btn_img_mail);
        imgbtn2.setBackgroundTintList(null);

        /*
        Button btn2 = (Button) findViewById(R.id.btn_tips);
        btn2.setBackgroundTintList(null);
        btn2.setBackgroundResource(R.drawable.btn_grad2);
        btn2.setTextColor(Color.parseColor("gray"));
         */
    }

    private void btnStopDisp() {
        Button btn1 = (Button) findViewById(R.id.btn_startstop);
        btn1.setBackgroundTintList(null);
        btn1.setBackgroundResource(R.drawable.bak_grad);
        btn1.setTextColor(Color.parseColor("red"));

        if (_language.equals("es")) {
            btn1.setText("DETENER");
            btn1.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 48);
        } else if (_language.equals("pt")) {
            btn1.setText("PARE");
            btn1.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 54);
        } else {
            btn1.setText("STOP");
        }

        ImageButton imgbtn1 = (ImageButton) findViewById(R.id.btn_img_speaker);
        imgbtn1.setImageResource(R.drawable.speaker_1);
        imgbtn1.setBackgroundTintList(null);
        imgbtn1.setBackgroundResource(R.drawable.bak_emer);

        ImageButton imgbtn2 = (ImageButton) findViewById(R.id.btn_img_mail);
        imgbtn2.setBackgroundTintList(null);

        /*
        Button btn2 = (Button) findViewById(R.id.btn_tips);
        btn2.setBackgroundTintList(null);
        btn2.setBackgroundResource(R.drawable.btn_grad2);
        btn2.setTextColor(Color.parseColor("gray"));
         */

    }


    //  ボタン：メール送信
    public void onImgMail(View view) {
        mailSend();
    }

    public void composeEmail(String[] addresses, String subject, String message) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_EMAIL, addresses);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, message);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    public void mailSend() {
        /* 送信 */
        String all_mailaddr[] = new String[5];
        String message = "";
        if (mailaddr1.isEmpty() == false) all_mailaddr[0] = "" + mailaddr1;
        if (mailaddr2.isEmpty() == false) all_mailaddr[1] = "" + mailaddr2;
        if (mailaddr3.isEmpty() == false) all_mailaddr[2] = "" + mailaddr3;
        if (mailaddr4.isEmpty() == false) all_mailaddr[3] = "" + mailaddr4;
        if (mailaddr5.isEmpty() == false) all_mailaddr[4] = "" + mailaddr5;
        message = "" + mess_mail + "\n" + mailtext;
        composeEmail(all_mailaddr, mailtitle, message);
    }

    //  戻るボタン
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_BACK:
            // ダイアログ表示など特定の処理を行いたい場合はここに記述
            // 親クラスのdispatchKeyEvent()を呼び出さずにtrueを返す

                /* アラームが鳴っている場合は警告表示 */
                if (bgm.isPlaying() == false) {
                    break;
                }

                AlertDialog.Builder ad = new AlertDialog.Builder(this);
                if (_language.equals("ja")) {
                    ad.setTitle("[戻る]は操作無効です");
                    ad.setMessage("\n\nアラームを停止して下さい\n\nアラーム停止後は操作可能です\n\n\n\n");
                }
                else if (_language.equals("zh")) {
                    ad.setTitle("[返回]按钮是无效操作");
                    ad.setMessage("\n\n请停止报警\n\n后报警停止操作是\n\n\n\n");
                }
                else if (_language.equals("es")) {
                    ad.setTitle("[Volver] es operación no válida");
                    ad.setMessage("\n\nPor favor, detener la alarma.\n\nDespués de la parada de la alarma está operativa.\n\n\n");
                }
                else if (_language.equals("pt")) {
                    ad.setTitle("[Voltar] é uma operação inválida");
                    ad.setMessage("\n\nPor favor, pare o alarme.\n\nApós a paragem de alarme está operacional.\n\n\n");
                }
                else {
                    ad.setTitle("Invalid operation");
                    ad.setMessage("\n\n\nPlease stop the alarm.\n\nOperation is possible after the alarm is stopped.\n\n\n");
                }
                ad.setPositiveButton("ＯＫ", null);
                ad.show();
                return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    //  メニュー
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    //  メニュー選択時の処理
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent1 = new Intent(this, CrSetActivity.class);
            startActivity(intent1);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onResume() {
        super.onResume();
        Log.v("LifeCycle", "------------------------------>onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.v("LifeCycle", "------------------------------>onPause");
        //  DB更新
        AppDBUpdated();
    }

    @Override
    public void onRestart() {
        super.onRestart();
        Log.v("LifeCycle", "------------------------------>onRestart");
    }

    @Override
    public void onStop() {
        super.onStop();// ATTENTION: This was auto-generated to implement the App Indexing API.
        Log.v("LifeCycle", "------------------------------>onStop");
        //  DB更新
        AppDBUpdated();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v("LifeCycle", "------------------------------>onDestroy");

        //  DB更新
        AppDBUpdated();

        if (bgm.isPlaying() == true) {
            bgm.stop();
            bgm = null;
        }
        if (mLocationManager != null){
            mLocationManager = null;
        }
    }

    /**
     * DB関連処理
     */

    /***************************************************
        DB初期ロードおよび設定
    ****************************************************/
    public void AppDBInitRoad() {
        SQLiteDatabase db = helper.getReadableDatabase();
        StringBuilder sql = new StringBuilder();
        sql.append(" SELECT");
        sql.append(" isopen");
        sql.append(" ,level");
        sql.append(" ,data1");
        sql.append(" ,data2");
        sql.append(" ,data3");
        sql.append(" FROM appinfo;");
        try {
            Cursor cursor = db.rawQuery(sql.toString(), null);
            //TextViewに表示
            StringBuilder text = new StringBuilder();
            if (cursor.moveToNext()) {
                db_isopen = cursor.getInt(0);
                db_level = cursor.getInt(1);
                db_data1 = cursor.getInt(2);
                db_data2 = cursor.getInt(3);
                db_data3 = cursor.getInt(4);
            }
        } finally {
            db.close();
        }

        db = helper.getWritableDatabase();
        if (db_isopen == 0) {
            long ret;
            /* 新規レコード追加 */
            ContentValues insertValues = new ContentValues();
            insertValues.put("isopen", 1);
            insertValues.put("level", 0);
            insertValues.put("data1", 0);
            insertValues.put("data2", 0);
            insertValues.put("data3", 0);
            insertValues.put("data4", 0);
            insertValues.put("data5", 0);
            insertValues.put("data6", 0);
            insertValues.put("data7", 0);
            insertValues.put("data8", 0);
            insertValues.put("data9", 0);
            insertValues.put("data10", 0);
            try {
                ret = db.insert("appinfo", null, insertValues);
            } finally {
                db.close();
            }
            db_isopen = 1;
            db_level = 0;
            /*
            if (ret == -1) {
                Toast.makeText(this, "DataBase Create.... ERROR", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "DataBase Create.... OK", Toast.LENGTH_SHORT).show();
            }
             */
        } else {
            /*
            Toast.makeText(this, "Data Loading...  interval:" + db_interval, Toast.LENGTH_SHORT).show();
             */
        }
    }

    /***************************************************
        DB更新
    ****************************************************/
    public void AppDBUpdated() {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues insertValues = new ContentValues();
        insertValues.put("isopen", db_isopen);
        insertValues.put("level", db_level);
        insertValues.put("data1", db_data1);
        insertValues.put("data2", db_data2);
        insertValues.put("data3", db_data3);
        int ret;
        try {
            ret = db.update("appinfo", insertValues, null, null);
        } finally {
            db.close();
        }
        /*
        if (ret == -1) {
            Toast.makeText(this, "Saving.... ERROR ", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Saving.... OK "+ "op=0:"+db_isopen+" interval=1:"+db_interval+" brightness=2:"+db_brightness, Toast.LENGTH_SHORT).show();
        }
         */
    }


}
