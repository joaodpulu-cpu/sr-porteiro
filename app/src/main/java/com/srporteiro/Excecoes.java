package com.srporteiro;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.LinkedHashMap;
import java.util.Map;

public class Excecoes {

    private static final String PREFS = "senhor_porteiro_excecoes";
    private static final String PREFIXO_NUMERO = "numero_";
    private static final String CHAVE_TODOS_ATE = "todos_ate";

    private final SharedPreferences prefs;

    public Excecoes(Context context) {
        prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public void liberarNumero(String numero, long duracaoMs) {

        String normalizado = normalizar(numero);

        if (normalizado.isEmpty()) {
            return;
        }

        long ate = System.currentTimeMillis() + duracaoMs;

        prefs.edit()
                .putLong(PREFIXO_NUMERO + normalizado, ate)
                .apply();
    }

    public void liberarDesconhecidos(long duracaoMs) {

        prefs.edit()
                .putLong(
                        CHAVE_TODOS_ATE,
                        System.currentTimeMillis() + duracaoMs
                )
                .apply();
    }

    public boolean numeroEstaLiberado(String numero) {

        String recebido = normalizar(numero);

        if (recebido.isEmpty()) {
            return false;
        }

        long agora = System.currentTimeMillis();

        Map<String, ?> todos = prefs.getAll();

        for (Map.Entry<String, ?> item : todos.entrySet()) {

            String chave = item.getKey();

            if (!chave.startsWith(PREFIXO_NUMERO)) {
                continue;
            }

            Object valor = item.getValue();

            if (!(valor instanceof Long)) {
                continue;
            }

            long ate = (Long) valor;

            if (ate <= agora) {
                prefs.edit().remove(chave).apply();
                continue;
            }

            String salvo = chave.substring(PREFIXO_NUMERO.length());

            if (numerosSaoIguais(salvo, recebido)) {
                return true;
            }
        }

        return false;
    }

    public boolean desconhecidosEstaoLiberados() {

        long agora = System.currentTimeMillis();
        long ate = prefs.getLong(CHAVE_TODOS_ATE, 0);

        if (ate > agora) {
            return true;
        }

        if (ate != 0 && ate <= agora) {
            prefs.edit()
                    .remove(CHAVE_TODOS_ATE)
                    .apply();
        }

        return false;
    }

    public Map<String, Long> listarNumerosLiberados() {

        Map<String, Long> ativos = new LinkedHashMap<>();
        Map<String, ?> todos = prefs.getAll();

        long agora = System.currentTimeMillis();

        for (Map.Entry<String, ?> item : todos.entrySet()) {

            String chave = item.getKey();

            if (!chave.startsWith(PREFIXO_NUMERO)) {
                continue;
            }

            Object valor = item.getValue();

            if (!(valor instanceof Long)) {
                continue;
            }

            long ate = (Long) valor;

            if (ate <= agora) {
                prefs.edit().remove(chave).apply();
                continue;
            }

            String numero = chave.substring(PREFIXO_NUMERO.length());

            ativos.put(numero, ate);
        }

        return ativos;
    }

    public long desconhecidosLiberadosAte() {

        long ate = prefs.getLong(CHAVE_TODOS_ATE, 0);

        if (ate <= System.currentTimeMillis()) {

            if (ate != 0) {
                prefs.edit()
                        .remove(CHAVE_TODOS_ATE)
                        .apply();
            }

            return 0;
        }

        return ate;
    }

    public void cancelarNumero(String numero) {
        String normalizado = normalizar(numero);
        if (normalizado.isEmpty()) {
            return;
        }
        prefs.edit().remove(PREFIXO_NUMERO + normalizado).apply();
    }

    public void cancelarDesconhecidos() {
        prefs.edit().remove(CHAVE_TODOS_ATE).apply();
    }

    private boolean numerosSaoIguais(String a, String b) {

        if (a == null || b == null) {
            return false;
        }

        if (a.equals(b)) {
            return true;
        }

        if (a.length() >= 11 && b.length() >= 11) {

            String fimA = a.substring(a.length() - 11);
            String fimB = b.substring(b.length() - 11);

            return fimA.equals(fimB);
        }

        return false;
    }

    private String normalizar(String numero) {

        if (numero == null) {
            return "";
        }

        return numero.replaceAll("[^0-9]", "");
    }
}
