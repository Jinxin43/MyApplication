package com.DingTu.CoordinateSystem;

import com.DingTu.Cargeometry.Coordinate;

/**
 * Created by Dingtu2 on 2017/5/31.
 */

public class Project_UTM {

    public static Coordinate UTMBLToXY(double L1, double B1, CoorSystem coorSystem)
    {
        if ((L1 == 0) || (B1 == 0))
            return null;
        double L0 = coorSystem.GetCenterMeridian() * Math.PI / 180;  //中央经线(弧度)
        double K = 1;// 0.9996;                     //投影比例因子
        double FE = 500000L;               //东经偏移
        double FN = 0;                    //北纬偏移
        double a = 6378137;               //椭球长半轴
        double b = 6356752.3142451793;    //椭球短半轴
        //double f = (a - b) / a;           //扁率
        double de = Math.sqrt(1 - (b / a) * (b / a));  //第一偏心率
        double ee = Math.sqrt((a / b) * (a / b) - 1);  //第二偏心率
        double B = Math.PI * B1 / 180;    //纬度(单位:弧度)
        double L = Math.PI * L1 / 180;    //经度(单位:弧度)
        double T = Math.tan(B) * Math.tan(B);
        double C = (ee * ee) * (Math.cos(B) * Math.cos(B));
        double A = (L - L0) * Math.cos(B);
        double M = a * ((1 - de * de / 4 - 3 * Math.pow(de, 4) / 64 - 5 * Math.pow(de, 6) / 256) * B
                - (3 * de * de / 8 + 3 * Math.pow(de, 4) / 32 + 45 * Math.pow(de, 6) / 1024) * Math.sin(2 * B)
                + (15 * Math.pow(de, 4) / 256 + 45 * Math.pow(de, 6) / 1024) * Math.sin(4 * B)
                - 35 * Math.pow(de, 6) / 3072 * Math.sin(6 * B));
        double N = a / Math.sqrt(1 - (de * de) * (Math.sin(B) * Math.sin(B))); //卯酉圈曲率半径
        double XN = FN + K * (M + N * Math.tan(B) * (A * A / 2 + (5 - T + 9 * C + 4 * C * C) * Math.pow(A, 4) / 24)
                + (61 - 58 * T + T * T + 600 * C - 330 * ee * ee) * Math.pow(A, 6) / 720);
        double YE = FE + K * N * (A + (1 - T + C) * Math.pow(A, 3) / 6
                + (5 - 18 * T + T * T + 72 * C - 58 * ee * ee) * Math.pow(A, 5) / 120);
//        X = YE;
//        Y = XN;

        return new Coordinate(YE,XN);
    }

    public static Coordinate UTMXYToBL(double X, double Y, CoorSystem coorSystem)
    {

        double B = 0; double L = 0;
        if ((X == 0) || (Y == 0)) return null;
        double L0 = coorSystem.GetCenterMeridian() * Math.PI / 180;  //中央经度 (参数001)
        double K = 1;// 0.9996;                       //投影比例因子(参数002)
        double a = 6378137;               //椭球长半轴(参数003)
        double b = 6356752.3142451793;    //椭球短半轴(参数004)
        //double f = (a - b) / a;           //扁率(参数005)
        double de = Math.sqrt(1 - (b / a) * (b / a));  //第一偏心率(参数006)
        double ee = Math.sqrt((a / b) * (a / b) - 1);  //第二偏心率(参数007)
        double XN = Y;              //纬度
        double YE = X;              //经度
        double FE = 500000L;        //东经偏移(参数009)
        double FN = 0;              //北纬偏移(参数010)
        double Mf = (XN - FN) / K;
        double e1 = (1 - b / a) / (1 + b / a);
        double Q = Mf / (a * (1 - de * de / 4 - 3 * Math.pow(de, 4) / 64 - 5 * Math.pow(de, 6) / 256));
        double Bf = Q + (3 * e1 / 2 - 27 * e1 * e1 * e1 / 32) * Math.sin(2 * Q)
                + (21 * e1 * e1 / 16 - 55 * Math.pow(e1, 4) / 32) * Math.sin(4 * Q)
                + (151 * e1 * e1 * e1 / 96) * Math.sin(6 * Q)
                + (1097 * Math.pow(e1, 4) / 512) * Math.sin(8 * Q);
        double Nf = a / Math.sqrt(1 - de * de * Math.sin(Bf) * Math.sin(Bf));
        double D = (YE - FE) / (K * Nf);
        double Tf = Math.tan(Bf) * Math.tan(Bf);
        double Cf = (ee * ee) * (Math.cos(Bf) * Math.cos(Bf));
        double Rf = a * (1 - de * de) / Math.sqrt(Math.pow((1 - de * de * Math.sin(Bf) * Math.sin(Bf)), 3));
        B = Bf - (Nf * Math.tan(Bf) / Rf) * (D * D / 2 - (5 + 3 * Tf + 10 * Cf - 4 * Cf * Cf - 9 * ee * ee) * Math.pow(D, 4) / 24
                + (61 + 90 * Tf + 298 * Cf + 45 * Tf * Tf - 252 * ee * ee - 3 * Cf * Cf) * Math.pow(D, 6) / 720);
        L = L0 + (1 / Math.cos(Bf)) * (D - (1 + 2 * Tf + Cf) * Math.pow(D, 3) / 6
                + (5 - 2 * Cf + 28 * Tf - 3 * Cf * Cf + 8 * ee * ee + 24 * Tf * Tf) * Math.pow(D, 5) / 120);
        B = B * 180 / Math.PI;
        L = L * 180 / Math.PI;
        return new Coordinate(L,B);
    }
}
