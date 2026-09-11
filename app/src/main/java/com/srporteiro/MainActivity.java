package com.srporteiro;

import android.app.Activity;
import android.app.role.RoleManager;
import android.os.Bundle;
import android.provider.Settings;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.Manifest;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.graphics.Color;
import android.view.Gravity;

import java.util.Map;

public class MainActivity extends Activity {

    private static final int PERMISSAO_CONTATOS = 100;
    private static final int PERMISSAO_NOTIFICACOES = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (android.os.Build.VERSION.SDK_INT >= 33 &&
                checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    PERMISSAO_NOTIFICACOES
            );
        }

        mostrarTela();
    }

    private void mostrarTela() {

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(40, 40, 40, 40);
        layout.setBackgroundColor(Color.rgb(10, 31, 68));


        ImageView logo = new ImageView(this);
        logo.setImageResource(R.drawable.logo_senhor_porteiro);
        LinearLayout.LayoutParams logoParams = new LinearLayout.LayoutParams(220, 220);
        logoParams.setMargins(0, 0, 0, 20);
        logo.setLayoutParams(logoParams);

        TextView titulo = new TextView(this);
        titulo.setText("SENHOR PORTEIRO");
        titulo.setTextSize(28);
        titulo.setTextColor(Color.WHITE);
        titulo.setGravity(Gravity.CENTER);

        TextView mensagem = new TextView(this);
        mensagem.setText(
                "\nProteção contra chamadas indesejadas\n\n" +
                "Se não está nos seus contatos, o Senhor Porteiro não deixa tocar."
        );
        mensagem.setTextSize(18);
        mensagem.setTextColor(Color.rgb(220, 230, 245));
        mensagem.setGravity(Gravity.CENTER);

        Button configurar = new Button(this);
        configurar.setText("CONFIGURAR SENHOR PORTEIRO");
        estilizarBotao(configurar);
        configurar.setOnClickListener(v -> configurarAplicativo());

        Button liberarNumero = new Button(this);
        liberarNumero.setText("🔓 LIBERAR ESTE NÚMERO");
        estilizarBotao(liberarNumero);
        liberarNumero.setOnClickListener(v -> mostrarLiberarNumero());

        Button liberarDesconhecidos = new Button(this);
        liberarDesconhecidos.setText("🌐 LIBERAR DESCONHECIDOS");
        estilizarBotao(liberarDesconhecidos);
        liberarDesconhecidos.setOnClickListener(v -> mostrarDuracoesDesconhecidos());

        Button excecoesAtivas = new Button(this);
        excecoesAtivas.setText("📋 EXCEÇÕES ATIVAS");
        estilizarBotao(excecoesAtivas);
        excecoesAtivas.setOnClickListener(v -> mostrarExcecoesAtivas());

        layout.addView(logo);
        layout.addView(titulo);
        layout.addView(mensagem);
        layout.addView(configurar);
        layout.addView(liberarNumero);
        layout.addView(liberarDesconhecidos);
        layout.addView(excecoesAtivas);

        setContentView(layout);
    }

    private void mostrarLiberarNumero() {

        LinearLayout layout = criarLayout();

        TextView titulo = criarTitulo("🔓 LIBERAR ESTE NÚMERO");

        TextView instrucao = new TextView(this);
        instrucao.setText(
                "Digite o número que deseja liberar temporariamente:"
        );
        instrucao.setTextSize(18);
        instrucao.setGravity(Gravity.CENTER);
        instrucao.setTextColor(Color.rgb(220, 230, 245));

        EditText numero = new EditText(this);
        numero.setHint("(XX) XXXXX-XXXX");
        numero.setTextColor(Color.WHITE);
        numero.setHintTextColor(Color.rgb(170, 190, 215));
        numero.setInputType(android.text.InputType.TYPE_CLASS_PHONE);

        layout.addView(titulo);
        layout.addView(instrucao);
        layout.addView(numero);

        adicionarDuracoesNumero(layout, numero);

        Button voltar = new Button(this);
        voltar.setText("VOLTAR");
        voltar.setTextColor(Color.WHITE);
        voltar.setBackgroundResource(R.drawable.botao_secundario);
        LinearLayout.LayoutParams voltarParams = new LinearLayout.LayoutParams(-1, -2);
        voltarParams.setMargins(0, 10, 0, 0);
        voltar.setLayoutParams(voltarParams);
        voltar.setOnClickListener(v -> mostrarTela());

        layout.addView(voltar);

        setContentView(layout);
    }

    private void adicionarDuracoesNumero(
            LinearLayout layout,
            EditText numero) {

        Button trinta = new Button(this);
        trinta.setText("30 MINUTOS");
        estilizarBotao(trinta);
        trinta.setOnClickListener(v ->
                liberarNumero(numero.getText().toString(), 30 * 60 * 1000L));

        Button umaHora = new Button(this);
        umaHora.setText("1 HORA");
        estilizarBotao(umaHora);
        umaHora.setOnClickListener(v ->
                liberarNumero(numero.getText().toString(), 60 * 60 * 1000L));

        Button quatroHoras = new Button(this);
        quatroHoras.setText("4 HORAS");
        estilizarBotao(quatroHoras);
        quatroHoras.setOnClickListener(v ->
                liberarNumero(numero.getText().toString(), 4 * 60 * 60 * 1000L));

        Button umDia = new Button(this);
        umDia.setText("1 DIA");
        estilizarBotao(umDia);
        umDia.setOnClickListener(v ->
                liberarNumero(numero.getText().toString(), 24 * 60 * 60 * 1000L));

        layout.addView(trinta);
        layout.addView(umaHora);
        layout.addView(quatroHoras);
        layout.addView(umDia);
    }

    private void liberarNumero(String numero, long duracao) {

        if (numero.trim().isEmpty()) {
            Toast.makeText(
                    this,
                    "Digite um número primeiro.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        Excecoes excecoes = new Excecoes(this);
        excecoes.liberarNumero(numero, duracao);

        Toast.makeText(
                this,
                "Número liberado temporariamente.",
                Toast.LENGTH_SHORT
        ).show();

        mostrarTela();
    }

    private void mostrarDuracoesDesconhecidos() {

        LinearLayout layout = criarLayout();

        TextView titulo = criarTitulo("🌐 LIBERAR DESCONHECIDOS");

        TextView aviso = new TextView(this);
        aviso.setText(
                "⚠️ ATENÇÃO\n" +
                "Durante esse período, números que não estão " +
                "nos seus contatos poderão ligar normalmente."
        );
        aviso.setTextSize(18);
        aviso.setGravity(Gravity.CENTER);
        aviso.setTextColor(Color.rgb(220, 230, 245));

        layout.addView(titulo);
        layout.addView(aviso);

        adicionarDuracoesDesconhecidos(layout);

        Button voltar = new Button(this);
        voltar.setText("VOLTAR");
        voltar.setTextColor(Color.WHITE);
        voltar.setBackgroundResource(R.drawable.botao_secundario);
        LinearLayout.LayoutParams voltarParams = new LinearLayout.LayoutParams(-1, -2);
        voltarParams.setMargins(0, 10, 0, 0);
        voltar.setLayoutParams(voltarParams);
        voltar.setOnClickListener(v -> mostrarTela());

        layout.addView(voltar);

        setContentView(layout);
    }

    private void adicionarDuracoesDesconhecidos(LinearLayout layout) {

        Button trinta = new Button(this);
        trinta.setText("30 MINUTOS");
        estilizarBotao(trinta);
        trinta.setOnClickListener(v ->
                liberarDesconhecidos(30 * 60 * 1000L));

        Button umaHora = new Button(this);
        umaHora.setText("1 HORA");
        estilizarBotao(umaHora);
        umaHora.setOnClickListener(v ->
                liberarDesconhecidos(60 * 60 * 1000L));

        Button quatroHoras = new Button(this);
        quatroHoras.setText("4 HORAS");
        estilizarBotao(quatroHoras);
        quatroHoras.setOnClickListener(v ->
                liberarDesconhecidos(4 * 60 * 60 * 1000L));

        Button umDia = new Button(this);
        umDia.setText("1 DIA");
        estilizarBotao(umDia);
        umDia.setOnClickListener(v ->
                liberarDesconhecidos(24 * 60 * 60 * 1000L));

        layout.addView(trinta);
        layout.addView(umaHora);
        layout.addView(quatroHoras);
        layout.addView(umDia);
    }

    private void liberarDesconhecidos(long duracao) {

        Excecoes excecoes = new Excecoes(this);
        excecoes.liberarDesconhecidos(duracao);

        Toast.makeText(
                this,
                "Desconhecidos liberados temporariamente.",
                Toast.LENGTH_SHORT
        ).show();

        mostrarTela();
    }

    private void mostrarExcecoesAtivas() {

        LinearLayout layout = criarLayout();

        TextView titulo = criarTitulo("📋 EXCEÇÕES ATIVAS");
        layout.addView(titulo);

        Excecoes excecoes = new Excecoes(this);

        Map<String, Long> numeros = excecoes.listarNumerosLiberados();
        long desconhecidosAte = excecoes.desconhecidosLiberadosAte();

        boolean temAlguma = false;

        for (Map.Entry<String, Long> item : numeros.entrySet()) {

            temAlguma = true;

            final String numero = item.getKey();

            TextView texto = new TextView(this);

            texto.setText(
                    "\n📞 " + formatarNumero(numero) +
                    "\n⏱️ " + formatarTempoRestante(item.getValue())
            );

            texto.setTextSize(18);
            texto.setTextColor(Color.rgb(220, 230, 245));
            texto.setGravity(Gravity.CENTER);

            layout.addView(texto);

            Button cancelar = new Button(this);
            cancelar.setText("❌ CANCELAR LIBERAÇÃO");
            cancelar.setTextColor(Color.WHITE);
            cancelar.setBackgroundResource(R.drawable.botao_secundario);
            LinearLayout.LayoutParams cancelarParams = new LinearLayout.LayoutParams(-1, -2);
            cancelarParams.setMargins(0, 10, 0, 0);
            cancelar.setLayoutParams(cancelarParams);

            cancelar.setOnClickListener(v -> {

                excecoes.cancelarNumero(numero);

                Toast.makeText(
                        this,
                        "Liberação cancelada",
                        Toast.LENGTH_SHORT
                ).show();

                mostrarExcecoesAtivas();
            });

            layout.addView(cancelar);
        }

        if (desconhecidosAte > 0) {

            temAlguma = true;

            TextView texto = new TextView(this);

            texto.setText(
                    "\n🌐 DESCONHECIDOS LIBERADOS" +
                    "\n⏱️ " + formatarTempoRestante(desconhecidosAte)
            );

            texto.setTextSize(18);
            texto.setTextColor(Color.rgb(220, 230, 245));
            texto.setGravity(Gravity.CENTER);

            layout.addView(texto);

            Button bloquearNovamente = new Button(this);
            bloquearNovamente.setText("🔒 BLOQUEAR NOVAMENTE");
            bloquearNovamente.setTextColor(Color.WHITE);
            bloquearNovamente.setBackgroundResource(R.drawable.botao_secundario);
            LinearLayout.LayoutParams bloquearParams = new LinearLayout.LayoutParams(-1, -2);
            bloquearParams.setMargins(0, 10, 0, 0);
            bloquearNovamente.setLayoutParams(bloquearParams);

            bloquearNovamente.setOnClickListener(v -> {

                excecoes.cancelarDesconhecidos();

                Toast.makeText(
                        this,
                        "Desconhecidos bloqueados novamente",
                        Toast.LENGTH_SHORT
                ).show();

                mostrarExcecoesAtivas();
            });

            layout.addView(bloquearNovamente);
        }

        if (!temAlguma) {

            TextView vazio = new TextView(this);

            vazio.setText(
                    "\nNenhuma exceção ativa no momento."
            );

            vazio.setTextSize(18);
            vazio.setTextColor(Color.rgb(220, 230, 245));
            vazio.setGravity(Gravity.CENTER);

            layout.addView(vazio);
        }

        Button voltar = new Button(this);
        voltar.setText("VOLTAR");
        voltar.setTextColor(Color.WHITE);
        voltar.setBackgroundResource(R.drawable.botao_secundario);
        LinearLayout.LayoutParams voltarParams = new LinearLayout.LayoutParams(-1, -2);
        voltarParams.setMargins(0, 10, 0, 0);
        voltar.setLayoutParams(voltarParams);
        voltar.setOnClickListener(v -> mostrarTela());

        layout.addView(voltar);

        setContentView(layout);
    }

    private String formatarTempoRestante(long ate) {

        long restante = ate - System.currentTimeMillis();

        if (restante <= 0) {
            return "Expirado";
        }

        long minutos = (restante + 59999) / 60000;

        if (minutos < 60) {
            return "Restam " + minutos + " min";
        }

        long horas = minutos / 60;
        long minRestantes = minutos % 60;

        if (minRestantes == 0) {
            return "Restam " + horas + " h";
        }

        return "Restam " + horas + " h " + minRestantes + " min";
    }

    private String formatarNumero(String numero) {

        if (numero == null) {
            return "";
        }

        if (numero.length() == 11) {
            return "(" +
                    numero.substring(0, 2) +
                    ") " +
                    numero.substring(2, 7) +
                    "-" +
                    numero.substring(7);
        }

        return numero;
    }

    private LinearLayout criarLayout() {

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(40, 40, 40, 40);
        layout.setBackgroundColor(Color.rgb(10, 31, 68));

        return layout;
    }

    private void estilizarBotao(Button botao) {
        botao.setTextColor(Color.WHITE);
        botao.setTextSize(16);
        botao.setAllCaps(false);
        botao.setBackgroundResource(R.drawable.botao_principal);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.setMargins(0, 6, 0, 6);
        botao.setLayoutParams(params);
    }

    private TextView criarTitulo(String texto) {


        ImageView logo = new ImageView(this);
        logo.setImageResource(R.drawable.logo_senhor_porteiro);
        LinearLayout.LayoutParams logoParams = new LinearLayout.LayoutParams(220, 220);
        logoParams.setMargins(0, 0, 0, 20);
        logo.setLayoutParams(logoParams);

        TextView titulo = new TextView(this);
        titulo.setText(texto);
        titulo.setTextSize(26);
        titulo.setTextColor(Color.WHITE);
        titulo.setGravity(Gravity.CENTER);

        return titulo;
    }

    private void configurarAplicativo() {

        if (android.os.Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.READ_CONTACTS)
                    != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(
                        new String[]{Manifest.permission.READ_CONTACTS},
                        PERMISSAO_CONTATOS
                );

                return;
            }
        }

        abrirConfiguracaoChamadas();
    }

    private void abrirConfiguracaoChamadas() {

        if (android.os.Build.VERSION.SDK_INT >= 29) {

            RoleManager roleManager =
                    (RoleManager) getSystemService(ROLE_SERVICE);

            if (roleManager != null &&
                    roleManager.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING)) {

                if (!roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)) {

                    Intent intent = roleManager.createRequestRoleIntent(
                            RoleManager.ROLE_CALL_SCREENING
                    );

                    startActivityForResult(intent, 1);

                } else {
                    Toast.makeText(
                            this,
                            "Senhor Porteiro já está ativado.",
                            Toast.LENGTH_SHORT
                    ).show();
                }

                return;
            }
        }

        Intent intent = new Intent(
                "android.telecom.action.CHANGE_DEFAULT_SCREENING_APP"
        );

        intent.putExtra(
                "android.telecom.extra.CHANGE_DEFAULT_SCREENING_APP_PACKAGE_NAME",
                getPackageName()
        );

        try {
            startActivity(intent);
        } catch (Exception e) {
            Intent fallback = new Intent(
                    Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS
            );
            startActivity(fallback);
        }
    }
}
