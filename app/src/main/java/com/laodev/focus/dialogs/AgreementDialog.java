package com.laodev.focus.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.laodev.focus.R;

public class AgreementDialog extends Dialog {

    public AgreementDialog(@NonNull Context context, Bitmap qrBitmap) {
        super(context);

        setContentView(R.layout.dialog_agreement);

        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        setTitle(null);
        setCanceledOnTouchOutside(false);

        initUIView(qrBitmap);
    }

    private void initUIView(Bitmap qrBitmap) {
        ImageView imgView = findViewById(R.id.img_myqr_image);
        imgView.setImageBitmap(qrBitmap);
    }

}
