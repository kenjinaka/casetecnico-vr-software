package com.example.caseTecnico;

import com.example.caseTecnico.view.PedidosSwingApp;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.swing.*;

@SpringBootApplication
public class CaseTecnicoApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(CaseTecnicoApplication.class, args);
    }

    @Override
    public void run(String... args) {
        if (!java.awt.GraphicsEnvironment.isHeadless()) {
            SwingUtilities.invokeLater(() -> new PedidosSwingApp().setVisible(true));
        } else {
            System.out.println("Erro ao inicializar o GUI");
        }
    }
}