package com.example.akito.imedictconv;

import android.app.AlertDialog;
import android.os.Environment;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by akito on 2015/07/02.
 */
public class UserDictionaryConverter {
    private String homePath;
    private String inputPath;
    private String outputPath;
    private BufferedReader bufReader;
    private PrintWriter printWriter;
//    private String charSet;

    public UserDictionaryConverter(String inpath, String outpath) {
        homePath = Environment.getExternalStorageDirectory().getPath();
        inputPath = homePath + "/" + inpath;
        outputPath = homePath + "/" + outpath;
        bufReader = null;
        printWriter = null;
    }
    public UserDictionaryConverter() {
        this("", "");
    }

    public String getOutputPath() {
        return outputPath;
    }

    public void setInputPath(String inpath) {
        inputPath = homePath + "/" + inpath;
    }
    public void setOutputPath(int outNumber) {
        switch (outNumber) {
            case 0:
                outputPath = homePath + "/GoogleIMEDic.txt";
                break;
            case 1:
                outputPath = homePath + "/ATOK/ATOK_user_dic.txt";
                break;
            case 2:
                outputPath = homePath + "/Simeji/simeji_user_dic.txt";
                break;
            case 3:
                outputPath = homePath + "/FlickWnn/user_dic.txt";
                break;
            case 4:
                outputPath = homePath + "/pobox/backup_dic/JPNUserDict.txt";
                break;
            default:
                break;
        }
    }

    public void closeFile() {
        if (bufReader != null ) try {bufReader.close();} catch (IOException e) {}
        if (printWriter != null ) printWriter.close();
    }



    public void convertNormal(MainActivity ma, String inSeparator, String outSeparator, String charSet, String lf_code) throws IOException, IndexOutOfBoundsException {
        if (!checkBeforeRead(ma, charSet)) return;
        checkBeforeWrite(charSet);

        String str = null;
        if (inputPath.equals(homePath + "/pobox/backup_dic/JPNUserDict.txt")) {
            while ((str = bufReader.readLine()) != null) {
                if(!str.startsWith(";")) {
                    printWriter.print( str.replaceFirst("\t", outSeparator) + lf_code);
                }
            }

        } else if(inputPath.equals(homePath + "/GoogleIMEDic.txt")) {
            Pattern p = Pattern.compile("(^.+\t.+)(\t.+\t$)$");
            while ((str = bufReader.readLine()) != null) {
                Matcher m = p.matcher(str);
                if (m.find()) {
                    printWriter.print(m.group(1).replaceFirst("\t", outSeparator) + lf_code);
                }
            }

        } else if(inSeparator.equals(outSeparator)) {
            while ((str = bufReader.readLine()) != null) {
                printWriter.print(str+lf_code);
            }

        } else {
            while ((str = bufReader.readLine()) != null) {
                printWriter.print(str.replaceFirst(inSeparator, outSeparator) + lf_code);
            }
        }

        Toast.makeText(ma, outputPath + "に変換ファイルを保存しました", Toast.LENGTH_LONG).show();
    }

    public void convertFromSimeji(MainActivity ma, String separator, String charSet, String lf_code) throws IOException, IndexOutOfBoundsException {
        if (!checkBeforeRead(ma, charSet)) return;
        checkBeforeWrite(charSet);

        String data[] = (bufReader.readLine()).split("\"\\],\"JAJP_VALUE\":\\[\"");
        String key[] = data[0].split("\",\"");
        String value[] = data[1].split("\",\"");
        int last = key.length - 1;

        if (last > 0) {
            key[0] = key[0].substring(14);
            value[last] = value[last].substring(0, value[last].length() - 29);
        } else {
            // 辞書登録数が1のときの処理
            return;
        }

        for (int i = 0; i <= last; i++) {
            printWriter.print(key[i] + separator + value[i] + lf_code);
        }
        Toast.makeText(ma, outputPath + "に変換ファイルを保存しました", Toast.LENGTH_LONG).show();
    }

    public void convertToSimeji(MainActivity ma, String separator, String charSet) throws IOException, IndexOutOfBoundsException {
        if (!checkBeforeRead(ma, charSet)) return;
        checkBeforeWrite(charSet);

        ArrayList<String> value = new ArrayList<>();
        ArrayList<String> key = new ArrayList<>();
        String str = null;
        if (inputPath.equals(homePath + "/pobox/backup_dic/JPNUserDict.txt")) {
//            Pattern p = Pattern.compile("^;.*");
            while ((str = bufReader.readLine()) != null) {
//                if (!p.matcher(str).matches()) {
                if(!str.startsWith(";")) {
                    String[] tmp = str.split(separator);
                    key.add(tmp[0]);
                    value.add(tmp[1]);
                }
            }
        } else {
            while ((str = bufReader.readLine()) != null) {
                String[] tmp = str.split(separator);
                key.add(tmp[0]);
                value.add(tmp[1]);
            }
        }

        // 単語の数 keyまたはvalue引く1
        int last = key.size() - 1;

        //書き込み
        printWriter.print("{\"JAJP_KEY\":[");
        for (int i = 0; i < last; i++) {
            printWriter.print("\"" + key.get(i) + "\",");
        }
        printWriter.print("\"" + key.get(last) + "\"],\"JAJP_VALUE\":[");
        for (int i = 0; i < last; i++) {
            printWriter.print("\"" + value.get(i) + "\",");
        }
        printWriter.print("\"" + value.get(last) + "\"],\"EN_KEY\":[],\"EN_VALUE\":[]}" + "\n");
        Toast.makeText(ma, outputPath + "に変換ファイルを保存しました", Toast.LENGTH_LONG).show();
    }


    private boolean checkBeforeRead(MainActivity ma, String charSet) throws IOException {
        File file = new File(inputPath);
        if (file.exists()) if (file.isFile()) if (file.canRead()) {
            bufReader = new BufferedReader(new InputStreamReader(new FileInputStream(inputPath), charSet));
            return true;
        }
        new AlertDialog.Builder(ma)
                .setPositiveButton("OK", null)
                .setMessage(inputPath + "が読み込めませんでした\n\n" + "移行元IMEのユーザー辞書をエキスポートしてから再度お試しください").show();
        return false;
    }

    private void checkBeforeWrite(String charSet) throws IOException {
        File fi = new File(outputPath);
        File dir = fi.getParentFile();
        if (dir != null && !dir.exists()) {
            dir.mkdirs();
        }
        printWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputPath), charSet)));
    }

}
