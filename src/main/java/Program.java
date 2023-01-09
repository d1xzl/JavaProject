import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class Program {

    public static void main(String[] args) throws IOException, CsvException, ClassNotFoundException, SQLException {

        var list = new ArrayList<Earthquake>();

        try (CSVReader reader = new CSVReader(new FileReader("Землетрясения.csv"))) {
            reader.readNext();
            var a = reader.readAll();
            a.forEach(x -> list.add(new Earthquake(x)));
        }


        var statmt = database(list);

        firstTask(statmt);
        secondTask(statmt);
        thirdTask(statmt);

        statmt.close();
    }

    private static Statement database(ArrayList<Earthquake> list) throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");

        var cnt = DriverManager.getConnection("jdbc:sqlite:earthquakes.db");
        var stm = cnt.createStatement();

        stm.execute("DROP TABLE 'earthquakes';");
        stm.execute(
                "CREATE TABLE IF NOT EXISTS 'earthquakes' (" +
                        "'id' VARCHAR PRIMARY KEY, " +
                        "'depth' INTEGER, " +
                        "'magnitudeType' VARCHAR," +
                        "'magnitude' REAL, " +
                        "'state' TEXT, " +
                        "'time' TIME);");


        var statement = cnt.prepareStatement(
                "INSERT INTO 'earthquakes' ('id','depth','magnitudeType','magnitude','state','time') VALUES (?,?,?,?,?,?);");


        list.forEach(x -> {
            try {
                statement.setString(1, x.ID);
                statement.setInt(2, x.depth);
                statement.setString(3, x.magnitudeType);
                statement.setDouble(4, x.magnitude);
                statement.setString(5, x.state);
                statement.setString(6, x.time);
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
        return stm;
    }

    private static void firstTask(Statement stm) throws SQLException {
        System.out.println("Среднее количество землетрясений в каждом году");
        var a = stm.executeQuery("SELECT COUNT(*) AS count,strftime('%Y',time) AS year FROM earthquakes WHERE (year IS NOT NULL)  GROUP BY year;");

        while (a.next()) {
            System.out.println(a.getString("year")+" "+a.getString("count"));
        }
    }

    private static void secondTask(Statement stm) throws SQLException {
        System.out.println("Средняя магнитуда для штата \"West Virginia\"");
        System.out.println(stm.executeQuery("SELECT AVG(magnitude) AS avg FROM earthquakes WHERE state='West Virginia'").getDouble("avg"));
    }

    private static void thirdTask(Statement stm) throws SQLException {
        System.out.println("Название штата, в котором произошло самое глубокое землетрясение в 2013 году");
        System.out.println(stm.executeQuery("SELECT state,MAX(depth),strftime('%Y',time) AS year FROM earthquakes WHERE year ='2013';").getString("state"));
    }
}
