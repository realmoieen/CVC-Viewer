package com.moieen.cvc.gui.cvcviewer;

import javax.swing.*;
import java.awt.*;
import java.io.PrintWriter;
import java.io.StringWriter;

public class ErrorDialogUtil {

    public static void showError(Component parentComponent,Exception exceptionError) {

        exceptionError.printStackTrace();

        String title = exceptionError.getClass().getName();

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exceptionError.printStackTrace(pw);
        String fullStackTrace = sw.toString();

        showError(parentComponent,fullStackTrace, title);
    }

    public static void showError(Component parentComponent,String stackTrace, String title) {

        JTextArea textArea = new JTextArea(20, 80);
        textArea.setText(stackTrace);
        textArea.setEditable(false);
        textArea.setCaretPosition(0); // Scroll to top
        textArea.setFont(new Font("Consolas", Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        JOptionPane.showMessageDialog(
                parentComponent,
                scrollPane,
                title,
                JOptionPane.ERROR_MESSAGE
        );
    }
}
