package com.example.ajedrez;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class MainActivity extends AppCompatActivity {
    // Vista del tablero de ajedrez y botones
    private TableroAjedrez chessBoardView;
    private Button botonJugar, botonOpciones;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Configurar modo oscuro según preferencias
        SharedPreferences prefs = getSharedPreferences("OpcionesJuego", MODE_PRIVATE);
        boolean modoOscuro = prefs.getBoolean("modoOscuro", true);
        AppCompatDelegate.setDefaultNightMode(
                modoOscuro ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Inicializar botones
        botonJugar = findViewById(R.id.jugar);
        botonOpciones = findViewById(R.id.opciones);

        // Configurar listeners para los botones
        botonJugar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {//al hacer clic en "Jugar", iniciar ActivityJugar
                Intent intent = new Intent(MainActivity.this, ActivityJugar.class);
                startActivity(intent);
            }
        });

        botonOpciones.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {//al hacer clic en "Opciones", iniciar ActivityOpciones
                Intent intent = new Intent(MainActivity.this, ActivityOpciones.class);
                startActivity(intent);
            }
        });

    }
}