package com.example.ajedrez;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.*;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ActivityOpciones extends AppCompatActivity {

    // Elementos de la interfaz de usuario
    private EditText editNombreJugador;
    private SeekBar seekVolumenGeneral;
    private TextView txtVolumenGeneral;
    private Switch switchMusica, switchMovimientos, switchVibracion, switchModoOscuro;
    private RadioGroup radioGroupTiempo;
    private Button btnGuardarNombre, btnGuardarOpciones, btnVolverAtras;

    // Preferencias compartidas
    private SharedPreferences prefs;
    // Tiempo de partida guardado
    private int tiempoGuardado = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_opciones);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, systemBars.bottom);
            return insets;
        });

        IniciarUI();
    }

    private void IniciarUI(){
        //Inicializar elementos UI
        editNombreJugador = findViewById(R.id.editNombreJugador);
        btnGuardarNombre = findViewById(R.id.btnGuardarNombre);
        seekVolumenGeneral = findViewById(R.id.seekVolumenGeneral);
        txtVolumenGeneral = findViewById(R.id.txtVolumenGeneral);
        switchMusica = findViewById(R.id.switchMusica);
        switchMovimientos = findViewById(R.id.switchMovimientos);
        switchVibracion = findViewById(R.id.switchVibracion);
        switchModoOscuro = findViewById(R.id.switchModoOscuro);
        radioGroupTiempo = findViewById(R.id.radioGroupTiempo);
        btnGuardarOpciones = findViewById(R.id.btnGuardarOpciones);
        btnVolverAtras = findViewById(R.id.btnVolverAtras);

        //Cargar opciones de SharedPreferences
        prefs = getSharedPreferences("OpcionesJuego", MODE_PRIVATE);
        CargarOpciones();

        //Control del volumen
        seekVolumenGeneral.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                txtVolumenGeneral.setText(progress + "%");// actualizar el texto del volumen si fue cambiado el progress bar
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Guardar nombre
        btnGuardarNombre.setOnClickListener(v -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("nombreJugador", editNombreJugador.getText().toString());// guardar el nombre
            editor.apply();// guardar el nombre
            Toast.makeText(this, "Nombre guardado", Toast.LENGTH_SHORT).show();// mostrar mensaje
        });

        //Guardar todas las opciones
        btnGuardarOpciones.setOnClickListener(v -> {
            GuardarOpciones();
            Toast.makeText(this, "Opciones guardadas", Toast.LENGTH_SHORT).show();
            finish(); // Cierra la Activity
        });

        switchModoOscuro.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);// activar modo oscuro
                    } else {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);// desactivar modo oscuro
                    }
            // Guardar la preferencia
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("modoOscuro", isChecked);
            editor.apply();
        });

        // Volver sin guardar
        btnVolverAtras.setOnClickListener(v -> finish());
    }

    //cargar las preferencias
    private void CargarOpciones() {
        // Cargar preferencias guardadas
        editNombreJugador.setText(prefs.getString("nombreJugador", "Usuario"));
        seekVolumenGeneral.setProgress(prefs.getInt("volumenGeneral", 70));
        txtVolumenGeneral.setText(prefs.getInt("volumenGeneral", 70) + "%");

        // Ajustar switches según preferencias
        switchMusica.setChecked(prefs.getBoolean("musicaFondo", true));
        switchMovimientos.setChecked(prefs.getBoolean("mostrarMovimientos", true));
        switchVibracion.setChecked(prefs.getBoolean("vibracion", false));
        switchModoOscuro.setChecked(prefs.getBoolean("modoOscuro", true));

// Ajustar radio buttons según preferencias
        tiempoGuardado = prefs.getInt("tiempoPartida", 10);
// seleccionar el radio button correspondiente
        if (tiempoGuardado == 5) {// si es 5 minutos
            radioGroupTiempo.check(R.id.radio5min);// seleccionar 5 minutos
        } else if (tiempoGuardado == 10) {// si es 10 minutos
            radioGroupTiempo.check(R.id.radio10min);// seleccionar 10 minutos
        } else if (tiempoGuardado == 30) {// si es 30 minutos
            radioGroupTiempo.check(R.id.radio30min);// seleccionar 30 minutos
        }

    }

    // Guardar todo
    private void GuardarOpciones() {
        SharedPreferences.Editor editor = prefs.edit();
//Guardar todas las opciones en preferencias
        editor.putString("nombreJugador", editNombreJugador.getText().toString());
        editor.putInt("volumenGeneral", seekVolumenGeneral.getProgress());
        editor.putBoolean("musicaFondo", switchMusica.isChecked());
        editor.putBoolean("mostrarMovimientos", switchMovimientos.isChecked());
        editor.putBoolean("vibracion", switchVibracion.isChecked());
        editor.putBoolean("modoOscuro", switchModoOscuro.isChecked());


        int idSeleccionado = radioGroupTiempo.getCheckedRadioButtonId();
//proceso inverso para guardar el tiempo seleccionado
        if (idSeleccionado == R.id.radio5min) {
            tiempoGuardado = 5;
        } else if (idSeleccionado == R.id.radio10min) {
            tiempoGuardado = 10;
        } else if (idSeleccionado == R.id.radio30min) {
            tiempoGuardado = 30;
        }
        editor.putInt("tiempoPartida", tiempoGuardado);
        editor.apply();


        editor.apply();
    }
}
