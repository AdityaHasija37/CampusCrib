package com.campuscrib.ui;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/** Calls one action for any text change. */
public record SimpleDocumentListener(Runnable onChange) implements DocumentListener {
    @Override
    public void insertUpdate(DocumentEvent e) {
        onChange.run();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        onChange.run();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        onChange.run();
    }
}
