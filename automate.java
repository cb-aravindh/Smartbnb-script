package com.chargebee.tool;

//pending

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.ibm.json.java.OrderedJSONObject;
import org.json.JSONObject;
import org.json.simple.JSONArray;

import java.io.BufferedReader;
import java.io.FileReader;
import java.time.Month;
import java.util.*;

class TextParser {
    static String year;
    static String month;
    static String day;
    static String dates;
    static String images;
    static boolean status = false;
    static Multimap<String, Multimap> bucket = ArrayListMultimap.create();
    static org.json.JSONObject object = new JSONObject();
    static JSONArray yearList = new JSONArray();
    String title;
    String tag;
    String link;
    String content;
    String date;
    String image;

    public static void main(String[] args) throws Exception {

        String inputFile = "/Users/cb-aravindh/Desktop/test/index.txt";

        object.put("headline", "A timeline of feature releases");
        object.put("head-copy", "Subscribe below to get periodic digests on new features and their business impacts.");

        try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
            String line;
            //year and month object

            List<String> list = new ArrayList<>();
            String content = "";
            while ((line = br.readLine()) != null) {
                try {
                    if (line.contains("section")) {
                        if (content.length() != 0) {
                            list.add("CONTENT:" + removeTag(content.replace("[]", "").trim()));
                            content = "";
                        }
                        // list clean
                        if (list.size() != 0) {
                            execute(list);
                            list.clear();
                            images = "NOIMAGE";
                            status = false;
                        }

                        year = line.split(",")[1].split(" ")[2].replace("\"", "").replace("]", "");
                        month = getMonth(line.split(",")[1].split(" ")[1].replace("\"", "").replace("]", ""));
                        day = line.split(",")[0].split(" ")[2];
                        dates = getDate(day, Month.valueOf(month.toUpperCase()).getValue(), year);

                    } else if (line.contains("hero") || line.contains("relnote")) {
                        if (content.length() != 0) {
                            list.add("CONTENT:" + removeTag(content.replace("[]", "")));
                            content = "";
                        }
                        if (list.size() != 0) {
                            execute(list);
                            list.clear();
                            images = "NOIMAGE";
                        }
                        String title = line.substring(line.indexOf('"') + 1, line.lastIndexOf('"'));
                        list.add("TITLE:" + title);
                        String tag = line.split(" ")[1].equals("Introducing") ? "new"
                                : line.split(" ")[1].equals("Improved") ? "NOTAG"
                                : line.split(" ")[1].equals("Beta") ? "beta"
                                : line.split(" ")[1].equals("new") ? "new" : "NOTAG";
                        list.add("TAG:" + tag);
                        try {
                            String link = line.substring(line.indexOf("http"), line.lastIndexOf("]"));
                            list.add("LINK:" + link);
                        } catch (Exception e) {
                            list.add("LINK:NOLINK");
                        }
                        status = true;
                    } else if (line.contains("image")) {
                        images = line.split(" ")[1].replace("]", "");
                    } else if (status) {
                        content += line;
                    }
                } catch (StringIndexOutOfBoundsException e) {
                    e.printStackTrace();

                    System.exit(1);
                }
            }
            if (content.length() != 0) {
                list.add("CONTENT:" + removeTag(content.replace("[]", "")));
                content = "";
            }
            if (list.size() != 0) {
                execute(list);
                list.clear();
            }
            check();
        }
    }
    //TITLE
    //TAG
    //LINK
    //IMAGE
    //CONTENT

    public static String removeTag(String content) {
        while (content.indexOf('<') != -1) {
            int x = content.indexOf('<');
            int y = content.indexOf('>');
            content = content.replace(content.substring(x, y + 1), "");
        }
        return content;
    }

    public static void execute(List list) {
        Multimap<String, TextParser> monthBucket = ArrayListMultimap.create();
        TextParser textParser = new TextParser();
        for (int i = 0; i < list.size(); i++) {
            String val = list.get(i).toString();
            String first = val.substring(0, val.indexOf(':'));
            String second = val.substring(val.indexOf(':') + 1, val.length());
            switch (first) {
                case "TITLE":
                    textParser.setTitle(second);
                    break;
                case "TAG":
                    textParser.setTag(second);
                    break;
                case "LINK":
                    textParser.setLink(second);
                    break;
                case "CONTENT":
                    textParser.setContent(second);
                    break;
            }
        }
        textParser.setDate(dates);
        textParser.setImage(images);
        monthBucket.put(month, textParser);
        bucket.put(year, monthBucket);

    }

    public static String getMonth(String month) {
        String monthVal = null;

        if (month.equals("Jan") || month.equals("January")) {
            monthVal = "January";
        } else if (month.equals("Feb") || month.equals("February")) {
            monthVal = "February";
        } else if (month.equals("Mar") || month.equals("March")) {
            monthVal = "March";
        } else if (month.equals("Apr") || month.equals("April")) {
            monthVal = "April";
        } else if (month.equals("May")) {
            monthVal = "May";
        } else if (month.equals("Jun") || month.equals("June")) {
            monthVal = "June";
        } else if (month.equals("Jul") || month.equals("July")) {
            monthVal = "July";
        } else if (month.equals("Aug") || month.equals("August")) {
            monthVal = "August";
        } else if (month.equals("Sep") || month.equals("September")) {
            monthVal = "September";
        } else if (month.equals("Oct") || month.equals("October")) {
            monthVal = "October";
        } else if (month.equals("Nov") || month.equals("November")) {
            monthVal = "November";
        } else if (month.equals("Dec") || month.equals("December")) {
            monthVal = "December";
        }

        return monthVal;

    }

    public static String getDate(String day, int month, String year) {
        String dayVal = null;

        if (day.equals("1")) {
            dayVal = "02";
        } else if (day.equals("2")) {
            dayVal = "09";
        } else if (day.equals("3")) {
            dayVal = "16";
        } else if (day.equals("4")) {
            dayVal = "23";
        } else {
            dayVal = "29";
        }

        return dayVal + "-" + month + "-" + year;

    }

    public static void check() throws Exception {

        String year[] = {"2020", "2019", "2018", "2017", "2016", "2015"};
        String month[] = {"December", "November", "October", "September", "August", "July", "June", "May", "April", "March", "February", "January"};
        JSONArray yl = new JSONArray();
        for( String y :year) {
            Collection<Multimap> years = bucket.get(y);
            org.json.JSONObject yearobj = new org.json.JSONObject();
            yearobj.put("year", y);
            JSONArray monthList = new JSONArray();
            for (String monthval : month) {
                JSONArray arr = new JSONArray();
                org.json.JSONObject monthobj = new org.json.JSONObject();
                monthobj.put("month", monthval);
                for (Multimap map : years) {
                    Collection<TextParser> dd = map.get(monthval);
                    for (TextParser list : dd) {
                        org.json.JSONObject obj = new org.json.JSONObject();
                        obj.put("feature", list.getTitle());
                        JSONArray tagList =new JSONArray();
                        if(!list.getTag().equals("NOTAG")){
                            tagList.add(list.getTag());
                            obj.put("tags",tagList);
                        }
                        try {
                            if (!list.getImage().equals("NOIMAGE")) {
                                obj.put("image", list.getImage());
                            }
                        }
                        catch (Exception e){}
                        if(!list.getLink().equals("NOLINK")){
                            obj.put("learn_more",list.getLink());
                        }
                        obj.put("desc", list.getContent());
                        obj.put("release_date",list.getDate());
                        arr.add(obj);
                    }

                }
                if (arr.size() != 0) {
                    monthobj.put("features", arr);
                    monthList.add(monthobj);
                }
            }
            yearobj.put("months", monthList);
            yl.add(yearobj);
        }
        object.put("years",yl);
        System.out.println(object.toString());


//        JSONArray yearLists = new JSONArray();
//        for (String yearVal : year) {
//            Collection<Multimap> yearList = bucket.get(yearVal);
//            JSONObject yearObj = new JSONObject();
//            yearObj.put("year",yearVal);
//            for (Multimap monthobj : yearList) {
//                JSONArray monthList = new JSONArray();
//                for (String monthVal : month) {
//                    JSONObject monthObj = new JSONObject();
//                    monthObj.put("month",monthVal);
//                    Collection<TextParser> dd = monthobj.get(monthVal);
//                    System.out.println(dd.size());
//                    JSONArray featureList =new JSONArray();
//                    for (TextParser ff : dd) {
//                        JSONObject featureObj = new JSONObject();
//                        featureObj.put("feature",ff.getTitle());
//                        JSONArray tagList =new JSONArray();
//                        if(!ff.getTag().equals("NOTAG")){
//                            tagList.add(ff.getTag());
//                            featureObj.put("tag",tagList);
//                        }
//                        try {
//                            if (!ff.getImage().equals("NOIMAGE")) {
//                                featureObj.put("image", ff.getImage());
//                            }
//                        }
//                        catch (Exception e){}
//                        featureObj.put("desc",ff.getContent());
//                        if(!ff.getLink().equals("NOLINK")){
//                            featureObj.put("learn_more",ff.getLink());
//                        }
//                        featureObj.put("release_date",ff.getDate());
//                        featureList.add(featureObj);
//                    }
//                    if(featureList.size()!=0) {
//                        System.out.println(featureList.toJSONString());
//                        System.out.println("=============");
//                        monthObj.put("features", featureList);
//                        monthList.add(monthObj);
//                    }
//                }
//                if(monthList.size()!=0) {
//                    yearObj.put("months", monthList);
//                    yearLists.add(yearObj);
//                }
//            }
//
//
//        }

    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}