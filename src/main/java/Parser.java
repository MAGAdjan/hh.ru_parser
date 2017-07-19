import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.sql.*;

public class Parser {
    private static final String DBURL = "jdbc:mysql://localhost:3306/headhunterdb?useUnicode=true" +
            "&useSSL=true" +
            "&useJDBCCompliantTimezoneShift=true" +
            "&useLegacyDatetimeCode=false" +
            "&serverTimezone=UTC";

    private static final String USERNAME = "root";
    private static final String PASSWORD = "root";

    private static String URL;
    private static String SQL = "CREATE TABLE hh( " +
            "ID INT Primary Key AUTO_INCREMENT, " +
            "vacancy VARCHAR(164), " +
            "salary VARCHAR(64)," +
            "company VARCHAR(164))";

    private static Statement statement;
    private static PreparedStatement preparedStatement;
    private static Connection connection;


    public static void main(String[] args) throws IOException{
        try {
            connection = DriverManager.getConnection(DBURL, USERNAME, PASSWORD);
            statement = connection.createStatement();
            statement.execute(SQL);
            statement.close();
            preparedStatement = connection.prepareStatement("INSERT INTO hh (vacancy, salary, company) VALUES (?, ?, ?)");

            for(int i = 0; i < Integer.MAX_VALUE; i++) {
                URL = "https://ekaterinburg.hh.ru/search/vacancy?text=&area=1261&page=" + i;
                Document doc = Jsoup.connect(URL).get();
                Elements elements = doc.select("div[class=search-result-description__item search-result-description__item_primary]");
                for (Element element : elements) {
                    String vacancy = element.child(0).text();
                    String salary = element.select("div[class=b-vacancy-list-salary]").text();
                    String company = element.select("div[class=search-result-item__company]").text();
                    preparedStatement.setString(1, vacancy);
                    preparedStatement.setString(2, salary);
                    preparedStatement.setString(3, company);
                    preparedStatement.execute();
                }
            }
        }catch(HttpStatusException e){
            if(e.getStatusCode() == 404) System.out.println("Reached end of data. Finish parsing!");
        }catch(IOException e){
            System.out.println("Incorrect URL!");
        }catch(SQLException e){
            e.printStackTrace();
        }finally {
            try{
                statement.close();
                preparedStatement.close();
                connection.close();
            }catch(SQLException e){
                System.out.println("Unable to close!");
            }
        }
    }
}
