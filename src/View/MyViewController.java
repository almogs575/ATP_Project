package View;

import Model.MyModel;
import ViewModel.MyViewModel;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.*;
import javafx.util.Duration;

import javax.swing.text.html.ImageView;
import java.io.File;
import java.nio.file.Paths;
import java.util.Observable;
import java.util.Observer;
import java.util.Optional;

import static javax.swing.text.StyleConstants.Bold;
/**
 * MyViewController class extends Observable implements IView
 * In charge of View part in the App
 * Connecting to the view fxml file
 */
public class MyViewController implements Observer, IView {
//Time:
    private static final int startTime = 120;
    private static final String startLives = "* * *";
    private Timeline timeline= new Timeline(60);
//fxml:
    @FXML
    private static MyViewModel viewModel = new MyViewModel(new MyModel());
    private final StringProperty lives = new SimpleStringProperty(startLives);
    private final IntegerProperty timeSeconds = new SimpleIntegerProperty(startTime);
    public MazeDisplay mazeDisplay = new MazeDisplay();
    public StringProperty characterPositionRow = new SimpleStringProperty();
    public StringProperty characterPositionColumn = new SimpleStringProperty();
    //TextField:
    public javafx.scene.control.TextField text_row;
    public javafx.scene.control.TextField text_col;
    //Labels:
    public javafx.scene.control.Label label_rowsNum;
    public javafx.scene.control.Label label_columnsNum;
    public javafx.scene.control.Label label_timeLeft;
    public javafx.scene.control.Label label_livesLeft;
    //Buttons:
    public Button button;
    public javafx.scene.control.Button button_GenerateMaze;
    public javafx.scene.control.Button button_SolveMaze;
    public javafx.scene.control.Button button_StopMusic;

    public AnchorPane MazePane;
    public ChoiceBox cbBCharacter;

    boolean showOnce = false;
    boolean songOnce = true;
    private Timeline time;
    private MediaPlayer mediaPlayer;


    public void setViewModel(MyViewModel viewModel) {
        this.viewModel = viewModel;
        bindProperties(viewModel);
        button_GenerateMaze.setDisable(true);
    }

    private void bindProperties(MyViewModel viewModel) {
        label_timeLeft.textProperty().bind(timeSeconds.asString());
        label_livesLeft.textProperty().bind(lives);
        label_rowsNum.textProperty().bind(viewModel.characterPositionRow);
        label_columnsNum.textProperty().bind(viewModel.characterPositionColumn);
    }

    @Override
    public void update(Observable o, Object arg) {
        if (o == viewModel) {
            mazeDisplay.setMaze(viewModel.getMaze());
            mazeDisplay.setCharacterPosition(viewModel.getCharacterPositionRow(), viewModel.getCharacterPositionColumn());
            mazeDisplay.setGoalPosition(viewModel.getEndPosition());
            displayMaze(viewModel.getMaze());
            if (viewModel.gameFinish() && !showOnce) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setContentText("Well Done,\n" +
                        "You are the king of the seven kingdoms");
                Music(1);
                time.stop();
                alert.show();
                button_GenerateMaze.setDisable(false);
                showOnce = true;
            }
            mazeDisplay.redraw();
        }
    }
    @Override
    public void displayMaze(int[][] maze) {
        int characterPositionRow = viewModel.getCharacterPositionRow();
        int characterPositionColumn = viewModel.getCharacterPositionColumn();
        mazeDisplay.setCharacterPosition(characterPositionRow, characterPositionColumn);
        mazeDisplay.endPosition(viewModel.getEndPosition());
        mazeDisplay.Solved(viewModel.getMazeSolutionArr());
        mazeDisplay.isSolved(viewModel.isSolved());
        this.characterPositionRow.set(characterPositionRow + "");
        this.characterPositionColumn.set(characterPositionColumn + "");
        if (viewModel.isSolved())
            mazeDisplay.redraw();
    }

    public void generateMaze() {
        if (songOnce == true)
            Music(0);
        button_StopMusic.setVisible(true);
        button_GenerateMaze.setDisable(true);
        lives.setValue("* * *");
        if (time != null)
            time.stop();
        Timer();
        showOnce = false;
        int row, col;
        try {
            row = Integer.valueOf(text_row.getText());
            if (row <= 0) {
                row = 10;
                text_row.setText("10");
            }
        } catch (Exception e) {
            row = 10;
            text_row.setText("10");
        }
        try {
            col = Integer.valueOf(text_col.getText());
            if (col <= 0) {
                col = 10;
                text_col.setText("10");
            }
        } catch (Exception e) {
            col = 10;
            text_col.setText("10");
        }
        int[][] temp = viewModel.generateMaze(row, col);
        mazeDisplay.setMaze(temp);
        mazeDisplay.endPosition(viewModel.getEndPosition());
        button_SolveMaze.setVisible(true);
        displayMaze(temp);
    }

    public void KeyPressed(KeyEvent keyEvent) {
        viewModel.moveCharacter(keyEvent.getCode());
        keyEvent.consume();
    }

    public void mouseClicked() {
        this.mazeDisplay.requestFocus();
    }

    public void setResizeEvent(Scene scene) {
        scene.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number newSceneWidth) {
                mazeDisplay.redraw();
            }
        });
        scene.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneHeight, Number newSceneHeight) {
                mazeDisplay.redraw();
            }
        });
    }

    public void solveMaze() {
        viewModel.getSolution(this.viewModel, this.viewModel.getCharacterPositionRow(), this.viewModel.getCharacterPositionColumn(), "solve");
    }

    public void exit() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setContentText("Are you sure you want to leave the game?\n"
                + "Don't miss the chance to be the\n"
                + "King of the seven kingdom!");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            Platform.exit();
        }
    }
//Add about stage
    public void About() {
        try {
            Stage aboutStage = new Stage();
            aboutStage.setTitle("About");
            FXMLLoader fxmlLoader = new FXMLLoader();
            Parent root = fxmlLoader.load(getClass().getResource("About.fxml").openStream());
            Scene scene = new Scene(root, 300, 165);
            scene.getStylesheets().add("box.css");
            aboutStage.setScene(scene);
            aboutStage.initModality(Modality.APPLICATION_MODAL); //Lock the window until it closes
            aboutStage.show();
        } catch (Exception e) {
            System.out.println("Error About.fxml not found");
        }
    }
//Add help stage
    public void Help() {
        try {
            Stage helpStage = new Stage();
            helpStage.setTitle("Help");
            FXMLLoader fxmlLoader = new FXMLLoader();
            Parent root = fxmlLoader.load(getClass().getResource("Help.fxml").openStream());
            Scene scene = new Scene(root);
            scene.getStylesheets().add("box.css");
            helpStage.setScene(scene);
            helpStage.initModality(Modality.APPLICATION_MODAL); //Lock the window until it closes
            helpStage.show();
        } catch (Exception e) {
            System.out.println("Error miss file: Help.fxml");
        }
    }
//add option stage
    public void Option() {
        try {
            Stage optionStage = new Stage();
            optionStage.setTitle("Option");
            FXMLLoader fxmlLoader = new FXMLLoader();
            Parent root = fxmlLoader.load(getClass().getResource("Option.fxml").openStream());
            Scene scene = new Scene(root);
            scene.getStylesheets().add("box.css");
            optionStage.setScene(scene);
            optionStage.initModality(Modality.APPLICATION_MODAL); //Lock the window until it closes
            optionStage.show();
        } catch (Exception e) {
            System.out.println("Error miss file: Option.fxml");
        }
    }

    public void saveGame() {
        FileChooser fileChooser = new FileChooser();
        File filePath = new File("./GameOfThrones_Mazes/");
        if (!filePath.exists())
            filePath.mkdir();
        fileChooser.setTitle("Saving maze");
        fileChooser.setInitialFileName("GameOfThrones_MazeNumber");
        fileChooser.setInitialDirectory(filePath);
        File file = fileChooser.showSaveDialog(mazeDisplay.getScene().getWindow());
        if (file != null)
            viewModel.save(file);
    }

    public void loadGame() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Loading maze");
        File filePath = new File("./GameOfThrones_Mazes/");
        if (!filePath.exists())
            filePath.mkdir();
        fileChooser.setInitialDirectory(filePath);
        File file = fileChooser.showOpenDialog(new PopupWindow() {
        });
        if (file != null && file.exists() && !file.isDirectory()) {
            viewModel.load(file);
            if (songOnce == true)
                Music(0);
            mazeDisplay.redraw();
        }
    }
    //button of change characters.
    public void cbCharacter() {
        if (cbBCharacter.getValue().equals("JonSnow"))
            mazeDisplay.changeImages("JonSnow");
        else if (cbBCharacter.getValue().equals("Daenerys"))
            mazeDisplay.changeImages("Daenerys");
        else
            mazeDisplay.changeImages("CerseiLannister");
        button_GenerateMaze.setDisable(false);
        //button_GenerateMaze.setVisible(true);
    }

    //set music on
    public void Music(int on) {
        if (mediaPlayer != null)
            mediaPlayer.stop();
        String path;
        if (on == 0) {
            songOnce = false;
            path = "resources\\music\\start.mp3";
        } else {
            songOnce = true;
            path = "resources\\music\\end.mp3";
        }
        Media temp = new Media(Paths.get(path).toUri().toString());
        mediaPlayer = new MediaPlayer(temp);
        mediaPlayer.play();
    }

    //Update time for timer, every time 2 min(120 seconds) for finish.
    public void updateTime() {
        int seconds = timeSeconds.get();
        timeSeconds.set(seconds - 1);
        if (timeSeconds.get() <= 0) {
            viewModel.setCharacterPositionColumn(0);
            viewModel.setCharacterPositionRow(0);
            mazeDisplay.setCharacterPosition(0, 0);
            mazeDisplay.redraw();
            time.stop();
            updateLives();
            timeSeconds.set(startTime);
            if (!lives.get().equals("")) {
                time.stop();
                Stage stage = new Stage();
                stage.setTitle("Alert");
                button = new Button();
                button.setText("OK");
                button.setOnAction(event -> {
                    Timer();
                    Stage s = (Stage) button.getScene().getWindow();
                    s.close();
                });
                button.setAlignment(Pos.BOTTOM_CENTER);
                button.setLayoutX(100);
                button.setLayoutY(138);

                Pane layout = new Pane();
                layout.setPrefHeight(180);
                layout.setPrefWidth(260);
                layout.getChildren().add(button);

                Text t1 = new Text();
                t1.setText("Time is up!");
                t1.setFont(Font.font("System", FontWeight.BOLD,22));
                t1.setLayoutX(70);
                t1.setLayoutY(45);
                // t1.setFont(Font.font(System,19.5,));
                layout.getChildren().add(t1);

                Text t2 = new Text();
                t2.setText("Try again," + "You can do it!");
                t2.setFont(Font.font("System",17.7));
                t2.setLayoutY(100);
                t2.setLayoutX(35);

                layout.getChildren().add(t2);
                Scene scene = new Scene(layout, 260, 185);

                stage.setScene(scene);
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.show();
            }
        }
    }

    //Update the life of the hero, when the time is over.
    public void updateLives() {
        String livesLeft = lives.get();
        if (livesLeft.equals("* * *"))
            lives.set("* *");
        else if (livesLeft.equals("* *"))
            lives.set("*");
        else if (livesLeft.equals("*")) {
            lives.set("");
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("You lose the crown,\n" +
                    "try again next time");
            alert.show();
            time.stop();
            button_GenerateMaze.setDisable(false);
        }
    }

    public void Timer() {
        time = new Timeline(new KeyFrame(Duration.seconds(1), evt -> updateTime()));
        time.setCycleCount(Animation.INDEFINITE); // repeat over and over again
        timeSeconds.set(startTime);
        time.play();
    }
    //start music.
    private void setMusic(boolean musicOn) {
        if (musicOn) {
            this.mediaPlayer.play();
            button_StopMusic.setText("Mute");
        } else {
            this.mediaPlayer.stop();
            button_StopMusic.setText("Music");

        }
    }
    //set music on mute
    public void Mute() {
        if (button_StopMusic.getText().equals("Music")) {
            setMusic(true);
        } else {
            setMusic(false);
        }
    }

    public void mouseDragged(MouseEvent mouseEvent) {
        if (mazeDisplay != null) {
            int maxSize = Math.max(viewModel.getMaze()[0].length, viewModel.getMaze().length);
            double cellHeight = mazeDisplay.getHeight() / maxSize;
            double cellWidth = mazeDisplay.getWidth() / maxSize;
            double canvasHeight = mazeDisplay.getHeight();
            double canvasWidth = mazeDisplay.getWidth();
            int rowMazeSize = viewModel.getMaze().length;
            int colMazeSize = viewModel.getMaze()[0].length;
            double startRow = (canvasHeight / 2-(cellHeight * rowMazeSize / 2)) / cellHeight;
            double startCol = (canvasWidth / 2-(cellWidth * colMazeSize / 2)) / cellWidth;
            double mouseX = (int) ((mouseEvent.getX()) / (mazeDisplay.getWidth() / maxSize)-startCol);
            double mouseY = (int) ((mouseEvent.getY()) / (mazeDisplay.getHeight() / maxSize)-startRow);
            if (!viewModel.isAtTheEnd()) {
                if (mouseY < viewModel.getCharacterPositionRow() && mouseX == viewModel.getCharacterPositionColumn()) {
                    viewModel.moveCharacter(KeyCode.UP);
                }
                if (mouseY > viewModel.getCharacterPositionRow() && mouseX == viewModel.getCharacterPositionColumn()) {
                    viewModel.moveCharacter(KeyCode.DOWN);
                }
                if (mouseX < viewModel.getCharacterPositionColumn() && mouseY == viewModel.getCharacterPositionRow()) {
                    viewModel.moveCharacter(KeyCode.LEFT);
                }
                if (mouseX > viewModel.getCharacterPositionColumn() && mouseY == viewModel.getCharacterPositionRow()) {
                    viewModel.moveCharacter(KeyCode.RIGHT);
                }
            }
        }
    }

    public void zooming(ScrollEvent scrollEvent) {
        try {
            viewModel.getMaze();
            double zoomFactor;
            if (scrollEvent.isControlDown()) {
                zoomFactor = 1.5;
                double deltaY = scrollEvent.getDeltaY();
                if (deltaY < 0)
                    zoomFactor = 1 / zoomFactor;
                zoom(mazeDisplay, zoomFactor, scrollEvent.getSceneX(), scrollEvent.getSceneY());
                scrollEvent.consume();
            }
        } catch(NullPointerException e) {
            scrollEvent.consume();
        }
    }

    public void zoom(Node node, double factor, double x, double y) {
        // determine scale
        double oldScale = node.getScaleX();
        double scale = oldScale * factor;
        double f = (scale / oldScale)-1;

        // determine offset that we will have to move the node
        Bounds bounds = node.localToScene(node.getLayoutBounds(), true);
        double dx = (x-(bounds.getWidth() / 2+bounds.getMinX()));
        double dy = (y-(bounds.getHeight() / 2+bounds.getMinY()));

        // timeline that scales and moves the node
        timeline.getKeyFrames().clear();
        timeline.getKeyFrames().addAll(
                new KeyFrame(Duration.millis(100), new KeyValue(node.translateXProperty(), node.getTranslateX()-f * dx)),
                new KeyFrame(Duration.millis(100), new KeyValue(node.translateYProperty(), node.getTranslateY()-f * dy)),
                new KeyFrame(Duration.millis(100), new KeyValue(node.scaleXProperty(), scale)),
                new KeyFrame(Duration.millis(100), new KeyValue(node.scaleYProperty(), scale))
        );
        timeline.play();
    }
}
