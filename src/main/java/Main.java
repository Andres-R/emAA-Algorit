
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONObject;

import java.io.*;
import java.util.Calendar;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;


public class Main {

    public static final int INTERVAL_COUNT = 13;
    static int rowid = 0;

    public static void main(String[] args) throws IOException {
        emAAAlgorit();

    }

    public static void coachStuff() {
        String[] tickers = {"GOOG", "MSFT", "EVRI", "OSMT", "ZYXI",};

        for (String ticker : tickers) {
            CloseableHttpClient httpclient = HttpClients.createDefault();
            String URL = "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol=" + ticker + "&apikey=L00JOCOTWMW4FTS3";
            HttpGet getURL = new HttpGet(URL);

            try {
                CloseableHttpResponse response1 = httpclient.execute(getURL);
                HttpEntity entity = response1.getEntity();
                JSONObject json = new JSONObject(EntityUtils.toString(entity));

                // prints entire json string
                // String test = json.toString();
                // System.out.println(test);

                String date = "2020-07-15";
                String target = "4. close";
                String output = json.getJSONObject("Time Series (Daily)").getJSONObject(date).get(target).toString();

                System.out.println("Ticker: " + ticker + ": " + output);

                response1.close();
            } catch (Exception e) {
                System.out.println("RiiiiiLp");
                e.printStackTrace();
            }
        }
    }

    public static void emAAAlgorit() throws IOException {

        String ticker = "MSFT";
        String[] dates = {
                "2020-06-30", "2020-07-01", "2020-07-02",
                "2020-07-06", "2020-07-08", "2020-07-09", "2020-07-10",
                "2020-07-13", "2020-07-14", "2020-07-15", "2020-07-16", "2020-07-17",
                "2020-07-20", "2020-07-21", "2020-07-22", "2020-07-23", "2020-07-24",
                "2020-07-27",
        };
        String[] hours = {
                "09:30:00", "10:00:00", "10:30:00", "11:00:00", "11:30:00",
                "12:00:00", "12:30:00", "13:00:00", "13:30:00", "14:00:00",
                "14:30:00", "15:00:00", "15:30:00", "16:00:00"
        };

        String[][] prices = new String[dates.length][INTERVAL_COUNT + 1];

        CloseableHttpClient httpclient = HttpClients.createDefault();
        String URL = "https://www.alphavantage.co/query?function=TIME_SERIES_INTRADAY&symbol=" + ticker + "&interval=30min&outputsize=full&apikey=L00JOCOTWMW4FTS3";
        HttpGet getURL = new HttpGet(URL);
        try {
            CloseableHttpResponse response1 = httpclient.execute(getURL);
            HttpEntity entity = response1.getEntity();
            JSONObject json = new JSONObject(EntityUtils.toString(entity));


            // get closing price of stock at 30m timestamp
            for (int i = 0; i < dates.length; i++) {
                for (int j = 0; j < hours.length; j++) {
                    String closePriceOfInterval = json.getJSONObject("Time Series (30min)").getJSONObject(dates[i] + " " + hours[j]).get("4. close").toString();
                    prices[i][j] = closePriceOfInterval;
                }
            }
            response1.close();
        } catch (Exception e) {
            System.out.println("RiiiiiLp");
            e.printStackTrace();
        }

//        for (int i = 0; i < dates.length; i++) {
//            for (int j = 0; j < hours.length; j++) {
//                System.out.print(prices[i][j] + " ");
//            }
//            System.out.println();
//        }

        // determine performance of intervals
        char[][] performance = generatePerformance(dates.length, INTERVAL_COUNT, prices);

        // determine probability of performance intervals
        double[] probabilities_of_ups = generateProbabilities(performance, dates.length);

        // print results
        printEmAAAlgoritResults(ticker, prices, performance, probabilities_of_ups, dates);


    }

    public static void printEmAAAlgoritResults(String ticker, String[][] prices, char[][] performance, double[] probabilitiesOfUps, String[] dates) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Stocks");

        int rowNumber = 1;
        int day = 1;
        Object[] x = new Object[15];
        Map<String, Object[]> information = new TreeMap<String, Object[]>();

        information.put(Integer.toString(rowNumber), new Object[]{ticker,
                "09:30", "10:00", "10:30", "11:00", "11:30",
                "12:00", "12:30", "01:00", "01:30", "02:00",
                "02:30", "03:00", "03:30", "04:00"});

        printThatShit(information, workbook, sheet, rowNumber);
        rowNumber++;

        for (int i = 0; i < prices.length; i++) {
            x[0] = "Day " + day;
            day++;
            for (int j = 0; j < prices[i].length; j++) {
                x[j + 1] = prices[i][j];
            }
            information.put(Integer.toString(rowNumber), x);
            printThatShit(information, workbook, sheet, rowNumber);
            rowNumber++;
        }
    }

    public static void printThatShit(Map<String, Object[]> empinfo, XSSFWorkbook workbook, XSSFSheet sheet, int rowNumber) throws IOException {
        XSSFRow row;
        row = sheet.createRow(rowid++);
        Object[] objectArr = empinfo.get(Integer.toString(rowNumber));
        int cellid = 0;
        for (Object obj : objectArr) {
            Cell cell = row.createCell(cellid++);
            cell.setCellValue(obj.toString());
        }


        FileOutputStream out = new FileOutputStream(new File("Book1.xlsx"));
        workbook.write(out);
        out.close();
    }

    public static double[] generateProbabilities(char[][] performance, int days) {

        double[] probabilitiesOfUps = new double[INTERVAL_COUNT];
        char target = 'U';

        for (int k = 0; k < INTERVAL_COUNT; k++) {
            char[] pattern = new char[days];
            // populate pattern array from performance
            for (int i = 0; i < days; i++) {
                for (int j = 0; j < INTERVAL_COUNT; j++) {
                    if (k == j) {
                        pattern[i] = performance[i][j];
                    }
                }
            }
            // find number of 'U's in pattern
            // REMEMBER, pattern represents the performance of a specific interval over x amount of days
            int targetCount = 0;
            for (int i = 0; i < pattern.length; i++) {
                if (pattern[i] == target) {
                    targetCount++;
                }
            }
            probabilitiesOfUps[k] = (float) targetCount / (float) days;
        }
        return probabilitiesOfUps;
    }

    public static char[][] generatePerformance(int rows, int columns, String[][] prices) {

        // prices has a column length of 14 since there are 14 timestamps
        // performance has a column length of 13 since there are 13 intervals

        char[][] performance = new char[rows][columns];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                double difference = Double.parseDouble(prices[i][j + 1]) - Double.parseDouble(prices[i][j]);
                if (difference > 0) {
                    performance[i][j] = 'U';
                } else if (difference < 0) {
                    performance[i][j] = 'D';
                } else {
                    performance[i][j] = 'S';
                }
            }
        }
        return performance;
    }

    public static String getNextTradingDay(Calendar cal) {

        // day is a friday
        if (cal.get(Calendar.DAY_OF_WEEK) == 1) {
            cal.add(Calendar.DATE, 3);
        }
        // day is a saturday
        else if (cal.get(Calendar.DAY_OF_WEEK) == 2) {
            cal.add(Calendar.DATE, 2);
        }
        // day is a sunday
        else if (cal.get(Calendar.DAY_OF_WEEK) == 3) {
            cal.add(Calendar.DATE, 1);
        } else {
            cal.add(Calendar.DATE, 1);
        }
        return formatDate(cal);
    }

    public static String formatDate(Calendar cal) {
        String dateFormated = cal.get(Calendar.YEAR) + "-" +
                ((cal.get(Calendar.MONTH) + "").length() > 1 ? cal.get(Calendar.MONTH) : "0" + cal.get(Calendar.MONTH))
                + "-" + cal.get(Calendar.DAY_OF_MONTH);
        return dateFormated;
    }
}