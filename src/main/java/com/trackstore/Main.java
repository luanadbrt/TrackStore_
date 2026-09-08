package com.trackstore;

import com.trackstore.dao.HibernateUtil;
import com.trackstore.view.TelaLogin;
import com.trackstore.view.UI;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        UI.prepararLookAndFeel();

        // Fecha a SessionFactory (e o pool de conexões) ao encerrar a aplicação.
        // Antes o método encerrar() existia mas nunca era chamado por ninguém.
        Runtime.getRuntime().addShutdownHook(new Thread(HibernateUtil::encerrar));

        SwingUtilities.invokeLater(() -> new TelaLogin().setVisible(true));
    }
}
