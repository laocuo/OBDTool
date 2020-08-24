package com.laocuo.obdtool.widget;

import androidx.fragment.app.FragmentManager;

public class DialogManager {

    private static CommonDialog commonDialog;

    public static void show(CommonDialog dialog, FragmentManager fragmentManager) {
        if (fragmentManager == null || dialog == null) return;
        if (commonDialog != null && commonDialog.isVisible()) {
            commonDialog.dismiss();
        }
        dialog.show(fragmentManager, "CommonDialog");
        commonDialog = dialog;
    }
}
