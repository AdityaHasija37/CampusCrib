package com.campuscrib.ui;

import javax.swing.JComponent;

/** One page in the sidebar. Screens reload their data each time they're shown. */
public interface Screen {
    String label();

    JComponent view();

    void refresh();
}
