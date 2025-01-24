package com.example.lab3_2;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HelloApplication extends Application {
    private static final int SIZE = 4; // Размер игрового поля (10x10)
    private static final int CELL_SIZE = 40; // Размер одной ячейки
    private static final int NUM_BOMBS = 2; // Количество бомб
    private final Cell[][] board = new Cell[SIZE][SIZE]; // Игровое поле
    private boolean gameOver = false; // Флаг окончания игры
    private GraphicsContext gc;

    @Override
    public void start(Stage stage) {
        initializeBoard(); // Инициализация игрового поля

        Canvas canvas = new Canvas(SIZE * CELL_SIZE, SIZE * CELL_SIZE);
        gc = canvas.getGraphicsContext2D(); // Присваиваем значение полю класса
        drawBoard(gc); // Отрисовка игрового поля

        canvas.setOnMouseClicked(this::onClick); // Обработка кликов

        VBox vBox = new VBox();
        vBox.getChildren().addAll(canvas);
        Scene scene = new Scene(vBox);
        stage.setScene(scene);
        stage.setTitle("Сапер");
        stage.show();
    }

    private void initializeBoard() {
        // Инициализация ячеек и расстановка бомб
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                board[i][j] = new Cell(i, j);
            }
        }
        placeBombs(); // Размещение бомб
        calculateNumbers(); // Подсчет чисел вокруг бомб
    }

    private void placeBombs() {
        List<Integer> bombPositions = new ArrayList<>();
        for (int i = 0; i < SIZE * SIZE; i++) {
            bombPositions.add(i);
        }
        Collections.shuffle(bombPositions); // Перемешивание позиций

        for (int i = 0; i < NUM_BOMBS; i++) {
            int position = bombPositions.get(i);
            int row = position / SIZE;
            int col = position % SIZE;
            board[row][col].setBomb(true);
        }
    }

    private void calculateNumbers() {
        // Подсчет чисел вокруг бомб
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (!board[i][j].isBomb()) {
                    int count = 0;
                    for (int x = -1; x <= 1; x++) {
                        for (int y = -1; y <= 1; y++) {
                            if (isInBounds(i + x, j + y) && board[i + x][j + y].isBomb()) {
                                count++;
                            }
                        }
                    }
                    board[i][j].setNumber(count);
                }
            }
        }
    }

    private boolean isInBounds(int row, int col) {
        return row >= 0 && row < SIZE && col >= 0 && col < SIZE;
    }

    private void drawBoard(GraphicsContext gc) {
        gc.clearRect(0, 0, SIZE * CELL_SIZE, SIZE * CELL_SIZE); // Очистка холста
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                board[i][j].draw(gc, j * CELL_SIZE, i * CELL_SIZE); // Отрисовка каждой ячейки
            }
        }
    }

    private void onClick(MouseEvent mouseEvent) {
        if (gameOver) {
            return; // Игнорируем клики, если игра окончена
        }

        double x = mouseEvent.getX();
        double y = mouseEvent.getY();

        int rowIndex = (int) (y / CELL_SIZE);
        int columnIndex = (int) (x / CELL_SIZE);

        if (isInBounds(rowIndex, columnIndex)) {
            Cell clickedCell = board[rowIndex][columnIndex];
            if (!clickedCell.isRevealed()) {
                clickedCell.reveal(); // Открываем ячейку
                drawBoard(gc); // Перерисовываем игровое поле

                if (clickedCell.isBomb()) {
                    gameOver = true; // Игра окончена
                    showGameOverAlert(false);
                } else if (clickedCell.getNumber() == 0) {
                    // Если ячейка пустая, открываем соседние ячейки
                    revealAdjacentCells(rowIndex, columnIndex);
                }

                checkVictory(); // Проверяем на выигрыш
            }
        }
    }

    private void revealAdjacentCells(int row, int col) {
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                int newRow = row + x;
                int newCol = col + y;
                if (isInBounds(newRow, newCol) && !board[newRow][newCol].isRevealed()) {
                    board[newRow][newCol].reveal();
                    drawBoard(gc); // Перерисовываем игровое поле
                    if (board[newRow][newCol].getNumber() == 0) {
                        revealAdjacentCells(newRow, newCol); // Рекурсивно открываем соседние ячейки
                    }
                }
            }
        }
    }

    private void checkVictory() {
        boolean allCellsRevealed = true;
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (!board[i][j].isRevealed() && !board[i][j].isBomb()) {
                    allCellsRevealed = false;
                }
            }
        }
        if (allCellsRevealed) {
            gameOver = true; // Игра окончена
            showGameOverAlert(true);
        }
    }

    private void showGameOverAlert(boolean isVictory) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(isVictory ? "Поздравляем!" : "Игра окончена");
        alert.setHeaderText(isVictory ? "Вы выиграли!" : "Вы попали на бомбу!");
        alert.setContentText(isVictory ? "Вы открыли все ячейки!" : "Игра завершена.");
        alert.showAndWait(); // Показываем сообщение
    }

    public static void main(String[] args) {
        launch(args);
    }

    // Вложенный класс для ячеек
    static class Cell {
        private final int row; // Строка
        private final int col; // Столбец
        private boolean isBomb = false; // Статус бомбы
        private boolean revealed = false; // Статус открытости
        private int number = 0; // Число вокруг ячейки

        public Cell(int row, int col) {
            this.row = row;
            this.col = col;
        }

        public void draw(GraphicsContext gc, int x, int y) {
            if (revealed) {
                // Рисуем открытую ячейку
                gc.setFill(Color.LIGHTGRAY);
                gc.fillRect(x, y, CELL_SIZE, CELL_SIZE);
                gc.strokeRect(x, y, CELL_SIZE, CELL_SIZE); // Рисуем границу
                if (isBomb) {
                    gc.setFill(Color.RED);
                    gc.fillText("💣", x + CELL_SIZE / 4, y + CELL_SIZE * 3 / 4);
                } else {
                    if (number > 0) {
                        gc.setFill(javafx.scene.paint.Color.BLACK);
                        gc.fillText(String.valueOf(number), x + CELL_SIZE / 4, y + CELL_SIZE * 3 / 4);
                    }
                }
            } else {
                // Рисуем закрытую ячейку
                gc.setFill(javafx.scene.paint.Color.GRAY);
                gc.fillRect(x, y, CELL_SIZE, CELL_SIZE);
                gc.strokeRect(x, y, CELL_SIZE, CELL_SIZE); // Рисуем границу
            }
        }

        public void reveal() {
            this.revealed = true; // Открываем ячейку
        }

        public void setBomb(boolean isBomb) {
            this.isBomb = isBomb; // Устанавливаем статус бомбы
        }

        public void setNumber(int number) {
            this.number = number; // Устанавливаем число вокруг ячейки
        }

        public boolean isBomb() {
            return isBomb; // Проверяем статус бомбы
        }

        public boolean isRevealed() {
            return revealed; // Проверяем статус открытости
        }

        public int getNumber() {
            return number; // Получаем число вокруг ячейки
        }
    }
}
