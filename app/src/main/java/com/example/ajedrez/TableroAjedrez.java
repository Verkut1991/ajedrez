package com.example.ajedrez;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class TableroAjedrez extends View {
//información de los cuadraditos del tablero guardados en paint
    private Paint cuadraditoBlanco;
    private Paint cuadraditoNegro;
    private Paint cuadraditoSeleccionado;
    private Paint cuadraditoMovimientoValido;
    private Paint representacionFigura;

    //variables de los cuadrados y tamaño de tablero
    private float cuadradoTamano = 0f;
    private float boardSize = 0f;
    private int selectedRow = -1;
    private int selectedCol = -1;

    //Script para la lógica del ajedrez
    private LogicaAjedrez logicaAjedrez;

    //Mapa para asignar los símbolos de las piezas
    private Map<Character, String> simbolosPiezas;

    //lista de movimientos válidos
    private List<int[]> validMoves = null;

    //variables de elementos variables en el entorno
    private boolean puedeVibrar=true,mostrarMovimientos=true;

    public TableroAjedrez(Context context) {
        super(context);
        init();
    }

    public TableroAjedrez(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TableroAjedrez(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
//Instanciamos las variables paint con un color y un estilo determinado
        cuadraditoBlanco = new Paint();
        cuadraditoBlanco.setColor(Color.parseColor("#F0D9B5"));
        cuadraditoBlanco.setStyle(Paint.Style.FILL);

        cuadraditoNegro = new Paint();
        cuadraditoNegro.setColor(Color.parseColor("#B58863"));
        cuadraditoNegro.setStyle(Paint.Style.FILL);

        cuadraditoSeleccionado = new Paint();
        cuadraditoSeleccionado.setColor(Color.parseColor("#7FFF6B4A"));
        cuadraditoSeleccionado.setStyle(Paint.Style.FILL);

        cuadraditoMovimientoValido = new Paint();
        cuadraditoMovimientoValido.setColor(Color.parseColor("#7F90EE90"));
        cuadraditoMovimientoValido.setStyle(Paint.Style.FILL);

        representacionFigura = new Paint();
        representacionFigura.setTextAlign(Paint.Align.CENTER);
        representacionFigura.setAntiAlias(true);


        MapearBoard();
    }

    public void setLogicaAjedrez(LogicaAjedrez logicaAjedrez){
        this.logicaAjedrez= logicaAjedrez;
    }

    private void MapearBoard(){
        // Mapeo de piezas a símbolos Unicode
        simbolosPiezas = new HashMap<>();
        simbolosPiezas.put('K', "♔");
        simbolosPiezas.put('Q', "♕");
        simbolosPiezas.put('R', "♖");
        simbolosPiezas.put('B', "♗");
        simbolosPiezas.put('N', "♘");
        simbolosPiezas.put('P', "♙");
        simbolosPiezas.put('k', "♚");
        simbolosPiezas.put('q', "♛");
        simbolosPiezas.put('r', "♜");
        simbolosPiezas.put('b', "♝");
        simbolosPiezas.put('n', "♞");
        simbolosPiezas.put('p', "♟");
    }

    //Método para medir el tamaño del tablero
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int size = Math.min(width, height);
        setMeasuredDimension(size, size);
    }

    //Metódo para cambiar el tamaño del tablero cuando se cambia la orientación o tamaño de la pantalla
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        boardSize = Math.min(w, h);//tamaño del tablero
        cuadradoTamano = boardSize / 8f;
        representacionFigura.setTextSize(cuadradoTamano * 0.8f);
    }

    //Método de renderizado para el tablero
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //carga las piezas almacenadas en la lógica del juego
        char[][] board = logicaAjedrez.getBoard();

        // Dibujar el tablero
        for (int filas = 0; filas < 8; filas++) {
            for (int columnas = 0; columnas < 8; columnas++) {
                boolean isLightSquare = (filas + columnas) % 2 == 0;
                Paint paint = isLightSquare ? cuadraditoBlanco : cuadraditoNegro;

                //Dibuja los cuadritos con el tamaño correspondiente
                float left = columnas * cuadradoTamano;
                float top = filas * cuadradoTamano;
                RectF rect = new RectF(left, top, left + cuadradoTamano, top + cuadradoTamano);

                canvas.drawRect(rect, paint);

                // Resaltar casilla seleccionada
                if (selectedRow == filas && selectedCol == columnas) {
                    canvas.drawRect(rect, cuadraditoSeleccionado);
                }

                // Resaltar movimientos válidos
                if (validMoves != null && mostrarMovimientos) {
                    for (int[] move : validMoves) {
                        if (move[0] == filas && move[1] == columnas) {
                            canvas.drawRect(rect, cuadraditoMovimientoValido);
                            break;
                        }
                    }
                }

                // Dibujar piezas
                char piece = board[filas][columnas];
                if (piece != ' ') {//si la pieza es diferente a nada
                    String symbol = simbolosPiezas.get(piece);
                    if (symbol != null) {
                        float x = left + cuadradoTamano / 2f;
                        float y = top + cuadradoTamano / 2f - (representacionFigura.descent() + representacionFigura.ascent()) / 2f;

                        // Color de la pieza
                        if (Character.isUpperCase(piece)) {
                            representacionFigura.setColor(Color.WHITE);
                        } else {
                            representacionFigura.setColor(Color.BLACK);
                        }

                        // Sombra para mejor visibilidad
                        representacionFigura.setShadowLayer(4f, 2f, 2f, Color.BLACK);

                        canvas.drawText(symbol, x, y, representacionFigura);
                    }
                }
            }
        }
    }

    //Método para detectar los toques en la pantalla
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {//cuando se detecta un toque en la pantalla
            int col = (int) (event.getX() / cuadradoTamano);//calcula la columna tocada
            int row = (int) (event.getY() / cuadradoTamano);//calcula la fila tocada

            if (row >= 0 && row < 8 && col >= 0 && col < 8) {//verifica que esté dentro del tablero
                if (selectedRow == -1) {//ninguna pieza seleccionada
                    // Seleccionar pieza si hay una y es del turno actual
                    PiezasAjedrez piece = logicaAjedrez.piezasLogicas[row][col];
                    if (piece != null && piece.isWhite == logicaAjedrez.isEsTurnoBlancos()) {
                        selectedRow = row;//selecciona la fila
                        selectedCol = col;//selecciona la columna
                        validMoves = logicaAjedrez.getValidMoves(row, col);//obtiene los movimientos válidos
                    }
                } else {
                    // Intentar mover la pieza
                    if (logicaAjedrez.HacerMovimiento(selectedRow, selectedCol, row, col)) {
                        Vibrar(this.getContext(),100);
                        // Movimiento exitoso
                        checkGameState();
                    } else {
                        // Movimiento incorrecto, intentar seleccionar otra pieza
                        PiezasAjedrez piece = logicaAjedrez.piezasLogicas[row][col];
                        if (piece != null && piece.isWhite == logicaAjedrez.isEsTurnoBlancos()) {
                            selectedRow = row;
                            selectedCol = col;
                            validMoves = logicaAjedrez.getValidMoves(row, col);
                        } else {// Deseleccionar si no es una pieza válida
                            selectedRow = -1;
                            selectedCol = -1;
                            validMoves = null;
                        }
                    }

                    // Si el movimiento fue exitoso, deseleccionar
                    if (selectedRow != row || selectedCol != col) {
                        selectedRow = -1;
                        selectedCol = -1;
                        validMoves = null;
                    }
                }
                invalidate();//redibuja el tablero
                return true;
            }
        }
        return super.onTouchEvent(event);
    }



    public void Vibrar(Context context, long milisegundos) {
        if(!puedeVibrar)return;//si no puede vibrar se devuelve

        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);//obtiene el servicio de vibración
        if (vibrator != null && vibrator.hasVibrator()) {
            // Vibrar con un efecto (duración en milisegundos, amplitud 255 = máximo)
            VibrationEffect efecto = VibrationEffect.createOneShot(milisegundos, VibrationEffect.DEFAULT_AMPLITUDE);
            vibrator.vibrate(efecto);
        }
    }

    private void checkGameState() {//verifica el estado del juego
        if (logicaAjedrez.isCheckmate()) {//si es jaque mate
            String winner = !logicaAjedrez.isEsTurnoBlancos() ? "Blancas" : "Negras";
            onGameEnd("¡Jaque mate! Ganan las " + winner);//llama al método onGameEnd con el mensaje correspondiente
        } else if (logicaAjedrez.isStalemate()) {//si es tablas por ahogado
            onGameEnd("¡Tablas por ahogado!");
        }
    }

    public void setPuedeVibrar(boolean puede){//establece si puede vibrar o no
        puedeVibrar=puede;
    }

    public void setMostrarMovimientos(boolean mostrar){//establece si se muestran los movimientos válidos o no
        mostrarMovimientos=mostrar;
    }
    // Método para ser sobrescrito o escuchado desde la Activity
    protected void onGameEnd(String mensaje) {
        // Override este método en tu Activity para mostrar un diálogo o Toast
        logicaAjedrez.MensajesPartida(mensaje);
    }

    // Método público para resetear el tablero
    public void resetBoard() {
        logicaAjedrez.reset();//resetea la lógica del ajedrez
        selectedRow = -1;
        selectedCol = -1;
        validMoves = null;
        invalidate();
    }

    // Método para saber de quién es el turno
    public String getCurrentTurn() {
        return logicaAjedrez.isEsTurnoBlancos() ? "Blancas" : "Negras";
    }
}