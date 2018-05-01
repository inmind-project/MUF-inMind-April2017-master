package edu.cmu.inmind.multiuser.sara.component.nlg;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by yoichimatsuyama on 10/18/16.
 */
public class Util {
    static public ArrayList<String> getFileList(String folderPath){
        ArrayList<String> resultList = new ArrayList<String>();
        File folder = new File(folderPath);
        File[] listOfFiles = folder.listFiles();

        for (File file : listOfFiles) {
            if (file.isFile()) {
                //System.out.println(file.getName());
                String name = file.getName().replace(".txt", "");
                resultList.add(name);
            }
        }
        return resultList;
    }

    static public ArrayList<String> getLines(String path){
        ArrayList<String> resultList = new ArrayList<String>();
        File file = new File(path);
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line = "";
            try {
                while ((line = br.readLine()) != null){
                    //System.out.println(line);
                    resultList.add(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return resultList;
    }


    public static boolean isNumeric(String str)
    {
        return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
    }

    public static String ignoreSpecialCases(String input){
        String output = input.replace(",", "");
        output = input.replace("(inaudible)","");
        output = input.replace("[inaudible]","");
        output = input.replace("\"","");
        //output = output.replace(".", "");
        //output = output.replace("!", "");
        //output = output.replace("?", "");
        output = output.replace("[", "");
        output = output.replace("]", "");
        output = output.replace("(", "");
        output = output.replace(")", "");
        output = output.replace("..", "");
        output = output.replace("...", "");
        //output = output.replace("\"", "");
        //output = output.replace("\'", "");
        return output;
    }

    public static void writeFile(String msg, String fileName) {
        File file = new File(fileName);
        try {
            FileWriter fw = new FileWriter(file);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(msg);
            bw.flush();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void appendFile(String msg, String fileName) {
        File file = new File(fileName);
        try {
            FileWriter fw = new FileWriter(file, true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(msg);
            bw.flush();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static Set<String> getSet(List<String> strList){
        Set<String> strSet = new HashSet<String>();
        for(String s : strList){
            strSet.add(s);
        }
        return strSet;
    }

    public static float average(ArrayList<Float> values){
        float sum = 0.0f;
        for(float f : values){
            sum += f;
        }
        float ave  = sum/values.size();
        return ave;
    }
}
