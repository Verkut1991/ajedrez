package com.example.ajedrez;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class IAAjedrez {

    // Lógica del juego de ajedrez
    private LogicaAjedrez logica;
    private Random random;

    // Valores de las piezas para evaluar capturas
    private static final int VALOR_PEON = 10;
    private static final int VALOR_CABALLO = 30;
    private static final int VALOR_ALFIL = 30;
    private static final int VALOR_TORRE = 50;
    private static final int VALOR_REINA = 90;
    private static final int VALOR_REY = 1000;

    public IAAjedrez(LogicaAjedrez logica) {
        this.logica = logica;
        this.random = new Random();
    }

   //Método principal para que la IA realice un movimiento
    public boolean RealizarMovimiento() {
        List<Movimiento> todosLosMovimientos = obtenerTodosLosMovimientosPosibles();// obtener todos los movimientos posibles para las piezas negras

        if (todosLosMovimientos.isEmpty()) {
            return false; // No hay movimientos válidos
        }

        // Evaluar y elegir el mejor movimiento
        Movimiento mejorMovimiento = elegirMejorMovimiento(todosLosMovimientos);

        // Realizar el movimiento
        return logica.HacerMovimiento(
                mejorMovimiento.desdeRow,
                mejorMovimiento.desdeCol,
                mejorMovimiento.hastaRow,
                mejorMovimiento.hastaCol
        );
    }


     //Obtiene todos los movimientos posibles para las piezas negras
     private List<Movimiento> obtenerTodosLosMovimientosPosibles() {
        List<Movimiento> movimientos = new ArrayList<>();

        // Recorrer todas las piezas en el tablero
        for (int fromRow = 0; fromRow < 8; fromRow++) {
            for (int fromCol = 0; fromCol < 8; fromCol++) {
                PiezasAjedrez pieza = logica.piezasLogicas[fromRow][fromCol];

                // Solo considerar piezas negras
                if (pieza != null && !pieza.isWhite) {
                    // Obtener todos los movimientos válidos para esta pieza
                    List<int[]> movimientosValidos = logica.getValidMoves(fromRow, fromCol);
                    // Agregar cada movimiento válido a la lista
                    for (int[] destino : movimientosValidos) {
                        movimientos.add(new Movimiento(fromRow, fromCol, destino[0], destino[1]));
                    }
                }
            }
        }

        return movimientos;
    }

    //Elige el mejor movimiento basándose en una evaluación simple
    private Movimiento elegirMejorMovimiento(List<Movimiento> movimientos) {
        int mejorPuntuacion = Integer.MIN_VALUE;
        List<Movimiento> mejoresMovimientos = new ArrayList<>();// lista de mejores movimientos

        for (Movimiento mov : movimientos) {// evaluar cada movimiento
            int puntuacion = evaluarMovimiento(mov);// obtener puntuación del movimiento

            if (puntuacion > mejorPuntuacion) {// si es mejor que la mejor puntuación actual
                mejorPuntuacion = puntuacion;// actualizar mejor puntuación
                mejoresMovimientos.clear();// limpiar lista de mejores movimientos
                mejoresMovimientos.add(mov);// agregar este movimiento como el mejor
            } else if (puntuacion == mejorPuntuacion) {// si es igual a la mejor puntuación
                mejoresMovimientos.add(mov);// agregar a la lista de mejores movimientos
            }
        }

        // Si hay varios movimientos con la misma puntuación, elegir uno al azar
        return mejoresMovimientos.get(random.nextInt(mejoresMovimientos.size()));
    }

    //Evalúa un movimiento y le asigna una puntuación
    //Mayor puntuación = mejor movimiento
    private int evaluarMovimiento(Movimiento mov) {
        int puntuacion = 0;

        PiezasAjedrez piezaDestino = logica.piezasLogicas[mov.hastaRow][mov.hastaCol];// pieza en la casilla de destino
        PiezasAjedrez piezaOrigen = logica.piezasLogicas[mov.desdeRow][mov.desdeCol];// pieza que se mueve

        // 1. CAPTURAR PIEZAS (prioridad alta)
        if (piezaDestino != null && piezaDestino.isWhite) {
            puntuacion += obtenerValorPieza(piezaDestino) * 10;// valor de la pieza capturada por 10
        }

        // 2. DAR JAQUE (muy bueno)
        if (CausaJaque(mov)) {
            puntuacion += 50;// bonus por dar jaque
        }

        // 3. MOVER HACIA EL CENTRO (bueno en early game)
        puntuacion += evaluarPosicionCentral(mov.hastaRow, mov.hastaCol);

        // 4. PROTEGER PIEZAS PROPIAS
        if (estaBajoAtaque(mov.desdeRow, mov.desdeCol)) {
            puntuacion += 20; // Bonus por salir de una casilla amenazada
        }

        // 5. DESARROLLAR PIEZAS (mover desde posición inicial)
        if (esPosicionInicial(piezaOrigen, mov.desdeRow)) {
            puntuacion += 15;
        }

        // 6. AVANZAR PEONES
        if (piezaOrigen instanceof Peon) {
            puntuacion += (7 - mov.hastaRow) * 2; // Más puntos mientras más avance
        }

        // 7. PEQUEÑA ALEATORIEDAD para variedad
        puntuacion += random.nextInt(5);

        return puntuacion;
    }

    //Verifica si un movimiento causa jaque al rey blanco
    private boolean CausaJaque(Movimiento mov) {
        // Hacer movimiento temporal
        char[][] board = logica.getBoard();
        char tempPiece = board[mov.hastaRow][mov.hastaCol];
        PiezasAjedrez tempPiezaLogica = logica.piezasLogicas[mov.hastaRow][mov.hastaCol];
// realizar el movimiento
        board[mov.hastaRow][mov.hastaCol] = board[mov.desdeRow][mov.desdeCol];
        board[mov.desdeRow][mov.desdeCol] = ' ';
        logica.piezasLogicas[mov.hastaRow][mov.hastaCol] = logica.piezasLogicas[mov.desdeRow][mov.desdeCol];
        logica.piezasLogicas[mov.desdeRow][mov.desdeCol] = null;

        // Verificar si el rey blanco está en jaque
        boolean hayJaque = isInCheck(true);

        // Deshacer movimiento
        board[mov.desdeRow][mov.desdeCol] = board[mov.hastaRow][mov.hastaCol];
        board[mov.hastaRow][mov.hastaCol] = tempPiece;
        logica.piezasLogicas[mov.desdeRow][mov.desdeCol] = logica.piezasLogicas[mov.hastaRow][mov.hastaCol];
        logica.piezasLogicas[mov.hastaRow][mov.hastaCol] = tempPiezaLogica;

        return hayJaque;
    }

   //Verifica si el rey del color especificado está en jaque
    private boolean isInCheck(boolean isWhite) {
        int kingRow = -1, kingCol = -1;// posición del rey

        for (int row = 0; row < 8; row++) {// buscar en todas las filas
            for (int col = 0; col < 8; col++) {// buscar en todas las columnas
                PiezasAjedrez piece = logica.piezasLogicas[row][col];
                if (piece instanceof Rey && piece.isWhite == isWhite) {// si es el rey del color correspondiente
                    kingRow = row;// guardar fila del rey
                    kingCol = col;// guardar columna del rey
                    break;
                }
            }
        }

        if (kingRow == -1) return false;// rey no encontrado, no puede estar en jaque

        // Verificar si alguna pieza enemiga puede atacar al rey
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                PiezasAjedrez piece = logica.piezasLogicas[row][col];
                if (piece != null && piece.isWhite != isWhite) {
                    if (piece.isValidMove(row, col, kingRow, kingCol, logica.getBoard())) {// si puede moverse al rey
                        if (piece instanceof Caballo || isPathClear(row, col, kingRow, kingCol)) {// si es un caballo o el camino está despejado
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }
    //Verifica si el camino entre dos casillas está despejado (sin piezas en medio)
    private boolean isPathClear(int fromRow, int fromCol, int toRow, int toCol) {
        int rowStep = Integer.compare(toRow, fromRow);
        int colStep = Integer.compare(toCol, fromCol);

        int currentRow = fromRow + rowStep;
        int currentCol = fromCol + colStep;

        while (currentRow != toRow || currentCol != toCol) {// mientras no se llegue a la casilla de destino
            if (logica.getBoard()[currentRow][currentCol] != ' ') {// si hay una pieza en el camino
                return false;
            }
            currentRow += rowStep;
            currentCol += colStep;
        }

        return true;
    }

    //Verifica si una casilla está bajo ataque por piezas blancas
    private boolean estaBajoAtaque(int row, int col) {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                PiezasAjedrez pieza = logica.piezasLogicas[r][c];
                if (pieza != null && pieza.isWhite) {
                    if (pieza.isValidMove(r, c, row, col, logica.getBoard())) {// si puede moverse a la casilla
                        if (pieza instanceof Caballo || isPathClear(r, c, row, col)) {// si es un caballo o el camino está despejado
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
//Evalúa qué tan buena es una posición central en el tablero
    private int evaluarPosicionCentral(int row, int col) {
        int distanciaCentro = Math.abs(row - 3) + Math.abs(col - 3) +
                Math.abs(row - 4) + Math.abs(col - 4);// distancia al centro (3,3) y (4,4)
        return Math.max(0, 8 - distanciaCentro);// más cerca del centro = mayor puntuación
    }

    /**
     * Verifica si una pieza está en su posición inicial
     */
    private boolean esPosicionInicial(PiezasAjedrez pieza, int row) {
        if (pieza.isWhite) return false; // Solo para piezas negras

        if (pieza instanceof Peon) {
            return row == 1;
        } else {
            return row == 0;
        }
    }


    //Obtiene el valor de una pieza para evaluar capturas
    private int obtenerValorPieza(PiezasAjedrez pieza) {
        if (pieza instanceof Peon) return VALOR_PEON;
        if (pieza instanceof Caballo) return VALOR_CABALLO;
        if (pieza instanceof Alfil) return VALOR_ALFIL;
        if (pieza instanceof Torre) return VALOR_TORRE;
        if (pieza instanceof Reina) return VALOR_REINA;
        if (pieza instanceof Rey) return VALOR_REY;
        return 0;
    }

   //Clase interna para representar un movimiento
    private static class Movimiento {
        int desdeRow, desdeCol, hastaRow, hastaCol;

        Movimiento(int desdeRow, int desdeCol, int hastaRow, int hastaCol) {// constructor
            this.desdeRow = desdeRow;
            this.desdeCol = desdeCol;
            this.hastaRow = hastaRow;
            this.hastaCol = hastaCol;
        }
    }
}