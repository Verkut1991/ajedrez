package com.example.ajedrez;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class TableroFragment extends Fragment {

    // ViewModel del Tablero
    private TableroViewModel mViewModel;
    //Scripts del tablero, lógica e IA
    private TableroAjedrez tableroAjedrez;
    private LogicaAjedrez logicaAjedrez;
    private IAAjedrez iaAjedrez;

    public static TableroFragment newInstance() {
        return new TableroFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tablero, container, false);
    }

    // Inicialización después de que la vista ha sido creada
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
// Referencia al TableroAjedrez en el layout
        tableroAjedrez = view.findViewById(R.id.tableroAjedrez);

        // Obtener las preferencias de la Activity
        if (getActivity() instanceof ActivityJugar) {
            // Configurar el tablero y la lógica de ajedrez
            ActivityJugar activity = (ActivityJugar) getActivity();
            activity.ConfigurarTablero(tableroAjedrez);
            logicaAjedrez = new LogicaAjedrez(activity);
            tableroAjedrez.setLogicaAjedrez(logicaAjedrez);
            iaAjedrez = new IAAjedrez(logicaAjedrez);
            activity.setIaAjedrez(iaAjedrez);
        }
    }

}