package com.yitu.pictureshare.bean;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.sql.Time;

public class SaveImgTools {

    /**
     *@time
     *@param
     *@description 工具类，负责保存图片到相册
     */
    public static void SaveImageToSysAlbum(Context context, ImageView imageView) {

        BitmapDrawable bmpDrawable = (BitmapDrawable) imageView.getDrawable();
        Bitmap bitmap = bmpDrawable.getBitmap();

        if (bitmap==null){
            return;
        }
        MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "HaiGouShareCode", "");
        //如果是4.4及以上版本
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            String fileName = System.currentTimeMillis() + ".png";
            File mPhotoFile = new File(fileName);
            Intent mediaScanIntent = new Intent(
                    Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri contentUri = Uri.fromFile(mPhotoFile);
            mediaScanIntent.setData(contentUri);
            context.sendBroadcast(mediaScanIntent);

        } else {
            context.sendBroadcast(new Intent(
                    Intent.ACTION_MEDIA_MOUNTED,
                    Uri.parse("file://"
                            + Environment.getExternalStorageDirectory())));
        }
        Toast.makeText(context,"已保存到相册",Toast.LENGTH_SHORT).show();
    }

    public interface HavePressSaveImgDialogSure {
        void havePressSure(int resId);
    }
}
