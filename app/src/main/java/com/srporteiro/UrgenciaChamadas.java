package com.srporteiro;

import android.content.Context;
import android.content.SharedPreferences;

public class UrgenciaChamadas {

    private static final String PREFS = "urgencia_chamadas";
    private static final long JANELA = 5 * 60 * 1000L;

    private final SharedPreferences prefs;

    public UrgenciaChamadas(Context context) {
        prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public int registrarTentativa(String numero) {

        String chaveContagem = "contagem_" + numero;
        String chaveUltima = "ultima_" + numero;

        long agora = System.currentTimeMillis();
        long ultima = prefs.getLong(chaveUltima, 0);
        int contagem = prefs.getInt(chaveContagem, 0);

        if (agora - ultima > JANELA) {
            contagem = 0;
        }

        contagem++;

        prefs.edit()
                .putInt(chaveContagem, contagem)
                .putLong(chaveUltima, agora)
                .apply();

        return contagem;
    }
}
