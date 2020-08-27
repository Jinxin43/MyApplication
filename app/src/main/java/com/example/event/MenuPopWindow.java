package com.example.event;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.DingTu.Base.ICallback;
import com.DingTu.Base.PubVar;
import com.DingTu.Base.Tools;
import com.example.event.db.xEntity.RoundExamineEntity;
import com.example.event.manager.PatrolManager;
import com.example.event.manager.UploadMananger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import jxl.Workbook;
import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.format.UnderlineStyle;
import jxl.format.VerticalAlignment;
import jxl.write.Label;
import jxl.write.NumberFormats;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;


class MenuPopWindow extends PopupWindow {
    private final Context mContetx;
    private final View conentView;
    private final View mExport;
    private final View mUpload;
    private boolean hasNext;
    private String mPhoto;
    private boolean hasPhoto;

    public MenuPopWindow(Activity context) {
        this.mContetx = context;
        LayoutInflater inflater = (LayoutInflater) mContetx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        conentView = inflater.inflate(R.layout.menu_popup_window, null);
        mExport = conentView.findViewById(R.id.ll_export);
        mExport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Export();
                close();
            }
        });
        mUpload = conentView.findViewById(R.id.ll_upload);
        mUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                upload();
                close();
            }
        });
        int h = context.getWindowManager().getDefaultDisplay().getHeight();
        int w = context.getWindowManager().getDefaultDisplay().getWidth();
        this.setContentView(conentView);
        // 设置SelectPicPopupWindow弹出窗体的宽
        this.setWidth(w / 3 - 30);
        // 设置SelectPicPopupWindow弹出窗体的高
        this.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        // 设置SelectPicPopupWindow弹出窗体可点击
        this.setFocusable(true);
        this.setOutsideTouchable(false);
        // 实例化一个ColorDrawable颜色为半透明
        ColorDrawable dw = new ColorDrawable(0000000000);
        // 点back键和其他地方使其消失,设置了这个才能触发OnDismisslistener ，设置其他控件变化等操作
        this.setBackgroundDrawable(dw);

    }

    private void upload() {
        uploadEventOneByOne(0);
        UploadPicture(0);

    }

    private void UploadPicture(final int index) {
        List<RoundExamineEntity> mlist = PatrolManager.getInstance().getExam();
        if (mlist != null && mlist.size() > 0) {
            hasNext = index < mlist.size();
            if (hasNext) {
                mPhoto = mlist.get(index).getPhotoList();
                UploadPhoto(0, mlist.get(index));
                UploadPicture(index + 1);

            }

        }
    }

    private void UploadPhoto(final int i, final RoundExamineEntity entity) {
        if (mPhoto != null && mPhoto.split(",").length > 0) {
            hasPhoto = i < mPhoto.split(",").length;
            if (hasPhoto) {
                UploadMananger.getInstance().uploadPhotoes(entity, entity.getPhotoList().split(",")[i], new ICallback() {
                    @Override
                    public void OnClick(String Str, Object ExtraStr) {
                        UploadPhoto(i + 1, entity);

                    }
                });
            }

        }

    }

    private void uploadEventOneByOne(final int index) {
        List<RoundExamineEntity> mlist = PatrolManager.getInstance().getExam();
        if (mlist != null && mlist.size() > 0) {
           hasNext = index < mlist.size();
            if(hasNext) {
                RoundExamineEntity entity = mlist.get(index);
                UploadMananger.getInstance().uploadEvent(entity, new ICallback() {
                    @Override
                    public void OnClick(String Str, Object ExtraStr) {
                        uploadEventOneByOne(index + 1);
                    }
                });
            }
        }

    }

    private void close() {
        this.dismiss();
    }

    private void Export() {
        try {
            List<RoundExamineEntity> mlist = PatrolManager.getInstance().getExam();
            if (mlist != null && mlist.size() > 0) {
                if (!Tools.ExistFile(PubVar.m_SysAbsolutePath + "/Data/导出/")) {
                    (new File(PubVar.m_SysAbsolutePath + "/Data/导出/")).mkdirs();
                } else {
                    deleteDir(new File(PubVar.m_SysAbsolutePath + "/Data/导出/"));
                }
                for (int i = 0; i < mlist.size(); i++) {
                    if (!Tools.ExistFile(PubVar.m_SysAbsolutePath + "/Data/导出/" + i + "/")) {
                        (new File(PubVar.m_SysAbsolutePath + "/Data/导出/" + i + "/")).mkdirs();
                    }
                    InputStream inputStream = mContetx.getResources().openRawResource(R.raw.expot_table);// 将raw中的test.db放入输入流中
                    FileOutputStream fileOutputStream = new FileOutputStream(
                            PubVar.m_SysAbsolutePath + "/Data/导出/" + i + "/"
                                    + mlist.get(i).getOrderNumber() + ".xls");// 将新的文件放入输出流中
                    byte[] buff = new byte[8192];
                    int len = 0;
                    while ((len = inputStream.read(buff)) > 0) {
                        fileOutputStream.write(buff, 0, len);
                    }
                    fileOutputStream.close();
                    inputStream.close();
                    getExcelContent(i, mlist.get(i));
                    exportPhoto(i, mlist.get(i).getPhotoList(), mlist.get(i).getPhotoOrderNum());
                }
                String ExportPath = PubVar.m_SysAbsolutePath + "/Data/导出";
                Tools.ShowMessageBox(mContetx, "数据成功导出！\r\n\r\n位于：【" + ExportPath + "】目录下");

            } else {
                Toast.makeText(mContetx, "当前没有数据，无法导出!", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
            Tools.ShowMessageBox("导出文件失败" + e.getMessage());
            return;
        }


    }

    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            //递归删除目录中的子目录
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        // 目录此时为空，可以删除
        return dir.delete();
    }

    private void exportPhoto(int i, String photolist, String orderlist) {
        if (photolist != null && !TextUtils.isEmpty(photolist) && orderlist != null && !TextUtils.isEmpty(orderlist)) {
            String[] mPhoto = photolist.split(",");
            String[] mOderlist = orderlist.split(",");
            for (int j = 0; j < mPhoto.length; j++) {
                String oldPath = PubVar.m_SysAbsolutePath + "/Photo/" + mPhoto[j];
                String newFileName = PubVar.m_SysAbsolutePath + "/Data/导出/" + i + "/" + mOderlist[j] + ".jpg";
                CopyFile(oldPath, newFileName);
            }
        }
    }


    private int CopyFile(String fromFile, String toFile) {
        try {
            File dest = new File(toFile);
            if (dest.exists()) {
                dest.delete();
            }
            dest.createNewFile();

            InputStream fosfrom = new FileInputStream(fromFile);
            OutputStream fosto = new FileOutputStream(dest);
            int size = fosfrom.available();
            byte bt[] = new byte[size];
            int c;
            while ((c = fosfrom.read(bt)) > 0) {
                fosto.write(bt, 0, c);
            }
            fosfrom.close();
            fosto.close();
            return 0;

        } catch (Exception ex) {
            Log.e("CopyFile", ex.getMessage());
            // Tools.ShowMessageBox(ex.getMessage());
        }

        return -1;
    }


    private void getExcelContent(int i, RoundExamineEntity entity) {
        try {
            Workbook wb = Workbook.getWorkbook(new File(PubVar.m_SysAbsolutePath + "/Data/导出/" + i + "/" + entity.getOrderNumber() + ".xls"));
            WritableWorkbook book = Workbook.createWorkbook(new File(PubVar.m_SysAbsolutePath + "/Data/导出/" + i + "/" + entity.getOrderNumber() + ".xls"), wb);
            WritableSheet sheet0 = book.getSheet(0);
            Label labelConte1 = new Label(11, 4, entity.getOrderNumber(), getBodyCellStyle());
            sheet0.addCell(labelConte1);

            Label labelConte2 = new Label(1, 5, entity.getXian(), getBodyCellStyle());
            sheet0.addCell(labelConte2);

            Label labelConte3 = new Label(5, 5, entity.getSheng(), getBodyCellStyle());
            sheet0.addCell(labelConte3);

            Label labelConte4 = new Label(9, 5, entity.getAddress(), getBodyCellStyle());
            sheet0.addCell(labelConte4);

            Label labelConte5 = new Label(1, 7, entity.getExmainPerson(), getBodyCellStyle());
            sheet0.addCell(labelConte5);

            Label labelConte6 = new Label(5, 7, entity.getFillPerson(), getBodyCellStyle());
            sheet0.addCell(labelConte6);

            Label labelConte7 = new Label(9, 7, entity.getExamineDate(), getBodyCellStyle());
            sheet0.addCell(labelConte7);

            Label labelConte8 = new Label(1, 9, entity.getZhongCName(), getBodyCellStyle());
            sheet0.addCell(labelConte8);

            Label labelConte9 = new Label(5, 9, entity.getShuCName(), getBodyCellStyle());
            sheet0.addCell(labelConte9);

            Label labelConte10 = new Label(9, 9, entity.getKeCName(), getBodyCellStyle());
            sheet0.addCell(labelConte10);

            Label labelConte11 = new Label(1, 11, entity.getZhongLaName(), getBodyCellStyle());
            sheet0.addCell(labelConte11);

            Label labelConte12 = new Label(5, 11, entity.getShuLaName(), getBodyCellStyle());
            sheet0.addCell(labelConte12);

            Label labelConte13 = new Label(9, 11, entity.getKeLaName(), getBodyCellStyle());
            sheet0.addCell(labelConte13);

            Label labelConte14 = new Label(1, 13, entity.getLatitude() + "", getBodyCellStyle());
            sheet0.addCell(labelConte14);

            Label labelConte15 = new Label(5, 13, entity.getLongtitude() + "", getBodyCellStyle());
            sheet0.addCell(labelConte15);

            Label labelConte16 = new Label(9, 13, entity.getHight() + "", getBodyCellStyle());
            sheet0.addCell(labelConte16);

            Label labelConte17 = new Label(1, 15, entity.getPoXiang(), getBodyCellStyle());
            sheet0.addCell(labelConte17);

            Label labelConte18 = new Label(5, 15, entity.getPoWei(), getBodyCellStyle());
            sheet0.addCell(labelConte18);

            Label labelConte19 = new Label(9, 15, entity.getPoDu(), getBodyCellStyle());
            sheet0.addCell(labelConte19);

            Label labelConte20 = new Label(1, 17, entity.getTreeHight() + "", getBodyCellStyle());
            sheet0.addCell(labelConte20);

            Label labelConte21 = new Label(5, 17, entity.getXiongJin() + "", getBodyCellStyle());
            sheet0.addCell(labelConte21);

            Label labelConte22 = new Label(9, 17, entity.getGuanFu() + "", getBodyCellStyle());
            sheet0.addCell(labelConte22);

            Label labelConte23 = new Label(1, 19, entity.getZhiHight() + "", getBodyCellStyle());
            sheet0.addCell(labelConte23);

            Label labelConte24 = new Label(5, 19, entity.getXuji() + "", getBodyCellStyle());
            sheet0.addCell(labelConte24);

            Label labelConte25 = new Label(9, 19, entity.getTuType(), getBodyCellStyle());
            sheet0.addCell(labelConte25);

            Label labelConte26 = new Label(1, 21, entity.getImportDescribe(), getBodyCellStyle());
            sheet0.addCell(labelConte26);

            Label labelConte27 = new Label(1, 25, entity.getPhotoOrderNum(), getBodyCellStyle());
            sheet0.addCell(labelConte27);
            Label labelConte28 = new Label(5, 25, entity.getTakePerson(), getBodyCellStyle());
            sheet0.addCell(labelConte28);
            Label labelConte29 = new Label(9, 25, entity.getTakeDate(), getBodyCellStyle());
            sheet0.addCell(labelConte29);

            Label labelConte30 = new Label(1, 27, entity.getStateDescribe(), getBodyCellStyle());
            sheet0.addCell(labelConte30);

            book.write();
            book.close();
            wb.close();

        } catch (Exception ex) {
            ex.printStackTrace();
            Tools.ShowMessageBox("导出表格失败" + ex.getMessage());
        }

    }


    public WritableCellFormat getBodyCellStyle() {

        /*
         * WritableFont.createFont("宋体")：设置字体为宋体 10：设置字体大小
         * WritableFont.BOLD:设置字体加粗（BOLD：加粗 NO_BOLD：不加粗） false：设置非斜体
         * UnderlineStyle.NO_UNDERLINE：没有下划线
         */
        WritableFont font = new WritableFont(WritableFont.createFont("宋体"), 11, WritableFont.NO_BOLD, false,
                UnderlineStyle.NO_UNDERLINE);

        WritableCellFormat headerFormat = new WritableCellFormat(NumberFormats.TEXT);
        try {
            // 添加字体设置
            headerFormat.setFont(font);
            // 设置表头表格边框样式
            // 整个表格线为粗线、黑色
            headerFormat.setBorder(Border.ALL, BorderLineStyle.THIN, Colour.BLACK);
            // 表头内容水平居中显示
            headerFormat.setAlignment(Alignment.CENTRE);
            headerFormat.setVerticalAlignment(VerticalAlignment.CENTRE);
        } catch (WriteException e) {
            System.out.println("内容单元格样式设置失败！");
        }
        return headerFormat;
    }


    public void showPopupWindow(View parent) {
        if (!this.isShowing()) {
            // 以下拉方式显示popupwindow
            this.showAsDropDown(parent);
        } else {
            this.dismiss();
        }
    }

}
