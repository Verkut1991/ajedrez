package com.example.ajedrez;

import java.util.ArrayList;
import java.util.List;

public class LogicaAjedrez {

    // Representación interna del tablero y estado del juego
    private char[][] board;
    private boolean esTurnoBlancos = true;
    private boolean reyBlancoMovio = false;
    private boolean reyNegroMovio = false;
    // Flags de movimiento de torres para enroque
    private boolean torreBlancaLadoReyMovida = false;
    private boolean torreBlancaLadoDamaMovida = false;
    private boolean torreNegraLadoReyMovida = false;
    private boolean torreNegraLadoDamaMovida = false;
    // Referencia a la ActivityJugar para actualizar la interfaz
    private ActivityJugar activityJugar;
    private int contadorMovimientosSinProgreso = 0;// contador para la regla de los movimientos limitados

    // Para el movimiento en passant
    private int enPassantCol = -1;
    private int enPassantRow = -1;

    // Para la regla de los jaques repetidos
    private int jaquesConsecutivos = 0;
    private boolean ultimoMovimientoFueJaque = false;

    // Matriz de piezas lógicas
    public PiezasAjedrez[][] piezasLogicas = new PiezasAjedrez[8][8];

    public LogicaAjedrez(ActivityJugar activityJugar) {
        this.activityJugar = activityJugar;
        initBoard();
        setupBoardPieces();
    }

    // Inicializar el tablero con la configuración estándar
    private void initBoard() {
        board = new char[][] { // representación del tablero con caracteres, mayúsculas para blancas y
                               // minúsculas para negras
                { 'r', 'n', 'b', 'q', 'k', 'b', 'n', 'r' },
                { 'p', 'p', 'p', 'p', 'p', 'p', 'p', 'p' },
                { ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ' },
                { ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ' },
                { ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ' },
                { ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ' },
                { 'P', 'P', 'P', 'P', 'P', 'P', 'P', 'P' },
                { 'R', 'N', 'B', 'Q', 'K', 'B', 'N', 'R' }
        };
    }

    // Configurar las piezas lógicas en el tablero asignándolas a la matriz
    // piezasLogicas
    private void setupBoardPieces() {
        piezasLogicas[0] = new PiezasAjedrez[] { // piezas negras de la fila más atrás
                new Torre(false), new Caballo(false), new Alfil(false), new Reina(false), new Rey(false),
                new Alfil(false), new Caballo(false), new Torre(false)
        };
        piezasLogicas[1] = new PiezasAjedrez[] { // piezas negras peones
                new Peon(false), new Peon(false), new Peon(false), new Peon(false),
                new Peon(false), new Peon(false), new Peon(false), new Peon(false)
        };
        for (int i = 2; i < 6; i++) {// filas vacías
            for (int j = 0; j < 8; j++)
                piezasLogicas[i][j] = null;
        }
        piezasLogicas[6] = new PiezasAjedrez[] { // piezas blancas peones
                new Peon(true), new Peon(true), new Peon(true), new Peon(true),
                new Peon(true), new Peon(true), new Peon(true), new Peon(true)
        };
        piezasLogicas[7] = new PiezasAjedrez[] { // piezas blancas de la fila más adelante
                new Torre(true), new Caballo(true), new Alfil(true), new Reina(true), new Rey(true),
                new Alfil(true), new Caballo(true), new Torre(true)
        };
    }

    // Obtener el estado actual del tablero
    public char[][] getBoard() {
        return board;
    }

    // Obtener de quién es el turno
    public boolean isEsTurnoBlancos() {
        return esTurnoBlancos;
    }

    // Verificar si un movimiento es válido
    public boolean isMovimientoValido(int fromRow, int fromCol, int toRow, int toCol) {
        // Verificar límites
        if (!isInBounds(fromRow, fromCol) || !isInBounds(toRow, toCol)) {
            return false;
        }
        // Obtener la pieza lógica
        PiezasAjedrez piece = piezasLogicas[fromRow][fromCol];

        // No hay pieza
        if (piece == null) {
            return false;
        }

        // Verificar turno
        boolean isPieceWhite = piece.isWhite;
        if (isPieceWhite != esTurnoBlancos) {
            return false;
        }

        // No se puede capturar pieza propia
        PiezasAjedrez targetPiece = piezasLogicas[toRow][toCol];
        if (targetPiece != null && targetPiece.isWhite == isPieceWhite) {
            return false;
        }

        // Verificar movimiento según tipo de pieza usando su método
        if (!piece.isValidMove(fromRow, fromCol, toRow, toCol, board)) {
            return false;
        }

        // Para piezas que se mueven en línea (no caballos), verificar camino despejado
        if (!(piece instanceof Caballo) && !(piece instanceof Rey)) {
            if (!isCaminoDespejado(fromRow, fromCol, toRow, toCol)) {
                return false;
            }
        }


        // Verificar enroque especial para el rey
        if (piece instanceof Rey && Math.abs(toCol - fromCol) == 2) {
            if (!PuedeEnrocar(fromRow, fromCol, toRow, toCol)) {
                return false;
            }
        }

        // Verificar que el rey no quede en jaque después del movimiento
        return !estariaEnJaque(fromRow, fromCol, toRow, toCol, isPieceWhite);
    }

    // Realizar un movimiento
    public boolean HacerMovimiento(int fromRow, int fromCol, int toRow, int toCol) {
        if (!isMovimientoValido(fromRow, fromCol, toRow, toCol)) {// si el movimiento no es válido se vuelve falso
            return false;
        }

        // Realizar el movimiento
        char piece = board[fromRow][fromCol];
        PiezasAjedrez piezaLogica = piezasLogicas[fromRow][fromCol];
        PiezasAjedrez piezaDestino = piezasLogicas[toRow][toCol]; // Guarda si capturamos algo

        // Resetear en passant
        enPassantCol = -1;
        enPassantRow = -1;

        // Detectar movimiento doble de peón para en passant
        if (piezaLogica instanceof Peon && Math.abs(toRow - fromRow) == 2) {
            enPassantCol = fromCol;
            enPassantRow = esTurnoBlancos ? fromRow - 1 : fromRow + 1;
        }

        // Manejar enroque
        if (piezaLogica instanceof Rey && Math.abs(toCol - fromCol) == 2) {// si el rey se mueve dos casillas
            // Mover la torre también
            if (toCol > fromCol) { // Enroque corto
                board[fromRow][5] = board[fromRow][7];// mover torre a su nueva posición
                board[fromRow][7] = ' ';// marcar la posición anterior de la torre como vacía
                piezasLogicas[fromRow][5] = piezasLogicas[fromRow][7];// mover pieza lógica de la torre
                piezasLogicas[fromRow][7] = null;// marcar la posición anterior de la torre como vacía
            } else { // Enroque largo
                board[fromRow][3] = board[fromRow][0];// mover torre a su nueva posición
                board[fromRow][0] = ' ';// marcar la posición anterior de la torre como vacía
                piezasLogicas[fromRow][3] = piezasLogicas[fromRow][0];// mover pieza lógica de la torre
                piezasLogicas[fromRow][0] = null;// marcar la posición anterior de la torre como vacía
            }
        }

        // Realizar el movimiento
        board[toRow][toCol] = piece;
        board[fromRow][fromCol] = ' ';
        piezasLogicas[toRow][toCol] = piezaLogica;
        piezasLogicas[fromRow][fromCol] = null;

        // Promoción de peón
        if (piezaLogica instanceof Peon) {
            if ((esTurnoBlancos && toRow == 0) || (!esTurnoBlancos && toRow == 7)) {// si el peón llega a la fila final
                // Promover a reina automáticamente
                board[toRow][toCol] = esTurnoBlancos ? 'Q' : 'q';// cambiar el carácter en el tablero según el color
                piezasLogicas[toRow][toCol] = new Reina(esTurnoBlancos);// reemplazar peón por reina
            }
        }

        // Actualizar flags de enroque
        ActualizarFlagsEnroque(piezaLogica, fromRow, fromCol);

        // Aquí añadimos la lógica de las tablas:
        if (piezaLogica instanceof Peon || piezaDestino != null) {
            // Si se mueve un peón o se captura una pieza → reiniciamos
            contadorMovimientosSinProgreso = 0;
        } else {
            // Si no hubo peón ni captura → aumentamos
            contadorMovimientosSinProgreso++;
        }

        // Registrar captura
        if (piezaDestino != null) {
            String nombrePieza = piezaDestino.getClass().getSimpleName();

            if (piezaDestino.isWhite) {// si la pieza capturada es blanca
                activityJugar.piezasTotalesCazadasEnemigo++;// incrementar contador de piezas cazadas por el enemigo
            } else {// si la pieza capturada es negra
                activityJugar.piezasTotalesCazadasJugador++;// incrementar contador de piezas cazadas por el jugador
                switch (nombrePieza) {
                    case "Peon":
                        activityJugar.peonesCazadosJugador++;// incrementar contador de peones cazados por el jugador
                        break;
                    case "Torre":
                        activityJugar.torresCazadasJugador++;// incrementar contador de torres cazadas por el jugador
                        break;
                    case "Reina":
                        activityJugar.reinasCazadasJugador++;// incrementar contador de reinas cazadas por el jugador
                        break;
                    case "Rey":
                        activityJugar.reyesCazadosJugador++;// incrementar contador de reyes cazados por el jugador
                        break;
                    case "Caballo":
                        activityJugar.caballosCazadosJugador++;// incrementar contador de caballos cazados por el
                                                               // jugador
                        break;
                    case "Alfil":
                        activityJugar.alfilesCazadosJugador++;// incrementar contador de alfiles cazados por el jugador
                        break;

                }
            }
        }

        // Si llega a 60 (30 turnos por jugador = 60 jugadas en total)
        if (contadorMovimientosSinProgreso >= 60) {
            activityJugar.AcabarPartida("TABLAS:Jugadores superaron límite de jugadas totales sin hacer nada.");// mensaje
                                                                                                                // de
                                                                                                                // tablas
                                                                                                                // y
                                                                                                                // acabar
                                                                                                                // partida
            return true;
        }

        CambiarTurno();// cambiar turno después de un movimiento exitoso

        return true;
    }

    private void CambiarTurno() {
        // Cambiar turno
        esTurnoBlancos = !esTurnoBlancos;
        activityJugar.EstablecerTurnoTxt(esTurnoBlancos ? "blancas" : "negras");

        // 👇 Detectar si el jugador que debe mover ahora está en jaque
        boolean estaEnJaque = EstaEnJaque(esTurnoBlancos);

        if (estaEnJaque) {// si el jugador está en jaque
            if (ultimoMovimientoFueJaque) {// si el último movimiento también fue jaque
                jaquesConsecutivos++;// incrementar contador de jaques consecutivos
            } else {
                jaquesConsecutivos = 1;// si no, reiniciar el contador
                ultimoMovimientoFueJaque = true;// marcar que el último movimiento fue jaque
            }
        } else {
            // Si no hay jaque, reiniciamos el seguimiento
            ultimoMovimientoFueJaque = false;
            jaquesConsecutivos = 0;
        }

        // Si se repiten los jaques demasiadas veces
        if (jaquesConsecutivos >= 10) { // puedes ajustar el número
            MensajesPartida("TABLAS!!Ambas partes agotaron sus oportunidades para acabar con los reyes");
        }

        // detectar si hay tablas por rey atrapado (stalemate)
        if (isStalemate()) {
            MensajesPartida("TABLAS: el rey está atrapado y no puede moverse.");
            return;
        }

        // También puedes detectar jaque mate aquí si quieres:
        if (isCheckmate()) {
            MensajesPartida((esTurnoBlancos ? "Negras" : "Blancas") + " ganan por jaque mate!");
            return;
        }
    }

    public void MensajesPartida(String mensaje) {// mostrar mensaje y acabar partida
        activityJugar.AcabarPartida(mensaje);// llamar al método de la Activity para acabar la partida
    }

    private void ActualizarFlagsEnroque(PiezasAjedrez piece, int row, int col) {// actualizar las banderas de enroque
        if (piece instanceof Rey) {// si la pieza es un rey
            if (piece.isWhite) {// si es el rey blanco
                reyBlancoMovio = true;// marcar que el rey blanco se ha movido
            } else {
                reyNegroMovio = true;// marcar que el rey negro se ha movido
            }
        } else if (piece instanceof Torre) {// si la pieza es una torre
            if (piece.isWhite) {// si es blanca
                if (col == 0)
                    torreBlancaLadoDamaMovida = true;// si la columna es 0 se activa esta flag
                if (col == 7)
                    torreBlancaLadoReyMovida = true;// si la columna es 7 se activa esta flag
            } else {
                if (col == 0)
                    torreNegraLadoDamaMovida = true;// si la columna es 0 se activa esta flag
                if (col == 7)
                    torreNegraLadoReyMovida = true;// si la columna es 7 se activa esta flag
            }
        }
    }

    // Obtener movimientos válidos para una pieza
    public List<int[]> getValidMoves(int row, int col) {
        List<int[]> validMoves = new ArrayList<>();// Se inicia la lista de movimientos válidos

        for (int toRow = 0; toRow < 8; toRow++) {// se comprueba por las filas
            for (int toCol = 0; toCol < 8; toCol++) {// y columnas
                if (isMovimientoValido(row, col, toRow, toCol)) {// si es un movimiento válido
                    validMoves.add(new int[] { toRow, toCol });// se añade a la lista
                }
            }
        }

        return validMoves;// se devuelve la lista de movimientos válidos
    }

    private boolean PuedeEnrocar(int fromRow, int fromCol, int toRow, int toCol) {// verificar si el enroque es posible
        boolean isWhite = piezasLogicas[fromRow][fromCol].isWhite;// obtener el color del rey

        if (isWhite && reyBlancoMovio)
            return false;// si el rey blanco ya se movió, no puede enrocar
        if (!isWhite && reyNegroMovio)
            return false;// si el rey negro ya se movió, no puede enrocar

        // El rey no puede estar en jaque
        if (EstaEnJaque(isWhite))
            return false;

        // Enroque corto (lado del rey)
        if (toCol > fromCol) {
            if (isWhite && torreBlancaLadoReyMovida)
                return false;// si es blanco y la torre fue movida no puede
            if (!isWhite && torreNegraLadoReyMovida)
                return false;// lo mismo con la negra

            if (!isCaminoDespejado(fromRow, fromCol, fromRow, 7))
                return false;// si hay más piezas por el camino se cancela
            if (board[fromRow][7] != (isWhite ? 'R' : 'r'))
                return false;

            // Verificar que el rey no pase por jaque
            if (estariaEnJaque(fromRow, fromCol, fromRow, fromCol + 1, isWhite))
                return false;
        }
        // Enroque largo (lado de la reina)
        else {
            if (isWhite && torreBlancaLadoDamaMovida)
                return false;
            if (!isWhite && torreNegraLadoDamaMovida)
                return false;

            if (!isCaminoDespejado(fromRow, fromCol, fromRow, 0))
                return false;
            if (board[fromRow][0] != (isWhite ? 'R' : 'r'))
                return false;

            // Verificar que el rey no pase por jaque
            if (estariaEnJaque(fromRow, fromCol, fromRow, fromCol - 1, isWhite))
                return false;
        }

        return true;
    }

    // Verificar si el camino está despejado
    private boolean isCaminoDespejado(int fromRow, int fromCol, int toRow, int toCol) {
        int rowStep = Integer.compare(toRow, fromRow);// obtener el paso de fila
        int colStep = Integer.compare(toCol, fromCol);// obtener el paso de columna

        int currentRow = fromRow + rowStep;// iniciar en la siguiente fila
        int currentCol = fromCol + colStep;// iniciar en la siguiente columna

        while (currentRow != toRow || currentCol != toCol) {// mientras no se llegue al destino
            if (board[currentRow][currentCol] != ' ') {// si hay una pieza en el camino
                return false;// el camino no está despejado
            }
            currentRow += rowStep;// avanzar a la siguiente fila
            currentCol += colStep;// avanzar a la siguiente columna
        }

        return true;
    }

    // Verificar si una posición está en jaque
    private boolean estariaEnJaque(int fromRow, int fromCol, int toRow, int toCol, boolean isWhite) {
        // Hacer movimiento temporal
        char tempPiece = board[toRow][toCol];
        PiezasAjedrez tempPiezaLogica = piezasLogicas[toRow][toCol];

        board[toRow][toCol] = board[fromRow][fromCol];
        board[fromRow][fromCol] = ' ';
        piezasLogicas[toRow][toCol] = piezasLogicas[fromRow][fromCol];
        piezasLogicas[fromRow][fromCol] = null;

        // Verificar si está en jaque
        boolean inCheck = EstaEnJaque(isWhite);

        // Deshacer movimiento temporal
        board[fromRow][fromCol] = board[toRow][toCol];
        board[toRow][toCol] = tempPiece;
        piezasLogicas[fromRow][fromCol] = piezasLogicas[toRow][toCol];
        piezasLogicas[toRow][toCol] = tempPiezaLogica;

        return inCheck;// devolver si estaba en jaque o no
    }

    // Verificar jaque mate o tablas
    public boolean isCheckmate() {
        return EstaEnJaque(esTurnoBlancos) && !tieneMovimientosValidos(esTurnoBlancos);
    }

    public boolean isStalemate() {
        return !EstaEnJaque(esTurnoBlancos) && !tieneMovimientosValidos(esTurnoBlancos);
    }

    // Verificar si el rey está en jaque
    private boolean EstaEnJaque(boolean isWhite) {
        // Encontrar rey
        int kingRow = -1, kingCol = -1;

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {// buscar en todas las posiciones
                PiezasAjedrez piece = piezasLogicas[row][col];
                if (piece instanceof Rey && piece.isWhite == isWhite) {// si es el rey del color correspondiente
                    kingRow = row;
                    kingCol = col;
                    break;
                }
            }
        }

        if (kingRow == -1)
            return false; // No se encontró el rey

        // Verificar si está atacado
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                PiezasAjedrez piece = piezasLogicas[row][col];
                if (piece != null && piece.isWhite != isWhite) {
                    // Verificar si la pieza puede atacar al rey
                    if (piece.isValidMove(row, col, kingRow, kingCol, board)) {
                        // Para piezas que no sean caballos, verificar camino despejado
                        if (piece instanceof Caballo || isCaminoDespejado(row, col, kingRow, kingCol)) {// si es un caballo o el camino está despejado
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    // Verificar si el jugador tiene movimientos válidos
    private boolean tieneMovimientosValidos(boolean isWhite) {
        // Buscar todas las piezas del jugador
        for (int fromRow = 0; fromRow < 8; fromRow++) {
            for (int fromCol = 0; fromCol < 8; fromCol++) {
                PiezasAjedrez piece = piezasLogicas[fromRow][fromCol];
                if (piece != null && piece.isWhite == isWhite) {// si es una pieza del jugador
                    // Probar todos los movimientos posibles
                    for (int toRow = 0; toRow < 8; toRow++) {
                        for (int toCol = 0; toCol < 8; toCol++) {// por todas las posiciones del tablero
                            if (isMovimientoValido(fromRow, fromCol, toRow, toCol)) {// si el movimiento es válido
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean isInBounds(int row, int col) {// verificar si las coordenadas están dentro del tablero
        return row >= 0 && row < 8 && col >= 0 && col < 8;
    }

    public void reset() {// reiniciar el juego
        initBoard();
        setupBoardPieces();
        esTurnoBlancos = true;
        reyBlancoMovio = false;
        reyNegroMovio = false;
        torreBlancaLadoReyMovida = false;
        torreBlancaLadoDamaMovida = false;
        torreNegraLadoReyMovida = false;
        torreNegraLadoDamaMovida = false;
        enPassantCol = -1;
        enPassantRow = -1;
    }
}