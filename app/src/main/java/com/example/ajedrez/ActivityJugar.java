package com.example.ajedrez;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import android.media.MediaPlayer;


public class ActivityJugar extends AppCompatActivity {

    // Fragments para el tablero y las estadísticas
    private TableroFragment fragmentTablero;
    private EstadisticasFragment fragmentEstadisticas;

    //Scripts del tablero y la IA
    private TableroAjedrez tableroAjedrez;
    private IAAjedrez iaAjedrez;

    // Opciones del juego
    private String nombreJugador="Usuario",MENSAJE_FIN_PARTIDA;
    private boolean puedeVibrar=true,mostrarMovimientos=true,musicaFondo=true;
    private float volumen=0.5f;
    private int duracionPartida=10;

    // Timer de la partida
    private CountDownTimer countDownTimer;
    private long tiempoRestante;  // tiempo que queda en milisegundos
    private boolean timerEnMarcha = false;

    // Elementos de la UI
    private Button btnRendirse,btnSalir;

    // Estadísticas de la partida
    public int reyesCazadosJugador,peonesCazadosJugador,alfilesCazadosJugador,
            reinasCazadasJugador,torresCazadasJugador,caballosCazadosJugador,
            piezasTotalesCazadasJugador,piezasTotalesCazadasEnemigo;

            // Elementos de la UI para estadísticas
    private TextView txtGanador,txtPiezasComidasMaquina,txtPiezasComidasJugador,txtTiempo,txtRey,txtPeon,txtAlfil,txtReina,txtTorre,txtCaballo,txtNombreJugador,txtTimer,txtTurno;

    // Música de fondo
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

// Cargar preferencias y guardarlas en variables
        SharedPreferences prefs = getSharedPreferences("OpcionesJuego", MODE_PRIVATE);
        puedeVibrar = prefs.getBoolean("vibracion", true);
        mostrarMovimientos=prefs.getBoolean("mostrarMovimientos",true);
        nombreJugador=prefs.getString("nombreJugador","Usuario");
        duracionPartida=prefs.getInt("tiempoPartida",10);
        musicaFondo=prefs.getBoolean("musicaFondo",true);
        int volInt=prefs.getInt("volumenGeneral",70);

        volumen = (float)volInt/100;

        setContentView(R.layout.activity_jugar);

        //Inicializar los fragments antes de usarlos
        fragmentTablero = new TableroFragment();
        fragmentEstadisticas = new EstadisticasFragment();



        // Mostrar el fragment inicial solo si es la primera vez
        if (savedInstanceState == null) {
            mostrarFragment(fragmentTablero);
        }

        IniciarUI();
        if(!musicaFondo)return;//si no hay música de fondo activada, salir
        IniciarMusica();
    }

    private void IniciarMusica(){
        // Crear MediaPlayer con la canción de fondo
        mediaPlayer = MediaPlayer.create(this, R.raw.musica_fondo);

        // Reproducir en bucle
        mediaPlayer.setLooping(true);

        // Establecer volumen inicial
        mediaPlayer.setVolume(volumen, volumen); // izquierda y derecha

        // Iniciar reproducción
        mediaPlayer.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();// Liberar recursos del MediaPlayer
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void IniciarUI(){// Inicializar elementos de la UI
        txtTimer=findViewById(R.id.txtTimer);
        txtTurno=findViewById(R.id.txtTurno);
        txtNombreJugador=findViewById(R.id.txtNombreJugador);
        txtNombreJugador.setText(nombreJugador);
        txtTurno.setText("Turno de las blancas");

        txtAlfil=findViewById(R.id.txtAlfil);
        txtPeon=findViewById(R.id.txtPeon);
        txtRey=findViewById(R.id.txtRey);
        txtReina=findViewById(R.id.txtReina);
        txtCaballo=findViewById(R.id.txtCaballo);
        txtTorre=findViewById(R.id.txtTorre);

        btnRendirse=findViewById(R.id.btnRendirse);

        btnRendirse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {// Al hacer clic en rendirse, acabar la partida
                AcabarPartida("TABLAS:Solicitado por el jugador.");
            }
        });


        iniciarTimer(duracionPartida);// Iniciar el timer con la duración seleccionada

    }

    private void mostrarFragment(Fragment fragment) {// Método para mostrar un fragmento
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.posFragmentosTablero, fragment)
                .commit();// Reemplaza el contenedor con el fragmento dado
    }

    // Método público para configurar el tablero desde el fragment
    public void ConfigurarTablero(TableroAjedrez tablero) {
        tableroAjedrez=tablero;
        if (tablero != null) {// Si el tablero no es nulo, configurar opciones
            tablero.setPuedeVibrar(puedeVibrar);
            tablero.setMostrarMovimientos(mostrarMovimientos);
        }
    }

    public void EstablecerTurnoTxt(String turnoDe){// Actualizar el texto del turno
        txtTurno.setText("Turno de las "+turnoDe);
    EstadisticasDurantePartida();
        if (turnoDe.equalsIgnoreCase("blancas")) {
            reanudarTimer();// Reanudar el timer si es el turno del jugador
        } else {
            pausarTimer();
            DarOrdenIA();// Dar orden a la IA si es su turno
        }

    }

    private void EstadisticasDurantePartida(){// Actualizar estadísticas durante la partida

        //Si las piezas cazadas son más de 0, mostrar el texto correspondiente
        txtTorre.setText(torresCazadasJugador>0 ? "Torres:"+torresCazadasJugador : "");
        txtAlfil.setText(alfilesCazadosJugador>0 ? "Alfiles:"+alfilesCazadosJugador : "");
        txtCaballo.setText(caballosCazadosJugador>0 ? "Caballos:"+caballosCazadosJugador : "");
        txtRey.setText(reyesCazadosJugador>0 ? "Rey:"+reyesCazadosJugador : "");
        txtReina.setText(reinasCazadasJugador>0 ? "Reina:"+reinasCazadasJugador : "");
        txtPeon.setText(peonesCazadosJugador>0 ? "Peones:"+peonesCazadosJugador : "");
    }

    private void DarOrdenIA(){
        // Esperar un momento para que sea más natural (opcional)
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                iaAjedrez.RealizarMovimiento();
                tableroAjedrez.invalidate(); // Actualizar el tablero
            }
        }, 500); // Espera 500ms antes de mover
    }

    // Llamas a este método para iniciar el timer (por primera vez)
    private void iniciarTimer(int duracionPartidaMinutos) {
        long tiempoEnMilisegundos = duracionPartida * 60 * 1000;
        tiempoRestante = tiempoEnMilisegundos;
        //inicia el timer llamando al método
        iniciarCountDown();
    }

    // Método que crea y empieza el CountDownTimer
    private void iniciarCountDown() {
        countDownTimer = new CountDownTimer(tiempoRestante, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tiempoRestante = millisUntilFinished; // guardamos lo que queda

                // Formatear el tiempo restante en mm:ss
                int minutos = (int) (millisUntilFinished / 1000) / 60;
                int segundos = (int) (millisUntilFinished / 1000) % 60;

                String tiempoFormateado = String.format("%02d:%02d", minutos, segundos);
                txtTimer.setText(tiempoFormateado);
            }

            @Override
            public void onFinish() {// Cuando el timer termina
                txtTimer.setText("00:00");// Mostrar 00:00 al acabar
                AcabarPartida("PIERDE EL JUGADOR: ACABÓ EL TIEMPO");// Acabar la partida
                timerEnMarcha = false;
            }
        }.start();

        timerEnMarcha = true;
    }

    // Método para pausar el timer
    private void pausarTimer() {
        if (countDownTimer != null && timerEnMarcha) {
            countDownTimer.cancel();
            timerEnMarcha = false;
        }
    }

    // Método para reanudar el timer
    private void reanudarTimer() {
        if (!timerEnMarcha) {
            if (countDownTimer != null) countDownTimer.cancel();
            iniciarCountDown();
        }
    }


    public void MostrarEstadisticasEnFragmento(){
        // Mostrar estadísticas en el fragmento de estadísticas
        txtGanador=findViewById(R.id.txtGanador);
        txtPiezasComidasJugador=findViewById(R.id.txtPiezasComidasJugador);
        txtPiezasComidasMaquina=findViewById(R.id.txtPiezasComidasMaquina);
        txtTiempo=findViewById(R.id.txtTiempo);
        btnSalir=findViewById(R.id.btnSalir);

        // Rellenar los textos con las estadísticas
        txtGanador.setText(MENSAJE_FIN_PARTIDA);
        txtPiezasComidasMaquina.setText("CPU logró eliminar:"+piezasTotalesCazadasEnemigo+" piezas del jugador.");
        txtPiezasComidasJugador.setText(nombreJugador+" logró eliminar:"+piezasTotalesCazadasJugador+" piezas de la CPU.");

        // Calcular el tiempo transcurrido
        long tiempoTranscurridoPartida =(duracionPartida*60*1000)-tiempoRestante;
        int minutos = (int) (tiempoTranscurridoPartida / 1000) / 60;
        int segundos = (int) (tiempoTranscurridoPartida / 1000) % 60;
        //Formatear el tiempo
        String tiempoFormateado = String.format("%02d:%02d", minutos, segundos);
        txtTiempo.setText("La partida ha durado: "+tiempoFormateado);

        // Configurar el botón de salir
        btnSalir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {// Al hacer clic en salir, volver al MainActivity
                Intent intent = new Intent(ActivityJugar.this, MainActivity.class);
                startActivity(intent);
            }
        });

    }


    public void AcabarPartida(String mensaje) {// Método para acabar la partida
        MENSAJE_FIN_PARTIDA=mensaje;
        pausarTimer();
        mostrarFragment(fragmentEstadisticas);
    }

    public void setIaAjedrez(IAAjedrez iaAjedrez){// Setter para la IA
        this.iaAjedrez=iaAjedrez;
    }

}