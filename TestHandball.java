import com.sportsmanager.handball.HandballSport;
import com.sportsmanager.core.Sport;

public class TestHandball {
    public static void main(String[] args) {
        Sport s = new HandballSport();
        System.out.println("Handball lineup size: " + s.getStartingLineupSize());
    }
}
