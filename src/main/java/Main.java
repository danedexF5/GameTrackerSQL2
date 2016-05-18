import org.h2.tools.Server;
import spark.ModelAndView;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

import static spark.Spark.halt;


public class Main {

    public static void main(String[] args) throws SQLException {
        Server server = Server.createTcpServer("-baseDir", "./data").start();

        // Start a new instance of the H2 database.
        String jdbcUrl = "jdbc:h2: " + server.getURL() + "/main";

        // Create a connection object using DriverManager
        Connection connection = DriverManager.getConnection("jdbc:h2:./data/game");

        // initialize the database using the initializeDatabase() method. This will create the required table.
        initializeDatabase(connection);

        // fill in the spark route definition
        Spark.get(
                // set the route to "/"
                "/",
                // add the lambda
                (request, response) -> {
                    // create a hashmap for the model
                    HashMap m = new HashMap();

                    // use your selectGames method to select all of the games from the database
                    ArrayList<Game> listOfGames = selectGames(connection);

                    // put the games arraylist into the model
                    m.put("games", listOfGames);

                    // show the home page template
                    return new ModelAndView(m, "home.mustache");
                },
                // add the mustache template engine
                new MustacheTemplateEngine()


        );


        // create a "get" spark route for the create-game endpoint
        Spark.get(
                // set the endpoint
                "/create-game",
                // add your lambda
                (request, response) -> {
                    // create a hashmap for your model
                    HashMap m2 = new HashMap();
                    // add a value named "action" to the model that sets the target that the game form will post to.
                    // for example, if you wanted the action to be "/create-game", then you'd set the "action" property
                    // to "/create-game"
                    m2.put("action", "/create-game");

                    // show game form
                    return new ModelAndView(m2, "gameForm.mustache");

                },
                // add the mustache template engine
                new MustacheTemplateEngine()
        );


        // create a post route for create-game
        Spark.post(
                // set the endpoint path
                "/create-game",
                // add your lambda
                (request, response) -> {

                    // use a try/catch block when creating the game. This is used to catch validation
                    // errors on the game year.

                    // create a new instance of game using the data posted from the game form.
                    // set the ID to 0 since this is a new game.
                    // You will need to parse the gameYear parameter into an integer too. This is what might throw an error
                    try {
                        Game game = new Game(

                                0,
                                request.queryParams("gameName"),
                                request.queryParams("gameGenre"),
                                request.queryParams("gamePlatform"),
                                Integer.parseInt(request.queryParams("releaseYear"))
                        );


                        // use the insertGame method to insert the game into the database
                        insertGame(connection, game);

                        // redirect to the webroot
                        response.redirect("/");
                        // halt this request
                        halt();

                        // here you will need to catch any exceptions thrown if the game year isn't actually a number
                    } catch (NumberFormatException nfe) {
                        // create a hashmap for your model
                        HashMap m = new HashMap();
                        // add an error message, set the key in the model to "error"
                        m.put("error", "there was a prob with your release yr.");

                        // Add the five fields posted (for id, name, genre, platform, and year) into the model
                        // this should be five lines of code.
                        m.put("gameId", request.queryParams("gameId"));
                        m.put("gameName", request.queryParams("gameName"));
                        m.put("gameGenre", request.queryParams("gameGenre"));
                        m.put("gamePlatform", request.queryParams("gamePlatform"));
                        m.put("releaseYear", request.queryParams("releaseYear"));
                        // set the action property in the model to the create-game path
                        m.put("action", "/create-game");
                        // show the gameForm template.
                        return new ModelAndView(m, "gameForm.mustache");


                    }
                    return null;
                },
                // add the mustache template engine
                new MustacheTemplateEngine()

        );


        // create a get route for the edit-game page
        Spark.get(
                // set the endpoint route
                "/edit-game",
                // add your lambda
                (request, response) -> {
                    // create a hashmap for your model
                    HashMap m = new HashMap();

                    // You should receive a query parameter named id. This will be an integer in string form.
                    // parse the value as an integer and put this into a variable
                    int editGame = Integer.parseInt(request.queryParams("id"));

                    // use the readGame method to read your game using the id you just parsed

                    // put the game's data into five fields in the model for id, name, genre, platform and year.
                    // you can read these values directly from the game.
                    // you should write five lines of code below.
                    Game newReadGame = readGame(connection, editGame);
                    m.put("gameId", newReadGame.id);
                    m.put("gameName", newReadGame.name);
                    m.put("gameGenre", newReadGame.genre);
                    m.put("gamePlatform", newReadGame.platform);
                    m.put("releaseYear", newReadGame.releaseYear);

                    // set a property in the model named "action" and set it to the endpoint for edit-game
                    m.put("action", "/edit-game");

                    // show game form
                    return new ModelAndView(m, "gameForm.mustache");
                },
                // add the mustache tempalte engine
                new MustacheTemplateEngine()

        );

        // create a spark post endpoint for edit-game
        Spark.post(
                // add the endpoint for edit-game
                "/edit-game",
                // add your lambda
                (request, response) -> {
                    // try create the game and add to the user
                    // you'll need to handle any exceptions related to the game release year
                    try {
                        Game game = new Game(

                                Integer.parseInt(request.queryParams("gameId")),
                                request.queryParams("gameName"),
                                request.queryParams("gameGenre"),
                                request.queryParams("gamePlatform"),
                                Integer.parseInt(request.queryParams("releaseYear"))
                        );
                        // create a new instance of Game and set its properties to the submitted data.
                        // take note that when editing a game the id of the game will be submitted and you'll
                        // need to put that into your instance. Also, use the constructor to provide these five values


                        // use the updateGame method to update the game record in the database
                        updateGame(connection, game);

                        // redirect to the homepage
                        response.redirect("/");
                        // halt this request
                        halt();

                        // now, be sure to catch any error related to parsing the game year.
                    } catch (NumberFormatException nfe) {

                        // create a hashmap for the model
                        HashMap m = new HashMap();
                        // add a key for error and set an error message
                        m.put("error", "there was a prob with your release yr.");

                        // add all five submitted values (id, name, genre, platform, and year) into the model
                        // there should be five lines of code below.
                        m.put("gameId", request.queryParams("gameId"));
                        m.put("gameName", request.queryParams("gameName"));
                        m.put("gameGenre", request.queryParams("gameGenre"));
                        m.put("gamePlatform", request.queryParams("gamePlatform"));
                        m.put("releaseYear", request.queryParams("releaseYear"));

                        // set the post action to the endpoint for edit-game in the model
                        m.put("action", "/edit-game");
                        // show game form
                        return new ModelAndView(m, "gameForm.mustache");

                    }
                    return null;
                },
                // add the mustache template engine
                new MustacheTemplateEngine()

        );

        // add a get endpoint for delete-game
        Spark.get(
                // set the endpoint
                "/delete-game",
                // add your lambda
                (request, response) -> {

                    // call the deleteGame method. You'll need to parse the id in the query params
                    // I'm not handling any number parsing errors here because in the perfect would
                    // we're controlling this value when generating the form. However, in the real
                    // world we would need to.
                    deleteGame(connection, Integer.valueOf(request.queryParams("id")));

                    // redirect to the homepage
                    response.redirect("/");

                    // halt the request
                    halt();

                    // return null
                    return null;
                }

        );
    }


    private static void deleteGame(Connection connection, int id) throws SQLException {
        // create a prepared statement to delete the game that has the provided id.
        PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM game WHERE id = ?");
        // set the parameter for the id to the provided id.
        preparedStatement.setInt(1, id);
        // execute the statement
        preparedStatement.execute();
    }

    private static void updateGame(Connection connection, Game game) throws SQLException {
        // create a prepared statement to update the game record in the DB for the provided Game instance
        PreparedStatement preparedStatement = connection.prepareStatement("UPDATE game SET name = ?, genre = ?, platform ?, releaseYear ?, WHERE id = ?");

        // set the give properties in the update statement (name, genre, platform, year, and id)
        // there should be give lines of code
        preparedStatement.setString(1, game.name);
        preparedStatement.setString(2, game.genre);
        preparedStatement.setString(3, game.platform);
        preparedStatement.setInt(4, game.releaseYear);
        preparedStatement.setInt(5, game.id);

        // execute the statement.
        preparedStatement.execute();
    }

    private static Game readGame(Connection connection, int id) throws SQLException {
        // create a prepared statement to select the game matching the provided ID
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM game WHERE id = ?");
        // set the parameter for the game id
        preparedStatement.setInt(1, id);
        // execute the query and set this into a ResultSet variable
        ResultSet resultSet = preparedStatement.executeQuery();
        // read the first line of the result set using the next() method on ResultSet
        resultSet.next();

        // create a new game. Pass the give fields you read from the database into the constructor
        Game game = new Game(
                resultSet.getInt("id"),
                resultSet.getString("name"),
                resultSet.getString("genre"),
                resultSet.getString("platForm"),
                resultSet.getInt("releaseYear"));

        // return the game
        return game;
    }

    private static ArrayList<Game> selectGames(Connection connection) throws SQLException {
        // create an arraylist to hold all the games in our database
        ArrayList<Game> listOfGames = new ArrayList<>();
        // create a new statement
        Statement statement = connection.createStatement();
        // use the statement to execute a query to select all rows from the game table
        ResultSet resultSet = statement.executeQuery("SELECT * FROM game");

        // iterate over the result set while we have records to read.
        while (resultSet.next()) {
            // create a new instance of game using the data in the query.
            Game game = new Game(
                    resultSet.getInt("id"),
                    resultSet.getString("name"),
                    resultSet.getString("genre"),
                    resultSet.getString("platForm"),
                    resultSet.getInt("releaseYear"));

            // add the game to the games arraylist
            listOfGames.add(game);

        }
        // return the arraylist of games
        return listOfGames;
    }

    private static void insertGame(Connection connection, Game game) throws SQLException {
        // create a prepared statement to insert a new game into the game table
        PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO game VALUES (NULL, ?, ?, ?, ?)");
        // set the four fields (not ID!) for the prepared statement
        // you should have four lines of code here
        preparedStatement.setString(1, game.name);
        preparedStatement.setString(2, game.genre);
        preparedStatement.setString(3, game.platform);
        preparedStatement.setInt(4, game.releaseYear);


        // execute the statement
        preparedStatement.execute();
    }

    private static void initializeDatabase(Connection connection) throws SQLException {
        // Create a new SQL statement
        Statement statement = connection.createStatement();

        // execute a statement to create the game table if it doesn't exist already.
        // the id field should be an IDENTITY
        statement.execute("CREATE TABLE IF NOT EXISTS game (id IDENTITY, name VARCHAR, genre VARCHAR, platform VARCHAR, releaseYear INT)");

    }

}
