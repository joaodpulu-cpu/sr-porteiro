package com.srporteiro;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.ContactsContract;
import android.telecom.Call;
import android.telecom.CallScreeningService;
import android.util.Log;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Notification;
import android.os.Build;

public class SenhorPorteiroCallService extends CallScreeningService {

    @Override
    public void onScreenCall(Call.Details callDetails) {

        if (Build.VERSION.SDK_INT >= 29 &&
                callDetails.getCallDirection() == Call.Details.DIRECTION_OUTGOING) {
            permitirChamada(callDetails);
            return;
        }

        Uri handle = callDetails.getHandle();
        Log.d("SrPorteiroTESTE", "handle recebido = " + handle + " | presentation = " + callDetails.getHandlePresentation());

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
            UrgenciaChamadas urgencia = new UrgenciaChamadas(this);
            int tentativas = urgencia.registrarTentativa(numero);
            Log.d("SrPorteiroURGENCIA", "numero = " + numero + " | tentativas = " + tentativas);
            if (tentativas >= 2) {
                avisarUrgencia(numero, tentativas);
            }
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

    private void avisarUrgencia(String numero, int tentativas) {

        String canalId = "urgencia_chamadas";

        NotificationManager manager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (manager == null) {
            return;
        }

        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel canal = new NotificationChannel(
                    canalId,
                    "Possíveis urgências",
                    NotificationManager.IMPORTANCE_HIGH
            );
            canal.setDescription("Avisos quando um número desconhecido insiste em ligar.");
            manager.createNotificationChannel(canal);
        }

        String titulo;
        String texto;

        if (tentativas >= 3) {
            titulo = "⚠️ Possível urgência";
            texto = "Este número tentou falar com você 3 vezes seguidas. Considere retornar a ligação ou liberar temporariamente.";
        } else {
            titulo = "Possível chamada importante";
            texto = "Este número ligou 2 vezes seguidas. Pode ser algo importante.";
        }

        Notification.Builder builder;

        if (Build.VERSION.SDK_INT >= 26) {
            builder = new Notification.Builder(this, canalId);
        } else {
            builder = new Notification.Builder(this);
        }

        builder.setSmallIcon(android.R.drawable.sym_action_call)
                .setContentTitle(titulo)
                .setContentText(texto)
                .setStyle(new Notification.BigTextStyle().bigText(texto))
                .setAutoCancel(true);

        manager.notify(numero.hashCode(), builder.build());
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
