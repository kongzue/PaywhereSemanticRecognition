import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class SemanticRecognition {

    //单例
    private static SemanticRecognition semanticRecognition;
    private RecognitionResponsePackage recognitionResponsePackage;

    private SemanticRecognition() {
    }

    //默认请求创建方法
    public static SemanticRecognition getInstance() {
        synchronized (SemanticRecognition.class) {
            if (semanticRecognition == null) {
                semanticRecognition = new SemanticRecognition();
            }
        }
        return semanticRecognition;
    }

    public RecognitionResponsePackage doRecognition(String text) {

        if (text == null || text.isEmpty()) {
            return null;
        }

        Calendar mycalendar = Calendar.getInstance();       //获取当前日期
        int now_year = mycalendar.get(Calendar.YEAR);
        int now_month = mycalendar.get(Calendar.MONTH);

        int numberStartFlag = -1;
        boolean isNumStage = false;                 //是否为数字段
        boolean isChineseNumStage = false;          //是否为中文数字段
        String numStageCache = "";                  //数字缓存区
        String numStageCacheCN = "";                  //中文数字缓存区
        String otherStageCache = "";                //杂料缓存区
        String yearCache = now_year + "", monthCache = now_month + "", dayCache = "";      //日期缓存区
        recognitionResponsePackage = new RecognitionResponsePackage();

        //默认当前日期
        recognitionResponsePackage.setDate(mycalendar.getTime());

        //预处理
        text = text.replace("块钱", "块");
        if (!text.contains("元") && !text.contains("圆") && !text.contains("块")) {
            text += "元";
        }

        for (int i = 0; i < text.length(); i++) {
            char child = text.charAt(i);            //当前文字Char
            String childStr = child + "";           //当前文字String

            //阿拉伯数字
            if (child >= 48 && child <= 57) {
                if (numberStartFlag == -1) {
                    numberStartFlag = i;
                }
                isNumStage = true;
                numStageCache += childStr;
            } else {
                if (child == 46) {
                    //点
                    isNumStage = true;
                    numStageCache += childStr;
                } else {
                    if (isNumStage) {
                        //阿拉伯数字段处理
                        if (childStr.equals("元") || childStr.equals("圆") || childStr.equals("块")) {
                            recognitionResponsePackage.setMoney(numStageCache);
                        } else {
                            if (childStr.equals("年")) {
                                yearCache = numStageCache;
                            } else if (childStr.equals("月")) {
                                monthCache = numStageCache;
                            } else if (childStr.equals("日") || childStr.equals("号")) {
                                dayCache = numStageCache;
                                try {
                                    Date date = new Date(Integer.parseInt(yearCache) - 1900, Integer.parseInt(monthCache) - 1, Integer.parseInt(dayCache));
                                    recognitionResponsePackage.setDate(date);
                                } catch (Exception e) {
                                    otherStageCache += childStr;
                                }
                            } else {
                                otherStageCache += childStr;
                            }
                        }
                    } else {
                        otherStageCache += childStr;
                    }
                    numStageCache = "";
                    isNumStage = false;
                }
            }

            //中文数字段判断
            if (isChineseNUM(childStr)) {
                isChineseNumStage = true;
                numStageCacheCN += childStr;
            } else {
                if (isChineseNumStage) {
                    if (numStageCacheCN.contains("元") || numStageCacheCN.contains("块") || numStageCacheCN.contains("圆")
                            || numStageCacheCN.contains("角") || numStageCacheCN.contains("毛")
                            || numStageCacheCN.contains("分")
                            || numStageCacheCN.contains("百") || numStageCacheCN.contains("千") || numStageCacheCN.contains("万") || numStageCacheCN.contains("亿")
                            || numStageCacheCN.contains("拾") || numStageCacheCN.contains("佰") || numStageCacheCN.contains("仟")
                            ) {
                        //仅当单位和数字都存在时
                        otherStageCache = otherStageCache.replace(numStageCacheCN, "");
                        numStageCacheCN = replaceOldCNToNewCN(numStageCacheCN);
                        try {
                            if (recognitionResponsePackage.getMoney() == null) {
                                recognitionResponsePackage.setMoney(ChineseToNumber(numStageCacheCN) + numCNdecimal);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    isChineseNumStage = false;
                }
            }
        }
        if (isChineseNumStage) {
            if (numStageCacheCN.contains("元") || numStageCacheCN.contains("块") || numStageCacheCN.contains("圆")
                    || numStageCacheCN.contains("角") || numStageCacheCN.contains("毛")
                    || numStageCacheCN.contains("分")
                    || numStageCacheCN.contains("百") || numStageCacheCN.contains("千") || numStageCacheCN.contains("万") || numStageCacheCN.contains("亿")
                    || numStageCacheCN.contains("拾") || numStageCacheCN.contains("佰") || numStageCacheCN.contains("仟")
                    ) {
                //仅当单位和数字都存在时
                otherStageCache = otherStageCache.replace(numStageCacheCN, "");
                numStageCacheCN = replaceOldCNToNewCN(numStageCacheCN);
                try {
                    if (recognitionResponsePackage.getMoney() == null) {
                        recognitionResponsePackage.setMoney(ChineseToNumber(numStageCacheCN) + numCNdecimal);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            isChineseNumStage = false;
        }

        //模糊日期判断
        if (otherStageCache.contains("昨天") || otherStageCache.contains("昨日") || otherStageCache.contains("一天前")) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -1);
            Date date = cal.getTime();
            recognitionResponsePackage.setDate(date);
            otherStageCache = otherStageCache.replace("昨天", "");
            otherStageCache = otherStageCache.replace("昨日", "");
            otherStageCache = otherStageCache.replace("一天前", "");
        } else if (otherStageCache.contains("大前天") || otherStageCache.contains("大前日") || otherStageCache.contains("三天前")) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -3);
            Date date = cal.getTime();
            recognitionResponsePackage.setDate(date);
            otherStageCache = otherStageCache.replace("大前天", "");
            otherStageCache = otherStageCache.replace("大前日", "");
            otherStageCache = otherStageCache.replace("三天前", "");
        } else if (otherStageCache.contains("前天") || otherStageCache.contains("前日") || otherStageCache.contains("二天前") || otherStageCache.contains("两天前")) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -2);
            Date date = cal.getTime();
            recognitionResponsePackage.setDate(date);
            otherStageCache = otherStageCache.replace("前天", "");
            otherStageCache = otherStageCache.replace("前日", "");
            otherStageCache = otherStageCache.replace("二天前", "");
            otherStageCache = otherStageCache.replace("两天前", "");
        }

        //剔除掉无意义语段
        otherStageCache = otherStageCache.replace("花了", "");
        otherStageCache = otherStageCache.replace("消费", "");
        otherStageCache = otherStageCache.replace("付款", "");
        otherStageCache = otherStageCache.replace("用了", "");
        otherStageCache = otherStageCache.replace("刚买了", "");
        otherStageCache = otherStageCache.replace("买了", "");

        recognitionResponsePackage.setOtherText(otherStageCache);

        return recognitionResponsePackage;
    }

    private String numCNdecimal = "";        //中文小数部分

    private String replaceOldCNToNewCN(String numStageCacheCN) {
        numStageCacheCN = numStageCacheCN.replace("壹", "一");
        numStageCacheCN = numStageCacheCN.replace("贰", "二");
        numStageCacheCN = numStageCacheCN.replace("叁", "三");
        numStageCacheCN = numStageCacheCN.replace("肆", "四");
        numStageCacheCN = numStageCacheCN.replace("伍", "五");
        numStageCacheCN = numStageCacheCN.replace("陆", "六");
        numStageCacheCN = numStageCacheCN.replace("柒", "七");
        numStageCacheCN = numStageCacheCN.replace("捌", "八");
        numStageCacheCN = numStageCacheCN.replace("玖", "九");

        numStageCacheCN = numStageCacheCN.replace("拾", "十");
        numStageCacheCN = numStageCacheCN.replace("佰", "百");
        numStageCacheCN = numStageCacheCN.replace("仟", "千");

        //识别是否有小数
        numStageCacheCN = numStageCacheCN.replace("块", ".");
        numStageCacheCN = numStageCacheCN.replace("圆", ".");
        numStageCacheCN = numStageCacheCN.replace("元", ".");
        numStageCacheCN = numStageCacheCN.replace("点", ".");

        if (numStageCacheCN.endsWith(".")) {
            numStageCacheCN = numStageCacheCN.replace(".", "");
        }

        if (numStageCacheCN.contains(".")) {
            //有小数
            int pointIndex = numStageCacheCN.indexOf(".");
            numCNdecimal = numStageCacheCN.substring(pointIndex, numStageCacheCN.length());

            numCNdecimal = numCNdecimal.replace("毛", "");
            numCNdecimal = numCNdecimal.replace("角", "");
            numCNdecimal = numCNdecimal.replace("分", "");

            numCNdecimal = numCNdecimal.replace("一", "1");
            numCNdecimal = numCNdecimal.replace("二", "2");
            numCNdecimal = numCNdecimal.replace("三", "3");
            numCNdecimal = numCNdecimal.replace("四", "4");
            numCNdecimal = numCNdecimal.replace("五", "5");
            numCNdecimal = numCNdecimal.replace("六", "6");
            numCNdecimal = numCNdecimal.replace("七", "7");
            numCNdecimal = numCNdecimal.replace("八", "8");
            numCNdecimal = numCNdecimal.replace("九", "9");
            numCNdecimal = numCNdecimal.replace("零", "0");

            numStageCacheCN = numStageCacheCN.substring(0, pointIndex);
        } else {
            numCNdecimal = "";
        }
        return numStageCacheCN;
    }

    private boolean isChineseNUM(String s) {
        if (s.equals("一") || s.equals("二") || s.equals("三") || s.equals("四") || s.equals("五") || s.equals("六") || s.equals("七") || s.equals("八") || s.equals("九") || s.equals("十")
                || s.equals("块") || s.equals("元") || s.equals("圆") || s.equals("角") || s.equals("毛") || s.equals("分")
                || s.equals("百") || s.equals("千") || s.equals("万") || s.equals("亿")
                || s.equals("壹") || s.equals("贰") || s.equals("叁") || s.equals("肆") || s.equals("伍") || s.equals("陆") || s.equals("柒") || s.equals("捌") || s.equals("玖")
                || s.equals("拾") || s.equals("佰") || s.equals("仟")
                ) {
            return true;
        }
        return false;
    }

    private Map<Character, Integer> digit = new HashMap<>();
    private Map<Character, Integer> position = new HashMap<>();

    public String ChineseToNumber(String str) {

        digit.put('零', 0);
        digit.put('一', 1);
        digit.put('二', 2);
        digit.put('三', 3);
        digit.put('四', 4);
        digit.put('五', 5);
        digit.put('六', 6);
        digit.put('七', 7);
        digit.put('八', 8);
        digit.put('九', 9);
        position.put('十', 1);
        position.put('百', 2);
        position.put('千', 3);

        char[] nums = new char[8];
        int p = 7, now = 7;
        char[] c = str.toCharArray();
        /**
         * 从右到左逐个字符解析
         * 通过p控制万位,pp控制万以下的单位,now记录当前单位
         */
        for (int i = c.length - 1; i >= 0; i--) {
            if (c[i] == '万') {
                now = p = 3;
            } else {
                int d = digit.getOrDefault(c[i], -1);
                if (d == 0) {
                    continue;
                }
                if (d == -1) {
                    int pp = position.getOrDefault(c[i], -1);
                    if (pp == -1)
                        throw new RuntimeException("\"" + str + "\"中存在无法解析的字符: " + i + ":" + c[i]);
                    else {
                        now = p - pp;
                        if (now == 6)
                            nums[now] = 1;
                    }
                } else {
                    nums[now] = (char) d;
                }
            }
        }
        for (int i = 0; i < nums.length; i++) {
            nums[i] = (char) (nums[i] + '0');
        }
        return Integer.parseInt(new String(nums)) + "";
    }

    public class RecognitionResponsePackage {
        private Date date;
        private String money;
        private String otherText;

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }

        public String getMoney() {
            return money;
        }

        public void setMoney(String money) {
            this.money = money;
        }

        public String getOtherText() {
            return otherText;
        }

        public void setOtherText(String otherText) {
            this.otherText = otherText;
        }

        @Override
        public String toString() {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            return "RecognitionResponsePackage{" +
                    "date=" + df.format(date) +
                    ", money='" + money + '\'' +
                    ", otherText='" + otherText + '\'' +
                    '}';
        }
    }
}
