package se.iths.java21.patrik.lab3;

import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;
import se.iths.java21.patrik.lab3.shapes.Circle;
import se.iths.java21.patrik.lab3.shapes.Rectangle;
import se.iths.java21.patrik.lab3.shapes.Shape;
import se.iths.java21.patrik.lab3.shapes.ShapesFactory;

import java.io.*;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Model {
    Deque<ObservableList<Shape>> undoDeque;
    Deque<ObservableList<Shape>> redoDeque;
    ObservableList<Shape> shapes;
    ObservableList<Shape> selectedShapes;

    private final BooleanProperty circleSelected;
    private final BooleanProperty rectangleSelected;
    private final BooleanProperty lineSelected;
    private final BooleanProperty pointSelected;

    private final ObjectProperty<Color> color;
    private final ObjectProperty<Color> borderColor;
    private final StringProperty shapeSize;

    private final BooleanProperty selectMode;

    public Model() {
        this.undoDeque = new ArrayDeque<>();
        this.redoDeque = new ArrayDeque<>();
        this.shapes = FXCollections.observableArrayList();
        this.selectedShapes = FXCollections.observableArrayList();

        this.circleSelected = new SimpleBooleanProperty();
        this.rectangleSelected = new SimpleBooleanProperty();
        this.lineSelected = new SimpleBooleanProperty();
        this.pointSelected = new SimpleBooleanProperty();

        this.color = new SimpleObjectProperty<>(Color.BLACK);
        this.borderColor = new SimpleObjectProperty<>();
        this.borderColor.set(Color.TRANSPARENT);
        this.shapeSize = new SimpleStringProperty("18");

        this.selectMode = new SimpleBooleanProperty();
    }


    public boolean isCircleSelected() {
        return circleSelected.get();
    }

    public BooleanProperty circleSelectedProperty() {
        return circleSelected;
    }

    public void setCircleSelected(boolean circleSelected) {
        this.circleSelected.set(circleSelected);
    }

    public boolean isRectangleSelected() {
        return rectangleSelected.get();
    }

    public BooleanProperty rectangleSelectedProperty() {
        return rectangleSelected;
    }

    public void setRectangleSelected(boolean rectangleSelected) {
        this.rectangleSelected.set(rectangleSelected);
    }

    public boolean isLineSelected() {
        return lineSelected.get();
    }

    public BooleanProperty lineSelectedProperty() {
        return lineSelected;
    }

    public void setLineSelected(boolean lineSelected) {
        this.lineSelected.set(lineSelected);
    }

    public boolean isPointSelected() {
        return pointSelected.get();
    }

    public BooleanProperty pointSelectedProperty() {
        return pointSelected;
    }

    public void setPointSelected(boolean pointSelected) {
        this.pointSelected.set(pointSelected);
    }


    public Color getColor() {
        return color.get();
    }

    public ObjectProperty<Color> colorProperty() {
        return color;
    }

    public void setColor(Color color) {
        this.color.set(color);
    }

    public Color getBorderColor() {
        return borderColor.get();
    }

    public ObjectProperty<Color> borderColorProperty() {
        return borderColor;
    }

    public void setBorderColor(Color borderColor) {
        this.borderColor.set(borderColor);
    }

    public String getShapeSize() {
        return shapeSize.get();
    }

    public Double getShapeSizeAsDouble() {
        return Double.parseDouble(getShapeSize());
    }

    public StringProperty shapeSizeProperty() {
        return shapeSize;
    }

    public void setShapeSize(String shapeSize) {
        this.shapeSize.set(shapeSize);
    }


    public boolean isSelectMode() {
        return selectMode.get();
    }

    public BooleanProperty selectModeProperty() {
        return selectMode;
    }

    public void setSelectMode(boolean selectMode) {
        this.selectMode.set(selectMode);
    }


    public void deleteSelectedShapes() {

        ObservableList<Shape> tempList = getTempList();

        undoDeque.addLast(FXCollections.observableArrayList(tempList));

        for (var shape : selectedShapes) {
            shapes.remove(shape);
        }
    }

    public void changeSizeOnSelectedShapes() {
        ObservableList<Shape> tempList = getTempList();

        undoDeque.addLast(FXCollections.observableArrayList(tempList));

        for (var shape : selectedShapes) {
            shape.setSize(getShapeSizeAsDouble());
        }
    }

    public void changeColorOnSelectedShapes() {
        ObservableList<Shape> tempList = getTempList();

        undoDeque.addLast(FXCollections.observableArrayList(tempList));

        for (var shape : selectedShapes) {
            shape.setColor(getColor());
        }
    }

    public ObservableList<Shape> getTempList() {
        ObservableList<Shape> tempList = FXCollections.observableArrayList();

        for (Shape shape : shapes) {
            if (shape.getClass() == Rectangle.class)
                tempList.add(ShapesFactory.rectangleCopyOf(shape));
            if (shape.getClass() == Circle.class)
                tempList.add(ShapesFactory.circleCopyOf(shape));
        }
        return tempList;
    }

    private Socket socket;
    private PrintWriter writer; // Skickar meddelanden till servern
    private BufferedReader reader; // Läser meddelanden från servern
    public BooleanProperty connected = new SimpleBooleanProperty();
    ExecutorService executorService = Executors.newSingleThreadExecutor();

    public void connect() {
        if (connected.get()) {
            System.out.println("Disconnected from server...");
            connected.set(false);
            return;
        }

        try {
            socket = new Socket("localhost", 8000);
            OutputStream output = socket.getOutputStream();
            writer = new PrintWriter(output, true);

            InputStream input = socket.getInputStream();
            reader = new BufferedReader(new InputStreamReader(input));
            connected.set(true);
            System.out.println("Connected to Server...");

            executorService.submit(this::networkHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendToServer(Shape shape) {
        if (connected.get()) {
            writer.println("Created a new Shape with coords, x:" + shape.getX() + " y:" + shape.getY());
        }
    }

    private void networkHandler() {
        try {
            while (true) {
                String line = reader.readLine();
                System.out.println(line);
                Platform.runLater(() ->
                shapes.add(ShapesFactory.rectangleOf(Color.PINK, 100, 100, 50)));
            }
        } catch (IOException e) {
            System.out.println("I/O error. Disconnected.");
            Platform.runLater(() -> connected.set(false));
        }
    }
}
