package com.example.akito.imedictconv;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private String[] IME_Name;
    private TextView debugTv;
    private Button inBtn;
    private Button outBtn;
    private int inPosition = -1;
    private int outPosition = -1;
    private com.example.akito.imedictconv.UserDictionaryConverter userDicCon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        debugTv = (TextView) findViewById(R.id.debugtv);
        inBtn = (Button) findViewById(R.id.inbtn);
        outBtn = (Button) findViewById(R.id.outbtn);
        IME_Name = getResources().getStringArray(R.array.IMEList);
        userDicCon = new com.example.akito.imedictconv.UserDictionaryConverter();

        //ボタンで英小文字を表示できるように設定
        inBtn.setAllCaps(false);
        outBtn.setAllCaps(false);

        //ボタンを押したら選択ダイアログ表示
        inBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSelectionDialog(inPosition, true, "移行元のIMEを選択してください");
            }
        });
        outBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSelectionDialog(outPosition, false, "移行先のIMEを選択してください");
            }
        });

        findViewById(R.id.actionbt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkPosition()) {
                    receiveConvert();
                }
            }
        });
    }


    //チェックボタンダイアログの表示
    private void showSelectionDialog(int position, final boolean isInbtn, String title) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setSingleChoiceItems(IME_Name, position, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        //選択されたIMEの位置を取得
                        if (isInbtn) {
                            inBtn.setText("移行元:" + IME_Name[item]);
                            inPosition = item;
                        } else {
                            outBtn.setText("移行先:" + IME_Name[item]);
                            outPosition = item;
                        }
                        debugTv.setText(String.valueOf(inPosition) + ", " + String.valueOf(outPosition));
                        dialog.cancel();
                    }
                }).show();
    }

    //変換ボタンが押されたら最初に正しいラジオボタンが選択されているかチェック
    private boolean checkPosition() {
        AlertDialog.Builder aldb = new AlertDialog.Builder(this);
        aldb.setTitle("警告");
        aldb.setPositiveButton("OK", null);

        if (inPosition == -1 && outPosition == -1) {
            aldb.setMessage("IMEを選択してください").show();
            return false;
        } else if (inPosition == outPosition) {
            aldb.setMessage("異なるIMEを選択してください").show();
            return false;
        } else if (inPosition == -1) {
            aldb.setMessage("移行元のIMEを選択してください").show();
            return false;
        } else if (outPosition == -1) {
            aldb.setMessage("移行先のIMEを選択してください").show();
            return false;
        }
        return true;
    }

    //チェックを通ったらユーザーに確認して変換開始
    private void receiveConvert() {
        new AlertDialog.Builder(this)
                .setMessage(IME_Name[inPosition] + "のユーザー辞書を" + IME_Name[outPosition] + "用に変換しますか？")
                .setPositiveButton("変換", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startConverter();
                    }
                })
                .setNegativeButton("キャンセル", null)
                .show();
    }



    //移行元IMEの取得
    private void startConverter() {
        try {
            switch (inPosition) {
                case 0:
                    selectConverterFromGoogle();
                    break;
                case 1:
                    Toast.makeText(this, "すみません未実装です", Toast.LENGTH_SHORT).show();
                    break;
                case 2:
                    selectConverterFromSimeji();
                    break;
                case 3:
                    selectConverterFromWnn();
                    break;
                case 4:
                    selectConverterFromPobox();
                    break;
                case 5:
                    Toast.makeText(this, "すみません未実装です", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    debugTv.setText(String.valueOf(inPosition) + ", " + String.valueOf(outPosition));
            }
            debugTv.setText(userDicCon.getOutputPath());
        } catch (IOException | IndexOutOfBoundsException e) {
            debugTv.setText(e.toString());
            Toast.makeText(this, "例外が発生しました", Toast.LENGTH_SHORT).show();
        } finally {
            userDicCon.closeFile();
        }
    }


    //移行元IME毎のメソッドを呼ぶ

    private void selectConverterFromGoogle() throws IOException, IndexOutOfBoundsException {
        userDicCon.setInputPath("GoogleIMEDic.txt");
        userDicCon.setOutputPath(outPosition);
        switch (outPosition) {
            case 2:
                userDicCon.convertToSimeji(this, "\t", "UTF-8");
                break;
            case 3:
                userDicCon.convertNormal(this, "\t", " ", "UTF-8", "\n");
                break;
            case 4:
                userDicCon.convertNormal(this, "\t", "\t", "UTF-8", "\r\n");
                break;
            default:
                Toast.makeText(this, "すみません未実装です", Toast.LENGTH_SHORT).show();
        }
    }

    private void selectConverterFromSimeji() throws IOException, IndexOutOfBoundsException {
        userDicCon.setInputPath("Simeji/simeji_user_dic.txt");
        userDicCon.setOutputPath(outPosition);
        switch (outPosition) {
            case 0:
                userDicCon.convertFromSimeji(this, "\t", "UTF-8", "\t名\t\n");
                break;
            case 3:
                userDicCon.convertFromSimeji(this, " ", "UTF-8", "\n");
                break;
            case 4:
                userDicCon.convertFromSimeji(this, "\t", "UTF-8", "\r\n");
                break;
            default:
                Toast.makeText(this, "すみません未実装です", Toast.LENGTH_SHORT).show();
        }
    }

    private void selectConverterFromWnn() throws IOException, IndexOutOfBoundsException {
        userDicCon.setInputPath("FlickWnn/user_dic.txt");
        userDicCon.setOutputPath(outPosition);
        switch (outPosition) {
            case 0:
                userDicCon.convertNormal(this, " ", "\t", "UTF-8", "\t名\t\n");
                break;
            case 2:
                userDicCon.convertToSimeji(this, " ", "UTF-8");
                break;
            case 4:
                userDicCon.convertNormal(this, " ", "\t", "UTF-8", "\r\n");
                break;
            default:
                Toast.makeText(this, "すみません未実装です", Toast.LENGTH_SHORT).show();
        }
    }

    private void selectConverterFromPobox() throws IOException, IndexOutOfBoundsException {
        userDicCon.setInputPath("pobox/backup_dic/JPNUserDict.txt");
        userDicCon.setOutputPath(outPosition);
        switch (outPosition) {
            case 0:
                userDicCon.convertNormal(this, "\t", "\t", "UTF-8", "\t名\t\n");
                break;
            case 2:
                userDicCon.convertToSimeji(this, "\t", "UTF-8");
                break;
            case 3:
                userDicCon.convertNormal(this, "\t", " ", "UTF-8", "\n");
                break;
            default:
                Toast.makeText(this, "すみません未実装です", Toast.LENGTH_SHORT).show();
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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
