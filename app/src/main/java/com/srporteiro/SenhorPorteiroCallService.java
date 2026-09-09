package com.srporteiro;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.ContactsContract;
import android.telecom.Call;
import android.telecom.CallScreeningService;

public class SenhorPorteiroCallService extends CallScreeningService {

    @Override
    public void onScreenCall(Call.Details callDetails) {

        Uri handle = callDetails.getHandle();

        Excecoes excecoes = new Excecoes(this);

        if (handle == null) {
            if (excecoes.desconhecidosEstaoLiberados()) {
                permitirChamada(callDetails);
            } else {
                bloquearChamada(callDetails);
            }
            return;
        }

        String numero = handle.getSchemeSpecificPart();

        if (numero == null || numero.isEmpty()) {
            if (excecoes.desconhecidosEstaoLiberados()) {
                permitirChamada(callDetails);
            } else {
                bloquearChamada(callDetails);
            }
            return;
        }

        if (excecoes.numeroEstaLiberado(numero)) {
            permitirChamada(callDetails);
            return;
        }

        if (numeroEstaNosContatos(numero)) {
            permitirChamada(callDetails);
        } else if (excecoes.desconhecidosEstaoLiberados()) {
            permitirChamada(callDetails);
        } else {
            bloquearChamada(callDetails);
        }
    }

    private boolean numeroEstaNosContatos(String numero) {

        ContentResolver resolver = getContentResolver();

        Uri lookupUri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(numero)
        );

        try {
            android.database.Cursor cursor = resolver.query(
                    lookupUri,
                    new String[]{ContactsContract.PhoneLookup._ID},
                    null,
                    null,
                    null
            );

            if (cursor != null) {
                boolean encontrado = cursor.moveToFirst();
                cursor.close();
                return encontrado;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    private void permitirChamada(Call.Details callDetails) {

        CallResponse.Builder resposta = new CallResponse.Builder();

        resposta.setDisallowCall(false);
        resposta.setRejectCall(false);

        respondToCall(callDetails, resposta.build());
    }

    private void bloquearChamada(Call.Details callDetails) {

        CallResponse.Builder resposta = new CallResponse.Builder();

        resposta.setDisallowCall(true);
        resposta.setRejectCall(true);

        if (android.os.Build.VERSION.SDK_INT >= 29) {
            resposta.setSkipNotification(true);
        }

        respondToCall(callDetails, resposta.build());
    }
}
