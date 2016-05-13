/**
 * Created by danedexheimer on 5/13/16.
 */
public class Game {

    // public property for an integer id
    int id;

    // public property for name
    String name;

    // public property for genre
    String genre;

    // public property for platform
    String platform;

    // public property for releaseYear
    int releaseYear;




    // create a constructor for all 5 properties.


    public Game(int id, String name, String genre, String platform, int releaseYear) {
        this.id = id;
        this.name = name;
        this.genre = genre;
        this.platform = platform;
        this.releaseYear = releaseYear;
    }
}


