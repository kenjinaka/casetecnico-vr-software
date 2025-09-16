package com.example.caseTecnico.view;

import com.example.caseTecnico.controller.in.dto.PedidoDto;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.swing.*;
import java.awt.*;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.UUID;

public class PedidosSwingApp extends JFrame {

    private JTextField produtoField;
    private JTextField quantidadeField;
    private JButton enviarButton;
    private JTextArea statusArea;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();;
    private final String backendUrl = "http://localhost:8080/api/pedidos";

    public PedidosSwingApp() {
        super("Pedidos");
        initComponents();
    }

    private void initComponents() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JPanel inputPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        inputPanel.add(new JLabel("Produto:"));
        produtoField = new JTextField();
        inputPanel.add(produtoField);

        inputPanel.add(new JLabel("Quantidade:"));
        quantidadeField = new JTextField();
        inputPanel.add(quantidadeField);

        add(inputPanel, BorderLayout.NORTH);

        enviarButton = new JButton("Enviar Pedido");
        add(enviarButton, BorderLayout.CENTER);

        statusArea = new JTextArea();
        statusArea.setEditable(false);
        add(new JScrollPane(statusArea), BorderLayout.SOUTH);

        enviarButton.addActionListener(e -> enviarPedido());
    }

    private void enviarPedido() {
        String produto = produtoField.getText().trim();
        String quantidadeText = quantidadeField.getText().trim();

        if (produto.isEmpty() || quantidadeText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Preencha todos os campos!", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int quantidade;
        try {
            quantidade = Integer.parseInt(quantidadeText);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Quantidade deve ser um número inteiro!", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        PedidoDto pedido = new PedidoDto(UUID.randomUUID(), produto, quantidade, LocalDateTime.now());

        // Status inicial
        statusArea.append(String.format("ID: %s | Status: ENVIADO, AGUARDANDO PROCESSO%n", pedido.getId()));

        produtoField.setText("");
        quantidadeField.setText("");

        // Envio assíncrono via POST
        new Thread(() -> {
            try {
                URL url = new URL(backendUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                String json = objectMapper.writeValueAsString(pedido);
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(json.getBytes());
                    os.flush();
                }

                int responseCode = conn.getResponseCode();
                SwingUtilities.invokeLater(() -> {
                    if (responseCode == 200 || responseCode == 201) {
                        statusArea.append(String.format("ID: %s - Status: ENVIADO COM SUCESSO%n", pedido.getId()));
                    } else {
                        statusArea.append(String.format("ID: %s - Status: ERRO AO ENVIAR%n", pedido.getId()));
                    }
                });

                conn.disconnect();
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() ->
                        statusArea.append(String.format("ID: %s - Status: ERRO AO ENVIAR%n", pedido.getId()))
                );
                ex.printStackTrace();
            }
        }).start();
    }
}
