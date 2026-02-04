package com.example.ajedrez;

public abstract class PiezasAjedrez {

    // Indica si la pieza es blanca o negra
    boolean isWhite;

    PiezasAjedrez(boolean isWhite) {
        this.isWhite = isWhite;
    }

    // Método abstracto para validar movimientos
    abstract boolean isValidMove(int srcRow, int srcCol, int destRow, int destCol, char[][] board);
}

class Rey extends PiezasAjedrez {
    Rey(boolean isWhite) {
        super(isWhite);
    }

    @Override
    boolean isValidMove(int srcRow, int srcCol, int destRow, int destCol, char[][] board) {
        int dRow = Math.abs(destRow - srcRow);// diferencia absoluta de filas
        int dCol = Math.abs(destCol - srcCol);// diferencia absoluta de columnas

        // Movimiento normal del rey (1 casilla)
        if (dRow <= 1 && dCol <= 1) {
            return true;
        }

        // Enroque: el rey se mueve 2 casillas horizontalmente
        if (dRow == 0 && dCol == 2) {
            return true; // La validación completa se hace en canCastle()
        }

        return false;
    }
}

class Reina extends PiezasAjedrez {
    Reina(boolean isWhite) {
        super(isWhite);
    }

    @Override
    boolean isValidMove(int srcRow, int srcCol, int destRow, int destCol, char[][] board) {
        return (srcRow == destRow || srcCol == destCol ||
                Math.abs(destRow - srcRow) == Math.abs(destCol - srcCol));// la reina se mueve como torre o alfil
    }
}

class Torre extends PiezasAjedrez {
    Torre(boolean isWhite) {
        super(isWhite);
    }

    @Override
    boolean isValidMove(int srcRow, int srcCol, int destRow, int destCol, char[][] board) {
        return (srcRow == destRow || srcCol == destCol);// la torre se mueve en línea recta
    }
}

class Alfil extends PiezasAjedrez {
    Alfil(boolean isWhite) {
        super(isWhite);
    }

    @Override
    boolean isValidMove(int srcRow, int srcCol, int destRow, int destCol, char[][] board) {
        return Math.abs(destRow - srcRow) == Math.abs(destCol - srcCol);// la alfil se mueve en diagonal
    }
}

class Caballo extends PiezasAjedrez {
    Caballo(boolean isWhite) {
        super(isWhite);
    }

    @Override
    boolean isValidMove(int srcRow, int srcCol, int destRow, int destCol, char[][] board) {
        int dRow = Math.abs(destRow - srcRow);// diferencia absoluta de filas
        int dCol = Math.abs(destCol - srcCol);// diferencia absoluta de columnas
        return (dRow == 2 && dCol == 1) || (dRow == 1 && dCol == 2);// la caballo se mueve en forma de L
    }
}

class Peon extends PiezasAjedrez {
    Peon(boolean isWhite) {
        super(isWhite);
    }

    @Override
    boolean isValidMove(int srcRow, int srcCol, int destRow, int destCol, char[][] board) {
        int direction = isWhite ? -1 : 1;// dirección del movimiento según el color
        int startRow = isWhite ? 6 : 1;// fila inicial del peón según el color
        // Movimiento hacia delante
        if (srcCol == destCol && board[destRow][destCol] == ' ') {
            if (destRow - srcRow == direction)
                return true;
            // Primer movimiento: dos casillas
            if (srcRow == startRow && destRow - srcRow == 2 * direction && board[srcRow + direction][srcCol] == ' ')
                return true;
        }
        // Captura
        if (Math.abs(destCol - srcCol) == 1 && destRow - srcRow == direction && board[destRow][destCol] != ' ') {// si
                                                                                                                 // hay
                                                                                                                 // una
                                                                                                                 // pieza
                                                                                                                 // enemiga
                                                                                                                 // para
                                                                                                                 // capturar
            return true;// captura válida
        }
        return false;
    }
}
